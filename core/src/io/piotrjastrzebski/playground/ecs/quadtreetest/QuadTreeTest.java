package io.piotrjastrzebski.playground.ecs.quadtreetest;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.math.Vector3;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 31/07/2015.
 */
public class QuadTreeTest extends BaseScreen {
	World world;
	public QuadTreeTest (PlaygroundGame game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();

		world = new World(config);
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.process();
	}

	Vector3 touch = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(touch.set(screenX, screenY, 0));
		// TODO do stuff
		return super.touchDown(screenX, screenY, pointer, button);
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);

	}

	@Override public void dispose () {
		super.dispose();

	}
}
