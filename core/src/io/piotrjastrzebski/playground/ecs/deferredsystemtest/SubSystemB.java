package io.piotrjastrzebski.playground.ecs.deferredsystemtest;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;

/**
 * Created by EvilEntity on 11/07/2015.
 */
public class SubSystemB extends EntitySystem implements SubSystem {
	DeferredSystem dms;

	public SubSystemB (DeferredSystem dms) {
		super(Aspect.all(CommonComp.class, CompB.class));
		this.dms = dms;
		setPassive(true);
	}

	@Override protected void initialize () {

	}

	@Override public void inserted (Entity e) {
		dms.inserted(e, this);
	}

	@Override public void begin () {

	}

	private Bag<Component> fill = new Bag<>();
	@Override public void process (Entity e) {
		fill.clear();
		e.getComponents(fill);
		Gdx.app.log("SubSystemB", "process " + e + " " + fill);
	}

	@Override public void end () {

	}

	@Override public void removed (Entity e) {
		dms.removed(e, this);
	}

	@Override
	protected void processSystem () {

	}
}
