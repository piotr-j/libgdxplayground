package io.piotrjastrzebski.playground.ecs.sat.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.piotrjastrzebski.playground.ecs.Input;
import io.piotrjastrzebski.playground.ecs.sat.components.GUIActor;

/**
 * Created by PiotrJ on 27/08/15.
 */
@Wire
public class GUI extends EntitySystem implements Input {
	protected ComponentMapper<GUIActor> mGUIActor;

	@Wire Stage stage;

	public GUI () {
		super(Aspect.all(GUIActor.class));
	}

	@Override protected void inserted (int entityId) {
		GUIActor guiActor = mGUIActor.get(entityId);
		stage.addActor(guiActor.actor);
	}

	@Override protected void processSystem () {
		stage.act(world.delta);
		stage.draw();
	}

	@Override protected void removed (int entityId) {
		mGUIActor.get(entityId).actor.remove();
	}

	@Override public int priority () {
		return 0;
	}

	@Override public InputProcessor get () {
		return stage;
	}
}
