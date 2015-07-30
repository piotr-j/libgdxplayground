package io.piotrjastrzebski.playground.jobtest;

import com.artemis.*;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Job test with ecs
 * Created by EvilEntity on 28/07/2015.
 */
public class ECSJobTest extends BaseScreen {
	private final static String TAG = ECSJobTest.class.getSimpleName();
	public static final int NULL_ID = -1;

	World world;
	public ECSJobTest (PlaygroundGame game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();
		config.setSystem(new JobSystem());
		config.setSystem(new WorkerSystem());
		world = new World(config);

		createWorker();
//		createWorker();
//		createJob();
//		createJob();
//		createChainJob();
		createReqJob();
	}

	private void createWorker () {
		Entity e = world.createEntity();
		Worker worker = e.edit().create(Worker.class);
		worker.eid = e.id;

	}

	private void createJob () {
		Entity e = world.createEntity();
		Job job = e.edit().create(Job.class);
		job.eid = e.id;
	}

	private void createChainJob () {
		Entity e = world.createEntity();
		Job jobA = e.edit().create(Job.class);
		jobA.eid = e.id;

		e = world.createEntity();
		Job jobB = e.edit().create(Job.class);
		jobB.eid = e.id;

		e = world.createEntity();
		Job jobC = e.edit().create(Job.class);
		jobC.eid = e.id;

		jobA.next = jobC.eid;
		jobB.next = jobA.eid;
	}

	private void createReqJob () {
		Entity e = world.createEntity();
		Job jobA = e.edit().create(Job.class);
		jobA.eid = e.id;

		e = world.createEntity();
		Job jobB = e.edit().create(Job.class);
		jobB.eid = e.id;

		e = world.createEntity();
		Job jobC = e.edit().create(Job.class);
		jobC.eid = e.id;

		jobA.require(jobB);
		jobB.require(jobC);
	}

	private void tick () {
		world.process();
	}

	public static class Job extends Component {
		// id if entity
		public int eid = NULL_ID;
		public int workerID = NULL_ID;
		public IntArray required = new IntArray();
		public int next = NULL_ID;
		public int previous = NULL_ID;
		public float progress;

		public void next(Job job) {
			next = job.eid;
		}

		public void prev(Job job) {
			previous = job.eid;
		}

		public void require(Job job) {
			required.add(job.eid);
		}

		@Override public String toString () {
			return "Job{" +
				"eid=" + eid +
				'}';
		}
	}

	public static class Worker extends Component {
		// id if entity
		public int eid = -1;
		public int jid = -1;

		@Override public String toString () {
			return "Worker{" +
				"eid=" + eid +
				'}';
		}
	}

	@Wire
	public static class JobSystem extends EntityProcessingSystem {
		private final static String TAG = JobSystem.class.getSimpleName();
		private ComponentMapper<Job> mJob;

		IntMap<Job> jobById = new IntMap<>();
		Array<Job> jobs = new Array<>();
		public JobSystem () {
			super(Aspect.all(Job.class));
		}

		@Override protected void inserted (Entity e) {
			Gdx.app.log(TAG, entityToStr(e));
			Job job = mJob.get(e);
			jobById.put(e.id, job);
			jobs.add(job);
		}

		@Override protected void process (Entity e) {

		}

		@Override protected void removed (Entity e) {
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

		private boolean isDone(Job job) {
			// already removed
			if (job == null) return true;
			return job.previous > 1;
		}

		private boolean isDone(int id) {
			return isDone(jobById.get(id, null));
		}

		private boolean isAvailable (Job job) {
			if (job.previous != NULL_ID && !isDone(job.previous)) {
				return false;
			}
			for (int i = 0; i < job.required.size; i++) {
				int id = job.required.get(i);
				if (!isDone(id)) return false;
			}
			return !isClaimed(job);
		}

		private boolean isClaimed(Job job) {
			return job.workerID != NULL_ID;
		}

		public void finish (Job job) {
			if (job.progress < 1) return;
			Gdx.app.log(TAG, "Job done " + job);
			world.getEntity(job.eid).deleteFromWorld();
		}
	}

	@Wire
	public static class WorkerSystem extends EntityProcessingSystem {
		private final static String TAG = WorkerSystem.class.getSimpleName();
		private ComponentMapper<Worker> mWorker;
		JobSystem jobs;

		public WorkerSystem () {
			super(Aspect.all(Worker.class));
		}

		@Override protected void inserted (Entity e) {
			Gdx.app.log(TAG, entityToStr(e));

		}

		@Override protected void process (Entity e) {
			Worker worker = mWorker.get(e);
			// job set
			if (worker.jid != NULL_ID) {
				Job job = jobs.getJob(worker.jid);
				// work on job
				job.progress += 0.5f;
				if (job.progress > 1) {
					// if done, finish it
					jobs.finish(job);
					worker.jid = NULL_ID;
				}
			} else {
				// no job, try to find one
				Job job = jobs.getJob();
				if (job != null) {
					// found some job, assign worker
					Gdx.app.log(TAG, worker + " found job " + job);
					job.workerID = e.id;
					worker.jid = job.eid;
				}
			}
		}

		@Override protected void removed (Entity e) {

		}
	}

	static Bag<Component> fill = new Bag<>();
	public static String entityToStr(Entity e) {
		fill.clear();
		StringBuilder sb = new StringBuilder();
		sb.append("Entity{");
		sb.append(e.id);
		sb.append("}[");
		e.getComponents(fill);
		for (int i = 0; fill.size() > i; i++) {
			if (i > 0) sb.append(", ");
			sb.append(fill.get(i));
		}
		sb.append("]");
		return sb.toString();
	}

	float tick = 1;
	@Override public void render (float delta) {
		super.render(delta);
		tick+=delta;
		if (tick >= 1) {
			tick -= 1;
			tick();
		}
	}
}
