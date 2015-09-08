package io.piotrjastrzebski.playground.ecs.aijobs.tasks;

import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Task;
import io.piotrjastrzebski.playground.ecs.aijobs.Mover;
import io.piotrjastrzebski.playground.ecs.aijobs.components.Worker;

/**
 * Created by PiotrJ on 19/08/15.
 */
@Wire
public class MoveTask extends BaseTask {
	private final static String TAG = MoveTask.class.getSimpleName();

	Mover mover;

	@Override public void run () {
		// move to target -> done
		Worker worker = getObject();
		if (mover.atTarget(worker.eid)){
			Gdx.app.log(TAG, "At target " + worker.eid);
			success();
		} else if (mover.isUnreachable(worker.eid)){
			Gdx.app.log(TAG, "Unreachable " + worker.eid);
			fail();
		} else {
//			Gdx.app.log(TAG, "Moving " + worker.eid);
			running();
		}
	}

	@Override protected Task<Worker> copyTo (Task<Worker> task) {
		MoveTask moveTask = (MoveTask)task;
		moveTask.mover = mover;
		return super.copyTo(task);
	}
}
