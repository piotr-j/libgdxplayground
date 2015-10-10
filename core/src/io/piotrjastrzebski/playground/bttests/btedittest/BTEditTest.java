package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.bttests.dog.Dog;

import java.io.Reader;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class BTEditTest extends BaseScreen {
	private static final String TAG = BTEditTest.class.getSimpleName();

	private BehaviorTree<Dog> tree;
	ModelTree<Dog> modelTree;
	ViewTree<Dog> viewTree;
	VisWindow window;

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

		window = new VisWindow("Stuff");
		stage.addActor(window);
		window.setSize(600, 600);
		window.centerWindow();

		viewTree = new ViewTree<>();
		window.add(viewTree);
		modelTree = new ModelTree<>();
		createModel(tree);
	}

	private void createModel (BehaviorTree<Dog> tree) {
		modelTree.init(tree);
		viewTree.init(modelTree);
	}

	float elapsedTime;
	int step;
	@Override public void render (float delta) {
		super.render(delta);

		elapsedTime += delta;

		if (elapsedTime > 1 && modelTree.isValid()) {
			Gdx.app.log(TAG, "Step: " + (++step));
			modelTree.step();
			elapsedTime -= 1;
		}
		viewTree.update(delta);
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
