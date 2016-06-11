package io.piotrjastrzebski.playground.bttests.savetest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.utils.StreamUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.bttests.dog.Dog;

import java.io.Reader;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class BTSaveTest extends BaseScreen {

	private BehaviorTree<Dog> tree;

	public BTSaveTest (GameReset game) {
		super(game);
		tree = null;
		Reader reader = null;
		try {
			reader = Gdx.files.internal("btree/dog.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE) {
				protected BehaviorTree<Dog> createBehaviorTree (Task<Dog> root, Dog object) {
					if (debug > BehaviorTreeParser.DEBUG_LOW) printTree(root, 0);
					return new BehaviorTree<>(root, object);
				}
			};
			tree = parser.parse(reader, null);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
		tree.setObject(new Dog("Welp"));
	}

	private void load () {
		Reader reader = null;
		try {
			reader = Gdx.files.external("save.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE) {
				protected BehaviorTree<Dog> createBehaviorTree (Task<Dog> root, Dog object) {
					if (debug > BehaviorTreeParser.DEBUG_LOW) printTree(root, 0);
					return new BehaviorTree<>(root, object);
				}
			};
			BehaviorTree tree = parser.parse(reader, null);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.F5:
			Gdx.app.log("", "save");
			BehaviorTreeSaver.save(tree, "save.tree");
			break;
		case Input.Keys.F9:
			Gdx.app.log("", "load");
			load();
			break;
		}
		return super.keyDown(keycode);
	}
}
