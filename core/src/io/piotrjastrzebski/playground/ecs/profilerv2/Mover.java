package io.piotrjastrzebski.playground.ecs.profilerv2;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by PiotrJ on 14/08/15.
 */
@Wire
public class Mover extends EntityProcessingSystem {
	protected ComponentMapper<ECSProfilerTest.Transform> mTransform;
	protected ComponentMapper<ECSProfilerTest.Velocity> mVelocity;

	public Mover () {
		super(Aspect.all(ECSProfilerTest.Transform.class, ECSProfilerTest.Velocity.class));
	}


	@Override protected void process (Entity e) {
		ECSProfilerTest.Transform transform = mTransform.get(e);
		ECSProfilerTest.Velocity velocity = mVelocity.get(e);
		transform.x += velocity.x * world.delta;
		transform.y += velocity.y * world.delta;
	}
}
