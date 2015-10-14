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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StreamUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.bttests.btedittest.ModelTree;
import io.piotrjastrzebski.playground.bttests.btedittest.ViewTask;
import io.piotrjastrzebski.playground.bttests.btedittest.ViewTree;
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

		btModel = new BTModel<>();
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
