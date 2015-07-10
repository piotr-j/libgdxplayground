package io.piotrjastrzebski.playground.asyncscreentest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.async.AsyncTask;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class AsyncScreenTest extends BaseScreen {
	IsometricTiledMapRenderer mapRenderer;
	AsyncExecutor executor;
	AsyncResult<SlowLoadStuff> result;

	public AsyncScreenTest (PlaygroundGame game) {
		super(game);
		// we will do only one thing
		executor = new AsyncExecutor(1);
		result = executor.submit(new AsyncTask<SlowLoadStuff>() {
			@Override public SlowLoadStuff call () throws Exception {
				return new SlowLoadStuff();
			}
		});
		TiledMap map = new TmxMapLoader().load("tiled/iso.tmx");
		mapRenderer = new IsometricTiledMapRenderer(map, INV_SCALE, batch);
	}

	@Override public void render (float delta) {
		super.render(delta);
		if (result != null && result.isDone()) {
			Gdx.app.log("", "SlowLoadStuff done " + result.get());
			// got to reset or it will be called all the time
			result = null;
		}
		gameCamera.position.x += 1 * delta;
		gameCamera.update();
		mapRenderer.setView(gameCamera);
		mapRenderer.render();

	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);

	}

	@Override public void dispose () {
		super.dispose();
		executor.dispose();
	}

	private class SlowLoadStuff {
		public SlowLoadStuff () throws InterruptedException {
			Gdx.app.log("SlowLoadStuff", "Started");
			Thread.sleep(2000);
			Gdx.app.log("SlowLoadStuff", "Finished");
		}
	}
}
