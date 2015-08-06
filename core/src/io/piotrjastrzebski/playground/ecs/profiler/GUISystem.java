package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Created by PiotrJ on 05/08/15.
 */
@Wire
public class GUISystem extends BaseSystem {

	@Wire Stage stage;

	@Override protected void initialize () {
		super.initialize();

	}

	@Override protected void processSystem () {
		stage.act(world.delta);
		stage.draw();
	}

	public void addActor (Actor actor) {
		stage.addActor(actor);
	}
}
