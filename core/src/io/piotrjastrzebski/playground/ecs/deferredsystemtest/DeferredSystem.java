package io.piotrjastrzebski.playground.ecs.deferredsystemtest;

import com.artemis.Entity;

/**
 * Created by EvilEntity on 11/07/2015.
 */
public interface DeferredSystem {
	public void inserted (Entity e, SubSystem system);
	public void removed (Entity e, SubSystem system);
}
