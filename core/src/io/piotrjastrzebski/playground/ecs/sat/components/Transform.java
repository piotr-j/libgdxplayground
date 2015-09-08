package io.piotrjastrzebski.playground.ecs.sat.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by PiotrJ on 27/08/15.
 */
public class Transform extends PooledComponent {
	public Vector2 pos = new Vector2();
	public float rot;
	public float scale = 1;

	@Override protected void reset () {
		pos.setZero();
		rot = 0;
		scale = 1;
	}
}
