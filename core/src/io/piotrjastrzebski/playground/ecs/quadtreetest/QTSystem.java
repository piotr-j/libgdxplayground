package io.piotrjastrzebski.playground.ecs.quadtreetest;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

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

	@Override protected void inserted (int e) {
		Position position = mPosition.get(e);
		Size size = mSize.get(e);
		base.insert(e, position.x, position.y, size.width, size.height);
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

	@Override protected void removed (int e) {
		base.remove(e);
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
}
