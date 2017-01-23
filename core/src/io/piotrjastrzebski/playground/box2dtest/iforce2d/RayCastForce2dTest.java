package io.piotrjastrzebski.playground.box2dtest.iforce2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 31/07/15.
 */
public class RayCastForce2dTest extends BaseIForce2dTest implements RayCastCallback {
	private static final String TAG = RayCastForce2dTest.class.getSimpleName();

	public RayCastForce2dTest (GameReset game) {
		super(game);
		world.setGravity(new Vector2());

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1, 1);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1;
		for (int i = 0; i < 16; i++) {
			bodyDef.position.set(MathUtils.random(-VP_WIDTH/2 + 2, VP_WIDTH/2 - 2), MathUtils.random(-VP_HEIGHT/2 + 2, VP_HEIGHT/2 - 2));
			Fixture fixture = world.createBody(bodyDef).createFixture(fixtureDef);
		}
		shape.dispose();

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(1);
		fixtureDef.shape = circleShape;
		for (int i = 0; i < 16; i++) {
			bodyDef.position.set(MathUtils.random(-VP_WIDTH/2 + 2, VP_WIDTH/2 - 2), MathUtils.random(-VP_HEIGHT/2 + 2, VP_HEIGHT/2 - 2));
			world.createBody(bodyDef).createFixture(fixtureDef);
		}
		circleShape.dispose();
	}

	private float rayAngle = 0;
	private Vector2 tmpA = new Vector2();
	private Vector2 tmpB = new Vector2();
	private Vector2 tmpC = new Vector2();
	private Vector2 tmpD = new Vector2();
	private Vector2 point = new Vector2();
	private Vector2 normal = new Vector2();
	private int bounces;
	@Override public void update (float delta) {
		rayAngle += 360/20*delta;
		float rayLength = 25;
		tmpA.set(0, 0);
		tmpB.set(rayLength, 0).rotate(rayAngle);
		bounces = 0;
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		drawReflect(tmpA, tmpB);
		renderer.end();
	}

	Body last;
	private void drawReflect(Vector2 start, Vector2 end) {
		bounces++;
		last = null;
		world.rayCast(this, start, end);
		renderer.setColor(Color.WHITE);
		renderer.line(start, point);
		renderer.setColor(Color.GREEN);
		renderer.line(point, tmpD.set(point).add(normal));
		if (last != null) {
			// note that the bounds position is at center of the screen
			Vector2 position = last.getPosition();
			renderer.setColor(Color.ORANGE);
			renderer.circle(position.x, position.y, .5f, 16);
		}
		if (bounces >= 2) return;

		Vector2 remainingRay = tmpC.set(end).sub(point);
		Vector2 projectedNormal = normal.scl(remainingRay.dot(normal));
		Vector2 nextEnd = end.sub(projectedNormal.scl(2)).scl(10);
		// note we must move the start point, or it will be inside the fixture
		tmpC.set(nextEnd).setLength2(0.0025f);
		drawReflect(tmpA.set(point).add(tmpC), tmpB.set(nextEnd));
	}

	@Override public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
		this.point.set(point);
		this.normal.set(normal);
		last = fixture.getBody();
		return fraction;
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, RayCastForce2dTest.class);
	}
}
