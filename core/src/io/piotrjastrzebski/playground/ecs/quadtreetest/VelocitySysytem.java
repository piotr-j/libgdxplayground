package io.piotrjastrzebski.playground.ecs.quadtreetest;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

/**
 * Created by EvilEntity on 31/07/2015.
 */
@Wire
public class VelocitySysytem extends EntityProcessingSystem {
	private ComponentMapper<Position> mPosition;
	private ComponentMapper<Velocity> mVelocity;

	public VelocitySysytem () {
		super(Aspect.all(Position.class, Velocity.class));
	}

	@Override protected void process (Entity e) {
		Position position = mPosition.get(e);
		Velocity velocity = mVelocity.get(e);
		position.x += velocity.x * world.delta;
		position.y += velocity.y * world.delta;
	}
}
