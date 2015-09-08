package io.piotrjastrzebski.playground.gdxai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.Include;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibrary;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibraryManager;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.math.MathUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 01/09/15.
 */
public class AIParallelTest extends BaseScreen {
	BehaviorTree<Brain> brainBT;
	public AIParallelTest (GameReset game) {
		super(game);
		BehaviorTreeLibraryManager libraryManager = BehaviorTreeLibraryManager.getInstance();
		BehaviorTreeLibrary library = new BehaviorTreeLibrary(BehaviorTreeParser.DEBUG_HIGH);
		registerBehavior(library);
		libraryManager.setLibrary(library);
		brainBT = libraryManager.createBehaviorTree("brain", new Brain("1"));
	}

	private void registerBehavior (BehaviorTreeLibrary library) {
		Include<Brain> include = new Include<>();
		include.lazy = false;
		include.subtree = "brain.actual";
		BehaviorTree<Brain> includeBehavior = new BehaviorTree<>(include);
		library.registerArchetypeTree("brain", includeBehavior);

		BehaviorTree<Brain> actualBehavior = new BehaviorTree<>(createBrainBehavior());
		library.registerArchetypeTree("brain.actual", actualBehavior);
	}

	public static Task<Brain> createBrainBehavior () {
		Selector<Brain> selector = new Selector<>();

		MyParallel<Brain> parallel = new MyParallel<>();
		parallel.addChild(new TaskA());
		parallel.addChild(new TaskB());

		selector.addChild(parallel);

		Sequence<Brain> sequence = new Sequence<>();
		selector.addChild(sequence);

		sequence.addChild(new TaskC("1"));
		sequence.addChild(new TaskC("2"));
		sequence.addChild(new TaskC("3"));
		return selector;
	}

	float timer = 1;
	@Override public void render (float delta) {
		super.render(delta);
		timer += delta;
		if (timer > 1f) {
			timer -= 1f;
			Gdx.app.log("", "Step");
			brainBT.step();
		}
	}

	public static class Brain {
		public String name;

		public Brain (String name) {
			this.name = name;
		}
	}

	public static class TaskA extends LeafTask<Brain> {
		private final static String TAG = TaskA.class.getSimpleName();

		@Override public void run () {
			if (MathUtils.random() > 0.8f) {
				Gdx.app.log(TAG, "Success");
				success();
			} else {
				Gdx.app.log(TAG, "Fail");
				fail();
			}
		}

		@Override public void end () {
			Gdx.app.log(TAG, "end");
		}

		@Override protected Task<Brain> copyTo (Task<Brain> task) {
			return task;
		}
	}
	public static class TaskB extends LeafTask<Brain> {
		private final static String TAG = TaskB.class.getSimpleName();

		@Override public void run () {
//			if (MathUtils.random() > 0.5f) {
//				Gdx.app.log(TAG, "Success");
//				success();
//			} else {
				Gdx.app.log(TAG, "Running");
				running();
//			}
		}

		@Override public void end () {
			Gdx.app.log(TAG, "end");
		}

		@Override protected Task<Brain> copyTo (Task<Brain> task) {
			return task;
		}
	}

	public static class TaskC extends LeafTask<Brain> {
		private final static String TAG = TaskC.class.getSimpleName();
		public String name;

		public TaskC () { this("");}
		public TaskC (String name) {
			this.name = name;
		}

		@Override public void run () {
			if (MathUtils.random() > 0.5f) {
				Gdx.app.log(TAG+name, "Success");
				success();
			} else {
				Gdx.app.log(TAG+name, "Fail");
				fail();
			}
		}

		@Override protected Task<Brain> copyTo (Task<Brain> task) {
			TaskC taskC = (TaskC)task;
			taskC.name = name;
			return task;
		}
	}
}
