package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.particletest.ParticleFaceTest;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class FullScreenFBODrawText extends BaseScreen {
	private static final String TAG = FullScreenFBODrawText.class.getSimpleName();
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;
	private ParticleFaceTest particleTest;
	private boolean drawFbo;
	private TextureRegion fbRegion;
	public FullScreenFBODrawText (GameReset game) {
		super(game);
		// we use particle face test so we have something to draw
		particleTest = new ParticleFaceTest(game);
		// got to fix input so we get it in here
		multiplexer.addProcessor(particleTest);
		Gdx.input.setInputProcessor(multiplexer);
		// reinitialize stuff so its clearer whats going on
		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);

		Gdx.app.log(TAG, "Space - toggle between normal and fbo drawing");
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (drawFbo) {
			// capture a screenshot if there isnt one
			if (fbRegion == null) {
				// we want this frame, not lst one
				particleTest.render(delta);
				fbRegion = ScreenUtils.getFrameBufferTexture();
			}
			// clear again to red to see if there are any gaps
			Gdx.gl.glClearColor(1, 0, 0, 1);
			batch.setProjectionMatrix(gameCamera.combined);
			batch.begin();
			float width = gameViewport.getWorldWidth();
			float height = gameViewport.getWorldHeight();
			// no need to blend in this case
			batch.disableBlending();
			// this assumes that camera is in the center of the screen
			batch.draw(
				fbRegion,
				gameCamera.position.x - width/2, gameCamera.position.y - height/2,
				width, height
			);
			batch.end();
			batch.enableBlending();
		} else {
			particleTest.render(delta);
		}
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.SPACE) {
			drawFbo = !drawFbo;
			if (drawFbo) {
				Gdx.app.log(TAG, "Draw fbo");
			} else {
				Gdx.app.log(TAG, "Draw normally");
				disposeFBO();
			}
		}
		return super.keyDown(keycode);
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		gameViewport.update(width, height, false);
		disposeFBO();
		drawFbo = false;
	}

	private void disposeFBO () {
		// we need to dispose native resource
		if (fbRegion != null) {
			fbRegion.getTexture().dispose();
			fbRegion = null;
		}
	}

	@Override public void dispose () {
		super.dispose();
		disposeFBO();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, FullScreenFBODrawText.class);
	}
}
