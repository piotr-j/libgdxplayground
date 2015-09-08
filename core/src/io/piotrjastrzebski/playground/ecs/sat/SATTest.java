package io.piotrjastrzebski.playground.ecs.sat;

import com.artemis.*;
import com.badlogic.gdx.graphics.Color;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.ecs.ECSTestBase;
import io.piotrjastrzebski.playground.ecs.profiler.GUISystem;
import io.piotrjastrzebski.playground.ecs.sat.components.*;
import io.piotrjastrzebski.playground.ecs.sat.processors.*;

/**
 * Separating Axis Theorem Test
 * Created by EvilEntity on 28/07/2015.
 */
public class SATTest extends ECSTestBase {
	public SATTest (GameReset game) {
		super(game);
	}

	@Override protected void preInit (WorldConfiguration config) {
		// add systems/managers here
		config.setSystem(new Controller());
		config.setSystem(new PolygonUpdater());
		config.setSystem(new CircleUpdater());
		config.setSystem(new Renderer());
		config.setSystem(new Collision());
		config.setSystem(new GUISystem());
	}

	@Override protected void postInit () {
		// add entities here
		createCircle(-5, 0, 1.5f);
		createCircle(5, 0, 1.5f);
//		createRectPoly(-5, 0, 5f, 2.5f);
//		createRectPoly(5, 0, 2.5f, 5f);
	}

	private void createCircle(float x, float y, float radius) {
		EntityEdit ee = world.createEntity().edit();
		ee.create(Tint.class).color.set(Color.CYAN);
		Circle circle = ee.create(Circle.class);
		circle.radius = radius;
		circle.circle.set(0, 0, circle.radius);
		ee.create(Transform.class).pos.set(x, y);
		ee.create(Controllable.class);
		ee.create(Collider.class);
		ee.create(AABB.class).color.set(Color.GREEN);

	}

	private void createRectPoly(float x, float y, float width, float height) {
		float w = width/2;
		float h = height/2;
		createPoly(x, y, new float[] {-w, -h, w, -h, w, h, -w, h});
	}

	private void createPoly(float x, float y, float[] verts) {
		EntityEdit ee = world.createEntity().edit();
		ee.create(Tint.class).color.set(Color.CYAN);
		ee.create(Polygon.class).polygon.setVertices(verts);
		ee.create(Transform.class).pos.set(x, y);
		ee.create(Controllable.class);
		ee.create(Collider.class);
		ee.create(AABB.class).color.set(Color.GREEN);;
	}
}
