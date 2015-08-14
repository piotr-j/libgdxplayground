package io.piotrjastrzebski.playground.ecs.profilerv2;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * Created by PiotrJ on 14/08/15.
 */
@Wire
public class Bounder extends EntityProcessingSystem {
	protected ComponentMapper<ECSProfilerTest.Transform> mTransform;
	protected ComponentMapper<ECSProfilerTest.Size> mSize;
	protected ComponentMapper<ECSProfilerTest.Velocity> mVelocity;
	@Wire ExtendViewport viewport;
	public Bounder () {
		super(Aspect.all(ECSProfilerTest.Transform.class, ECSProfilerTest.Size.class, ECSProfilerTest.Velocity.class));
	}

	Rectangle world = new Rectangle();

	@Override protected void initialize () {
		world.set(
			-viewport.getWorldWidth() / 2,
			-viewport.getWorldHeight() / 2,
			viewport.getWorldWidth(),
			viewport.getWorldHeight()
		);
	}

	@Override protected void process (Entity e) {
		ECSProfilerTest.Transform transform = mTransform.get(e);
		ECSProfilerTest.Size size = mSize.get(e);
		ECSProfilerTest.Velocity velocity = mVelocity.get(e);
		if (transform.x < world.x) {
			transform.x = world.x;
			velocity.x = -velocity.x;
		}

		if (transform.x + size.width > world.x + world.width) {
			transform.x = world.x + world.width - size.width;
			velocity.x = -velocity.x;
		}

		if (transform.y < world.y) {
			transform.y = world.y;
			velocity.y = -velocity.y;
		}

		if (transform.y + size.height > world.y + world.height) {
			transform.y = world.y + world.height - size.height;
			velocity.y = -velocity.y;
		}
	}
}
