package io.piotrjastrzebski.playground.ecs.fancywalltest.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by PiotrJ on 30/09/15.
 */
public class Wall extends PooledComponent {
	public float height;
	public Vector2 off = new Vector2();

	@Override protected void reset () {
		height = 0;
		off.setZero();
	}
}
