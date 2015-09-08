package io.piotrjastrzebski.playground.ecs.sat.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.piotrjastrzebski.playground.ecs.sat.SATTest;
import io.piotrjastrzebski.playground.ecs.sat.components.AABB;
import io.piotrjastrzebski.playground.ecs.sat.components.Circle;
import io.piotrjastrzebski.playground.ecs.sat.components.Polygon;
import io.piotrjastrzebski.playground.ecs.sat.components.Transform;

/**
 * Created by PiotrJ on 27/08/15.
 */
@Wire
public class PolygonUpdater extends EntityProcessingSystem {
	protected ComponentMapper<Transform> mTransform;
	protected ComponentMapper<Polygon> mPolygon;
	protected ComponentMapper<AABB> mAABB;

	public PolygonUpdater () {
		super(Aspect.all(Transform.class, Polygon.class, AABB.class));
	}

	@Override protected void process (Entity e) {
		Transform transform = mTransform.get(e);
		Polygon polygon = mPolygon.get(e);
		polygon.polygon.setPosition(transform.pos.x, transform.pos.y);
		polygon.polygon.setRotation(transform.rot);
		polygon.polygon.setScale(transform.scale, transform.scale);
		AABB aabb = mAABB.get(e);
		aabb.rect.set(polygon.polygon.getBoundingRectangle());
	}
}
