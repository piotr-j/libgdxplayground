package io.piotrjastrzebski.playground.box2dtest.iforce2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Based on http://www.iforce2d.net/b2dtut/constant-speed
 *
 * Created by PiotrJ on 31/07/15.
 */
public class MovingForce2dTest extends BaseIForce2dTest {
	private static final String TAG = MovingForce2dTest.class.getSimpleName();
	enum MoveState {STOP, LEFT, RIGHT}
	enum MoveType {DIRECT, FORCE, IMPULSE}
	private Body body;
	private MoveState moveState = MoveState.STOP;
	private MoveType moveType = MoveType.DIRECT;
	private boolean gradual;

	public MovingForce2dTest (GameReset game) {
		super(game);
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1, 1);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1;

		bodyDef.position.set(0, -VP_HEIGHT/2 + 1);
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);

		shape.dispose();

		Gdx.app.log(TAG, "A - move left");
		Gdx.app.log(TAG, "S - move stop");
		Gdx.app.log(TAG, "D - move right");
		Gdx.app.log(TAG, "1 - type direct");
		Gdx.app.log(TAG, "2 - type force");
		Gdx.app.log(TAG, "3 - type impulse");
		Gdx.app.log(TAG, "X - gradual toggle");
	}


	@Override public void update (float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
			gradual = !gradual;
			Gdx.app.log(TAG, "Gradual="+gradual);
		}
		moveState = MoveState.STOP;
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			moveState = MoveState.LEFT;
		} else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			moveState = MoveState.STOP;
		} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			moveState = MoveState.RIGHT;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			moveType = MoveType.DIRECT;
			Gdx.app.log(TAG, "MoveType="+moveType);
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			moveType = MoveType.FORCE;
			Gdx.app.log(TAG, "MoveType="+moveType);
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
			moveType = MoveType.IMPULSE;
			Gdx.app.log(TAG, "MoveType="+moveType);
		}

		switch (moveType) {
		case DIRECT:
			if (gradual) {
				moveDirectGradual();
			} else {
				moveDirect();
			}
			break;
		case FORCE:
			if (gradual) {
				moveForceGradual();
			} else {
				moveForce();
			}
			break;
		case IMPULSE:
			if (gradual) {
				moveImpulseGradual();
			} else {
				moveImpulse();
			}
			break;
		}
	}

	private void moveDirect () {
		Vector2 vel = body.getLinearVelocity();
		switch (moveState) {
		case STOP:
			vel.x = 0;
			break;
		case LEFT:
			vel.x = -5;
			break;
		case RIGHT:
			vel.x = 5;
			break;
		}
		body.setLinearVelocity(vel);
	}

	private void moveDirectGradual () {
		Vector2 vel = body.getLinearVelocity();
		switch (moveState) {
		case STOP:
			vel.x *= .98f;
			break;
		case LEFT:
			vel.x = Math.max(vel.x -0.1f, -5f);
			break;
		case RIGHT:
			vel.x = Math.min(vel.x + 0.1f, 5f);
			break;
		}
		body.setLinearVelocity(vel);
	}

	private void moveForce () {
		Vector2 vel = body.getLinearVelocity();
		Vector2 wc = body.getWorldCenter();
		float targetVel = 0;
		switch (moveState) {
		case STOP:
			targetVel = 0;
			break;
		case LEFT:
			targetVel = -5;
			break;
		case RIGHT:
			targetVel = 5;
			break;
		}
		float diff = targetVel - vel.x;
		float force = body.getMass() * diff/(1/60f); // step time
		body.applyForce(force, 0, wc.x, wc.y, true);
	}

	private void moveForceGradual () {
		Vector2 vel = body.getLinearVelocity();
		Vector2 wc = body.getWorldCenter();
		float force = 0;
		switch (moveState) {
		case STOP:
				force = vel.x * -10;
			break;
		case LEFT:
			if (vel.x > -5) force = -50;
			break;
		case RIGHT:
			if (vel.x < 5) force = 50;
			break;
		}
		body.applyForce(force, 0, wc.x, wc.y, true);
	}

	private void moveImpulse () {
		Vector2 vel = body.getLinearVelocity();
		Vector2 wc = body.getWorldCenter();
		float targetVel = 0;
		switch (moveState) {
		case STOP:
			targetVel = 0;
			break;
		case LEFT:
			targetVel = -5;
			break;
		case RIGHT:
			targetVel = 5;
			break;
		}
		float diff = targetVel - vel.x;
		float impulse = body.getMass() * diff;
		body.applyLinearImpulse(impulse, 0, wc.x, wc.y, true);
	}

	private void moveImpulseGradual () {
		Vector2 vel = body.getLinearVelocity();
		Vector2 wc = body.getWorldCenter();
		float targetVel = 0;
		switch (moveState) {
		case STOP:
			targetVel = vel.x * 0.98f;
			break;
		case LEFT:
			targetVel = Math.max(vel.x -0.1f, -5f);
			break;
		case RIGHT:
			targetVel = Math.min(vel.x +0.1f, 5f);
			break;
		}
		float diff = targetVel - vel.x;
		float impulse = body.getMass() * diff;
		body.applyLinearImpulse(impulse, 0, wc.x, wc.y, true);
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, MovingForce2dTest.class);
	}
}
