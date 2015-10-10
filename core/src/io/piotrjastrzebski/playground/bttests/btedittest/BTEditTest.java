package io.piotrjastrzebski.playground.bttests.btedittest;

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
public class BTEditTest extends BaseScreen {

	private BehaviorTree<Dog> tree;

	BehaviorTree<Dog> dogBehaviorTreeArchetype;
	public BTEditTest (GameReset game) {
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

		createModel(tree);
	}

	private void createModel (BehaviorTree<Dog> dogBehaviorTreeA) {
		// do we want a real model?
		ModelTree<Dog> modelTree = new ModelTree<>();
		modelTree.init(dogBehaviorTreeA);
	}

	float elapsedTime;
	int step;
	@Override public void render (float delta) {
		super.render(delta);

		elapsedTime += delta;

		if (elapsedTime > 1) {
			System.out.println("\nStep: " + (++step));
			tree.step();
			elapsedTime -= 1;
		}
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
