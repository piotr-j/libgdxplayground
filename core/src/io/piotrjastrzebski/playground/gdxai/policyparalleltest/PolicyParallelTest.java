package io.piotrjastrzebski.playground.gdxai.policyparalleltest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.decorator.Include;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibrary;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibraryManager;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
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
//				test(createSeqParallel());
//				test(createSelParallel());
			}
		});
		dialog.add(run);
		stage.addActor(dialog);
		dialog.centerWindow();
		test(createDefParallel());
//		test(createSeqParallel());
//		test(createSelParallel());
	}

	private void test(Task<Blackboard> task) {
		BehaviorTreeLibraryManager libraryManager = BehaviorTreeLibraryManager.getInstance();
		BehaviorTreeLibrary library = new BehaviorTreeLibrary(BehaviorTreeParser.DEBUG_HIGH);

		Include<Blackboard> include = new Include<>();
		include.lazy = false;
		include.subtree = "brain.actual";

		library.registerArchetypeTree("brain", new BehaviorTree<>(include));
		library.registerArchetypeTree("brain.actual", new BehaviorTree<>(task));

		libraryManager.setLibrary(library);
		BehaviorTree<Blackboard> tree = libraryManager.createBehaviorTree("brain.actual", new Blackboard("1"));

		for (int i = 0; i <= 10; i++) {
			Gdx.app.log("", "step");
			tree.step();
		}
	}

	private Task<Blackboard> createDefParallel () {
		Gdx.app.log("", "createDefParallel");
		Selector<Blackboard> selector = new Selector<>();

		selector.addChild(new StartTask());
		Parallel<Blackboard> parallel = new Parallel<>();
		parallel.addChild(new CheckTask());
		parallel.addChild(new RunningTask());

		selector.addChild(parallel);
		selector.addChild(new EndTask());

		return selector;
	}

	private Task<Blackboard> createSeqParallel () {
		Gdx.app.log("", "createSeqParallel");
		Selector<Blackboard> selector = new Selector<>();

		PolicyParallel<Blackboard> parallel = new PolicyParallel<>();
//		parallel.sequencePolicy = false;
		parallel.addChild(new CheckTask());
		parallel.addChild(new RunningTask());

		selector.addChild(parallel);
		selector.addChild(new EndTask());
		return selector;
	}

	private Task<Blackboard> createSelParallel () {
		Gdx.app.log("", "createSelParallel");
		Selector<Blackboard> selector = new Selector<>();

		PolicyParallel<Blackboard> parallel = new PolicyParallel<>();
//		parallel.sequencePolicy = true;

		parallel.addChild(new CheckTask());
		parallel.addChild(new RunningTask());

		selector.addChild(parallel);
		selector.addChild(new EndTask());

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

	public static class CheckTask extends LeafTask<Blackboard> {
		private final static String TAG = CheckTask.class.getSimpleName();

		int runs;
		@Override public void run () {
			if (runs++ >= 2) {
				System.out.print(" Fail! " );
				fail();
			} else {
				System.out.print(" Success! ");
				success();
//				runs = 0;
			}
		}

		@Override public void start () {
			System.out.print(TAG + " Start! ");
		}

		@Override public void end () {
			System.out.println(" End! ");
		}

		@Override protected Task<Blackboard> copyTo (Task<Blackboard> task) {
			return task;
		}
	}

	public static class RunningTask extends LeafTask<Blackboard> {
		private final static String TAG = RunningTask.class.getSimpleName();

		int runs;
		@Override public void run () {
			if (runs++ > 5) {
				System.out.print(TAG + " Fail! ");
				fail();
			} else {
				System.out.println(TAG + " Running! ");
				running();
			}
		}

		@Override public void start () {
			System.out.print(TAG + " Start! ");
		}

		@Override public void end () {
			System.out.println(" End! ");
		}

		@Override protected Task<Blackboard> copyTo (Task<Blackboard> task) {
			return task;
		}
	}

	public static class EndTask extends LeafTask<Blackboard> {
		private final static String TAG = EndTask.class.getSimpleName();

		@Override public void run () {
			System.out.print(" Success! ");
			success();
		}

		@Override public void start () {
			System.out.print(TAG+" Start! ");
		}

		@Override public void end () {
			System.out.println(" End! ");
		}

		@Override protected Task<Blackboard> copyTo (Task<Blackboard> task) {
			return task;
		}
	}
	public static class StartTask extends LeafTask<Blackboard> {
		private final static String TAG = StartTask.class.getSimpleName();

		@Override public void run () {
			System.out.print(" Success! ");
			fail();
		}

		@Override public void start () {
			System.out.print(TAG+" Start! ");
		}

		@Override public void end () {
			System.out.println(" End! ");
		}

		@Override protected Task<Blackboard> copyTo (Task<Blackboard> task) {
			return task;
		}
	}
}
