package io.piotrjastrzebski.playground.ecs.sat.components;

import com.artemis.PooledComponent;

/**
 * Created by PiotrJ on 27/08/15.
 */
public class Polygon extends PooledComponent {
	public com.badlogic.gdx.math.Polygon polygon = new com.badlogic.gdx.math.Polygon();
	@Override protected void reset () {
		polygon.setPosition(0, 0);
		polygon.setOrigin(0, 0);
		polygon.setRotation(0);
		polygon.setScale(1, 1);
	}
}
