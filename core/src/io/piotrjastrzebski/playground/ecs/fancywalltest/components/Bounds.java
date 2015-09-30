package io.piotrjastrzebski.playground.ecs.fancywalltest.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by PiotrJ on 30/09/15.
 */
public class Bounds extends PooledComponent {
	public Rectangle rect = new Rectangle();

	@Override protected void reset () {
		rect.set(0, 0, 0, 0);
	}

	public Bounds set (float w, float h) {
		rect.setSize(w, h);
		return this;
	}
}
