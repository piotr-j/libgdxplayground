package io.piotrjastrzebski.playground.clientserver;

/**
 * Created by PiotrJ on 21/06/15.
 */
public class Entity {
	float x;
	float speed = 10;
	public int id;

	public Entity (float x) {
		this.x = x;
	}

	public void applyInput(GameInput input) {
		x += speed * input.duration;
	}

	public float getX () {
		return x;
	}
}
