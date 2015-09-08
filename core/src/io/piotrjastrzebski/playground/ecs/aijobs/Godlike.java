package io.piotrjastrzebski.playground.ecs.aijobs;

import com.artemis.PooledComponent;
import com.badlogic.gdx.graphics.Color;
import com.kotcrab.vis.ui.widget.VisLabel;

/**
 * All we need besides jobs cus we are lazy
 * Created by EvilEntity on 17/08/2015.
 */
public class Godlike extends PooledComponent {
	public String name;
	public float x, y, width, height;
	public float vx, vy;
	public float tx, ty;
	public Color color = new Color();
	boolean selected;
	public VisLabel actor;
	public boolean atTarget;
	public boolean mover;
	public int id;

	@Override protected void reset () {
		name = null;
		x = y = width = height = vx = vy = 0;
		color.set(Color.WHITE);
		selected = false;
		actor = null;
	}

	@Override public String toString () {
		return "Godlike<"+name+">";
	}
}
