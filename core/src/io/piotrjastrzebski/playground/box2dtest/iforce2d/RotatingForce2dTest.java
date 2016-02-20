package io.piotrjastrzebski.playground.box2dtest.iforce2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * based on http://www.iforce2d.net/b2dtut/rotate-to-angle
 * <p/>
 * Created by PiotrJ on 31/07/15.
 */
public class RotatingForce2dTest extends BaseIForce2dTest {
	private static final String TAG = RotatingForce2dTest.class.getSimpleName();

	private enum RotateType {DIRECT, TORQUE, IMPULSE}

	private RotateType rotateType = RotateType.DIRECT;
	private Body polygonBody;
	private Body circleBody;
	private Body body;
	private boolean gradual;

	public RotatingForce2dTest (GameReset game) {
		super(game);
		world.setGravity(Vector2.Zero);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;

		// NOTE torque rotation doesnt work for this shape, as it requires that center of mass is in center of the body
		PolygonShape shape = new PolygonShape();
		float vertices[] = new float[12];
		for (int i = 0; i < 6; i++) {
			float angle = -i / 6.0f * 360 * MathUtils.degRad;
			vertices[i * 2] = MathUtils.sin(angle);
			vertices[i * 2 + 1] = MathUtils.cos(angle);
		}
		vertices[0] = 0;
		vertices[1] = 4;
		shape.set(vertices);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1;

		bodyDef.position.set(0, 0);
		polygonBody = world.createBody(bodyDef);
		polygonBody.createFixture(fixtureDef);
		shape.dispose();

		body = polygonBody;

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(1f);
		fixtureDef.shape = circleShape;

		circleBody = world.createBody(bodyDef);
		circleBody.createFixture(fixtureDef);
		circleBody.setTransform(-100, 0, 0);

		circleShape.dispose();

		Gdx.app.log(TAG, "1 - rotate direct");
		Gdx.app.log(TAG, "2 - rotate torque");
		Gdx.app.log(TAG, "3 - rotate impulse");
		Gdx.app.log(TAG, "X - gradual toggle");
		Gdx.app.log(TAG, "Z - Change shape");
	}

	@Override public void update (float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
			if (body == polygonBody) {
				polygonBody.setTransform(-100, 0, 0);
				polygonBody.setAngularVelocity(0);

				circleBody.setTransform(0, 0, 0);
				circleBody.setAngularVelocity(0);
				body = circleBody;
				Gdx.app.log(TAG, "Body=Circle");
			} else {
				circleBody.setTransform(-100, 0, 0);
				circleBody.setAngularVelocity(0);

				polygonBody.setTransform(0, 0, 0);
				polygonBody.setAngularVelocity(0);
				body = polygonBody;
				Gdx.app.log(TAG, "Body=Polygon");
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
			gradual = !gradual;
			Gdx.app.log(TAG, "Gradual=" + gradual);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			rotateType = RotateType.DIRECT;
			Gdx.app.log(TAG, "RotateType=" + rotateType);
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			rotateType = RotateType.TORQUE;
			Gdx.app.log(TAG, "RotateType=" + rotateType);
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
			rotateType = RotateType.IMPULSE;
			Gdx.app.log(TAG, "RotateType=" + rotateType);
		}
		switch (rotateType) {
		case DIRECT:
			if (!gradual) {
				rotateDirect();
			} else {
				rotateDirectGradual();
			}
			break;
		case TORQUE:
			if (!gradual) {
				rotateTorque();
			} else {
				rotateTorqueGradual();
			}
			break;
		case IMPULSE:
			if (!gradual) {
				rotateImpulse();
			} else {
				rotateImpulseGradual();
			}
			break;
		}
	}

	private void rotateDirect () {
		cursor.sub(body.getPosition());
		float targetAngle = cursor.angleRad() - 90 * MathUtils.degRad;
		body.setTransform(body.getPosition(), targetAngle);
		body.setAngularVelocity(0);
	}

	private void rotateDirectGradual () {
		float angle = body.getAngle();
		cursor.sub(body.getPosition());
		float targetAngle = cursor.angleRad() - 90 * MathUtils.degRad;
		float totalRotation = targetAngle - angle;
		while (totalRotation < -180 * MathUtils.degRad)
			totalRotation += 360 * MathUtils.degRad;
		while (totalRotation > 180 * MathUtils.degRad)
			totalRotation -= 360 * MathUtils.degRad;
		float change = 1 * MathUtils.degRad; //allow 1 degree rotation per time step
		float newAngle = angle + Math.min(change, Math.max(-change, totalRotation));
		body.setTransform(body.getPosition(), newAngle);
	}

	private void rotateTorque () {
		cursor.sub(body.getPosition());
		float targetAngle = cursor.angleRad() - 90 * MathUtils.degRad;
		float nextAngle = body.getAngle() + body.getAngularVelocity() / 60.0f;
		float totalRotation = targetAngle - nextAngle;
		while (totalRotation < -180 * MathUtils.degRad)
			totalRotation += 360 * MathUtils.degRad;
		while (totalRotation > 180 * MathUtils.degRad)
			totalRotation -= 360 * MathUtils.degRad;
		float desiredAngularVelocity = totalRotation * 60;
		float torque = body.getInertia() * desiredAngularVelocity / (1 / 60.0f);
		body.applyTorque(torque, true);
	}

	private void rotateTorqueGradual () {
		cursor.sub(body.getPosition());
		float targetAngle = cursor.angleRad() - 90 * MathUtils.degRad;
		float nextAngle = body.getAngle() + body.getAngularVelocity() / 3.0f;
		float totalRotation = targetAngle - nextAngle;
		while (totalRotation < -180 * MathUtils.degRad)
			totalRotation += 360 * MathUtils.degRad;
		while (totalRotation > 180 * MathUtils.degRad)
			totalRotation -= 360 * MathUtils.degRad;
		body.applyTorque(totalRotation < 0 ? -10 : 10, true);
	}

	private void rotateImpulse () {
		float angle = body.getAngle();
		cursor.sub(body.getPosition());
		float targetAngle = cursor.angleRad() - 90 * MathUtils.degRad;
		float nextAngle = angle + body.getAngularVelocity() / 3.0f;
		float totalRotation = targetAngle - nextAngle;
		while (totalRotation < -180 * MathUtils.degRad)
			totalRotation += 360 * MathUtils.degRad;
		while (totalRotation > 180 * MathUtils.degRad)
			totalRotation -= 360 * MathUtils.degRad;
		float desiredAngularVelocity = totalRotation * 3;
		float impulse = body.getInertia() * desiredAngularVelocity;
		body.applyAngularImpulse(impulse, true);
	}

	private void rotateImpulseGradual () {
		float angle = body.getAngle();
		cursor.sub(body.getPosition());
		float targetAngle = cursor.angleRad() - 90 * MathUtils.degRad;
		float nextAngle = angle + body.getAngularVelocity() / 3.0f;
		float totalRotation = targetAngle - nextAngle;
		while (totalRotation < -180 * MathUtils.degRad)
			totalRotation += 360 * MathUtils.degRad;
		while (totalRotation > 180 * MathUtils.degRad)
			totalRotation -= 360 * MathUtils.degRad;
		float desiredAngularVelocity = totalRotation * 3;
		float change = 1 * MathUtils.degRad; //allow 1 degree rotation per time step
		desiredAngularVelocity = Math.min(change, Math.max(-change, desiredAngularVelocity));
		float impulse = body.getInertia() * desiredAngularVelocity;
		body.applyAngularImpulse(impulse, true);
	}

	@Override public boolean keyDown (int keycode) {
		return super.keyDown(keycode);
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, RotatingForce2dTest.class);
	}
}
