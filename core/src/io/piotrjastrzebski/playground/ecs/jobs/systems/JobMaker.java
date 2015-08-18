package io.piotrjastrzebski.playground.ecs.jobs.systems;

import com.artemis.BaseSystem;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import io.piotrjastrzebski.playground.ecs.jobs.Godlike;
import io.piotrjastrzebski.playground.ecs.jobs.components.Job;

/**
 * Created by EvilEntity on 17/08/2015.
 */
@Wire
public class JobMaker extends BaseSystem {
	float DELAY = 7;
	float timer = DELAY;
	@Override protected void processSystem () {
		timer += world.delta;
		if (timer > DELAY) {
			timer -= DELAY;
			createRandomJobs();
		}
	}

	private void createRandomJobs () {
		// TODO randomize, shallow tree, max 3 depth
		Job jobA = createJob();
		Job jobB = createJob();
		Job jobC = createJob();
		jobA.require(jobB);
		jobB.require(jobC);

		Job jobD = createJob();
		Job jobE = createJob();
		Job jobF = createJob();
		jobF.require(jobD);
		jobE.require(jobF);

		jobA.next(jobF);
	}

	int jobs = 0;
	private Job createJob () {
		Entity e = world.createEntity();
		Job job = e.edit().create(Job.class);
		job.parent(e);

		Godlike godlike = e.edit().create(Godlike.class);
		godlike.color.set(Color.YELLOW);
		godlike.width = 0.5f;
		godlike.height = 0.5f;
		godlike.x = 0.25f + MathUtils.round(MathUtils.random(-20, 20));
		godlike.y = 0.25f + MathUtils.round(MathUtils.random(-10, 10));
		godlike.name = "Job " + jobs++;
		job.setName(godlike.name);

		return job;
	}
}
