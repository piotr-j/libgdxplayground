package io.piotrjastrzebski.playground.ecs.deferredsystemtest;

import com.artemis.*;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;

/**
 * Created by EvilEntity on 11/07/2015.
 */
public class SubSystemB extends IteratingSystem implements SubSystem {
	DeferredSystem dms;

	public SubSystemB (DeferredSystem dms) {
		super(Aspect.all(CommonComp.class, CompB.class));
		this.dms = dms;
	}

	@Override protected void initialize () {

	}

	@Override public void inserted (int e) {
		dms.inserted(e, this);
	}

	@Override public void begin () {

	}

	private Bag<Component> fill = new Bag<>();
	@Override public void process (int e) {
		fill.clear();
		world.getComponentManager().getComponentsFor(e, fill);
		Gdx.app.log("SubSystemB", "process " + e + " " + fill);
	}

	@Override public void end () {

	}

	@Override public void removed (int e) {
		dms.removed(e, this);
	}
}
