package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class ExtendViewportTest extends BaseScreen {
	private static final String TAG = ExtendViewportTest.class.getSimpleName();
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;

	protected OrthographicCamera gameCamera;
	protected ExtendViewport gameViewport;

	public ExtendViewportTest (GameReset game) {
		super(game);
		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, VP_WIDTH, VP_HEIGHT, gameCamera);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.setColor(Color.GREEN);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.rect(0, 0, VP_WIDTH, VP_HEIGHT);
		renderer.end();

	}

	@Override public void resize (int width, int height) {
		gameViewport.update(width, height, true);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, ExtendViewportTest.class);
	}
}
