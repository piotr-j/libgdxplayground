package io.piotrjastrzebski.playground.ecs.sat.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by PiotrJ on 27/08/15.
 */
public class GUIActor extends PooledComponent {
	public Actor actor;
	@Override protected void reset () {
		actor = null;
	}
}
