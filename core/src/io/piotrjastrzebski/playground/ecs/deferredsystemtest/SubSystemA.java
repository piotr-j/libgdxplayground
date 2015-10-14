package io.piotrjastrzebski.playground.ecs.deferredsystemtest;

import com.artemis.*;

import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;

/**
 * Created by EvilEntity on 11/07/2015.
 */
@Wire
public class SubSystemA extends IteratingSystem implements SubSystem {
	DeferredSystem dms;

	public SubSystemA (DeferredSystem dms) {
		super(Aspect.all(CommonComp.class, CompA.class));
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
		Gdx.app.log("SubSystemA", "process " + e + " " + fill);
	}

	@Override public void end () {

	}

	@Override public void removed (int e) {
		dms.removed(e, this);
	}
}
