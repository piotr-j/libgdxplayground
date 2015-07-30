package io.piotrjastrzebski.playground.jobtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Iterator;

/**
 * Simple lights
 * http://techblog.orangepixel.net/2015/07/shine-a-light-on-it
 * Created by EvilEntity on 28/07/2015.
 */
public class JobTest extends BaseScreen {
	private final static String TAG = JobTest.class.getSimpleName();

	Array<Worker> workers = new Array<>();
	Array<Job> jobs = new Array<>();
	public JobTest (PlaygroundGame game) {
		super(game);
		// TODO prototype job system
		// new job -> entity picks it up
		// must do entire chain if required or just a part of it
		// job chains
		// jobA -> jobB -> jobC
		// jobA && jobB -> jobC
		//
		addWorker();
		addWorker();

//		requireJobTest();
//		chainJobTest();
//		nextJobTest();
		complexJobTest();
	}

	private void requireJobTest () {
		Job jobA = createJob("Req Job A");
		Job jobB = createJob("Req Job B");
		jobA.require(jobB);
		Job jobC = createJob("Req Job C");
		jobA.require(jobC);
//		jobB.require(jobC);
	}

//	private void chainJobTest () {
//		Job jobD = createJob("Job D");
//		Job jobE = createJob("Job E");
//		Job jobF = createJob("Job F");
//		jobF.chain(jobE);
//		jobE.chain(jobD);
//	}

	private void nextJobTest () {
		Job jobA = createJob("A");
		Job jobB = createJob("B");
		Job jobC = createJob("C");
		// when C is done, B must be started immediately by same entity
		jobC.next(jobB);
		// when B is done, A must be started immediately by same entity
		jobB.next(jobA);
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

	}

	private Job createJob (String name){
		Job job = new Job(name);
		jobs.add(job);
		return job;
	}

	private void addWorker () {
		Worker worker = new Worker();
		Gdx.app.log(TAG, "New " + worker);
		workers.add(worker);
	}

	private void tick () {
//		Gdx.app.log(TAG, "Tick()");
//		addJobs();
		for (Worker worker : workers) {
			if (worker.needsJob()) {
				for (Job job : jobs) {
					if (job.isAvailable()){
						worker.claimJob(job);
						break;
					}
				}
			}
			worker.tick();
		}
		Iterator<Job> iterator = jobs.iterator();
		while (iterator.hasNext()) {
			Job next = iterator.next();
			if (next.isDone()) {
				iterator.remove();
			}
		}
	}

	private void addJobs () {
		Job job = new Job();
		Gdx.app.log(TAG, "New " + job);
		jobs.add(job);
		// random chained job or some such
		for (int i = 0; i < 3; i++) {
			// TODO tweak chance?
			if (MathUtils.random() > 0.3) {
				Job job2 = new Job();
				Gdx.app.log(TAG, "New " + job2);
				jobs.add(job2);
				if (MathUtils.randomBoolean()) {
					job.require(job2);
				} else {
					job.next(job2);
				}
			}
		}
	}

	static int jobID = 0;
	public static class Job {
		public int id = jobID++;
		private float progress;
		public Worker claimedBy;
		public Array<Job> required = new Array<>();
		public String name;
		// next job must be done by the same entity, ie go somewhere and do something
		public Job next;
		private Job previous;

		public Job () {
			name = "";
		}

		public Job (String name) {
			this.name = name;
		}

		@Override public String toString () {
			return "Job{" +
				"id=" + id +
				", name=" + name +
				(next!=null?", next=" + next:"") +
				(required.size>0?", required=" + required:"") +
				'}';
		}

		/**
		 * Jobs in chain must be done one after another by the same entity
		 */
//		public Job chain (Job job) {
//			chained.add(job);
//			job.chainedWith(this);
//			return this;
//		}
//
//		private void chainedWith (Job job) {
//			chainedWith = job;
//
//		}

		public void next (Job job) {
			next = job;
			job.previous = this;
		}

		public void prev (Job job) {
			previous = job;
			job.next = this;
		}

		/**
		 * Required jobs must be done before this one becomes available
		 */
		public Job require (Job job) {
			required.add(job);
			return this;
		}

		public boolean hasNext () {
			return next != null;
		}

		public boolean isDone () {
			// TODO should this check for previous jobs?
			// progress cant start if others are not done anyway
			return progress >= 1;
		}

		public boolean isAvailable () {
//			if (chainedWith != null && !chainedWith.isDone()) {
//				return false;
//			}
			if (previous != null && !previous.isDone()) {
				return false;
			}
			for (Job job : required) {
				if (!job.isDone()) {
					return false;
				}
			}
			return !isClaimed();
		}

		public void claim (Worker worker) {
			if (!isClaimed())
				claimedBy = worker;
		}

		public boolean isClaimed() {
			return claimedBy != null;
		}
	}
	static int workerID = 0;
	public static class Worker {
		public int id = workerID++;
		public Job job;

		public void tick () {
			if (job != null) {
				job.progress += MathUtils.random(0.25f, 0.5f);
				if (job.isDone()) {
					Gdx.app.log(TAG, this + " finished " + job);
					if (job.hasNext()) {
						claimJob(job.next);
					} else {
						job = null;
					}
				}
			}
		}

		public boolean needsJob () {
			return job == null;
		}

		@Override public String toString () {
			return "Worker{" +
				"id=" + id +
				'}';
		}

		public void claimJob (Job job) {
			Gdx.app.log(TAG, this + " claimed " + job);
			this.job = job;
			job.claim(this);
		}
	}

	float tick;
	@Override public void render (float delta) {
		super.render(delta);
		tick+=delta;
		if (tick >= 1) {
			tick -= 1;
			tick();
		}
	}
}
