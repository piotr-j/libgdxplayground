package io.piotrjastrzebski.playground.isotiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.nio.IntBuffer;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class StencilTiledTest extends BaseScreen {

	private final FrameBuffer fbo;
	private final TextureRegion fboRegion;
	Texture badlogic;
	OrthogonalTiledMapRenderer mapRenderer;
	ShaderProgram discardShader;
	ShaderProgram maskShader;
	private boolean renderStencil;

	public StencilTiledTest (GameReset game) {
		super(game);
		badlogic = new Texture("badlogic.jpg");
		gameCamera.position.x += VP_WIDTH/2;
		gameCamera.position.y += VP_HEIGHT;
		gameCamera.update();

		TiledMap map = new TmxMapLoader().load("tiled/stencil.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, INV_SCALE, batch);

		ShaderProgram.pedantic = false;
		discardShader = new ShaderProgram(Gdx.files.internal("tiled/shaders/discard.vert"), Gdx.files.internal("tiled/shaders/discard.frag"));
		maskShader = new ShaderProgram(Gdx.files.internal("tiled/shaders/mask.vert"), Gdx.files.internal("tiled/shaders/mask.frag"));

		fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);
		fboRegion = new TextureRegion(fbo.getColorBufferTexture());
		fboRegion.flip(false, true);
	}

	int[] bgLayer = new int[]{0};
	int[] fgLayer = new int[]{1};
	@Override public void render (float delta) {
		updateInput(delta);

		// https://github.com/mattdesl/lwjgl-basics/wiki/LibGDX-Masking
		// eh, looks like alpha masking is the way to go :/
		// blend stuff!
		// in fbo:
		// clear to 0,
		// disable all buy alpha
		// render stuff
		// render stuff with that fbo as mask
		// alt : shaperenderer :/

		//1. clear screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		/*
		mapRenderer.setView(gameCamera);
		mapRenderer.render(bgLayer);

		//2. clear our depth buffer with 1.0
		Gdx.gl.glClearDepthf(1f);
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

		//3. set the function to LESS
		Gdx.gl.glDepthFunc(GL20.GL_LESS);

		//4. enable depth writing
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		//5. Enable depth writing, disable RGBA color writing
		Gdx.gl.glDepthMask(true);
		Gdx.gl.glColorMask(false, false, false, false);

		batch.setProjectionMatrix(gameCamera.combined);
		///////////// Draw mask shape(s)
		renderer.setProjectionMatrix(gameCamera.combined);
		//6. render your primitive shapes
		renderer.begin(ShapeRenderer.ShapeType.Filled);

		renderer.setColor(1f, 0f, 0f, 0.5f);
		renderer.circle( gameCamera.position.x,  gameCamera.position.y, 5, 32);
//		renderer.setColor(0f, 1f, 0f, 0.5f);
		renderer.rect(5, 5, 10, 10);

		renderer.end();
		// so this wont work, kinda dick move
//		batch.begin();
//		batch.draw(badlogic, gameCamera.position.x - VP_WIDTH / 2 + 5, gameCamera.position.y - VP_HEIGHT / 2 + 5, VP_WIDTH - 10, VP_HEIGHT - 10);
//		batch.end();
		///////////// Draw sprite(s) to be masked
//		batch.begin();

		//8. Enable RGBA color writing
		//   (SpriteBatch.begin() will disable depth mask)
		Gdx.gl.glColorMask(true, true, true, true);

		//9. Make sure testing is enabled.
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		//10. Now depth discards pixels outside our masked shapes
		Gdx.gl.glDepthFunc(GL20.GL_EQUAL);

		mapRenderer.render(fgLayer);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		*/
		//push to the batch
//		batch.draw(badlogic, 0, 0, 10, 10);

		//end/flush your batch
//		batch.end();

		/*
//		super.render(delta);
		updateInput(delta);

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//		Gdx.gl.glColorMask(false, false, false, false);

		Gdx.gl.glDepthFunc(GL20.GL_ALWAYS);
		Gdx.gl.glDepthMask(false);

		batch.setShader(null);

//		batch.setShader(maskShader);

		batch.setProjectionMatrix(gameCamera.combined);
		mapRenderer.setView(gameCamera);
//		mapRenderer.render(bgLayer);
//		batch.begin();
//		batch.draw(badlogic, cs.x, cs.y, 1, 1);
//		batch.draw(badlogic, cs.x, cs.y + 1, 1, 1);
//		batch.end();
//		mapRenderer.render(fgLayer);

//		Gdx.gl.glClearDepthf(0xff);
		Gdx.gl.glClearDepthf(0x0);
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//
		Gdx.gl.glDepthMask(true);
//		Gdx.gl.glColorMask(false, false, false, false);
//		batch.setShader(discardShader);
//		mapRenderer.render(fgLayer);
//		batch.setShader(null);
//		Gdx.gl.glColorMask(true, true, true, true);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//		Gdx.gl.glDepthFunc(GL20.GL_EQUAL);
//		Gdx.gl.glDepthFunc(GL20.GL_GREATER);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);

		batch.setShader(maskShader);
		batch.begin();
		batch.draw(badlogic, cs.x, cs.y, 1, 1);
//		batch.draw(badlogic, gameCamera.position.x - VP_WIDTH / 2, gameCamera.position.y - VP_HEIGHT / 2, VP_WIDTH, VP_HEIGHT);
		batch.end();
		batch.setShader(null);
*/

		batch.setProjectionMatrix(gameCamera.combined);
		mapRenderer.setView(gameCamera);
		mapRenderer.render(bgLayer);
		batch.begin();
		batch.draw(badlogic, cs.x, cs.y, 2, 2);
// 		batch.draw(badlogic, gameCamera.position.x - VP_WIDTH / 2, gameCamera.position.y - VP_HEIGHT / 2, VP_WIDTH, VP_HEIGHT);
		batch.end();
		mapRenderer.render(fgLayer);

		// Enable Stencil Test
		Gdx.gl20.glEnable(GL20.GL_STENCIL_TEST);
		Gdx.gl20.glColorMask(false, false, false, false);
		Gdx.gl20.glDepthMask(false);
		Gdx.gl20.glStencilFunc(GL20.GL_NEVER, 1, 0xFF);
		Gdx.gl20.glStencilOp(GL20.GL_REPLACE, GL20.GL_REPLACE, GL20.GL_REPLACE);
		// Simple "if (gl_FragColor.a == 0.0) discard;" fragment shader
//		batch.setShader(discardShader);
//		mapRenderer.render(fgLayer);
//		batch.setShader(null);
		Gdx.gl20.glStencilMask(0xFF);
		Gdx.gl20.glClear(GL20.GL_STENCIL_BUFFER_BIT);

		mapRenderer.render(fgLayer);
//		renderer.setProjectionMatrix(gameCamera.combined);
//		renderer.begin(ShapeRenderer.ShapeType.Filled);
//		renderer.circle(gameCamera.position.x,  gameCamera.position.y, 5, 32);
//		renderer.end();

		Gdx.gl20.glColorMask(true, true, true, true);
		Gdx.gl20.glDepthMask(true);
		Gdx.gl20.glStencilMask(0x00);

		Gdx.gl20.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);

//		Gdx.gl20.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);

//		mapRenderer.render(fgLayer);

//		renderer.setColor(1, 0, 0, .5f);
//		renderer.setProjectionMatrix(gameCamera.combined);
//		renderer.begin(ShapeRenderer.ShapeType.Filled);
////		renderer.rect(gameCamera.position.x - VP_WIDTH / 2, gameCamera.position.y - VP_HEIGHT / 2, VP_WIDTH, VP_HEIGHT);
//		renderer.rect(cs.x, cs.y, 1, 1);
//		renderer.end();

		batch.setShader(maskShader);
		batch.begin();
		batch.draw(badlogic, cs.x, cs.y, 2, 2);
// 		batch.draw(badlogic, gameCamera.position.x - VP_WIDTH / 2, gameCamera.position.y - VP_HEIGHT / 2, VP_WIDTH, VP_HEIGHT);
		batch.end();
		batch.setShader(null);

		Gdx.gl20.glDisable(GL20.GL_STENCIL_TEST);
		//		Gdx.gl20.glClearColor(1, 0, 0, 1);
//		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		// "gl_FragColor = vec4(0.0, 1.0, 1.0, 0.2) * texture2D(u_texture, v_texCoord0).a;"
//		batch.setShader(maskShader);
//		batch.begin();
//		batch.draw(badlogic, cs.x, cs.y, 1, 1);
// 		batch.draw(badlogic, gameCamera.position.x - VP_WIDTH / 2, gameCamera.position.y - VP_HEIGHT / 2, VP_WIDTH, VP_HEIGHT);

//		batch.end();
//		batch.setShader(null);


//		if (renderStencil) {
//			fbo.begin();
//			Gdx.gl20.glClearColor(1, 0, 0, 1);
//			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
//
//			Gdx.gl20.glEnable(GL20.GL_STENCIL_TEST);
//			Gdx.gl20.glStencilFunc(GL20.GL_ALWAYS, 0x1, 0xFF);
//			Gdx.gl20.glStencilOp(GL20.GL_REPLACE, GL20.GL_REPLACE, GL20.GL_REPLACE);
//
//			batch.setShader(discardShader);
//			mapRenderer.render(fgLayer);
//			batch.setShader(null);
//			fbo.end();
//			Gdx.gl20.glDisable(GL20.GL_STENCIL_TEST);
//
//			batch.begin();
//			batch.draw(fboRegion, gameCamera.position.x - VP_WIDTH / 2, gameCamera.position.y - VP_HEIGHT / 2, VP_WIDTH, VP_HEIGHT);
//			batch.end();
//		}
	}

	private void updateInput (float delta) {
		float scale = (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) ? 10 : 1;
		scale *= delta * 10f;
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			gameCamera.position.x -= scale;
		} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			gameCamera.position.x += scale;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
			gameCamera.position.y += scale;
		} else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			gameCamera.position.y -= scale;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			renderStencil = !renderStencil;
		}
		gameCamera.update();
	}

	@Override public void resize (int width, int height) {
		gameViewport.update(width, height, false);
		guiViewport.update(width, height, true);
	}

	Vector3 temp = new Vector3();
	Vector2 cs = new Vector2();
	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
		return super.mouseMoved(screenX, screenY);
	}

	@Override public void dispose () {
		super.dispose();
		badlogic.dispose();
		discardShader.dispose();
		maskShader.dispose();
		fbo.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, StencilTiledTest.class);
	}
}
