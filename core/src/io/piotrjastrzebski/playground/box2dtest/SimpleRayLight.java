package io.piotrjastrzebski.playground.box2dtest;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;


/**
 * Created by PiotrJ on 18/09/15.
 */
public class SimpleRayLight implements RayCastCallback {
	private Vector2 pos = new Vector2();
	private World world;
	int rays = 360;
	float radius = 1;
	float[] xs = new float[rays];
	float[] ys = new float[rays];
	float[] fs = new float[rays];
	public SimpleRayLight (float x, float y, float radius, World world) {
		pos.set(x, y);
		this.radius = radius;
		this.world = world;

	}

	public SimpleRayLight setRadius (float radius) {
		this.radius = radius;
		return this;
	}

	public SimpleRayLight setPos (float x, float y) {
		this.pos.set(x, y);
		return this;
	}

	Vector2 target = new Vector2();
	int rayId;
	public void fixedUpdate() {
		for (int i = 0; i < rays; i++) {
			rayId = i;
			target.set(radius, 0).setAngle(i);
			target.add(pos);
			xs[rayId] = target.x;
			ys[rayId] = target.y;
			fs[rayId] = 1;
			world.rayCast(this, pos, target);
		}
	}

	@Override public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
		xs[rayId] = point.x;
		ys[rayId] = point.y;
		fs[rayId] = fraction;
		return fraction;
	}

	public void draw(ShapeRenderer renderer) {
		renderer.setColor(Color.RED);
		renderer.circle(pos.x, pos.y, 0.05f, 8);
		renderer.circle(pos.x, pos.y, radius, 32);

		renderer.setColor(Color.CYAN);
		for (int i = 0; i < rays; i++) {
//			renderer.line(pos.x, pos.y, pos.x+ xs[i], pos.y + ys[i]);
			renderer.line(pos.x, pos.y, xs[i], ys[i]);
		}
	}

}
