package io.piotrjastrzebski.playground.box2dtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

/**
 * Created by PiotrJ on 18/09/15.
 */
public class FancierRayLight implements RayCastCallback, QueryCallback {
	private Vector2 pos = new Vector2();
	private World world;
	int rays = 16;
	float radius = 1;
	float[] txs = new float[rays * 9];
	float[] tys = new float[rays * 9];
	float[] xs = new float[rays * 10];
	float[] ys = new float[rays * 10];
	float[] exs = new float[rays];
	float[] eys = new float[rays];
	float[] fs = new float[rays * 9];

	public FancierRayLight (float x, float y, float radius, World world) {
		pos.set(x, y);
		this.radius = radius;
		this.world = world;
		setEndPoints();
		for (int i = 0; i < 100; i++) {
			tmps.add(new Vector2());
		}
	}

	void setEndPoints() {
		float angleNum = 360f / (rays - 1);
		for (int i = 0; i < rays; i++) {
			final float angle = angleNum * i;
			exs[i] = radius * MathUtils.sinDeg(angle);
			eys[i] = radius * MathUtils.cosDeg(angle);
		}
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
		for (int i = 0; i < rays; i++) {
			rayId = i;
			target.x = exs[i] + pos.x;
			xs[i] = target.x;
			target.y = eys[i] + pos.y;
			ys[i] = target.y;
			fs[i] = 1;
			world.rayCast(this, pos, target);
		}

		tmpsLen = 0;
		// first find all fixtures that are withing our bounding box
		world.QueryAABB(this, pos.x - radius, pos.y - radius, pos.x + radius, pos.y + radius);

		for (int i = 0; i < tmpsLen; i++) {
			Vector2 e = tmps.get(i);
			txs[i] = e.x;
			tys[i] = e.y;
			e.sub(pos);
			e.limit(radius);
			rayId = rays + i;
			target.x = e.x + pos.x;
			xs[rayId] = target.x;
			target.y = e.y + pos.y;
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

	float extra = 0.5f;
	float cExtra = 1.5f;
	boolean ignoreRadius = true;
	Array<Vector2> tmps = new Array<>();
	int tmpsLen;
	Vector2 tmp = new Vector2();
	Vector2 tmp2 = new Vector2();
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
					tmps.get(tmpsLen).set(tmp);
					tmpsLen++;
					tmps.get(tmpsLen).set(tmp2.set(tmp).rotate(extra).setLength(radius));
					tmpsLen++;
					tmps.get(tmpsLen).set(tmp2.set(tmp).rotate(-extra).setLength(radius));
					tmpsLen++;

				}
			}
			break;
		case Circle:
			CircleShape circle = (CircleShape)shape;
			Vector2 cp = body.getWorldPoint(circle.getPosition());
			float r = circle.getRadius();
			float angle = tmp.set(pos).sub(p).angle();
			Gdx.app.log("Fixture", "Circle ");
			// TODO this is brokenwhen circle is close to origin, need to find proper points somehow
			tmp.set(cp.x + r * MathUtils.sinDeg(angle), cp.y - r * MathUtils.cosDeg(angle));
			if (ignoreRadius || tmp.dst2(pos) <= radius * radius) {
				// right edge
				tmps.get(tmpsLen).set(tmp);
				tmpsLen++;
				tmps.get(tmpsLen).set(tmp2.set(tmp).rotate(cExtra).setLength(radius));
				tmpsLen++;
			}
			tmp.set(cp.x + r * MathUtils.sinDeg(angle + 90), cp.y - r * MathUtils.cosDeg(angle + 90));
			if (ignoreRadius || tmp.dst2(pos) <= radius * radius) {
				// top
				tmps.get(tmpsLen).set(tmp);
				tmpsLen++;
			}
			tmp.set(cp.x + r * MathUtils.sinDeg(angle + 180), cp.y - r * MathUtils.cosDeg(angle + 180));
			if (ignoreRadius || tmp.dst2(pos) <= radius * radius) {
				// left edge
				tmps.get(tmpsLen).set(tmp);
				tmpsLen++;
				tmps.get(tmpsLen).set(tmp2.set(tmp).rotate(-cExtra).setLength(radius));
				tmpsLen++;
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
					tmps.get(tmpsLen).set(tmp);
					tmpsLen++;
				}
			}
		default:
			Gdx.app.log("Fixture", shape.getType().name());
		}
		// we want all
		return true;
	}

	public void draw(ShapeRenderer renderer) {
		renderer.setColor(Color.RED);
		renderer.circle(pos.x, pos.y, 0.05f, 8);
		renderer.circle(pos.x, pos.y, radius, 32);

		renderer.setColor(Color.CYAN);
		for (int i = 0; i < rays; i++) {
			renderer.line(pos.x, pos.y, xs[i], ys[i]);
		}
//
//		renderer.setColor(Color.GREEN);
//		for (int i = 0; i < tmpsLen; i++) {
//			renderer.line(pos.x, pos.y, txs[i], tys[i]);
//		}

		renderer.setColor(Color.RED);
		for (int i = rays; i < rays + tmpsLen; i++) {
			renderer.line(pos.x, pos.y, xs[i], ys[i]);
		}
	}

}
