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
public class FindJobTask extends BaseTask {
	private final static String TAG = FindJobTask.class.getSimpleName();

	Jobs jobs;
	@Override public void run () {
		if (jobs.findJobFor(getObject())) {
			Gdx.app.log(TAG, "Found job for " + getObject());
			success();
		} else {
//			Gdx.app.log(TAG, "No job for " + getObject());
			fail();
		}
	}

	@Override protected Task<Worker> copyTo (Task<Worker> task) {
		FindJobTask findJobTask = (FindJobTask)task;
		findJobTask.jobs = jobs;
		return task;
	}

}
