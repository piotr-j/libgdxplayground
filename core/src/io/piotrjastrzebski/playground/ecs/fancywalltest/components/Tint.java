package io.piotrjastrzebski.playground.ecs.fancywalltest.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.graphics.Color;

/**
 * Created by PiotrJ on 30/09/15.
 */
public class Tint extends PooledComponent {
	public Color color = new Color();

	@Override protected void reset () {
		color.set(1, 1, 1, 1);
	}
}
