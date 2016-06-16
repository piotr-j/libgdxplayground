package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StreamUtils;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.bttests.dog2.Dog;

import java.io.Reader;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISImpleGraphTest extends BaseScreen {
	private final BehaviorTree<Dog> dogBehaviorTreeArchetype;
	private final BehaviorTree<Dog> tree;
	VisWindow window;
	public UISImpleGraphTest (GameReset game) {
		super(game);
		Reader reader = null;
		try {
			reader = Gdx.files.internal("btree/dog2.tree").reader();
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

		window = new VisWindow("Tree Graph");
		window.add(createView(tree)).expand().fill();
		window.pack();
		stage.addActor(window);
		window.centerWindow();
		window.setResizable(true);
	}

	private Actor createView(BehaviorTree tree) {
		TextureRegionDrawable white = (TextureRegionDrawable)VisUI.getSkin().getDrawable("white");
		Task task = tree.getChild(0);
		GraphView view = new GraphView(white);
		GraphView.Node root = new GraphView.Node(createTaskActor(task));
		view.setRoot(root);
		for (int i = 0; i < task.getChildCount(); i++) {
			Task child = task.getChild(i);
			createNodes(root, child);
		}
		return view;
	}

	private void createNodes (GraphView.Node parent, Task task) {
		GraphView.Node node = new GraphView.Node(createTaskActor(task));
		parent.addNode(node);
		for (int i = 0; i < task.getChildCount(); i++) {
			Task child = task.getChild(i);
			createNodes(node, child);
		}
	}

	private Actor createTaskActor(Task task) {
		return new VisLabel(task.getClass().getSimpleName());
	}

	private Actor createView2(BehaviorTree tree) {
		Task root = tree.getChild(0);
		return createTaskView2(root);
	}

	private Actor createTaskView2(Task task) {
		final VisTable table = new VisTable(true);
		VisTable top = new VisTable();
		table.add(top).padBottom(25 * VisUI.getSizes().scaleFactor).row();
		top.add(new VisLabel(task.getClass().getSimpleName()));
		VisTable children = new VisTable(true);
		Actor add = children;
		if (task.getChildCount() > 0) {
			final CollapsibleWidget cw = new CollapsibleWidget(children, false);
			add = cw;
			final VisTextButton toggle = new VisTextButton("<");
			toggle.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					if (cw.isCollapsed()) {
						toggle.setText("<");
						cw.setCollapsed(false);
					} else {
						toggle.setText(">");
						cw.setCollapsed(true);
					}
				}
			});
			top.add(toggle);
		}
		table.add(add);
		for (int i = 0; i < task.getChildCount(); i++) {
			children.add(createTaskView2(task.getChild(i))).expand().top();
		}
		return table;
	}

	public static class GraphView extends Table {
		Node root;
		TextureRegionDrawable line;

		public GraphView (TextureRegionDrawable line) {
			this.line = line;
		}

		public void setRoot (Node root) {
			this.root = root;
			add(root);
		}

		public static class Node extends Table  {
			Actor actor;
			Array<Node> childrenNodes = new Array<>();
			Table children;

			public Node (Actor actor) {
				this.actor = actor;
				Table top = new Table();
				add(top).padBottom(25).row();
				top.add(actor);
				children = new Table();
				add(children);
			}

			public void addNode (Node node) {
				childrenNodes.add(node);
				children.add(node).pad(5).expand().top();
			}
		}

		@Override public void draw (Batch batch, float parentAlpha) {
			super.draw(batch, parentAlpha);
			drawConnections(batch, root);
		}

		Vector2 start = new Vector2();
		Vector2 end = new Vector2();
		Vector2 tmp = new Vector2();
		private void drawConnections (Batch batch, Node root) {
			Actor rootActor = root.actor;
			start.set(rootActor.getX() + rootActor.getWidth() / 2, rootActor.getY());
			rootActor.localToAscendantCoordinates(this, start);
			float sx = start.x;
			float sy = start.y;

			for (Node node : root.childrenNodes) {
				Actor nodeActor = node.actor;
				end.set(nodeActor.getX() + nodeActor.getWidth() / 2, nodeActor.getY() + nodeActor.getHeight());
				nodeActor.localToAscendantCoordinates(this, end);

				float len = tmp.set(end).sub(sx, sy).len();
				float angle = tmp.angle();
				batch.setColor(node.actor.getColor());
				line.draw(batch, sx, sy, 0, 1.5f, len, 3, 1, 1, angle);
				drawConnections(batch, node);
			}
		}
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}
}
