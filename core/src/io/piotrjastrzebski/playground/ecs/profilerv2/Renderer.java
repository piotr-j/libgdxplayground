package io.piotrjastrzebski.playground.ecs.profilerv2;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by PiotrJ on 14/08/15.
 */
@Wire
public class Renderer extends EntityProcessingSystem {
	protected ComponentMapper<ECSProfilerTest.Transform> mTransform;
	protected ComponentMapper<ECSProfilerTest.Size> mSize;
	protected ComponentMapper<ECSProfilerTest.Velocity> mVelocity;
	@Wire(name = "game") OrthographicCamera camera;
	@Wire ShapeRenderer renderer;

	public Renderer () {
		super(Aspect.all(ECSProfilerTest.Transform.class, ECSProfilerTest.Size.class, ECSProfilerTest.DebugRenderable.class));
	}

	@Override protected void begin () {
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
	}

	@Override protected void process (Entity e) {
		ECSProfilerTest.Transform transform = mTransform.get(e);
		ECSProfilerTest.Size size = mSize.get(e);
		if (mVelocity.has(e)) {
			renderer.setColor(Color.RED);
		} else {
			renderer.setColor(Color.MAROON);
		}
		renderer.rect(transform.x, transform.y, size.width, size.height);
	}

	@Override protected void end () {
		renderer.end();
	}
}
