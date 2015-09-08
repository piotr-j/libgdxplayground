package io.piotrjastrzebski.playground.gpushadows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Per-pixel shadows on GPU: https://github.com/mattdesl/lwjgl-basics/wiki/2D-Pixel-Perfect-Shadows
 *
 * @author mattdesl
 */
public class GpuShadows extends BaseScreen {
	public GpuShadows (PlaygroundGame game) {
		super(game);
		create();
	}

//  public static void main(String[] args) {
//	  	LwjglApplicationConfiguration  c = new LwjglApplicationConfiguration();
//	  	c.vSyncEnabled = true;
//	  	c.vSyncEnabled = false;
//	  	c.width = 800;
//	  	c.height = 600;
//	  	c.useGL20 = true;
//	  	c.depth = 0;
//		new LwjglApplication(new GpuShadows(), c);
//	}

	/**
	 * Compiles a new instance of the default shader for this batch and returns it. If compilation
	 * was unsuccessful, GdxRuntimeException will be thrown.
	 *
	 * @return the default shader
	 */
	public static ShaderProgram createShader (String vert, String frag) {
		ShaderProgram prog = new ShaderProgram(vert, frag);
		if (!prog.isCompiled())
			throw new GdxRuntimeException("could not compile shader: " + prog.getLog());
		if (prog.getLog().length() != 0)
			Gdx.app.log("GpuShadows", prog.getLog());
		return prog;
	}

	final static private int lightSize = 256;

	final static private int shadowSize = 256;

	final static float occulusionUpcale = 16.0f;

	final static float lightMapUpcale = 16.0f;

	final static int lightTessalation = 64;

	final static int maxLights = 8 * 1024;

	float shadowVertices[] = new float[maxLights * 3 * 6];

	Mesh lightMesh;
	Mesh lightMapMesh;
	Mesh shadowMapMesh;

	BitmapFont font;

	TextureRegion shadowMap1D; //1 dimensional shadow map
	TextureRegion occluders;   //occluder map

	FrameBuffer shadowMapFBO;
	FrameBuffer occludersFBO;

	FrameBuffer lightMap, lightMap2;

	Texture casterSprites;
	Texture light;

	Matrix4 tmp = new Matrix4();
	Matrix4 tmp2 = new Matrix4();

	ShaderProgram shadowMapShader, shadowRenderShader, occulusionMapShader, blurShader;

	Array<Light> lights = new Array<Light>();

	boolean additive = true;
	boolean softShadows = true;

	class Light {

		float x, y;
		Color color;

		public Light (float x, float y, Color color) {
			this.x = x;
			this.y = y;
			this.color = color;
		}
	}

	public void create () {
		ShaderProgram.pedantic = false;

		//read vertex pass-through shader
		final String VERT_SRC = Gdx.files.internal("gpushadows/shaders/pass.vert").readString();

		occulusionMapShader = createShader(VERT_SRC, Gdx.files.internal("gpushadows/shaders/occulusionMap.frag").readString());
		// renders occluders to 1D shadow map

		final String VERT4_SRC = Gdx.files.internal("gpushadows/shaders/shadowMap.vert").readString();
		shadowMapShader = createShader(VERT4_SRC, Gdx.files.internal("gpushadows/shaders/shadowMap.frag").readString());
		// samples 1D shadow map to create the blurred soft shadow
		final String VERT2_SRC = Gdx.files.internal("gpushadows/shaders/light.vert").readString();
		shadowRenderShader = createShader(VERT2_SRC, Gdx.files.internal("gpushadows/shaders/shadowRender.frag").readString());

		final String VERT3_SRC = Gdx.files.internal("gpushadows/shaders/blurspace.vert").readString();
		blurShader = createShader(VERT3_SRC, Gdx.files.internal("gpushadows/shaders/gaussian.frag").readString());

		//the occluders
		casterSprites = new Texture("gpushadows/cat4.png");
		//the light sprite
		light = new Texture("gpushadows/light.png");

		//build frame buffers

		int w2 = (int)(Gdx.graphics.getWidth() / occulusionUpcale);
		int h2 = (int)(Gdx.graphics.getHeight() / occulusionUpcale);
		occludersFBO = new FrameBuffer(Format.RGB565, w2, h2, false);
		occluders = new TextureRegion(occludersFBO.getColorBufferTexture());
		occluders.flip(false, true);

		//our 1D shadow map, shadowSize x 1 pixels, no depth
		shadowMapFBO = new FrameBuffer(Format.RGB888, shadowSize, maxLights, false);
		Texture shadowMapTex = shadowMapFBO.getColorBufferTexture();

		//use linear filtering and repeat wrap mode when sampling
		shadowMapTex.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		shadowMapTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

		//for debugging only; in order to render the 1D shadow map FBO to screen
		shadowMap1D = new TextureRegion(shadowMapTex);
		shadowMap1D.flip(false, true);

		int w = (int)(Gdx.graphics.getWidth() / lightMapUpcale);
		int h = (int)(Gdx.graphics.getHeight() / lightMapUpcale);

		lightMap = new FrameBuffer(Format.RGB888, w, h, false);
		lightMap2 = new FrameBuffer(Format.RGB888, w, h, false);

		font = new BitmapFont();

		gameCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gameCamera.setToOrtho(false);

		lightMesh = new Mesh(true, 2 * lightTessalation, 3 * lightTessalation,
			new VertexAttribute(Usage.Position, 3, "a_position"));

		float[] vertices = new float[lightTessalation * 6];
		double angleNum = 2.0 * Math.PI / (lightTessalation - 1);
		for (int i = 0; i < lightTessalation; i++) {
			final double angle = Math.PI - angleNum * i;
			vertices[6 * i + 0] = 0;
			vertices[6 * i + 1] = 0;
			vertices[6 * i + 2] = (i + 0.5f) / ((float)lightTessalation - 1.f);

			vertices[6 * i + 3] = (float)Math.cos(angle);
			vertices[6 * i + 4] = (float)Math.sin(angle);
			vertices[6 * i + 5] = i / ((float)lightTessalation - 1.f);
		}
		lightMesh.setVertices(vertices);

		short[] indices = new short[lightTessalation * 3];
		for (int i = 0; i < lightTessalation; i++) {
			indices[i * 3 + 0] = (short)(i * 2);
			indices[i * 3 + 1] = (short)(i * 2 + 1);
			indices[i * 3 + 2] = (short)(i * 2 - 1);
		}
		lightMesh.setIndices(indices);

		lightMapMesh = createLightMapMesh();

		shadowMapMesh = new Mesh(false, maxLights * 3, 0, new VertexAttribute(Usage.Position, 2, "a_position"),
			new VertexAttribute(Usage.Generic, 4, "a_scaleOffset"));

		clearLights();
	}

	Vector3 pos = new Vector3();


	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(pos.set(screenX, screenY, 0));
		lights.add(new Light(pos.x, pos.y, randomColor()));

//		for (int i = 0; i < 9; i++)
//			lights.add(new Light(MathUtils.random(0, 800), MathUtils.random(0, 600), randomColor()));

		return true;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(pos.set(screenX, screenY, 0));
		return super.mouseMoved(screenX, screenY);
	}

	public boolean keyDown (int key) {
		if (key == Keys.SPACE) {
			clearLights();
			return true;
		} else if (key == Keys.A) {
			additive = !additive;
			return true;
		} else if (key == Keys.S) {
			softShadows = !softShadows;
			return true;
		}
		return super.keyDown(key);
	}

	@Override public void resize (int width, int height) {
		gameCamera.setToOrtho(false, width, height);
		batch.setProjectionMatrix(gameCamera.combined);
	}

	@Override public void render (float delta) {
		//clear frame
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//STEP 1. render light region to occluder FBO

		//bind the occluder FBO
		occludersFBO.begin();

		//clear the FBO
		Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//set the orthographic camera to the size of our FBO
		gameCamera.setToOrtho(false, occludersFBO.getWidth() * occulusionUpcale, occludersFBO.getHeight() * occulusionUpcale);

		//update camera matrices
		gameCamera.update();

		//set up our batch for the occluder pass
		batch.setProjectionMatrix(gameCamera.combined);
		batch.setShader(occulusionMapShader); //use default shader
		batch.begin();
		batch.disableBlending();
		// ... draw any sprites that will cast shadows here ... //
		batch.draw(casterSprites, 0, 0);

		//end the batch before unbinding the FBO
		batch.end();

		//unbind the FBO
		occludersFBO.end();

		lightMap.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		lightMap.end();

		lightMap2.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		lightMap2.end();

		lights.get(lights.size - 1).x = pos.x;
		lights.get(lights.size - 1).y = pos.y;

		renderLights();

		gaussianBlur();

		batch.enableBlending();
		//STEP 4. render sprites in full colour		
		batch.begin();
		batch.setShader(null); //default shader

		batch.draw(casterSprites, 0, 0);

		//DEBUG RENDERING -- show occluder map and 1D shadow map
		batch.setColor(Color.WHITE);
		batch.draw(shadowMap1D, Gdx.graphics.getWidth() - shadowSize, 10, shadowSize, 10);

		//draw FPS
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond() + "\n\nLights: " + lights.size + "\nSPACE to clear lights"
			+ "\nA to toggle additive blending" + "\nS to toggle soft shadows", 10, Gdx.graphics.getHeight() - 10);

		batch.end();

	}

	void clearLights () {
		lights.clear();
		lights.add(new Light(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), Color.WHITE));
	}

	static Color randomColor () {
		float intensity = (float)Math.random() * 0.25f + 0.15f;
		return new Color((float)Math.random(), (float)Math.random(), (float)Math.random(), intensity);
	}

	void renderLights () {
		Gdx.gl20.glDepthMask(false);

		shadowMapFBO.begin();

		//clear it
		Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//set our shadow map shader
		shadowMapShader.begin();
		shadowMapShader.setUniformf("invResolution", 1.0f / (lightSize));

		occluders.getTexture().bind();
		//STEP 2. build a 1D shadow maps from occlude FBO
		int verticeCount = 0;

		float yStep = 1.f / maxLights;
		float scaleX = -0.5f * lightSize / (occulusionUpcale * occludersFBO.getWidth());
		float scaleY = -0.5f * lightSize / (occulusionUpcale * occludersFBO.getHeight());
		float invOccWidth = 1.f / (occulusionUpcale * occludersFBO.getWidth());
		float invOccHeigth = 1.f / (occulusionUpcale * occludersFBO.getHeight());

		float mx = Gdx.input.getX() * 0.001f;
		float my = (Gdx.graphics.getHeight() - Gdx.input.getY()) * 0.001f;

		for (int j = 0; j < lights.size; j++) {
			Light o = lights.get(j);

			o.x = 0.999f * o.x + mx;
			o.y = 0.999f * o.y + my;
			float offsetX = o.x * invOccWidth;
			float offsetY = o.y * invOccHeigth;
			shadowVertices[verticeCount++] = -1.f; //x
			shadowVertices[verticeCount++] = 2.f * (j + 0.5f) * yStep - 1.f;

			shadowVertices[verticeCount++] = scaleX;
			shadowVertices[verticeCount++] = scaleY;
			shadowVertices[verticeCount++] = offsetX;
			shadowVertices[verticeCount++] = offsetY;

			shadowVertices[verticeCount++] = 105.f; //x
			shadowVertices[verticeCount++] = 2.f * (j + 0.5f) * yStep - 1.f;

			shadowVertices[verticeCount++] = scaleX;
			shadowVertices[verticeCount++] = scaleY;
			shadowVertices[verticeCount++] = offsetX;
			shadowVertices[verticeCount++] = offsetY;

			shadowVertices[verticeCount++] = -1.f; //x
			shadowVertices[verticeCount++] = 2.f * (j + 1.0f) * yStep - 1.f; //y

			shadowVertices[verticeCount++] = scaleX;
			shadowVertices[verticeCount++] = scaleY;
			shadowVertices[verticeCount++] = offsetX;
			shadowVertices[verticeCount++] = offsetY;
		}

		shadowMapMesh.setVertices(shadowVertices, 0, verticeCount);
		shadowMapMesh.render(shadowMapShader, GL20.GL_TRIANGLES, 0, verticeCount);

		//reset our projection matrix to the FBO size
		gameCamera.setToOrtho(false, shadowMapFBO.getWidth(), shadowMapFBO.getHeight());
		//unbind shadow map FBO
		shadowMapFBO.end();
		gameCamera.setToOrtho(false);
		//reset projection matrix to screen
		batch.setProjectionMatrix(gameCamera.combined);

		lightMap.begin();

		//STEP 3. render the blurred shadows
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

		//set the shader which actually draws the light/shadow
		shadowRenderShader.begin();

		for (int j = 0; j < lights.size; j++) {
			Light o = lights.get(j);

			tmp.set(gameCamera.combined);
			tmp2.setToTranslationAndScaling(o.x, o.y, 0, lightSize * 0.5f, lightSize * 0.5f, 1.f);
			tmp.mul(tmp2);
			shadowRenderShader.setUniformMatrix("u_mvp", tmp);
			shadowRenderShader.setUniformf("y", yStep * (j + 0.5f));
			shadowRenderShader.setUniformf("u_color", o.color.r * o.color.a, o.color.g * o.color.a, o.color.b * o.color.a);

			shadowMap1D.getTexture().bind();
			lightMesh.render(shadowRenderShader, GL20.GL_TRIANGLES);
		}
		shadowRenderShader.end();
		lightMap.end();

	}

	public void gaussianBlur () {

		Gdx.gl20.glDisable(GL20.GL_BLEND);
		lightMap.getColorBufferTexture().bind(0);
		// horizontal
		lightMap2.begin();
		{
			blurShader.begin();
			blurShader.setUniformi("u_texture", 0);
			blurShader.setUniformf("dir", 1f, 0f);
			blurShader.setUniformf("size", lightMap2.getWidth(), lightMap2.getHeight());
			lightMapMesh.render(blurShader, GL20.GL_TRIANGLE_FAN, 0, 4);
			blurShader.end();
		}
		lightMap2.end();

		lightMap2.getColorBufferTexture().bind(0);
		// vertical
		//              lightMap.begin();
		{
			blurShader.begin();
			blurShader.setUniformf("dir", 0f, 1f);
			lightMapMesh.render(blurShader, GL20.GL_TRIANGLE_FAN, 0, 4);
			blurShader.end();

			//            lightMap.end();
		}

		Gdx.gl20.glEnable(GL20.GL_BLEND);

	}

	@Override public void pause () {

	}

	@Override public void resume () {
		// TODO Auto-generated method stub

	}

	@Override public void dispose () {
		// TODO Auto-generated method stub

	}

	private Mesh createLightMapMesh () {
		float[] verts = new float[VERT_SIZE];
		// vertex coord
		verts[X1] = -1;
		verts[Y1] = -1;

		verts[X2] = 1;
		verts[Y2] = -1;

		verts[X3] = 1;
		verts[Y3] = 1;

		verts[X4] = -1;
		verts[Y4] = 1;

		// tex coords
		verts[U1] = 0f;
		verts[V1] = 0f;

		verts[U2] = 1f;
		verts[V2] = 0f;

		verts[U3] = 1f;
		verts[V3] = 1f;

		verts[U4] = 0f;
		verts[V4] = 1f;

		Mesh tmpMesh = new Mesh(true, 4, 0, new VertexAttribute(Usage.Position, 2, "a_position"),
			new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));

		tmpMesh.setVertices(verts);
		return tmpMesh;

	}

	static public final int VERT_SIZE = 16;
	static public final int X1 = 0;
	static public final int Y1 = 1;
	static public final int U1 = 2;
	static public final int V1 = 3;
	static public final int X2 = 4;
	static public final int Y2 = 5;
	static public final int U2 = 6;
	static public final int V2 = 7;
	static public final int X3 = 8;
	static public final int Y3 = 9;
	static public final int U3 = 10;
	static public final int V3 = 11;
	static public final int X4 = 12;
	static public final int Y4 = 13;
	static public final int U4 = 14;
	static public final int V4 = 15;

}
