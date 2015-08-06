package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Position;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Size;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Velocity;

@Wire
@com.artemis.annotations.Profile(using = SystemProfiler.class, enabled = SystemProfiler.ENABLED)
public class BoundsSystem extends EntityProcessingSystem {
	private ComponentMapper<Position> mPosition;
	private ComponentMapper<Size> mSize;
	private ComponentMapper<Velocity> mVelocity;

	@Wire ExtendViewport viewport;

	public BoundsSystem () {
		super(Aspect.all(Position.class, Size.class, Velocity.class));
	}

	Rectangle bounds = new Rectangle();
	@Override protected void begin () {
		bounds.set(-8, -8, 16, 16);
	}

	@Override protected void process (Entity e) {
		Position position = mPosition.get(e);
		Size size = mSize.get(e);
		Velocity velocity = mVelocity.get(e);
		if (position.x < bounds.x) {
			position.x = bounds.x;
			velocity.x *= -1;
		}

		if (position.y < bounds.y) {
			position.y = bounds.y;
			velocity.y *= -1;
		}

		if (position.x + size.width > bounds.x + bounds.width) {
			position.x = bounds.x + bounds.width - size.width;
			velocity.x *= -1;
		}

		if (position.y + size.height > bounds.y + bounds.height) {
			position.y = bounds.y + bounds.height - size.height;
			velocity.y *= -1;
		}
	}
}
