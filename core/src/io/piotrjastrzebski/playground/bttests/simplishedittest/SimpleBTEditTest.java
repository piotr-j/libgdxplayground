package io.piotrjastrzebski.playground.bttests.simplishedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.*;
import com.badlogic.gdx.ai.btree.leaf.Failure;
import com.badlogic.gdx.ai.btree.leaf.Success;
import com.badlogic.gdx.ai.btree.leaf.Wait;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.ai.utils.random.ConstantIntegerDistribution;
import com.badlogic.gdx.utils.StreamUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.bttests.dog.*;

import java.io.Reader;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class SimpleBTEditTest extends BaseScreen {
	private static final String TAG = SimpleBTEditTest.class.getSimpleName();

	private BehaviorTree<Dog> tree;

	BehaviorTree<Dog> dogBehaviorTreeArchetype;
	BTModel<Dog> btModel;

	public SimpleBTEditTest (GameReset game) {
		super(game);

		dogBehaviorTreeArchetype = null;

		Reader reader = null;
		try {
			reader = Gdx.files.internal("btree/dog.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE) {
				protected BehaviorTree<Dog> createBehaviorTree (Task<Dog> root, Dog object) {
					if (debug > BehaviorTreeParser.DEBUG_LOW) printTree(root, 0);
					return new BehaviorTree<>(root, object);
				}
			};
			dogBehaviorTreeArchetype = parser.parse(reader, null);
		} finally {
			StreamUtils.closeQuietly(reader);
		}

		tree = (BehaviorTree<Dog>)dogBehaviorTreeArchetype.cloneTask();
		tree.setObject(new Dog("Dog A"));

		TaskAction.setLogger(new Logger() {
			@Override public void log (String tag, String msg) {
				Gdx.app.log(tag, msg);
			}

			@Override public void error (String tag, String msg) {
				Gdx.app.error(tag, msg);
			}

			@Override public void error (String tag, String msg, Exception e) {
				Gdx.app.error(tag, msg, e);
			}
		});

		// we probably could use BehaviorTreeLibrary for task store for add?
		btModel = new BTModel<>();

		TaskLibrary<Dog> lib = btModel.getTaskLibrary();
		lib.add(Sequence.class);
		lib.add(Selector.class);
		lib.add(Parallel.class);

		lib.add(AlwaysFail.class);
		lib.add(AlwaysSucceed.class);
		lib.add(Include.class);
		lib.add(Invert.class);
		lib.add(Random.class);
		lib.add(Repeat.class);
		lib.add(SemaphoreGuard.class);
		lib.add(UntilFail.class);
		lib.add(UntilSuccess.class);

		lib.add(Wait.class);
		lib.add(Success.class);
		lib.add(Failure.class);

		lib.add(BarkTask.class);
		lib.add(CareTask.class);
		lib.add(MarkTask.class);
		lib.add(RestTask.class);
		lib.add(WalkTask.class);

		btModel.init(tree);
	}

	float elapsedTime;
	int step;
	@Override public void render (float delta) {
		super.render(delta);

		elapsedTime += delta;

		if (elapsedTime > 1) {
//			Gdx.app.log(TAG, "Step: " + (++step));
			btModel.step();
			elapsedTime -= 1;
		}
		stage.act(delta);
		stage.draw();
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.F5:
			break;
		case Input.Keys.F9:
			break;
		}
		return super.keyDown(keycode);
	}
}
