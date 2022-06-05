package io.piotrjastrzebski.playground.isotiled;

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
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class SimpleTiledTest extends BaseScreen {
	public final static float SCALE = 32f; // size of single tile in pixels
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280/SCALE;
	public final static float VP_HEIGHT = 720/SCALE;

	OrthogonalTiledMapRenderer mapRenderer;
	public SimpleTiledTest (GameReset game) {
		super(game);
		// fields in super class
		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);
		guiCamera = new OrthographicCamera();
		guiViewport = new ScreenViewport(guiCamera);

		TiledMap map = new TmxMapLoader().load("tiled/simple.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, INV_SCALE, batch);
	}

	@Override public void render (float delta) {
		super.render(delta);

		mapRenderer.setView(gameCamera);
		mapRenderer.render();

		renderer.setColor(Color.CYAN);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		// keep it in center, this is dumb dont do this ever
		gameCamera.unproject(temp.set(VP_WIDTH /2 * SCALE, VP_HEIGHT/2 * SCALE, 0));
		renderer.rect(temp.x, temp.y, 1, 1);
		renderer.end();
	}

	@Override public void resize (int width, int height) {
		gameViewport.update(width, height, false);
		guiViewport.update(width, height, true);
	}

	Vector3 temp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		// fairly dumb
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		gameCamera.position.set(temp.x, temp.y, 0);
		gameCamera.update();
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		// dumb, dont do this!
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		temp.sub(gameCamera.position).scl(0.1f);
		gameCamera.position.add(temp.x, temp.y, 0);
		gameCamera.update();
		return true;
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, SimpleTiledTest.class);
	}
}
