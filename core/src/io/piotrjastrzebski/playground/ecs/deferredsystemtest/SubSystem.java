package io.piotrjastrzebski.playground.ecs.deferredsystemtest;

import com.artemis.Entity;

/**
 * Created by EvilEntity on 11/07/2015.
 */
public interface SubSystem {
	public void inserted (int e);
	public void begin();
	public void process (int e);
	public void end();
	public void removed (int e);
}
