package io.piotrjastrzebski.playground.gpushadows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 09/07/2015.
 */
public class GpuShadowTest extends BaseScreen {
	private int lightSize = 256;

	private float upScale = 1f; //for example; try lightSize=128, upScale=1.5f

	TextureRegion shadowMap1D; //1 dimensional shadow map
	TextureRegion occluders;   //occluder map

	FrameBuffer shadowMapFBO;
	FrameBuffer occludersFBO;

	Texture casterSprites;
	Texture light;

	ShaderProgram shadowMapShader, shadowRenderShader;

	Array<Light> lights = new Array<>();

	BitmapFont font;

	boolean additive = true;
	boolean softShadows = true;

	public GpuShadowTest (PlaygroundGame game) {
		super(game);

		ShaderProgram.pedantic = false;

		final String VERT_SRC = Gdx.files.internal("shadows/shadowPass.vert").readString();

		// renders occluders to 1D shadow map
		shadowMapShader = createShader(VERT_SRC, Gdx.files.internal("shadows/shadowMap.frag").readString());
		// samples 1D shadow map to create the blurred soft shadow
		shadowRenderShader = createShader(VERT_SRC, Gdx.files.internal("shadows/shadowRender.frag").readString());

		//the occluders
		casterSprites = new Texture("shadows/cat4.png");
		//the light sprite
		light = new Texture("shadows/light.png");

		//build frame buffers
		occludersFBO = new FrameBuffer(Pixmap.Format.RGBA8888, lightSize, lightSize, false);
		occluders = new TextureRegion(occludersFBO.getColorBufferTexture());
		occluders.flip(false, true);

		//our 1D shadow map, lightSize x 1 pixels, no depth
		shadowMapFBO = new FrameBuffer(Pixmap.Format.RGBA8888, lightSize, 1, false);
		Texture shadowMapTex = shadowMapFBO.getColorBufferTexture();

		//use linear filtering and repeat wrap mode when sampling
		shadowMapTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		shadowMapTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

		//for debugging only; in order to render the 1D shadow map FBO to screen
		shadowMap1D = new TextureRegion(shadowMapTex);
		shadowMap1D.flip(false, true);

		clearLights();

		font = new BitmapFont();
	}

	@Override public boolean keyDown (int keycode) {

		return super.keyDown(keycode);
	}

	Vector3 pos = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(pos.set(screenX, screenY, 0));
		lights.add(new Light(pos.x, pos.y, randomColor()));
		return super.touchDown(screenX, screenY, pointer, button);
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(pos.set(screenX, screenY, 0));
		return super.mouseMoved(screenX, screenY);
	}

	@Override public void render (float delta) {
		super.render(delta);
		if (additive)
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

		for (int i=0; i<lights.size; i++) {
			Light o = lights.get(i);
			if (i==lights.size-1) {
				o.x = pos.x;
				o.y = pos.y;
			}
			renderLight(o);
		}

		if (additive)
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		//STEP 4. render sprites in full colour
		batch.begin();
		batch.setShader(null); //default shader

		batch.draw(casterSprites, 0, 0);

		//DEBUG RENDERING -- show occluder map and 1D shadow map
		batch.setColor(Color.BLACK);
		batch.draw(occluders, Gdx.graphics.getWidth()-lightSize, 0);
		batch.setColor(Color.WHITE);
		batch.draw(shadowMap1D, Gdx.graphics.getWidth()-lightSize, lightSize+5);

		//DEBUG RENDERING -- show light
		batch.draw(light, pos.x-light.getWidth()/2f, pos.y-light.getHeight()/2f); //mouse
		batch.draw(light, Gdx.graphics.getWidth()-lightSize/2f-light.getWidth()/2f, lightSize/2f-light.getHeight()/2f);

		//draw FPS
		font.draw(batch, "FPS: "+Gdx.graphics.getFramesPerSecond()
			+"\n\nLights: "+lights.size
			+"\nSPACE to clear lights"
			+"\nA to toggle additive blending"
			+"\nS to toggle soft shadows", 10, Gdx.graphics.getHeight()-10);

		batch.end();
	}
	void clearLights() {
		lights.clear();
		lights.add(new Light(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY(), Color.WHITE));
	}

	static Color randomColor() {
		float intensity = (float)Math.random() * 0.5f + 0.5f;
		return new Color((float)Math.random(), (float)Math.random(), (float)Math.random(), intensity);
	}

	void renderLight(Light o) {
		float mx = o.x;
		float my = o.y;

		//STEP 1. render light region to occluder FBO

		//bind the occluder FBO
		occludersFBO.begin();

		//clear the FBO
		Gdx.gl.glClearColor(0f,0f,0f,0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//set the orthographic camera to the size of our FBO
		gameCamera.setToOrtho(false, occludersFBO.getWidth(), occludersFBO.getHeight());

		//translate camera so that light is in the center
		gameCamera.translate(mx - lightSize/2f, my - lightSize/2f);

		//update camera matrices
		gameCamera.update();

		//set up our batch for the occluder pass
		batch.setProjectionMatrix(gameCamera.combined);
		batch.setShader(null); //use default shader
		batch.begin();
		// ... draw any sprites that will cast shadows here ... //
		batch.draw(casterSprites, 0, 0);

		//end the batch before unbinding the FBO
		batch.end();

		//unbind the FBO
		occludersFBO.end();

		//STEP 2. build a 1D shadow map from occlude FBO

		//bind shadow map
		shadowMapFBO.begin();

		//clear it
		Gdx.gl.glClearColor(0f,0f,0f,0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//set our shadow map shader
		batch.setShader(shadowMapShader);
		batch.begin();
		shadowMapShader.setUniformf("resolution", lightSize, lightSize);
		shadowMapShader.setUniformf("upScale", upScale);

		//reset our projection matrix to the FBO size
		gameCamera.setToOrtho(false, shadowMapFBO.getWidth(), shadowMapFBO.getHeight());
		batch.setProjectionMatrix(gameCamera.combined);

		//draw the occluders texture to our 1D shadow map FBO
		batch.draw(occluders.getTexture(), 0, 0, lightSize, shadowMapFBO.getHeight());

		//flush batch
		batch.end();

		//unbind shadow map FBO
		shadowMapFBO.end();

		//STEP 3. render the blurred shadows

		//reset projection matrix to screen
		gameCamera.setToOrtho(false);
		batch.setProjectionMatrix(gameCamera.combined);

		//set the shader which actually draws the light/shadow
		batch.setShader(shadowRenderShader);
		batch.begin();

		shadowRenderShader.setUniformf("resolution", lightSize, lightSize);
		shadowRenderShader.setUniformf("softShadows", softShadows ? 1f : 0f);
		//set color to light
		batch.setColor(o.color);

		float finalSize = lightSize * upScale;

		//draw centered on light position
		batch.draw(shadowMap1D.getTexture(), mx-finalSize/2f, my-finalSize/2f, finalSize, finalSize);

		//flush the batch before swapping shaders
		batch.end();

		//reset color
		batch.setColor(Color.WHITE);
	}

	@Override public void dispose () {
		super.dispose();
		font.dispose();
		shadowMapFBO.dispose();
		occludersFBO.dispose();

		casterSprites.dispose();
		light.dispose();
	}

	class Light {
		float x, y;
		Color color;

		public Light(float x, float y, Color color) {
			this.x = x;
			this.y = y;
			this.color = color;
		}
	}
	/**
	 * Compiles a new instance of the default shader for this batch and returns it. If compilation
	 * was unsuccessful, GdxRuntimeException will be thrown.
	 * @return the default shader
	 */
	public static ShaderProgram createShader(String vert, String frag) {

		ShaderProgram prog = new ShaderProgram(vert, frag);
		if (!prog.isCompiled())
			throw new GdxRuntimeException("could not compile shader: " + prog.getLog());
		if (prog.getLog().length() != 0)
			Gdx.app.log("GpuShadows", prog.getLog());
		return prog;
	}
}
