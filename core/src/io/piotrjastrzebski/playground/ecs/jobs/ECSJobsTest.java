package io.piotrjastrzebski.playground.ecs.jobs;

import com.artemis.*;
import com.artemis.utils.Bag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.ecs.jobs.systems.JobMaker;
import io.piotrjastrzebski.playground.ecs.jobs.systems.Jobs;
import io.piotrjastrzebski.playground.ecs.jobs.systems.Workers;
import io.piotrjastrzebski.playground.ecs.jobs.components.Job;
import io.piotrjastrzebski.playground.ecs.jobs.components.Worker;

/**
 * Job test with ecs
 * Created by EvilEntity on 28/07/2015.
 */
public class ECSJobsTest extends BaseScreen {
	private final static String TAG = ECSJobsTest.class.getSimpleName();
	public static final int NULL_ID = -1;

	World world;
	public ECSJobsTest (GameReset game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();
		config.register("game-cam", gameCamera);
		config.register("gui-cam", guiCamera);
		config.register(renderer);
		config.register(stage);

		Selector selector = new Selector();
		multiplexer.addProcessor(selector);
		config.setSystem(selector);
		config.setSystem(new Mover());
		config.setSystem(new Renderer());
		config.setSystem(new GUI());

		config.setSystem(new Jobs());
		config.setSystem(new JobMaker());
		config.setSystem(new Workers());

		world = new World(config);

		createWorker("A");
		createWorker("B");
//		createJob("Z");
//		createJob();
//		createChainJob();
//		createReqJob();
//		complexJobTest();
	}

	int workers = 0;
	private void createWorker (String name) {
		Entity e = world.createEntity();
		Worker worker = e.edit().create(Worker.class);
		worker.eid = e.getId();
		worker.name = name;
		Godlike godlike = e.edit().create(Godlike.class);
		godlike.color.set(Color.BLUE);
		godlike.width = 1;
		godlike.height = 1;
		godlike.x = MathUtils.round(MathUtils.random(-18, 18));
		godlike.y = MathUtils.round(MathUtils.random(-10, 10));
		godlike.vx = 1f;
		godlike.vy = 1f;
		godlike.mover = true;
		godlike.name = "Worker " + workers++;
		godlike.actor = new VisLabel(godlike.name);
	}

	int jobs = 0;
	private Job createJob (String name) {
		Entity e = world.createEntity();
		Job job = e.edit().create(Job.class);
		job.parent(e);
		job.setName(name);

		Godlike godlike = e.edit().create(Godlike.class);
		godlike.color.set(Color.YELLOW);
		godlike.width = 0.5f;
		godlike.height = 0.5f;
		godlike.x = 0.25f + MathUtils.round(MathUtils.random(-20, 20));
		godlike.y = 0.25f + MathUtils.round(MathUtils.random(-10, 10));
		godlike.name = "Job " + jobs++;

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
	@Override public void render (float delta) {
		super.render(delta);
		world.delta = delta;
		world.process();
	}

	static Bag<Component> fill = new Bag<>();
	static StringBuilder sb = new StringBuilder();
	public static String entityToStr(World world, int e) {
		fill.clear();
		sb.setLength(0);
		sb.append("Entity{");
		sb.append(e);
		sb.append("}[\n");
		world.getEntity(e).getComponents(fill);
		for (int i = 0; fill.size() > i; i++) {
			if (i > 0) sb.append(",\n");
			sb.append("  ");
			sb.append(fill.get(i));
		}
		sb.append("\n]");
		return sb.toString();
	}
}
