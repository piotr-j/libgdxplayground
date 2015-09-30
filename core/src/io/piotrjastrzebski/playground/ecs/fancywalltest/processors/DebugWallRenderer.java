package io.piotrjastrzebski.playground.ecs.fancywalltest.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.ecs.ECSTestBase;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Bounds;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Tint;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Transform;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Wall;

/**
 * Created by PiotrJ on 30/09/15.
 */
@Wire
public class DebugWallRenderer extends EntityProcessingSystem {
	protected ComponentMapper<Transform> mTransform;
	protected ComponentMapper<Wall> mWall;
	protected ComponentMapper<Tint> mTint;

	@Wire(name = ECSTestBase.WIRE_GAME_CAM)
	OrthographicCamera cam;

	@Wire
	ShapeRenderer renderer;

	public DebugWallRenderer () {
		super(Aspect.all(Transform.class, Wall.class, Tint.class));
	}

	@Override protected void begin () {
		renderer.setProjectionMatrix(cam.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
	}

	Vector2 tmp = new Vector2();
	@Override protected void process (Entity e) {
		Transform t = mTransform.get(e);
		Wall wall = mWall.get(e);
		Tint tint = mTint.get(e);
		renderer.setColor(tint.color);
		tmp.set(1, 0).setAngle(t.angle).scl(wall.height);
		renderer.line(
			t.pos.x, t.pos.y,
			t.pos.x + tmp.x,
			t.pos.y + tmp.y
		);
	}

	@Override protected void end () {
		renderer.end();
	}
}
