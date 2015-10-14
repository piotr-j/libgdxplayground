package io.piotrjastrzebski.playground.ecs.aijobs;

import com.artemis.*;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.StreamUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.ecs.aijobs.components.Worker;
import io.piotrjastrzebski.playground.ecs.aijobs.systems.JobMaker;
import io.piotrjastrzebski.playground.ecs.aijobs.systems.Jobs;
import io.piotrjastrzebski.playground.ecs.aijobs.systems.Workers;
import io.piotrjastrzebski.playground.ecs.aijobs.tasks.BaseTask;

import java.io.Reader;

/**
 * Job test with ecs
 * Created by EvilEntity on 28/07/2015.
 */
public class ECSAIJobsTest extends BaseScreen {
	private final static String TAG = ECSAIJobsTest.class.getSimpleName();
	public static final int NULL_ID = -1;

	World world;
	public ECSAIJobsTest (GameReset game) {
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
		config.setSystem(new GUI(multiplexer));

		config.setSystem(new Jobs());
		config.setSystem(new JobMaker());
		config.setSystem(new Workers());

		world = new World(config);

		loadAI();

		createWorker("A");
		createWorker("B");
	}

	BehaviorTree<Worker> btArchetype = null;
	private void loadAI () {
		Reader reader = null;
		try {
			reader = Gdx.files.internal("aijobs/worker.tree").reader();
			BehaviorTreeParser<Worker> parser = new BehaviorTreeParser<>(BehaviorTreeParser.DEBUG_NONE);
			btArchetype = parser.parse(reader, null);
			injectTask(btArchetype);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	private void injectTask (Task task) {
		for (int i = 0; i < task.getChildCount(); i++) {
			Task child = task.getChild(i);
			if (child instanceof BaseTask) {
				try {
					((BaseTask)child).initialize(world);
				} catch (MundaneWireException e) {
					// we do not care if there is nothing to inject, perhaps we will at some point
				}
			} else if (child instanceof LeafTask) {
				Gdx.app.error(TAG, "All LeafTasks should extend BaseTask! " + child);
			} else {
				injectTask(child);
			}
		}
	}

	int workers = 0;
	private void createWorker (String name) {
		Entity e = world.createEntity();
		Worker worker = e.edit().create(Worker.class);
		worker.eid = e.getId();
		worker.name = name;
		worker.ai = (BehaviorTree<Worker>)btArchetype.cloneTask();


		Godlike godlike = e.edit().create(Godlike.class);
		godlike.color.set(Color.BLUE);
		godlike.width = 1;
		godlike.height = 1;
		godlike.x = MathUtils.round(MathUtils.random(-18, 18));
		godlike.y = MathUtils.round(MathUtils.random(-10, 10));
		godlike.vx = 4f;
		godlike.vy = 4f;
		godlike.mover = true;
		godlike.name = "Worker " + workers++;
		godlike.actor = new VisLabel(godlike.name);
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
