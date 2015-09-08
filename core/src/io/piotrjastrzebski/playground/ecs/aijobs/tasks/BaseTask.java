package io.piotrjastrzebski.playground.ecs.aijobs.tasks;

import com.artemis.MundaneWireException;
import com.artemis.World;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import io.piotrjastrzebski.playground.ecs.aijobs.components.Worker;

/**
 * Base task for all tasks we will use
 * Created by PiotrJ on 19/08/15.
 */
@Wire
public class BaseTask extends LeafTask<Worker> {
	@Override public void run () {

	}

	@Override protected Task<Worker> copyTo (Task<Worker> task) {
		// TODO add any injected fields

		return task;
	}

	public void initialize (World world) throws MundaneWireException {
		world.inject(this);
	}
}
