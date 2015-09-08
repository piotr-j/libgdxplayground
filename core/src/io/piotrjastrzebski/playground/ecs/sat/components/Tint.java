package io.piotrjastrzebski.playground.ecs.sat.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.graphics.Color;

/**
 * Created by PiotrJ on 27/08/15.
 */
public class Tint extends PooledComponent {
	public Color color = new Color();
	@Override protected void reset () {
		color.set(Color.WHITE);
	}
}
