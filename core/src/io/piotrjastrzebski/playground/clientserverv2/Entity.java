package io.piotrjastrzebski.playground.clientserverv2;

import com.badlogic.gdx.math.MathUtils;
/**
 * Created by PiotrJ on 21/06/15.
 */
public class Entity {
	public static final float MAX_ACCEL = 50;
	// coefficient of friction
	float friction = 3f;
	float positionX;
	float srcX;
	float viewX;
	float velocity = 0;
	public int id;
	float accel;

	public Entity (float x) {
		this.positionX = x;
		srcX = x;
	}

	public void applyInput(GameInput input) {
		// accumulate acceleration as server updates a lot slower than the clients send data
		// this assumes that accel can be anything withing -MAX_ACCEL, MAX_ACCEL range
		accel += input.accel;
		// clamp as we never want to exceed these values
		accel = MathUtils.clamp(accel, -MAX_ACCEL, MAX_ACCEL);
	}
	float alpha;
	public void update(float dt, float alpha) {
		// TODO physics update must be fixed, lerp postion, same as server tick rate
		this.alpha = alpha;
		viewX = MathUtils.lerp(srcX, positionX, alpha);
	}

	public void fixedUpdate() {
		srcX = viewX;

		velocity *= 0.9f;
//		velocity += -velocity * 2f * dt;
		float dt = Server.TICK_RATE;
		positionX += accel * dt * dt * 0.5f;
		positionX += velocity * dt;

		velocity += accel * dt;

		float hWidth = CSTestV2.VP_WIDTH/2;
		if (positionX > hWidth) {
			positionX = -hWidth;
			srcX = positionX;
		} else if (positionX < -hWidth) {
			positionX = hWidth;
			srcX = positionX;
		}
		accel = 0;
	}

	public float getPositionX() {
		return viewX;
	}
	public float getRealPositionX() {
		return positionX;
	}

	public void clearForces() {
		accel = 0;
	}
}
