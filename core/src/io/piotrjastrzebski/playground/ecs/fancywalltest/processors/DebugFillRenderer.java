package io.piotrjastrzebski.playground.ecs.fancywalltest.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.piotrjastrzebski.playground.ecs.ECSTestBase;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Bounds;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Filled;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Tint;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Transform;

/**
 * Created by PiotrJ on 30/09/15.
 */
@Wire
public class DebugFillRenderer extends EntityProcessingSystem {
	protected ComponentMapper<Transform> mTransform;
	protected ComponentMapper<Bounds> mBounds;
	protected ComponentMapper<Tint> mTint;

	@Wire(name = ECSTestBase.WIRE_GAME_CAM)
	OrthographicCamera cam;

	@Wire
	ShapeRenderer renderer;

	public DebugFillRenderer () {
		super(Aspect.all(Transform.class, Bounds.class, Tint.class, Filled.class));
	}

	@Override protected void begin () {
		renderer.setProjectionMatrix(cam.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
	}

	@Override protected void process (Entity e) {
		Transform t = mTransform.get(e);
		Bounds b = mBounds.get(e);
		Tint tint = mTint.get(e);
		renderer.setColor(tint.color);
		renderer.rect(
			t.pos.x, t.pos.y,
			b.rect.width / 2, b.rect.height / 2,
			b.rect.width, b.rect.height,
			1, 1, t.angle
		);
	}

	@Override protected void end () {
		renderer.end();
	}
}
