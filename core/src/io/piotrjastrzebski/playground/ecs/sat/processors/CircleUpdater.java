package io.piotrjastrzebski.playground.ecs.sat.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import io.piotrjastrzebski.playground.ecs.sat.components.AABB;
import io.piotrjastrzebski.playground.ecs.sat.components.Circle;
import io.piotrjastrzebski.playground.ecs.sat.components.Polygon;
import io.piotrjastrzebski.playground.ecs.sat.components.Transform;

/**
 * Created by PiotrJ on 27/08/15.
 */
@Wire
public class CircleUpdater extends EntityProcessingSystem {
	protected ComponentMapper<Transform> mTransform;
	protected ComponentMapper<Circle> mCircle;
	protected ComponentMapper<AABB> mAABB;


	public CircleUpdater () {
		super(Aspect.all(Transform.class, Circle.class, AABB.class));
	}

	@Override protected void process (Entity e) {
		Transform transform = mTransform.get(e);
		Circle circle = mCircle.get(e);
		circle.circle.setPosition(transform.pos.x, transform.pos.y);
		float size = circle.radius * transform.scale;
		circle.circle.setRadius(size);
		AABB aabb = mAABB.get(e);
		aabb.rect.set(transform.pos.x - size, transform.pos.y - size, size * 2, size * 2);
	}
}
