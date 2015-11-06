package io.piotrjastrzebski.playground.box2dtest.lights;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

/**
 * Created by PiotrJ on 18/09/15.
 */
public class FancierRayLight implements RayCastCallback, QueryCallback, Light {
	private Vector2 pos = new Vector2();
	private World world;
	int rayNum = 37;
	float radius = 1;
//	float[] txs = new float[rayNum * 9];
//	float[] tys = new float[rayNum * 9];
//	float[] xs = new float[rayNum * 10];
//	float[] ys = new float[rayNum * 10];
//	float[] exs = new float[rayNum];
//	float[] eys = new float[rayNum];
//	float[] fs = new float[rayNum * 9];
	Array<Ray> rays = new Array<>();
	Array<Ray> sorted = new Array<>();
	boolean dirty;

	public FancierRayLight (float x, float y, float radius, World world) {
		pos.set(x, y);
		this.radius = radius;
		this.world = world;
		setEndPoints();
		dirty = true;
//		for (int i = 0; i < 100; i++) {
//			tmps.add(new Vector2());
//		}
	}

	void setEndPoints() {
		float angleNum = 360f / (rayNum - 1);
		for (int i = 0; i < rayNum; i++) {
			final float angle = angleNum * i;
//			exs[i] = radius * MathUtils.sinDeg(angle);
//			eys[i] = radius * MathUtils.cosDeg(angle);
			rays.add(new Ray(
				radius * MathUtils.sinDeg(angle),
				radius * MathUtils.cosDeg(angle), angle));
		}
		sorted.addAll(rays);
	}

	public FancierRayLight setRadius (float radius) {
		this.radius = radius;
		return this;
	}

	public FancierRayLight setPos (float x, float y) {
		this.pos.set(x, y);
		return this;
	}

	Vector2 target = new Vector2();
	int rayId;
	public void fixedUpdate() {
		for (int i = 0; i < rayNum; i++) {
			Ray ray = rays.get(i);
			ray.reset(pos);
			rayId = i;
			target.x = pos.x + ray.x;
			target.y = pos.y + ray.y;
			world.rayCast(this, pos, target);
		}
//		for (int i = 0; i < rayNum; i++) {
//			rayId = i;
//			target.x = exs[i] + pos.x;
//			xs[i] = target.x;
//			target.y = eys[i] + pos.y;
//			ys[i] = target.y;
//			fs[i] = 1;
//			world.rayCast(this, pos, target);
//		}
//		for (int i = 0; i < rayTmpOff; i++) {
//			rays.get(i).a = -1;
//		}
		rayTmpOff = 0;
		// first find all fixtures that are withing our bounding box
		world.QueryAABB(this, pos.x - radius, pos.y - radius, pos.x + radius, pos.y + radius);

		for (int i = 0; i < rayTmpOff; i++) {
			rayId = rayNum + i;
			Ray ray = rays.get(rayId);
//			target.x = pos.x + ray.ex;
			target.x = ray.ex;
//			target.y = pos.y + ray.ey;
			target.y = ray.ey;
			world.rayCast(this, pos, target);
		}

//		for (int i = 0; i < rayTmpOff; i++) {
//			Vector2 e = tmps.get(i);
//			txs[i] = e.x;
//			tys[i] = e.y;
//			e.sub(pos);
//			e.limit(radius);
//			rayId = rayNum + i;
//			target.x = e.x + pos.x;
//			xs[rayId] = target.x;
//			target.y = e.y + pos.y;
//			ys[rayId] = target.y;
//			fs[rayId] = 1;
//			world.rayCast(this, pos, target);
//		}
	}

	@Override public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
		Ray ray = rays.get(rayId);
//		ray.set(0, 0, point.x, point.y);
		ray.set(point);
		ray.f = fraction;
//		xs[rayId] = point.x;
//		ys[rayId] = point.y;
//		fs[rayId] = fraction;
		return fraction;
	}

	float extra = 0.5f;
	float cExtra = 1.5f;
	boolean ignoreRadius = false;
//	Array<Vector2> tmps = new Array<>();
	int rayTmpOff;
	Vector2 tmp = new Vector2();
	Vector2 tmp2 = new Vector2();
	Vector2 tanA = new Vector2();
	Vector2 tanB = new Vector2();
	@Override public boolean reportFixture (Fixture fixture) {
		Shape shape = fixture.getShape();
		Body body = fixture.getBody();
		Vector2 p = body.getPosition();
		// TODO add rays to the side of the found positions
		switch (shape.getType()) {
		case Polygon:
			Gdx.app.log("Fixture", "Polygon");
			PolygonShape polygon = (PolygonShape)shape;
			int vertexCount = polygon.getVertexCount();
			for (int i = 0; i < vertexCount; i++) {
				polygon.getVertex(i, tmp);
				tmp.set(body.getWorldPoint(tmp));
				if (ignoreRadius || tmp.dst2(pos) <= radius * radius) {
//					getTmpRay().set(tmp);
					getTmpRay().set(tmp);
					getTmpRay().set(tmp2.set(tmp).sub(pos).rotate(extra).setLength(radius).add(pos));
					getTmpRay().set(tmp2.set(tmp).sub(pos).rotate(-extra).setLength(radius).add(pos));
//					tmps.get(rayTmpOff).set(tmp);
//					rayTmpOff++;
//					tmps.get(rayTmpOff).set(tmp2.set(tmp).rotate(extra).setLength(radius));
//					rayTmpOff++;
//					tmps.get(rayTmpOff).set(tmp2.set(tmp).rotate(-extra).setLength(radius));
//					rayTmpOff++;

				}
			}
			break;
		case Circle:
			Gdx.app.log("Fixture", "Circle ");

			CircleShape circle = (CircleShape)shape;
			Vector2 cp = body.getWorldPoint(circle.getPosition());
			float r = circle.getRadius();
			float dst2 = cp.dst2(pos);
			float r2 = radius * radius;
			if (ignoreRadius || dst2 <= (radius + r) * (radius + r)) {
				getTmpRay().set(cp);
				if (findTangents(cp, r, pos, tanA, tanB)) {
//					getTmpRay().set(tanA);
					getTmpRay().set(tmp.set(tanA).sub(pos).limit2(r2).add(pos));
					getTmpRay().set(tmp.set(tanA).sub(pos).rotate(cExtra).setLength2(r2).add(pos));
//					getTmpRay().set(tanB);
					getTmpRay().set(tmp.set(tanB).sub(pos).limit2(r2).add(pos));
					getTmpRay().set(tmp.set(tanB).sub(pos).rotate(-cExtra).setLength2(r2).add(pos));
				}
			}
			break;
		case Chain:
			Gdx.app.log("Fixture", "Chain");
			ChainShape chain = (ChainShape)shape;
			int vc = chain.getVertexCount();
			for (int i = 0; i < vc; i++) {
				chain.getVertex(i, tmp);
				tmp.set(body.getWorldPoint(tmp));
				if (ignoreRadius || tmp.dst2(pos) <= radius * radius) {
					getTmpRay().set(tmp);
				}
			}
		default:
			Gdx.app.log("Fixture", shape.getType().name());
		}
		// we want all
		return true;
	}

	private Ray getTmpRay () {
		rayTmpOff++;
		dirty = true;
		if (rays.size < rayNum + rayTmpOff) {
			Ray ray = new Ray();
			rays.add(ray);
			sorted.add(ray);
		}
		return rays.get(rayNum + rayTmpOff - 1);
	}

	private boolean findTangents (Vector2 center, float radius,
		Vector2 external,
		Vector2 oTanA, Vector2 oTanB) {

		float dx = center.x - external.x;
		float dy = center.y - external.y;
		float d2 = dx * dx + dy * dy;
		if (d2 < radius * radius) {
			oTanA.setZero();
			oTanB.setZero();
			return false;
		}
		float len = (float)Math.sqrt(d2 - radius * radius);

		int inters = findCircleCircleIntersection(
			center.x, center.y, radius,
			external.x, external.y, len,
			oTanA, oTanB
		);
		return inters > 1;
	}

	private int findCircleCircleIntersection (
		float x1, float y1, float r1,
		float x2, float y2, float r2,
		Vector2 outA, Vector2 outB
	) {
		float dx = x1 - x2;
		float dy = y1 - y2;
		float dist = (float)Math.sqrt(dx * dx + dy * dy);
		if (dist > r1 + r2) {
			// No solutions, the circles are too far apart.
			outA.setZero();
			outB.setZero();
			return 0;
		} else if (dist < Math.abs(r1 - r2)) {
			// No solutions, one circle contains the other.
			outA.setZero();
			outB.setZero();
			return 0;
		} else if ((dist == 0) && (r1 == r2)) {
			// No solutions, the circles coincide.
			outA.setZero();
			outB.setZero();
			return 0;
		} else {
			// Find a and h.
			float a = (r1 * r1 -
				r2 * r2 + dist * dist) / (2 * dist);
			float h = (float)Math.sqrt(r1 * r1 - a * a);

			// Find P2.
			float x3 = x1 + a * (x2 - x1) / dist;
			float cy2 = y1 + a * (y2 - y1) / dist;

			// Get the points P3.
			outA.set(
				x3 + h * (y2 - y1) / dist,
				cy2 - h * (x2 - x1) / dist);
			outB.set(
				x3 - h * (y2 - y1) / dist,
				cy2 + h * (x2 - x1) / dist);

			// See if we have 1 or 2 solutions.
			if (dist == r1 + r2) return 1;
			return 2;
		}
	}

	int lastVers;
	private void rebuildMesh () {
		// need to resize mesh if needed
	}


	public void draw(ShapeRenderer renderer) {
		if (dirty) {
			dirty = false;
			rebuildMesh();
		}
		renderer.setColor(Color.RED);
		renderer.circle(pos.x, pos.y, 0.05f, 8);
		renderer.circle(pos.x, pos.y, radius, 32);

//		renderer.setColor(Color.CYAN);
//		for (int i = 0; i < rayNum; i++) {
//			renderer.line(pos.x, pos.y, xs[i], ys[i]);
//		}
//
//		renderer.setColor(Color.GREEN);
//		for (int i = 0; i < rayTmpOff; i++) {
//			renderer.line(pos.x, pos.y, txs[i], tys[i]);
//		}

//		renderer.setColor(Color.RED);
//		for (int i = rayNum; i < rayNum + rayTmpOff; i++) {
//			renderer.line(pos.x, pos.y, xs[i], ys[i]);
//		}

		renderer.setColor(Color.GREEN);
//		for (int i = 0; i < rayNum + rayTmpOff; i++) {
//			Ray ray = rays.get(i);
//			if (ray.main) {
//				renderer.setColor(Color.GREEN);
//			} else {
//				renderer.setColor(Color.RED);
//				renderer.line(pos.x, pos.y, ray.ex, ray.ey);
//			}

//			float v = i/(float)(rayNum + rayTmpOff);
//			renderer.setColor(v, 0, 1-v, 1);
//			renderer.line(pos.x, pos.y, pos.x + ray.ex, pos.y + ray.ey);
//			renderer.line(pos.x, pos.y, ray.ex, ray.ey);
//		}

//		sorted.sort();
		for (int i = 0; i < rayNum + rayTmpOff; i++) {
			Ray ray = sorted.get(i);
//			if (ray.main) {
//				renderer.setColor(Color.GREEN);
//			} else {
//				renderer.setColor(Color.RED);
//				renderer.line(pos.x, pos.y, ray.ex, ray.ey);
//			}

			float v = i/(float)(rayNum + rayTmpOff);
			renderer.setColor(v, 0, 1-v, 1);
//			renderer.line(pos.x, pos.y, pos.x + ray.ex, pos.y + ray.ey);
			renderer.line(pos.x, pos.y, ray.ex, ray.ey);
		}
//		for (Ray ray : rays) {
//			if (ray.main) {
//				renderer.setColor(Color.GREEN);
//			} else {
//				renderer.setColor(Color.RED);
//			}
//			renderer.line(pos.x, pos.y, pos.x + ray.ex, pos.y + ray.ey);
//			renderer.line(pos.x, pos.y, ray.ex, ray.ey);
//		}
	}

	@Override public void draw (PolygonSpriteBatch batch) {

	}

	private Vector2 aTmp = new Vector2();
	private class Ray implements Comparable<Ray> {
		float x, y, a, f;
		float ex, ey;
		boolean main;
		public Ray (float x, float y, float a) {
			this.x = ex = x;
			this.y = ey = y;
			this.a = a;
			f = 1;
			main = true;
		}

		public Ray () {
			a = -1;
			f = 1;
		}

//		public void reset () {
//			f = 1;
//			a = -1;
//		}

		public void reset (Vector2 pos) {
			ex = pos.x + x;
			ey = pos.y + y;
			f = 1;
//			a = -1;
		}

		public void set (Vector2 e) {
//			x = e.x - pos.x;
//			y = e.y - pos.y;
			set(pos.x, pos.y, e.x, e.y);
		}

		public void set(float cx, float cy, float ex, float ey) {
			this.ex = ex;
			this.ey = ey;
			a = aTmp.set(cx, cy).sub(ex, ey).angle();
		}

		@Override public int compareTo (Ray o) {
			return Float.compare(a, o.a);
		}
	}
}
