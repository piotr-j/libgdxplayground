package io.piotrjastrzebski.playground.ecs.aijobs.tasks;

import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Task;
import io.piotrjastrzebski.playground.ecs.aijobs.components.Worker;
import io.piotrjastrzebski.playground.ecs.aijobs.systems.Jobs;

/**
 * Created by PiotrJ on 19/08/15.
 */
@Wire
public class WorkJobTask extends BaseTask {
	private final static String TAG = WorkJobTask.class.getSimpleName();

	Jobs jobs;

	@Override
	public Status execute () {
		Worker worker = getObject();
		// no job
		if (!jobs.hasJob(worker)){
//			Gdx.app.log(TAG, "No job " + worker.eid);
			return Status.FAILED;
		} else if (jobs.workDone(worker)) {
			Gdx.app.log(TAG, "Done " + worker.eid);
			return Status.SUCCEEDED;
		} else {
//			Gdx.app.log(TAG, "Working " + worker.eid);
			return Status.RUNNING;
		}
	}

	@Override protected Task<Worker> copyTo (Task<Worker> task) {
		WorkJobTask workTask = (WorkJobTask)task;
		workTask.jobs = jobs;
		return task;
	}
}
