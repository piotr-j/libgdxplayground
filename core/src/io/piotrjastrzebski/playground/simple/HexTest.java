package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.HexagonalTiledMapRenderer;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Simple hex map test
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class HexTest extends BaseScreen {
	private static final String TAG = HexTest.class.getSimpleName();

	HexagonalTiledMapRenderer mapRenderer;
	TiledMap map;
	public HexTest (GameReset game) {
		super(game);
		map = new TmxMapLoader().load("hex/hex-map.tmx");
		mapRenderer = new HexagonalTiledMapRenderer(map, 1f/32f);

		gameCamera.position.set(VP_WIDTH/2, VP_HEIGHT/2, 0);
		gameCamera.update();
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		mapRenderer.setView(gameCamera);
		mapRenderer.render();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
	}

	@Override public void dispose () {
		super.dispose();
		map.dispose();
		mapRenderer.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, HexTest.class);
	}
}
