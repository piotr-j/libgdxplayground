package io.piotrjastrzebski.playground.isotiled;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class IsoTest extends BaseScreen {
	IsometricTiledMapRenderer mapRenderer;
	public IsoTest (PlaygroundGame game) {
		super(game);
		TiledMap map = new TmxMapLoader().load("tiled/iso.tmx");
		mapRenderer = new IsometricTiledMapRenderer(map, INV_SCALE, batch);
	}

	@Override public void render (float delta) {
		super.render(delta);
		mapRenderer.setView(gameCamera);
		mapRenderer.render();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);

	}

	@Override public void dispose () {
		super.dispose();
	}
}
