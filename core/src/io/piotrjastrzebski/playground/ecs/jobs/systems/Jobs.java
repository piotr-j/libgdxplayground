package io.piotrjastrzebski.playground.ecs.jobs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import io.piotrjastrzebski.playground.ecs.jobs.ECSJobsTest;
import io.piotrjastrzebski.playground.ecs.jobs.components.Job;

/**
 * Created by EvilEntity on 17/08/2015.
 */
@Wire
public class Jobs extends EntityProcessingSystem{
	private final static String TAG = Jobs.class.getSimpleName();
	private ComponentMapper<Job> mJob;

	IntMap<Job> jobById = new IntMap<>();
	Array<Job> jobs = new Array<>();
	public Jobs () {
		super(Aspect.all(Job.class));
	}

	@Override protected void inserted (int e) {
		Gdx.app.log(TAG, ECSJobsTest.entityToStr(world, e));
		Job job = mJob.get(e);
		jobById.put(e, job);
		jobs.add(job);
	}

	@Override protected void process (Entity e) {

	}

	@Override protected void removed (int e) {
		Job job = mJob.get(e);
		jobs.removeValue(job, true);
		jobById.put(job.eid, null);
	}

	public Job getJob (int jid) {
		return jobById.get(jid, null);
	}

	public Job getJob () {
		for (Job job : jobs) {
			if (isAvailable(job)) {
				return job;
			}
		}
		return null;
	}

	private boolean isDone(int id) {
		return isDone(jobById.get(id, null));
	}

	private boolean isDone(Job job) {
		// already removed
		if (job == null) return true;
		return job.progress > 1;
	}

	private boolean isAvailable (Job job) {
		if (job.previous != ECSJobsTest.NULL_ID && !isDone(job.previous)) {
			return false;
		}
		for (int i = 0; i < job.required.size; i++) {
			int id = job.required.get(i);
			if (!isDone(id)) return false;
		}
		return !isClaimed(job);
	}

	private boolean isClaimed(Job job) {
		return job.workerID != ECSJobsTest.NULL_ID;
	}

	public void finish (Job job) {
		if (job.progress < 1) return;
		Gdx.app.log(TAG, job + " finished");
		world.getEntity(job.eid).deleteFromWorld();
	}

	public boolean hasNext (Job job) {
		return job.next != ECSJobsTest.NULL_ID;
	}

	public Job next (Job job) {
		return getJob(job.next);
	}
}
