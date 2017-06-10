package io.piotrjastrzebski.playground.simple;

import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import net.mostlyoriginal.api.utils.pooling.ObjectPool;
import net.mostlyoriginal.api.utils.pooling.Poolable;
import net.mostlyoriginal.api.utils.pooling.Pools;

/**
 * Simple collision detection with spatial component
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class Collisions2Test extends BaseScreen {
	private static final String TAG = Collisions2Test.class.getSimpleName();

	private Array<Collider> colliders = new Array<>();
	private Rectangle bounds = new Rectangle();
	private RootQuadTree<Collider> quadTree;

	public Collisions2Test (GameReset game) {
		super(game);
		MathUtils.random.setSeed(43);
		for (int i = 0; i < 50; i++) {
			colliders.add(new RectCollider(
				MathUtils.random(-VP_WIDTH/2 + 2, VP_WIDTH/2 - 2),
				MathUtils.random(-VP_HEIGHT/2 + 2, VP_HEIGHT/2 - 2),
				MathUtils.random(.5f, 2f), MathUtils.random(.5f, 2f),
				MathUtils.random(0, 90/5)*5));

			colliders.add(new CircleCollider(
				MathUtils.random(-VP_WIDTH/2 + 2, VP_WIDTH/2 - 2),
				MathUtils.random(-VP_HEIGHT/2 + 2, VP_HEIGHT/2 - 2),
				MathUtils.random(.5f, 2f)/2f));
		}
		MathUtils.random.setSeed(TimeUtils.millis());

		quadTree = new RootQuadTree<>(-VP_WIDTH/2 + .5f, -VP_HEIGHT/2 + .5f, VP_WIDTH - 1, VP_HEIGHT - 1, 4, 6);
		for (Collider collider : colliders) {
			Rectangle aabb = collider.aabb;
			quadTree.insert(collider, aabb.x, aabb.y, aabb.width, aabb.height);
		}

	}

	Bag<Collider> fill = new Bag<>();
	@Override public void render (float delta) {
		for (Collider collider : colliders) {
			collider.update(delta);
			Rectangle aabb = collider.aabb;
			quadTree.update(collider, aabb.x, aabb.y, aabb.width, aabb.height);
		}
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.BLACK);
		renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

		draw(renderer, quadTree);

		for (Collider collider : colliders) {
			collider.draw(renderer);
		}


		fill.clear();
		quadTree.get(fill, cs.x, cs.y);

		boolean contains = false;
		for (Collider collider : fill) {
			renderer.setColor(Color.GOLD);
			if (collider.contains(cs.x, cs.y)) {
				renderer.setColor(Color.RED);
				contains |= true;
			}
			Rectangle aabb = collider.aabb;
			renderer.rect(aabb.x, aabb.y, aabb.width, aabb.height);
		}
		renderer.setColor(Color.RED);
		if (contains) {
			renderer.setColor(Color.GREEN);
		}
		renderer.line(cs.x + .25f, cs.y + .25f, cs.x - .25f, cs.y - .25f);
		renderer.line(cs.x + .25f, cs.y - .25f, cs.x - .25f, cs.y + .25f);
		renderer.circle(cs.x, cs.y, .25f, 16);
		renderer.end();
	}

	private void draw (ShapeRenderer renderer, QuadTree<Collider> quadTree) {
		renderer.setColor(0, 1, 0, .25f);
		QuadTree.Container bounds = quadTree.getBounds();
		renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
		for (QuadTree tree : quadTree.getNodes()) {
			if (tree != null) {
				draw(renderer, tree);
			}
		}

	}

	private static abstract class Collider {
		public Rectangle aabb = new Rectangle();
		public abstract void update(float delta);
		public abstract void draw(ShapeRenderer renderer);

		public abstract boolean contains (float x, float y);
	}

	private static class CircleCollider extends Collider {
		public Circle shape = new Circle();

		public CircleCollider (float x, float y, float radius) {
			shape.set(x, y, radius);
		}

		@Override public void update (float delta) {
			aabb.set(shape.x - shape.radius, shape.y - shape.radius, shape.radius * 2, shape.radius * 2);
		}

		@Override public void draw (ShapeRenderer renderer) {
			renderer.setColor(Color.WHITE);
			renderer.circle(shape.x, shape.y, shape.radius, 16);
			float x = shape.x;
			float y = shape.y;
			renderer.line(x - .25f, y, x + .25f, y);
			renderer.line(x, y - .25f, x, y + .25f);

			renderer.setColor(Color.CYAN);
			renderer.rect(aabb.x, aabb.y, aabb.width, aabb.height);
		}

		@Override public boolean contains (float x, float y) {
			return aabb.contains(x, y) && shape.contains(x, y);
		}
	}

	private static class RectCollider extends Collider {
		protected float[] verts = new float[8];
		public Polygon shape = new Polygon();

		public RectCollider (float x, float y, float width, float height, float rotation) {
			verts[0] = - width/2;
			verts[1] = - height/2;
			verts[2] = + width/2;
			verts[3] = - height/2;
			verts[4] = + width/2;
			verts[5] = + height/2;
			verts[6] = - width/2;
			verts[7] = + height/2;
			shape.setVertices(verts);
			shape.setPosition(x, y);
			shape.setRotation(rotation);
		}

		@Override public void update (float delta) {
			shape.setRotation(shape.getRotation() + delta * 90);
			aabb.set(shape.getBoundingRectangle());
		}

		@Override public void draw (ShapeRenderer renderer) {
			renderer.setColor(Color.WHITE);
			renderer.polygon(shape.getTransformedVertices());
			float x = shape.getX();
			float y = shape.getY();
			renderer.line(x - .25f, y, x + .25f, y);
			renderer.line(x, y - .25f, x, y + .25f);

			renderer.setColor(Color.CYAN);
			renderer.rect(aabb.x, aabb.y, aabb.width, aabb.height);
		}

		@Override public boolean contains (float x, float y) {
			return aabb.contains(x, y) && shape.contains(x, y);
		}
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		bounds.set(
			gameCamera.position.x - gameCamera.viewportWidth/2 + 1,
			gameCamera.position.y - gameCamera.viewportHeight/2 + 1,
			gameCamera.viewportWidth - 2, gameCamera.viewportHeight -2);
	}

	public static class RootQuadTree<T> extends QuadTree<T> {
		ObjectPool<QuadTree> qtPool = Pools.getPool(QuadTree.class);
		ObjectPool<Container> cPool = Pools.getPool(Container.class);
		ObjectMap<T, Container> objectToContainer = new ObjectMap<>();

		public RootQuadTree (float x, float y, float width, float height, int maxInBucket, int maxDepth) {
			this.maxInBucket = maxInBucket;
			this.maxDepth = maxDepth;
			bounds = new Container();
			nodes = new QuadTree[4];
			root = this;
			init(0, x, y, width, height, null);
		}
	}

	protected static class QuadTree<T> implements Poolable {
		public final static int OUTSIDE = -1;
		public final static int SW = 0;
		public final static int SE = 1;
		public final static int NW = 2;
		public final static int NE = 3;
		protected int depth;
		protected Bag<Container<T>> containers;
		protected Container<T> bounds;
		protected QuadTree<T>[] nodes;
		protected QuadTree<T> parent;
		protected RootQuadTree<T> root;
		protected int maxInBucket;
		protected int maxDepth;

		/**
		 * Public constructor for {@link ObjectPool} use only
		 */
		public QuadTree () {
			this(0, 0, 0, 0);
		}

		protected QuadTree (float x, float y, float width, float height) {
			bounds = new Container();
			nodes = new QuadTree[4];
			init(0, x, y, width, height, null);
		}

		protected QuadTree init (int depth, float x, float y, float width, float height, QuadTree<T> parent) {
			this.depth = depth;
			bounds.set(x, y, width, height);
			this.parent = parent;
			if (parent != null) {
				root = parent.root;
				maxDepth = parent.maxDepth;
				maxInBucket = parent.maxInBucket;
			}
			containers = new Bag<>(maxInBucket);
			return this;
		}

		private int indexOf (float x, float y, float width, float height) {
			float midX = bounds.x + bounds.width / 2;
			float midY = bounds.y + bounds.height / 2;
			boolean top = y > midY;
			boolean bottom = y < midY && y + height < midY;
			if (x < midX && x + width < midX) {
				if (top) {
					return NW;
				} else if (bottom) {
					return SW;
				}
			} else if (x > midX) {
				if (top) {
					return NE;
				} else if (bottom) {
					return SE;
				}
			}
			return OUTSIDE;
		}

		/**
		 * Inserts given entity id to tree with given bounds
		 */
		public void insert (T object, float x, float y, float width, float height) {
			insert(root.cPool.obtain().set(object, x, y, width, height));
		}

		protected void insert (Container<T> c) {
			if (nodes[0] != null) {
				int index = indexOf(c.x, c.y, c.width, c.height);
				if (index != OUTSIDE) {
					nodes[index].insert(c);
					return;
				}
			}
			c.parent = this;
			root.objectToContainer.put(c.object, c);
			containers.add(c);

			if (containers.size() > maxInBucket && depth < maxDepth) {
				if (nodes[0] == null) {
					float halfWidth = bounds.width / 2;
					float halfHeight = bounds.height / 2;
					nodes[SW] = root.qtPool.obtain().init(depth + 1, bounds.x, bounds.y, halfWidth, halfHeight, this);
					nodes[SE] = root.qtPool.obtain().init(depth + 1, bounds.x + halfWidth, bounds.y, halfWidth, halfHeight, this);
					nodes[NW] = root.qtPool.obtain().init(depth + 1, bounds.x, bounds.y + halfHeight, halfWidth, halfHeight, this);
					nodes[NE] = root.qtPool.obtain().init(depth + 1, bounds.x + halfWidth, bounds.y + halfHeight, halfWidth, halfHeight, this);
				}

				Object[] items = containers.getData();
				for (int i = containers.size() - 1; i >= 0; i--) {
					Container next = (Container)items[i];
					int index = indexOf(next.x, next.y, next.width, next.height);
					if (index != OUTSIDE) {
						nodes[index].insert(next);
						containers.remove(i);
					}
				}
			}
		}

		/**
		 * Returns entity ids of entities that are inside {@link QuadTree}s that contain given point
		 *
		 * Returned entities must be filtered further as these results are not exact
		 */
		public Bag<T> get (Bag<T> fill, float x, float y) {
			if (bounds.contains(x, y)) {
				if (nodes[0] != null) {
					int index = indexOf(x, y, 0, 0);
					if (index != OUTSIDE) {
						nodes[index].get(fill, x, y, 0, 0);
					}
				}
				for (int i = 0; i < containers.size(); i++) {
					fill.add(containers.get(i).object);
				}
			}
			return fill;
		}

		/**
		 * Returns entity ids of entities that bounds contain given point
		 */
		public Bag<T> getExact (Bag<T> fill, float x, float y) {
			if (bounds.contains(x, y)) {
				if (nodes[0] != null) {
					int index = indexOf(x, y, 0, 0);
					if (index != OUTSIDE) {
						nodes[index].getExact(fill, x, y, 0, 0);
					}
				}
				for (int i = 0; i < containers.size(); i++) {
					Container<T> c = containers.get(i);
					if (c.contains(x, y)) {
						fill.add(c.object);
					}
				}
			}
			return fill;
		}

		/**
		 * Returns entity ids of entities that are inside {@link QuadTree}s that overlap given bounds
		 *
		 * Returned entities must be filtered further as these results are not exact
		 */
		public Bag<T> get (Bag<T> fill, float x, float y, float width, float height) {
			if (bounds.overlaps(x, y, width, height)) {
				if (nodes[0] != null) {
					int index = indexOf(x, y, width, height);
					if (index != OUTSIDE) {
						nodes[index].get(fill, x, y, width, height);
					} else {
						// if test bounds don't fully fit inside a node, we need to check them all
						for (int i = 0; i < nodes.length; i++) {
							nodes[i].get(fill, x, y, width, height);
						}
					}
				}
				for (int i = 0; i < containers.size(); i++) {
					Container<T> c = containers.get(i);
					fill.add(c.object);
				}
			}
			return fill;
		}

		/**
		 * Returns entity ids of entities that overlap given bounds
		 */
		public Bag<T> getExact (Bag<T> fill, float x, float y, float width, float height) {
			if (bounds.overlaps(x, y, width, height)) {
				if (nodes[0] != null) {
					int index = indexOf(x, y, width, height);
					if (index != OUTSIDE) {
						nodes[index].getExact(fill, x, y, width, height);
					} else {
						// if test bounds don't fully fit inside a node, we need to check them all
						for (int i = 0; i < nodes.length; i++) {
							nodes[i].getExact(fill, x, y, width, height);
						}
					}
				}
				for (int i = 0; i < containers.size(); i++) {
					Container<T> c = containers.get(i);
					if (c.overlaps(x, y, width, height)) {
						fill.add(c.object);
					}
				}
			}
			return fill;
		}

		/**
		 * Update position for this id with new one
		 */
		public void update (T object, float x, float y, float width, float height) {
			Container<T> c = root.objectToContainer.get(object);
			c.set(object, x, y, width, height);

			QuadTree qTree = c.parent;
			qTree.containers.remove(c);
			while (qTree.parent != null && !qTree.bounds.contains(c)) {
				qTree = qTree.parent;
			}
			qTree.insert(c);
		}

		/**
		 * Remove this id from the tree
		 */
		public void remove (T object) {
			Container<T> c = root.objectToContainer.get(object);
			if (c == null)
				return;
			if (c.parent != null) {
				c.parent.containers.remove(c);
			}
			root.cPool.free(c);
		}

		/**
		 * Reset the QuadTree by removing all nodes and stored ids
		 */
		@Override public void reset () {
			for (int i = containers.size() - 1; i >= 0; i--) {
				root.cPool.free(containers.remove(i));
			}
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i] != null) {
					root.qtPool.free(nodes[i]);
					nodes[i] = null;
				}
			}
		}

		/**
		 * Dispose of the QuadTree by removing all nodes and stored ids
		 */
		public void dispose () {
			reset();
		}

		/**
		 * @return {@link QuadTree[]} with nodes of these tree, nodes may be null
		 */
		public QuadTree[] getNodes () {
			return nodes;
		}

		/**
		 * @return {@link Container} that represents bounds of this tree
		 */
		public Container getBounds () {
			return bounds;
		}

		@Override public String toString () {
			return "QuadTree{" +
				"depth=" + depth + "}";
		}

		/**
		 * Simple container for entity ids and their bounds
		 */
		public static class Container<T> implements Poolable {
			T object;
			float x;
			float y;
			float width;
			float height;
			QuadTree parent;

			public Container () {}

			public Container set (T object, float x, float y, float width, float height) {
				this.object = object;
				this.x = x;
				this.y = y;
				this.width = width;
				this.height = height;
				return this;
			}

			public Container set (float x, float y, float width, float height) {
				this.x = x;
				this.y = y;
				this.width = width;
				this.height = height;
				return this;
			}

			public boolean contains (float x, float y) {
				return this.x <= x && this.x + this.width >= x && this.y <= y && this.y + this.height >= y;
			}

			public boolean overlaps (float x, float y, float width, float height) {
				return this.x < x + width && this.x + this.width > x && this.y < y + height && this.y + this.height > y;
			}

			public boolean contains (float ox, float oy, float owidth, float oheight) {
				float xmin = ox;
				float xmax = xmin + owidth;

				float ymin = oy;
				float ymax = ymin + oheight;

				return ((xmin > x && xmin < x + width) && (xmax > x && xmax < x + width)) && ((ymin > y && ymin < y + height) && (ymax > y && ymax < y + height));
			}

			public boolean contains (Container c) {
				return contains(c.x, c.y, c.width, c.height);
			}

			@Override public void reset () {
				object = null;
				x = 0;
				y = 0;
				width = 0;
				height = 0;
				parent = null;
			}

			public float getX () {
				return x;
			}

			public float getY () {
				return y;
			}

			public float getWidth () {
				return width;
			}

			public float getHeight () {
				return height;
			}
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, Collisions2Test.class);
	}
}
