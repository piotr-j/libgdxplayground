package io.piotrjastrzebski.playground.ecs.sat.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by PiotrJ on 27/08/15.
 */
public class AABB extends PooledComponent {
	public Color color = new Color();
	public Rectangle rect = new Rectangle();
	@Override protected void reset () {
		rect.set(0, 0, 0, 0);
		color.set(Color.WHITE);
	}
}
