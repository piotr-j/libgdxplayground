package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class LightningTest extends BaseScreen {
	private static final String TAG = LightningTest.class.getSimpleName();

	Array<Lightning> lightnings = new Array<>();
	Array<Vector2> points = new Array<>();
	public LightningTest (GameReset game) {
		super(game);

		lightnings.add(new Lightning());
//		points.add(new Vector2());
//		for (int i = 0; i < 1000; i++) {
//			points.add(rngPointInCircle(0, 0, 3, new Vector2()));
//			points.add(rngPoint(0, 5, 3, 15, new Vector2()));
//		}
	}

	protected static Vector2 rngPointInCircle (float x, float y, float range, Vector2 out) {
		float t = MathUtils.PI2 * MathUtils.random();
		float r = (float)Math.sqrt(MathUtils.random(2f));
		out.set(r * MathUtils.cos(t), r * MathUtils.sin(t));
		out.scl(range);
		out.add(x, y);
		return out;
	}

	static Vector2 rngPoint (float x, float y, float range, float angle, Vector2 out) {
		out.set(x, y);
		out.nor().rotate(MathUtils.random(-angle, angle)).scl(range);
		return out;
	}

	@Override public void render (float delta) {
		super.render(delta);
		enableBlending();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(0, 1, 0, .2f);
		for (Vector2 point : points) {
			renderer.circle(point.x, point.y, .1f, 8);
		}
		renderer.end();

		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(0, 1, 0, 1);
		for (Lightning lightning : lightnings) {
			lightning.update(delta);
			lightning.draw(renderer);
		}
		renderer.end();
	}

	/**
	 * https://gamedev.stackexchange.com/questions/71397/how-can-i-generate-a-lightning-bolt-effect
	 */
	protected static class Lightning {
		public Node start = new Node();
		public Node end = new Node(5, 0);
		protected Tree tree;
		public Lightning () {

		}

		float rebuild = 0;
		public void update (float delta) {
			rebuild -= delta;
			if (rebuild <= 0) {
				rebuild = 1;
				start.children.clear();
				end.children.clear();
				tree = build(start, end, 10);
			}
		}


		public void draw(ShapeRenderer renderer) {
			renderer.setColor(Color.GREEN);
			tree.root.draw(renderer);

			renderer.setColor(Color.YELLOW);
			renderer.circle(start.x, start.y, .2f, 12);
			renderer.setColor(Color.RED);
			renderer.circle(end.x, end.y, .2f, 12);
		}

		static class Node {
			public float x;
			public float y;
			public Array<Node> children = new Array<>();

			public Node () {
				this(0, 0);
			}

			public Node (float x, float y) {
				this.x = x;
				this.y = y;
			}

			public void draw (ShapeRenderer renderer) {
				renderer.circle(x, y, .1f, 8);
				for (Node child : children) {
					renderer.line(x, y, child.x, child.y);
					child.draw(renderer);
				}
			}

			public Node getClosestNode (Node target) {
				if (children.size == 0) return this;
				Node closest = this;
				float dst = closest.dst(target);
				for (Node child : children) {
					Node other = child.getClosestNode(target);
					float dst2 = other.dst(target);
					if (dst2 < dst) {
						dst = dst2;
						closest = other;
					}
				}
				return closest;
			}

			public float dst (Node other) {
				final float x_d = other.x - x;
				final float y_d = other.y - y;
				return (float)Math.sqrt(x_d * x_d + y_d * y_d);
			}

			public void add (Node child) {
				children.add(child);
			}

			public boolean isEqual (Node other) {
				return MathUtils.isEqual(x, other.x) && MathUtils.isEqual(y, other.y);
			}

			@Override public String toString () {
				return "Node{" +
					"x=" + x +
					", y=" + y +
					'}';
			}
		}

		static class Tree {
			public Node root;

			public Node getClosestNode (Node target) {
				return root.getClosestNode(target);
			}
		}

		static Tree build (Node start, Node end, int maxIters) {
			Tree tree = new Tree();
			tree.root = start;
			float dst = start.dst(end);
			int iter = 0;
			boolean goalReached = false;
			while (iter < maxIters) {
				Node rng = randomNode(end, dst);

				Node closest = tree.getClosestNode(rng);
				Node extension = createExtension(closest, rng, dst/maxIters, 0f);
				// If we managed to create a new node, add it to the tree.
				if (extension != null)
				{
					closest.add(extension);

					// If we haven't yet reached the goal, and the new node
					// is very near the goal, add the goal to the tree.
					if(extension.isEqual(end))
					{
						extension.add(end);
						goalReached = true;
						break;
					}
				}
				iter++;
			}
			if (!goalReached) {
//				Node closest = tree.getClosestNode(end);
//				closest.add(end);
			}
			return tree;
		}

		static Node randomNode(Node target, float range) {
			Node node = new Node();
			rngPointInCircle(target.x, target.y, range, tmp);
			node.x = tmp.x;
			node.y = tmp.y;
//			node.x = target.x;
//			node.y = target.y;
			return node;
		}

		static Vector2 tmp = new Vector2();
		static Node createExtension(Node from, Node to, float dst, float range) {
//			if (MathUtils.random() > .75f) return null;
			Node node = new Node();
			tmp.set(to.x, to.y).sub(from.x, from.y).nor().scl(dst);
//			rngPointInCircle(tmp.x, tmp.y, range, tmp);
			node.x = tmp.x;
			node.y = tmp.y;

//			float t = MathUtils.PI2 * MathUtils.random();
//			float u = MathUtils.random(2f);
//			float r = (float)Math.sqrt(u);
//			float scl = 1;
//			float x = r * MathUtils.cos(t) * scl;
//			float y = r * MathUtils.sin(t) * scl;
//			node.x = x;
//			node.y = y;

//			node.x = (from.x + to.x)/2 + MathUtils.random(-.25f, .25f) * dst;
//			node.y = (from.y + to.y)/2 + MathUtils.random(-.25f, .25f) * dst;
			return node;
		}
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		for (Lightning lightning : lightnings) {
			lightning.end.x = cs.x;
			lightning.end.y = cs.y;
		}
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		super.touchDragged(screenX, screenY, pointer);
		for (Lightning lightning : lightnings) {
			lightning.end.x = cs.x;
			lightning.end.y = cs.y;
		}
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		super.touchUp(screenX, screenY, pointer, button);
		return true;
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, LightningTest.class);
	}
}
