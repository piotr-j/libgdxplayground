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

	@Override
	public Status execute () {
		// move to target -> done
		Worker worker = getObject();
		if (mover.atTarget(worker.eid)){
			Gdx.app.log(TAG, "At target " + worker.eid);
			return Status.SUCCEEDED;
		} else if (mover.isUnreachable(worker.eid)){
			Gdx.app.log(TAG, "Unreachable " + worker.eid);
			return Status.FAILED;
		} else {
//			Gdx.app.log(TAG, "Moving " + worker.eid);
			return Status.RUNNING;
		}
	}

	@Override protected Task<Worker> copyTo (Task<Worker> task) {
		MoveTask moveTask = (MoveTask)task;
		moveTask.mover = mover;
		return super.copyTo(task);
	}
}
