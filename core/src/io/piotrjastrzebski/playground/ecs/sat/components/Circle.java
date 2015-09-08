package io.piotrjastrzebski.playground.ecs.sat.components;

import com.artemis.PooledComponent;

/**
 * Created by PiotrJ on 27/08/15.
 */
public class Circle extends PooledComponent {
	public com.badlogic.gdx.math.Circle circle = new com.badlogic.gdx.math.Circle();
	public float radius;
	@Override protected void reset () {
		circle.set(0, 0, 0);
		radius = 0;
	}
}
