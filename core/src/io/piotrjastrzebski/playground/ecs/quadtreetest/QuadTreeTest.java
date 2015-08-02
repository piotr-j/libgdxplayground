package io.piotrjastrzebski.playground.ecs.quadtreetest;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 31/07/2015.
 */
public class QuadTreeTest extends BaseScreen {
	World world;

	QTDebugSystem qtDebugSystem;
	public QuadTreeTest (PlaygroundGame game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();
		config.register(gameCamera);
		config.register(renderer);
		config.register(gameViewport);
		config.register(root);
		config.setSystem(new VelocitySystem());
		config.setSystem(new BoundsSystem());
		config.setSystem(new QTSystem());
		config.setSystem(qtDebugSystem = new QTDebugSystem());
		config.setSystem(new DebugDrawSystem());
		world = new World(config);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		world.delta = delta;
		world.process();
		stage.act(delta);
		stage.draw();
	}

	Vector3 touch = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(touch.set(screenX, screenY, 0));
		qtDebugSystem.touched(touch.x, touch.y, button);
		return super.touchDown(screenX, screenY, pointer, button);
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		gameCamera.unproject(touch.set(screenX, screenY, 0));
		qtDebugSystem.drag(touch.x, touch.y);
		return super.touchDragged(screenX, screenY, pointer);
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(touch.set(screenX, screenY, 0));
		qtDebugSystem.touchUp(touch.x, touch.y, button);
		return super.touchUp(screenX, screenY, pointer, button);
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);

	}

	@Override public void dispose () {
		super.dispose();

	}
}
