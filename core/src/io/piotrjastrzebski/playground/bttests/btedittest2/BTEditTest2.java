package io.piotrjastrzebski.playground.bttests.btedittest2;

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
import io.piotrjastrzebski.playground.bttests.btedittest2.model.BTModel;
import io.piotrjastrzebski.playground.bttests.btedittest2.model.TaskLibrary;
import io.piotrjastrzebski.playground.bttests.dog.*;

import java.io.Reader;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class BTEditTest2 extends BaseScreen implements ViewTree.ViewTaskSelectedListener<Dog> {
	private static final String TAG = BTEditTest2.class.getSimpleName();

	private BehaviorTree<Dog> tree;
	BTModel<Dog> modelTree;
	ViewTree<Dog> viewTree;
	VisWindow window;
	VisLabel trash;

	BehaviorTree<Dog> dogBehaviorTreeArchetype;

	Array<TaskNode> nodes = new Array<>();
	public BTEditTest2 (GameReset game) {
		super(game);

		dogBehaviorTreeArchetype = null;

		Reader reader = null;
		try {
			reader = Gdx.files.internal("btree/dog.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE) {
				protected BehaviorTree<Dog> createBehaviorTree (Task<Dog> root, Dog object) {
					if (debugLevel > BehaviorTreeParser.DEBUG_LOW) printTree(root, 0);
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
		window.setSize(600, 800);
		window.centerWindow();

		VisTable container = new VisTable(true);
		window.add(container);

		trash = new VisLabel("Trash -> [_]");
		container.add(trash).colspan(2);
		container.row();

		viewTree = new ViewTree<>();
		viewTree.addListener(this);
		viewTree.addTrash(trash);


		nodes.add(new TaskNode(Sequence.class));
		nodes.add(new TaskNode(Selector.class));
		nodes.add(new TaskNode(Parallel.class));

		nodes.add(new TaskNode(AlwaysFail.class));
		nodes.add(new TaskNode(AlwaysSucceed.class));
//		nodes.add(new TaskNode(Include.class));
		nodes.add(new TaskNode(Invert.class));
//		nodes.add(new TaskNode(Random.class));
		nodes.add(new TaskNode(Repeat.class));
		nodes.add(new TaskNode(SemaphoreGuard.class));
		nodes.add(new TaskNode(UntilFail.class));
		nodes.add(new TaskNode(UntilSuccess.class));

		nodes.add(new TaskNode(Wait.class));
		nodes.add(new TaskNode(Success.class));
		nodes.add(new TaskNode(Failure.class));

		nodes.add(new TaskNode(BarkTask.class));
		nodes.add(new TaskNode(CareTask.class));
		nodes.add(new TaskNode(MarkTask.class));
		nodes.add(new TaskNode(RestTask.class));
		nodes.add(new TaskNode(WalkTask.class));

		VisTable taskNodes = new VisTable();
		for (TaskNode node : nodes) {
			taskNodes.add(node.label).row();
			viewTree.addSource(node.label, node.taskClass);
		}

		container.add(viewTree);
		VisScrollPane pane = new VisScrollPane(taskNodes);
		container.add(pane);
		pane.setScrollingDisabled(true, false);

		modelTree = new BTModel<>();
		TaskLibrary<Dog> tl = modelTree.getTaskLibrary();
		tl.add(Sequence.class);
		tl.add(Selector.class);
		tl.add(Parallel.class);
		tl.add(AlwaysFail.class);
		tl.add(AlwaysSucceed.class);
//		tl.add(Include.class);
		tl.add(Invert.class);
//		tl.add(Random.class);
		tl.add(Repeat.class);
		tl.add(SemaphoreGuard.class);
		tl.add(UntilFail.class);
		tl.add(UntilSuccess.class);
		tl.add(Wait.class);
		tl.add(Success.class);
		tl.add(Failure.class);
		tl.add(BarkTask.class);
		tl.add(CareTask.class);
		tl.add(MarkTask.class);
		tl.add(RestTask.class);
		tl.add(WalkTask.class);

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

		if (elapsedTime > 1) {
//			Gdx.app.log(TAG, "Step: " + (++step));
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

	@Override public void selected (ViewTask<Dog> task) {
		Gdx.app.log(TAG, "Selected " + task);
	}

	@Override public void deselected () {
		Gdx.app.log(TAG, "Deselected");
	}

	public static class TaskNode {
		public Class<? extends Task> taskClass;
		public VisLabel label = new VisLabel();

		public TaskNode (Class<? extends Task> taskClass) {
			label.setText(taskClass.getSimpleName());
			this.taskClass = taskClass;
		}
	}
}
