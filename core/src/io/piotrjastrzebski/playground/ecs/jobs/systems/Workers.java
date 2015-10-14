package io.piotrjastrzebski.playground.ecs.jobs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import io.piotrjastrzebski.playground.ecs.jobs.ECSJobsTest;
import io.piotrjastrzebski.playground.ecs.jobs.Godlike;
import io.piotrjastrzebski.playground.ecs.jobs.components.Job;
import io.piotrjastrzebski.playground.ecs.jobs.components.Worker;

/**
 * Created by EvilEntity on 17/08/2015.
 */
@Wire
public class Workers extends IteratingSystem {
	private final static String TAG = Workers.class.getSimpleName();
	private ComponentMapper<Worker> mWorker;
	protected ComponentMapper<Godlike> mGodlike;
	Jobs jobs;

	public Workers () {
		super(Aspect.all(Worker.class));
	}

	@Override protected void inserted (int e) {
		Gdx.app.log(TAG, ECSJobsTest.entityToStr(world, e));

	}

	@Override protected void process (int e) {
		Godlike godlike = mGodlike.get(e);
		Worker worker = mWorker.get(e);
		// job set
		if (worker.jobID != ECSJobsTest.NULL_ID) {
			if (!godlike.atTarget) return;
			Job job = jobs.getJob(worker.jobID);
			// work on job
			job.progress += 1f * world.delta;
			if (job.progress > 1) {
				// if done, finish it
				jobs.finish(job);
				if (jobs.hasNext(job)) {
					claim(worker, jobs.next(job));
				} else {
					worker.jobID = ECSJobsTest.NULL_ID;
				}
			}
		} else {
			// no job, try to find one
			Job job = jobs.getJob();
			if (job != null) {
				// found some job, assign worker
				claim(worker, job);
			}
		}
	}

	private void claim (Worker worker, Job job) {
		worker.claim(job);
		Godlike joblike = mGodlike.get(job.eid);
		Godlike workerlike = mGodlike.get(worker.eid);
		workerlike.tx = joblike.x;
		workerlike.ty = joblike.y;
	}

	@Override protected void removed (int e) {

	}


}
