package io.piotrjastrzebski.playground.ecs.quadtreetest;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import net.mostlyoriginal.api.utils.pooling.ObjectPool;
import net.mostlyoriginal.api.utils.pooling.Poolable;
import net.mostlyoriginal.api.utils.pooling.Pools;

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
		base = new QuadTree(x, y, width, height);
	}

	public boolean rebuild = false;
	public long diff;
	private long start;
	@Override protected void begin () {
		start = System.nanoTime();
		if (rebuild) {
			base.reset();
		}
	}

	@Override protected void inserted (Entity e) {
		Position position = mPosition.get(e);
		Size size = mSize.get(e);
		base.insert(e.id, position.x, position.y, size.width, size.height);
	}

	@Override protected void process (Entity e) {
		Position position = mPosition.get(e);
		Size size = mSize.get(e);
		if (rebuild) {
			base.insert(e.id, position.x, position.y, size.width, size.height);
		} else if (position.dirty) {
			base.update(e.id, position.x, position.y, size.width, size.height);
		}
	}

	@Override protected void removed (Entity e) {
		base.remove(e.id);
	}

	@Override protected void end () {
		diff = System.nanoTime() - start;
	}

	public QuadTree getQuadTree () {
		return base;
	}

	@Override protected void dispose () {
		base.dispose();
	}

	/**
	 * Assumes bottom left for x,y
	 */
	public static class QuadTree implements Poolable {
		public final static int OUTSIDE = -1;
		public final static int SW = 0;
		public final static int SE = 1;
		public final static int NW = 2;
		public final static int NE = 3;
		public static int MAX_IN_BUCKET = 16;
		public static int MAX_DEPTH = 16;
		private int depth;
		private Bag<Container> containers;
		private Container bounds;
		private QuadTree[] nodes;
		private QuadTree parent;
		private boolean touched;

		static ObjectPool<QuadTree> qtPool = Pools.getPool(QuadTree.class);
		static ObjectPool<Container> cPool = Pools.getPool(Container.class);
		static Bag<Container> idToContainer = new Bag<>();

		public QuadTree() {
			this(0, 0, 0, 0);
		}

		public QuadTree(float x, float y, float width, float height) {
			bounds = new Container();
			containers = new Bag<>(MAX_IN_BUCKET);
			nodes = new QuadTree[4];
			init(0, x, y, width, height, null);
		}

		public QuadTree init (int depth, float x, float y, float width, float height, QuadTree parent) {
			this.depth = depth;
			bounds.set(x, y, width, height);
			this.parent = parent;
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
				int index = indexOf(c.x, c.y, c.width, c.height);
				if (index != OUTSIDE) {
					nodes[index].insert(c);
					return;
				}
			}
			c.tree = this;
			idToContainer.set(c.eid, c);
			containers.add(c);

			if (containers.size() > MAX_IN_BUCKET && depth < MAX_DEPTH) {
				if (nodes[0] == null) {
					float halfWidth = bounds.width / 2;
					float halfHeight = bounds.height / 2;
					nodes[SW] = qtPool.obtain().init(depth + 1, bounds.x, bounds.y, halfWidth, halfHeight, this);
					nodes[SE] = qtPool.obtain().init(depth + 1, bounds.x + halfWidth, bounds.y, halfWidth, halfHeight, this);
					nodes[NW] = qtPool.obtain().init(depth + 1, bounds.x, bounds.y + halfHeight, halfWidth, halfHeight, this);
					nodes[NE] = qtPool.obtain().init(depth + 1, bounds.x + halfWidth, bounds.y + halfHeight, halfWidth, halfHeight, this);
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
		 * Returns entity ids of entities that bounds contain given point
		 */
		public IntBag get(IntBag fill, float x, float y) {
			touched = true;
			int index = indexOf(x, y, 0, 0);
			if (index != OUTSIDE && nodes[0] != null) {
				nodes[index].get(fill, x, y, 0, 0);
			}
			for (int i = 0; i < containers.size(); i++) {
				Container c = containers.get(i);
				if (c.contains(x, y)) {
					fill.add(c.eid);
				}
			}
			return fill;
		}

		/**
		 *  Returns entity ids of entities that are inside quads that overlap given bounds
		 */
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

		/**
		 *  Returns entity ids of entities that overlap given bounds
		 */
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
					if (c.overlaps(x, y, width, height)) {
						fill.add(c.eid);
					}
				}
			}
			return fill;
		}

		public void update (int id, float x, float y, float width, float height) {
			Container c = idToContainer.get(id);
			c.set(id, x, y, width, height);

			QuadTree qTree = c.tree;
			qTree.remove(c);
			while (qTree.parent != null && !qTree.bounds.contains(c)){
				qTree = qTree.parent;
			}
			qTree.insert(c);
		}

		public static boolean FREE_ON_CLEAR = false;
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
				parent = null;
			} else {
				for (int i = 0; i < nodes.length; i++) {
					if (nodes[i] == null) continue;
					nodes[i].reset();
				}
			}
			touched = false;
		}

		public void remove (int id) {
			Container c = idToContainer.get(id);
			if (c == null) return;
			if (c.tree != null) c.tree.remove(c);
			cPool.free(c);
		}

		private void remove(Container c) {
			containers.remove(c);
		}

		public boolean isTouched () {
			return touched;
		}

		public QuadTree[] getNodes () {
			return nodes;
		}

		public Container getBounds () {
			return bounds;
		}

		@Override public String toString () {
			return "QuadTree{"+
				"depth="+depth
				+"}";
		}

		public void dispose () {
			for (int i = containers.size() - 1; i >= 0; i--) {
				cPool.free(containers.remove(i));
			}
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i] == null) continue;
				qtPool.free(nodes[i]);
				nodes[i] = null;
			}
		}
	}

	public static class Container implements Poolable {
		int eid;
		QuadTree tree;

		public Container() {}

		@Override public void reset () {
			eid = -1;
			x = 0;
			y = 0;
			width = 0;
			height = 0;
			tree = null;
		}

		public Container set (int eid, float x, float y, float width, float height) {
			this.eid = eid;
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

		float x;
		float y;
		float width;
		float height;

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

			return ((xmin > x && xmin < x + width) && (xmax > x && xmax < x + width))
				&& ((ymin > y && ymin < y + height) && (ymax > y && ymax < y + height));
		}

		public boolean contains (Container c) {
			return contains(c.x, c.y, c.width, c.height);
		}
	}
}
