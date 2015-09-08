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
import io.piotrjastrzebski.playground.GameReset;

/**
 * Job test with ecs
 * Created by EvilEntity on 28/07/2015.
 */
public class AIECSJobTest extends BaseScreen {
	private final static String TAG = AIECSJobTest.class.getSimpleName();
	public static final int NULL_ID = -1;

	World world;
	public AIECSJobTest (GameReset game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();
		config.setSystem(new JobSystem());
		config.setSystem(new WorkerSystem());
		world = new World(config);

		createWorker("A");
		createWorker("B");
//		createJob("Z");
//		createJob();
//		createChainJob();
//		createReqJob();
		complexJobTest();
	}

	private void createWorker (String name) {
		Entity e = world.createEntity();
		Worker worker = e.edit().create(Worker.class);
		worker.eid = e.id;
		worker.name = name;
	}

	private Job createJob (String name) {
		Entity e = world.createEntity();
		Job job = e.edit().create(Job.class);
		job.parent(e);
		job.setName(name);
		return job;
	}

	private void createChainJob () {
		Job jobA = createJob("A");
		Job jobB = createJob("B");
		Job jobC = createJob("C");

		jobA.next(jobC);
		jobB.next(jobA);
		// B -> A -> C
	}

	private void createReqJob () {
		Job jobA = createJob("A");
		Job jobB = createJob("B");
		Job jobC = createJob("C");
		Job jobD = createJob("D");

		jobA.require(jobB);
		jobB.require(jobC);
		jobB.require(jobD);
		// C, D -> B -> A
	}

	private void complexJobTest () {
		Job jobA = createJob("A");
		Job jobB = createJob("B");
		Job jobC = createJob("C");
		jobA.require(jobB);
		jobB.require(jobC);

		Job jobD = createJob("D");
		Job jobE = createJob("E");
		Job jobF = createJob("F");
		jobF.require(jobD);
		jobE.require(jobF);

		jobA.next(jobF);

		// C -> B -> A
		//           |
		//           v
		// 	  D -> F -> E
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

		public String name;

		public void setName (String name) {
			this.name = name;
		}

		/**
		 * Job that must be done after this one by same entity
		 */
		public void next(Job job) {
			next = job.eid;
			job.prev(this);
		}

		/**
		 * Previous unfinished job in a chain if any
		 */
		public void prev(Job job) {
			previous = job.eid;
		}

		/**
		 * That job must be done before this one is available
		 */
		public void require(Job job) {
			required.add(job.eid);
		}

		public void parent(Entity e) {
			eid = e.id;
		}

		@Override public String toString () {
//			return "Job{" +
//				"eid=" + eid +
//				", name=" + name +
//				(next!=NULL_ID?", next=" + next:"") +
//				(required.size>0?", required=" + required:"") +
//				'}';
			return "Job " + name;
		}
	}

	public static class Worker extends Component {
		// id if entity
		public int eid = -1;
		public int jobID = -1;
		public String name;

		public void claim (Job job) {
			Gdx.app.log(TAG, this + " -> " + job);
			jobID = job.eid;
			job.workerID = eid;
		}

		@Override public String toString () {
//			return "Worker{" +
//				"eid=" + eid +
//				'}';
			return "Worker " + name;
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

		@Override protected void inserted (int e) {
			Gdx.app.log(TAG, entityToStr(world, e));
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
			Gdx.app.log(TAG, job + " finished");
			world.getEntity(job.eid).deleteFromWorld();
		}

		public boolean hasNext (Job job) {
			return job.next != NULL_ID;
		}

		public Job next (Job job) {
			return getJob(job.next);
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

		@Override protected void inserted (int e) {
			Gdx.app.log(TAG, entityToStr(world, e));

		}

		@Override protected void process (Entity e) {
			Worker worker = mWorker.get(e);
			// job set
			if (worker.jobID != NULL_ID) {
				Job job = jobs.getJob(worker.jobID);
				// work on job
				job.progress += 0.5f;
				if (job.progress > 1) {
					// if done, finish it
					jobs.finish(job);
					if (jobs.hasNext(job)) {
						worker.claim(jobs.next(job));
					} else {
						worker.jobID = NULL_ID;
					}
				}
			} else {
				// no job, try to find one
				Job job = jobs.getJob();
				if (job != null) {
					// found some job, assign worker
					worker.claim(job);
				}
			}
		}

		@Override protected void removed (int e) {

		}
	}

	static Bag<Component> fill = new Bag<>();
	public static String entityToStr(World world, int e) {
		fill.clear();
		StringBuilder sb = new StringBuilder();
		sb.append("Entity{");
		sb.append(e);
		sb.append("}[");
		world.getEntity(e).getComponents(fill);
		for (int i = 0; fill.size() > i; i++) {
			if (i > 0) sb.append(", ");
			sb.append(fill.get(i));
		}
		sb.append("]");
		return sb.toString();
	}
	final float tTime = 0.25f;
	float tick = tTime;
	@Override public void render (float delta) {
		super.render(delta);
		tick+=delta;
		if (tick >= tTime) {
			tick -= tTime;
			tick();
		}
	}
}
