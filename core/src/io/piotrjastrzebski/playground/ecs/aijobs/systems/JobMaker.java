package io.piotrjastrzebski.playground.ecs.aijobs.systems;

import com.artemis.*;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.playground.ecs.aijobs.Godlike;
import io.piotrjastrzebski.playground.ecs.aijobs.components.Job;

/**
 * Created by EvilEntity on 17/08/2015.
 */
@Wire
public class JobMaker extends BaseSystem {
	float DELAY = 7;
	float timer = DELAY;
	EntitySubscription jobs;
	@Override protected void initialize () {
		super.initialize();
		AspectSubscriptionManager manager = world.getManager(AspectSubscriptionManager.class);
		jobs = manager.get(Aspect.all(Job.class));
	}

	@Override protected void processSystem () {
		timer += world.delta;
		if (timer > DELAY && jobs.getEntities().size() == 0) {
			timer -= DELAY;
			createRandomJobs();
		}
	}

	Color temp = new Color();

	private void createRandomJobs () {
		// TODO randomize, shallow tree, max 3 depth
		Job jobA = createJob("A", temp.set(1, 0.5f, 0.5f, 1));
		Job jobB = createJob("B", temp.set(1, 0.75f, 0.75f, 1));
		Job jobC = createJob("C", temp.set(1, 1, 1, 1));
		jobA.require(jobB);
		jobB.require(jobC);

		Job jobD = createJob("D", temp.set(1, 1, 1, 1));
		Job jobE = createJob("E", temp.set(0.75f, 1, 0.75f, 1));
		Job jobF = createJob("F", temp.set(0.5f, 1, 0.5f, 1));
		jobF.require(jobE);
		jobE.require(jobD);

		jobA.next(jobF);
	}

	int jobID = 0;
	private Job createJob (String suf, Color color) {
		Entity e = world.createEntity();
		Job job = e.edit().create(Job.class);
		job.parent(e);

		Godlike godlike = e.edit().create(Godlike.class);
		godlike.color.set(color);
		godlike.width = 0.5f;
		godlike.height = 0.5f;
		godlike.x = 0.25f + MathUtils.round(MathUtils.random(-18, 17));
		godlike.y = 0.25f + MathUtils.round(MathUtils.random(-10, 9));
		godlike.name = "Job " + suf;
		godlike.id = jobID++;

		godlike.actor = new VisLabel(godlike.name);
		godlike.actor.setColor(Color.RED);
		job.setName(godlike.name);

		return job;
	}
}
