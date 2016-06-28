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
		public Lightning () {

		}

		float rebuild = 0;
		public void update (float delta) {
			rebuild -= delta;
			if (rebuild <= 0) {
				rebuild = .25f;
				start.children.clear();
				build(start, end, 50);
			}
		}


		public void draw(ShapeRenderer renderer) {
			renderer.setColor(Color.GREEN);
			start.draw(renderer);

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

			public Node (Node node) {
				this.x = node.x;
				this.y = node.y;
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

		static Node rng = new Node();
		static Node build (Node start, Node end, int maxIters) {
			float dst = start.dst(end);
			int iter = 0;
			boolean goalReached = false;
			while (iter < maxIters) {
				Node rng = randomNode(end, dst/3);

				Node closest = start.getClosestNode(rng);

				Node extension = createExtension(closest, rng, .5f, 1.5f, 1f);
				// If we managed to create a new node, add it to the tree.
				if (extension != null) {
					closest.add(extension);

					// If we haven't yet reached the goal, and the new node
					// is very near the goal, add the goal to the tree.
					if(extension.isEqual(end)) {
						extension.add(end);
						goalReached = true;
						break;
					}
				}
				iter++;
			}
			if (!goalReached) {
				Node closest = start.getClosestNode(end);
				closest.add(end);
			}
			return start;
		}

		static Node randomNode(Node target, float range) {
			rngPointInCircle(target.x, target.y, range, tmp);
			rng.x = tmp.x;
			rng.y = tmp.y;
			return rng;
		}

		static Vector2 tmp = new Vector2();
		static Node createExtension(Node from, Node to, float minDst, float maxDst, float range) {
			if (MathUtils.random() > .9f) return null;
			Node node = new Node();
			tmp.set(to.x, to.y).sub(from.x, from.y).nor().scl(MathUtils.random(minDst, maxDst));
			rngPointInCircle(tmp.x, tmp.y, range, tmp);
			node.x = from.x + tmp.x;
			node.y = from.y + tmp.y;
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
