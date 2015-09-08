package io.piotrjastrzebski.playground.ecs.sat.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import io.piotrjastrzebski.playground.ecs.sat.SATTest;
import io.piotrjastrzebski.playground.ecs.sat.components.*;
import io.piotrjastrzebski.playground.ecs.sat.components.Circle;
import io.piotrjastrzebski.playground.ecs.sat.components.Polygon;

/**
 * Created by PiotrJ on 27/08/15.
 */
@Wire
public class Collision extends EntitySystem {
	protected ComponentMapper<Polygon> mPolygon;
	protected ComponentMapper<Circle> mCircle;
	protected ComponentMapper<AABB> mAABB;
	protected ComponentMapper<Tint> mTint;

	@Wire(name = SATTest.WIRE_GAME_CAM) OrthographicCamera camera;
	@Wire ShapeRenderer renderer;

	public Collision () {
		super(Aspect.all(Collider.class, AABB.class).one(Polygon.class, Circle.class));
	}

	@Override protected void begin () {
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
	}

	@Override protected void end () {
		renderer.end();
	}

	@Override protected void processSystem () {
		IntBag entities = getSubscription().getEntities();
		for (int e1 = 0; e1 < entities.size(); e1++) {
			for (int e2 = 0; e2 < entities.size(); e2++) {
				int eid1 = entities.get(e1);
				int eid2 = entities.get(e2);
				if (eid1 == eid2) continue;
				test(eid1, eid2);
			}
		}
	}

	private void test (int eid1, int eid2) {
		if (mCircle.has(eid1) && mCircle.has(eid2)) {
			testCircles(eid1, eid2);
		} else if (mPolygon.has(eid1) && mPolygon.has(eid2)) {
			testPolygons(eid1, eid2, false);
//			testPolygons(eid2, eid1, true);
		} else {
			if (mCircle.has(eid1)) {
				testCirclePoly(eid1, eid2);
			} else if (mCircle.has(eid2)) {
				testCirclePoly(eid2, eid1);
			}
		}
	}

	CollisionResult result = new CollisionResult();
	Vector2 tmp1 = new Vector2();
	private CollisionResult testCircles(int cidA, int cidB) {
		result.reset();
		// if AABBs are not overlapping we cant be touching
		AABB aabbA = mAABB.get(cidA);
		AABB aabbB = mAABB.get(cidB);
		if (aabbA.rect.overlaps(aabbB.rect)) {
			aabbA.color.set(Color.RED);
			aabbB.color.set(Color.RED);
		} else {
			aabbA.color.set(Color.GREEN);
			aabbB.color.set(Color.GREEN);
			// can bail early as optimization
			return null;
		}

		Circle circleA = mCircle.get(cidA);
		com.badlogic.gdx.math.Circle cA = circleA.circle;
		Circle circleB = mCircle.get(cidB);
		com.badlogic.gdx.math.Circle cB = circleB.circle;

		renderer.setColor(Color.BROWN);
		renderer.line(cA.x, cA.y, cB.x, cB.y);

		float radTotal = cA.radius + cB.radius;
		float dst2 = (cB.x - cA.x) * (cB.x - cA.x) + (cB.y - cA.y) * (cB.y - cA.y);
		Tint tintA = mTint.get(cidA);
		Tint tintB = mTint.get(cidB);
		if (dst2 > radTotal * radTotal) {
			tintA.color.set(Color.CYAN);
			tintB.color.set(Color.CYAN);
			return result;
		} else {
			tintA.color.set(Color.BLUE);
			tintB.color.set(Color.BLUE);
		}

		float dst = (float)Math.sqrt(dst2);
		float diff = radTotal - dst;
		result.collided = true;
		result.entityA = cidA;
		result.entityB = cidA;
		result.vector.set(cB.x - cA.x, cB.y - cA.y).nor();
		result.dst = dst;
		result.separation.set(result.vector.x * diff, result.vector.y * diff);

		renderer.setColor(Color.GREEN);
		renderer.circle(cA.x - result.separation.x/2, cA.y - result.separation.y/2, cA.radius, 32);
		renderer.circle(cB.x + result.separation.x / 2, cB.y + result.separation.y / 2, cB.radius, 32);

		result.entityAContained = (cA.radius<= cB.radius && dst <= cB.radius - cA.radius);
		if (result.entityAContained) {
			tintA.color.set(Color.YELLOW);

		}
		result.entityBContained = (cB.radius<= cA.radius && dst <= cA.radius - cB.radius);
		if (result.entityBContained) {
			tintB.color.set(Color.YELLOW);
		}

		return result;
	}

	Vector2 vAxis = new Vector2();
	Vector2 tmp3 = new Vector2();
	Vector2 vOffset = new Vector2();
	private CollisionResult testPolygons(int pidA, int pidB, boolean flip) {
		result.reset();

		Tint tintA = mTint.get(pidA);
		Tint tintB = mTint.get(pidB);
		tintA.color.set(Color.CYAN);
		tintB.color.set(Color.CYAN);

		// if AABBs are not overlapping we cant be touching
		AABB aabbA = mAABB.get(pidA);
		AABB aabbB = mAABB.get(pidB);
		if (aabbA.rect.overlaps(aabbB.rect)) {
			aabbA.color.set(Color.RED);
			aabbB.color.set(Color.RED);
		} else {
			aabbA.color.set(Color.GREEN);
			aabbB.color.set(Color.GREEN);
			// can bail early as optimization
			return null;
		}

		Polygon polygonA = mPolygon.get(pidA);
		Polygon polygonB = mPolygon.get(pidB);
		float[] vertsA = polygonA.polygon.getTransformedVertices();
		float[] vertsB = polygonB.polygon.getTransformedVertices();
		vOffset.set(vertsA[0] - vertsB[0], vertsA[1] - vertsB[1]);

		float min0, max0;
		float min1, max1;
		boolean gap = false;
		for (int i = 0; i < vertsA.length; i+=2) {
			getAxisNormal(vertsA, i, vAxis);

			renderer.setColor(Color.YELLOW);
			renderer.line(vertsA[i], vertsA[i + 1], vertsA[i] - vAxis.x * 100, vertsA[i + 1] - vAxis.y * 100);
			renderer.line(vertsA[i], vertsA[i + 1], vertsA[i] + vAxis.x * 100, vertsA[i + 1] + vAxis.y * 100);

			min0 = max0 = vAxis.dot(vertsA[0], vertsA[1]);
			for (int j = 0; j < vertsA.length; j+=2) {
				float t = vAxis.dot(vertsA[j], vertsA[j + 1]);
				if (t < min0) min0 = t;
				if (t > max0) max0 = t;
			}

			min1 = max1 = vAxis.dot(vertsB[0], vertsB[1]);
			for (int j = 0; j < vertsB.length; j+=2) {
				float t = vAxis.dot(vertsB[j], vertsB[j + 1]);
				if (t < min1) min1 = t;
				if (t > max1) max1 = t;
			}

			float sOffset = vAxis.dot(tmp1);
			min0 += sOffset;
			max0 += sOffset;

			float d0 = min0 - max1;
			float d1 = min1 - max0;
			if (d0 > 0 || d1 > 0) {
				gap = true;
			}
		}

		if (gap) {
			tintA.color.set(Color.CYAN);
			tintB.color.set(Color.CYAN);
			renderer.setColor(Color.CYAN);
		} else {
			tintA.color.set(Color.BLUE);
			tintB.color.set(Color.BLUE);
			renderer.setColor(Color.BLUE);
		};
		renderer.polygon(vertsA);
		return result;
	}

	Vector2 p1 = new Vector2();
	Vector2 p2 = new Vector2();
	private Vector2 getAxisNormal(float[] verts, int id, Vector2 ret) {
		if (id % 2 != 0) throw  new IllegalArgumentException("dummy!");
		p1.set(verts[id], verts[id + 1]);
		if(id == verts.length - 2 ){
			p2.set(verts[0], verts[1]);
		} else {
			p2.set(verts[id + 2], verts[id + 3]);
		}
		return ret.set(-(p2.y - p1.y), p2.x - p1.x).nor();
	}

	private CollisionResult testCirclePoly(int cid, int pid) {
		result.reset();
		// if AABBs are not overlapping we cant be touching
		AABB aabbA = mAABB.get(cid);
		AABB aabbB = mAABB.get(pid);
		if (aabbA.rect.overlaps(aabbB.rect)) {
			aabbA.color.set(Color.RED);
			aabbB.color.set(Color.RED);
		} else {
			aabbA.color.set(Color.GREEN);
			aabbB.color.set(Color.GREEN);
			// can bail early as optimization
			return null;
		}
		Circle circle = mCircle.get(cid);
		Polygon polygon = mPolygon.get(pid);

		return result;
	}

	private class CollisionResult {
		public boolean collided;
		public int entityA;
		public int entityB;
		public boolean entityAContained;
		public boolean entityBContained;
		public Vector2 vector = new Vector2();
		public float dst;
		public Vector2 separation = new Vector2();

		public CollisionResult () {
			reset();
		}

		public void reset() {
			collided = false;
			entityA = -1;
			entityB = -1;
			entityAContained = false;
			entityBContained = false;
			vector.setZero();
			dst = 0;
			separation.setZero();
		}
	}

}
