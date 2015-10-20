package io.piotrjastrzebski.playground.isotiled;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class SimpleTiledBrickTest extends BaseScreen {
	public final static float SCALE = 16f; // size of single tile in pixels
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280/SCALE;
	public final static float VP_HEIGHT = 720/SCALE;

	OrthogonalTiledMapRenderer mapRenderer;
	public SimpleTiledBrickTest (GameReset game) {
		super(game);
		// fields in super class
		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);
		guiCamera = new OrthographicCamera();
		guiViewport = new ScreenViewport(guiCamera);

		TiledMap map = new TmxMapLoader().load("tiled/simple-brick.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, INV_SCALE * 2, batch);
	}

	int moveX;
	int moveY;
	int shift = 1;
	@Override public void render (float delta) {
		super.render(delta);

		gameCamera.position.x += moveX * delta * 0.1f * shift;
		gameCamera.position.y += moveY * delta * 0.1f * shift;
		gameCamera.update();


		mapRenderer.setView(gameCamera);
		mapRenderer.render();

		renderer.setColor(Color.CYAN);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		// keep it in center, this is dumb dont do this ever
		renderer.end();
	}

	@Override public void resize (int width, int height) {
		gameViewport.update(width, height, false);
		guiViewport.update(width, height, true);
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.UP:
			moveY++;
			break;
		case Input.Keys.DOWN:
			moveY--;
			break;
		case Input.Keys.LEFT:
			moveX--;
			break;
		case Input.Keys.RIGHT:
			moveX++;
			break;
		case Input.Keys.SHIFT_LEFT:
			shift = 100;
			break;
		}
		return super.keyDown(keycode);
	}

	@Override public boolean keyUp (int keycode) {
		switch (keycode) {
		case Input.Keys.UP:
			moveY--;
			break;
		case Input.Keys.DOWN:
			moveY++;
			break;
		case Input.Keys.LEFT:
			moveX++;
			break;
		case Input.Keys.RIGHT:
			moveX--;
			break;
		case Input.Keys.SHIFT_LEFT:
			shift = 1;
			break;
		}
		return super.keyUp(keycode);
	}
}
