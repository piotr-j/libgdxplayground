package io.piotrjastrzebski.playground.ecs.quadtreetest;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

@Wire
public class QTSystem extends EntityProcessingSystem {
	private ComponentMapper<Position> mPosition;
	private ComponentMapper<Size> mSize;

	private QuadTree base;
	public QTSystem () {
		super(Aspect.all(Position.class, Size.class));
	}

	@Override protected void initialize () {
		super.initialize();
		init(-QuadTreeTest.VP_WIDTH / 2, -QuadTreeTest.VP_HEIGHT / 2, QuadTreeTest.VP_WIDTH, QuadTreeTest.VP_HEIGHT);
	}

	public void init(float x, float y, float width, float height) {
		base = new QuadTree(0, x, y, width, height);
	}

	@Override protected void begin () {
		base.reset();
	}

	@Override protected void process (Entity e) {
		Position position = mPosition.get(e);
		Size size = mSize.get(e);
		base.insert(e.id, position.x, position.y, size.width, size.height);
	}

	public QuadTree getQuadTree () {
		return base;
	}

	/**
	 * Assumes bottom left for x,y
	 */
	public static class QuadTree implements Pool.Poolable {
		public final static int OUTSIDE = -1;
		public final static int SW = 0;
		public final static int SE = 1;
		public final static int NW = 2;
		public final static int NE = 3;
		public static int MAX_IN_BUCKET = 16;
		public static int MAX_DEPTH = 16;
		private int depth;
		private Bag<Container> containers;
		private Bounds bounds;
		private QuadTree[] nodes;
		private boolean touched;

		static Pool<QuadTree> qtPool = Pools.get(QuadTree.class, Integer.MAX_VALUE);
		static Pool<Container> cPool = Pools.get(Container.class, Integer.MAX_VALUE);

		public QuadTree() {
			this.bounds = new Bounds();
			containers = new Bag<>(MAX_IN_BUCKET);
			nodes = new QuadTree[4];
		}

		public QuadTree(int depth, float x, float y, float width, float height) {
			this();
			init(depth, x, y, width, height);
		}

		public QuadTree init (int depth, float x, float y, float width, float height) {
			this.depth = depth;
			bounds.set(x, y, width, height);
			return this;
		}

		private int indexOf(float x, float y, float width, float height) {
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

		public void insert(int eid, float x, float y, float width, float height) {
			insert(cPool.obtain().set(eid, x, y, width, height));
		}

		private void insert(Container c) {
			if (nodes[0] != null) {
				int index = indexOf(c.bounds.x, c.bounds.y, c.bounds.width, c.bounds.height);
				if (index != OUTSIDE) {
					nodes[index].insert(c);
					return;
				}
			}
			containers.add(c);

			if (containers.size() > MAX_IN_BUCKET && depth < MAX_DEPTH) {

				if (nodes[0] == null) {
					float halfWidth = bounds.width / 2;
					float halfHeight = bounds.height / 2;
					nodes[SW] = qtPool.obtain().init(depth + 1, bounds.x, bounds.y, halfWidth, halfHeight);
					nodes[SE] = qtPool.obtain().init(depth + 1, bounds.x + halfWidth, bounds.y, halfWidth, halfHeight);
					nodes[NW] = qtPool.obtain().init(depth + 1, bounds.x, bounds.y + halfHeight, halfWidth, halfHeight);
					nodes[NE] = qtPool.obtain().init(depth + 1, bounds.x + halfWidth, bounds.y + halfHeight, halfWidth, halfHeight);
				}

				Object[] items = containers.getData();
				for (int i = containers.size() - 1; i >= 0; i--) {
					Container next = (Container)items[i];
					int index = indexOf(next.bounds.x, next.bounds.y, next.bounds.width, next.bounds.height);
					if (index != OUTSIDE) {
						nodes[index].insert(next);
						containers.remove(i);
					}
				}
			}
		}

		/**
		 * Returns entity ids of entities that overlap given rectangle
		 */
		public IntBag get(IntBag fill, float x, float y) {
			touched = true;
			int index = indexOf(x, y, 0, 0);
			if (index != OUTSIDE && nodes[0] != null) {
				nodes[index].get(fill, x, y, 0, 0);
			}
			for (int i = 0; i < containers.size(); i++) {
				Container c = containers.get(i);
				if (c.bounds.contains(x, y)) {
					fill.add(c.eid);
				}
			}
			return fill;
		}

		public IntBag get(IntBag fill, float x, float y, float width, float height) {
			if (bounds.overlaps(x, y, width, height)) {
				touched = true;
				if (nodes[0] != null) {
					int index = indexOf(x, y, width, height);
					if (index != OUTSIDE) {
						nodes[index].get(fill, x, y, width, height);
					} else {
						for (int i = 0; i < nodes.length; i++) {
							nodes[i].get(fill, x, y, width, height);
						}
					}
				}
				for (int i = 0; i < containers.size(); i++) {
					Container c = containers.get(i);
					fill.add(c.eid);
				}
			}
			return fill;
		}

		public IntBag getExact(IntBag fill, float x, float y, float width, float height) {
			if (bounds.overlaps(x, y, width, height)) {
				touched = true;
				if (nodes[0] != null) {
					int index = indexOf(x, y, width, height);
					if (index != OUTSIDE) {
						nodes[index].getExact(fill, x, y, width, height);
					} else {
						for (int i = 0; i < nodes.length; i++) {
							nodes[i].getExact(fill, x, y, width, height);
						}
					}
				}
				for (int i = 0; i < containers.size(); i++) {
					Container c = containers.get(i);
					if (c.bounds.overlaps(x, y, width, height)) {
						fill.add(c.eid);
					}
				}
			}
			return fill;
		}

		public static boolean FREE_ON_CLEAR = true;
		@Override
		public void reset() {
			for (int i = containers.size() - 1; i >= 0; i--) {
				cPool.free(containers.remove(i));
			}
			if (FREE_ON_CLEAR) {
				for (int i = 0; i < nodes.length; i++) {
					if (nodes[i] == null) continue;
					qtPool.free(nodes[i]);
					nodes[i] = null;
				}
			} else {
				for (int i = 0; i < nodes.length; i++) {
					if (nodes[i] == null) continue;
					nodes[i].reset();
				}
			}
			touched = false;
		}

		public boolean isTouched () {
			return touched;
		}

		public QuadTree[] getNodes () {
			return nodes;
		}

		public Bounds getBounds () {
			return bounds;
		}
	}

	public static class Container implements Pool.Poolable{
		int eid;
		Bounds bounds = new Bounds();

		public Container() {}

		@Override public void reset () {
			eid = -1;
			bounds.set(0, 0, 0, 0);
		}

		public Container set (int eid, float x, float y, float width, float height) {
			this.eid = eid;
			bounds.set(x, y, width, height);

			return this;
		}
	}

	public static class Bounds {
		float x;
		float y;
		float width;
		float height;

		public Bounds set (float x, float y, float width, float height) {
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
	}
}
