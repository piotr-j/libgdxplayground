package io.piotrjastrzebski.playground.ecs;

import com.artemis.*;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class ECSDeleteTest extends BaseScreen {
	private final static String TAG = ECSDeleteTest.class.getSimpleName();

	World world;
	public ECSDeleteTest (GameReset game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();


		world = new World(config);

	}

	@Override public void render (float delta) {
		super.render(delta);
		world.setDelta(delta);
		world.process();
	}
}
