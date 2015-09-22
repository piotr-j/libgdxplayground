package io.piotrjastrzebski.playground.box2dtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

/**
 * Created by PiotrJ on 18/09/15.
 */
public class PolyRayLight implements RayCastCallback, QueryCallback {
	private Vector2 pos = new Vector2();
	private World world;
	int rayNum = 128;
	float radius = 1;
	Array<Ray> rays = new Array<>();
	Array<Ray> sorted = new Array<>();
	boolean dirty;

	PolygonRegion poly;
	TextureRegion region;

	public PolyRayLight (float x, float y, float radius, World world, TextureRegion region) {
		pos.set(x, y);
		this.radius = radius;
		this.world = world;
		this.region = region;
		dirty = true;
		setEndPoints();
	}

	float[] vertices = new float[rayNum * 2 + 2];
	short[] triangles = new short[rayNum * 3];
	void setEndPoints() {
		// center in pixel size
		int w = region.getRegionWidth();
		int h = region.getRegionHeight();
		vertices[0] = w/2;
		vertices[1] = h/2;
		float angleNum = 360f / (rayNum);
		for (int i = 0; i < rayNum; i++) {
			final float angle = angleNum * i;
			rays.add(new Ray(
				radius * MathUtils.sinDeg(angle),
				radius * MathUtils.cosDeg(angle), angle));
			// verts must be in pixel space, normalize sin/cos and scale
			vertices[(i + 1) * 2] = w * (MathUtils.sinDeg(angle) + 1) / 2f;
			vertices[(i + 1) * 2 + 1] = h * (MathUtils.cosDeg(angle) + 1) / 2f;

		}
		sorted.addAll(rays);

		short vert = 1;
		for (int i = 0; i < triangles.length; i+=3) {
			// all start at 0
			triangles[i] = 0;
			triangles[i + 1] = vert;
			triangles[i + 2] = ++vert;
			// we want last one to point to 2nd vert to form complete fan
			if (i + 2 == triangles.length -1) {
				triangles[i + 2] = 1;
			}
		}

		poly = new PolygonRegion(region, vertices, triangles);
	}

	public PolyRayLight setRadius (float radius) {
		this.radius = radius;
		return this;
	}

	public PolyRayLight setPos (float x, float y) {
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
//		rayTmpOff = 0;
		// first find all fixtures that are withing our bounding box
//		world.QueryAABB(this, pos.x - radius, pos.y - radius, pos.x + radius, pos.y + radius);
//
//		for (int i = 0; i < rayTmpOff; i++) {
//			rayId = rayNum + i;
//			Ray ray = rays.get(rayId);
//			target.x = ray.ex;
//			target.y = ray.ey;
//			world.rayCast(this, pos, target);
//		}
	}

	@Override public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
		Ray ray = rays.get(rayId);
		ray.set(point);
		ray.f = fraction;
		if (ray.f < 1) dirty = true;
		return fraction;
	}

	float extra = 0.5f;
	float cExtra = 1.5f;
	boolean ignoreRadius = false;
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
					getTmpRay().set(tmp);
					getTmpRay().set(tmp2.set(tmp).sub(pos).rotate(extra).setLength(radius).add(pos));
					getTmpRay().set(tmp2.set(tmp).sub(pos).rotate(-extra).setLength(radius).add(pos));
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
					getTmpRay().set(tmp.set(tanA).sub(pos).limit2(r2).add(pos));
					getTmpRay().set(tmp.set(tanA).sub(pos).rotate(cExtra).setLength2(r2).add(pos));
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
		Gdx.app.log("", "rebuild!");

	}


	public void draw(ShapeRenderer renderer) {
		if (dirty) {
			dirty = false;
			rebuildMesh();
		}
		renderer.setColor(Color.RED);
		renderer.circle(pos.x, pos.y, 0.05f, 8);
		renderer.circle(pos.x, pos.y, radius, 32);

//		sorted.sort();
		for (int i = 0; i < rayNum + rayTmpOff; i++) {
			Ray ray = sorted.get(i);

			float v = i/(float)(rayNum + rayTmpOff);
			renderer.setColor(v, 0, 1-v, 1);
			renderer.line(pos.x, pos.y, ray.ex, ray.ey);
		}

		renderer.setColor(Color.GREEN);
		drawPoly(renderer);
	}

	private void drawPoly (ShapeRenderer renderer) {
		float scale = 1/128f * radius;
		for (int i = 0, n = triangles.length; i < n; i += 3) {
			short v1 = triangles[i];
			short v2 = triangles[i + 1];
			short v3 = triangles[i + 2];

			float x1 = pos.x + vertices[v1 * 2] * scale;
			float y1 = pos.y + vertices[v1 * 2 + 1] * scale;
			float x2 = pos.x + vertices[v2 * 2] * scale;
			float y2 = pos.y + vertices[v2 * 2 + 1] * scale;
			float x3 = pos.x + vertices[v3 * 2] * scale;
			float y3 = pos.y + vertices[v3 * 2 + 1] * scale;

			renderer.line(x1, y1, x2, y2);
			renderer.line(x2, y2, x3, y3);
			renderer.line(x3, y3, x1, y1);
		}
	}

	private Vector2 aTmp = new Vector2();

	public void draw (PolygonSpriteBatch batch) {
		batch.setColor(Color.RED);
		batch.draw(poly, pos.x - radius, pos.y - radius, radius * 2, radius * 2);
	}

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

		public void reset (Vector2 pos) {
			ex = pos.x + x;
			ey = pos.y + y;
			f = 1;
		}

		public void set (Vector2 e) {
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
