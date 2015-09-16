package io.piotrjastrzebski.playground.gdxai.policyparalleltest;

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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 01/09/15.
 */
public class PolicyParallelTest extends BaseScreen {
	public PolicyParallelTest (GameReset game) {
		super(game);
		VisWindow dialog = new VisWindow("Tests!");
		VisTextButton run = new VisTextButton("Run tests");
		run.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				test(createDefParallel());
				test(createSeqParallel());
				test(createSelParallel());
			}
		});
		dialog.add(run);
		stage.addActor(dialog);
		dialog.centerWindow();
	}

	private void test(Task<Blackboard> task) {
		BehaviorTreeLibraryManager libraryManager = BehaviorTreeLibraryManager.getInstance();
		BehaviorTreeLibrary library = new BehaviorTreeLibrary(BehaviorTreeParser.DEBUG_HIGH);

		Include<Blackboard> include = new Include<>();
		include.lazy = false;
		include.subtree = "brain.actual";
		BehaviorTree<Blackboard> includeBehavior = new BehaviorTree<>(include);
		library.registerArchetypeTree("brain", includeBehavior);
		BehaviorTree<Blackboard> actualBehavior = new BehaviorTree<>(task);

		library.registerArchetypeTree("brain.actual", actualBehavior);
		libraryManager.setLibrary(library);
		BehaviorTree<Blackboard> tree = libraryManager.createBehaviorTree("brain", new Blackboard("1"));

		for (int i = 0; i < 100; i++) {
			tree.step();
		}
	}

	private Task<Blackboard> createDefParallel () {
		Gdx.app.log("", "createDefParallel");
		Selector<Blackboard> selector = new Selector<>();

		PolicyParallel<Blackboard> parallel = new PolicyParallel<>();
		parallel.addChild(new TaskA());
		parallel.addChild(new TaskB());

		selector.addChild(parallel);

		Sequence<Blackboard> sequence = new Sequence<>();
		selector.addChild(sequence);

		sequence.addChild(new TaskC("1"));
		sequence.addChild(new TaskC("2"));
		sequence.addChild(new TaskC("3"));
		return selector;
	}

	private Task<Blackboard> createSeqParallel () {
		Gdx.app.log("", "createSeqParallel");
		Selector<Blackboard> selector = new Selector<>();

		PolicyParallel<Blackboard> parallel = new PolicyParallel<>();
		parallel.addChild(new TaskA());
		parallel.addChild(new TaskB());

		selector.addChild(parallel);

		Sequence<Blackboard> sequence = new Sequence<>();
		selector.addChild(sequence);

		sequence.addChild(new TaskC("1"));
		sequence.addChild(new TaskC("2"));
		sequence.addChild(new TaskC("3"));
		return selector;
	}

	private Task<Blackboard> createSelParallel () {
		Gdx.app.log("", "createSelParallel");
		Selector<Blackboard> selector = new Selector<>();

		PolicyParallel<Blackboard> parallel = new PolicyParallel<>();
		parallel.addChild(new TaskA());
		parallel.addChild(new TaskB());

		selector.addChild(parallel);

		Sequence<Blackboard> sequence = new Sequence<>();
		selector.addChild(sequence);

		sequence.addChild(new TaskC("1"));
		sequence.addChild(new TaskC("2"));
		sequence.addChild(new TaskC("3"));
		return selector;
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	public static class Blackboard {
		public String name;

		public Blackboard (String name) {
			this.name = name;
		}
	}

	public static class TaskA extends LeafTask<Blackboard> {
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

		@Override protected Task<Blackboard> copyTo (Task<Blackboard> task) {
			return task;
		}
	}
	public static class TaskB extends LeafTask<Blackboard> {
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

		@Override protected Task<Blackboard> copyTo (Task<Blackboard> task) {
			return task;
		}
	}

	public static class TaskC extends LeafTask<Blackboard> {
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

		@Override protected Task<Blackboard> copyTo (Task<Blackboard> task) {
			TaskC taskC = (TaskC)task;
			taskC.name = name;
			return task;
		}
	}
}
