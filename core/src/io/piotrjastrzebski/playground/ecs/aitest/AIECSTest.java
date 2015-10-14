package io.piotrjastrzebski.playground.ecs.aitest;

import com.artemis.*;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.utils.StreamUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.ecs.aitest.dog.Dog;
import io.piotrjastrzebski.playground.ecs.aitest.dog.DogTask;

import java.io.Reader;

/**
 * Job test with ecs
 * Created by EvilEntity on 28/07/2015.
 */
public class AIECSTest extends BaseScreen {
	private final static String TAG = AIECSTest.class.getSimpleName();
	public static final int NULL_ID = -1;

	World world;
	public AIECSTest (GameReset game) {
		super(game);

		loadAI();

		WorldConfiguration config = new WorldConfiguration();
		config.setSystem(new DogSystem());
		config.setSystem(new AISystem(behaviorTreeArchetype));
		world = new World(config);

		createDog("Dog 1");
		createDog("Dog 2");
		createDog("Dog 3");
	}

	BehaviorTree<Dog> behaviorTreeArchetype = null;
	private void loadAI () {

		Reader reader = null;
		try {
			reader = Gdx.files.internal("aiecs/dog.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<>(BehaviorTreeParser.DEBUG_NONE);
			behaviorTreeArchetype = parser.parse(reader, null);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	private void createDog(String name) {
		Entity entity = world.createEntity();
		EntityEdit edit = entity.edit();

		AI ai = edit.create(AI.class);
		ai.bTreeStr = "dogTree";

		Dog dog = edit.create(Dog.class);
		dog.setName(name);
	}

	private void tick () {
		world.process();
	}

	@Wire
	public static class AISystem extends IteratingSystem {
		private final static String TAG = AISystem.class.getSimpleName();
		private ComponentMapper<AI> mAI;
		private ComponentMapper<Dog> mDog;
		DogSystem dogSystem;

		BehaviorTree<Dog> behaviorTreeArchetype = null;

		public AISystem (BehaviorTree<Dog> behaviorTreeArchetype) {
			super(Aspect.all(AI.class, Dog.class));
			this.behaviorTreeArchetype = behaviorTreeArchetype;
		}

		@Override protected void initialize () {
			super.initialize();
			injectBTree(behaviorTreeArchetype);
		}

		@Override protected void inserted (int e) {
			AI ai = mAI.get(e);
			ai.bTree = (BehaviorTree<Dog>)behaviorTreeArchetype.cloneTask();
			Gdx.app.log(TAG, entityToStr(world, e));
		}

		@Override protected void process (int e) {
			AI ai = mAI.get(e);
			Dog brain = mDog.get(e);
			ai.bTree.setObject(brain);
			ai.bTree.step();
		}

		@Override protected void removed (int e) {
			AI ai = mAI.get(e);

		}

		private void injectBTree(Task tree) {
			for (int i = 0; i < tree.getChildCount(); i++) {
				Task child = tree.getChild(i);
				if (child instanceof DogTask) {
					injectTask((DogTask) child);
				} else if (child instanceof LeafTask) {
					Gdx.app.error(TAG, "All LeafTasks should extend DogTask! " + child);
				} else {
					injectBTree(child);
				}
			}
		}

		private void injectTask(DogTask task) {
			try {
				// TODO inject inherited without wire?
				world.inject(task);

				task.initialize(world);
			} catch (MundaneWireException e) {
				// we do not care if there is nothing to inject, perhaps we will at some point
			}
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

	final float tTime = 1.0f;
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
