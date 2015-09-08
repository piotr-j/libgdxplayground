package io.piotrjastrzebski.playground.ecs.sat.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import io.piotrjastrzebski.playground.ecs.sat.SATTest;
import io.piotrjastrzebski.playground.ecs.sat.components.*;

/**
 * Created by PiotrJ on 27/08/15.
 */
@Wire
public class Renderer extends EntityProcessingSystem {
	protected ComponentMapper<Tint> mTint;
	protected ComponentMapper<Polygon> mPolygon;
	protected ComponentMapper<Circle> mCircle;
	protected ComponentMapper<AABB> mAABB;

	@Wire(name = SATTest.WIRE_GAME_CAM) OrthographicCamera camera;
	@Wire ShapeRenderer renderer;

	public Renderer () {
		super(Aspect.all(Transform.class, Tint.class, AABB.class).one(Polygon.class, Circle.class));
	}

	@Override protected void begin () {
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
	}

	@Override protected void process (Entity e) {
		AABB aabb = mAABB.get(e);
		renderer.setColor(aabb.color);
		Rectangle b = aabb.rect;
		renderer.rect(b.x, b.y, b.width, b.height);

		renderer.setColor(mTint.get(e).color);
		Polygon polygon = mPolygon.getSafe(e);
		if (polygon != null) {
			renderer.polygon(polygon.polygon.getTransformedVertices());
		}

		Circle circle = mCircle.getSafe(e);
		if (circle != null) {
			com.badlogic.gdx.math.Circle c = circle.circle;
			renderer.circle(c.x, c.y, c.radius, 32);
		}

	}

	@Override protected void end () {
		renderer.end();
	}
}
