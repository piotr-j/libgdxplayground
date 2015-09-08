package io.piotrjastrzebski.playground.ecs.aijobs;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by EvilEntity on 17/08/2015.
 */
@Wire
public class Mover extends EntityProcessingSystem {
	private ComponentMapper<Godlike> mGodlike;
	public Mover () {
		super(Aspect.all(Godlike.class));
	}

	@Override protected void process (Entity e) {
		Godlike godlike = mGodlike.get(e);
		if (!godlike.mover) return;
		godlike.atTarget = false;
		if (!MathUtils.isEqual(godlike.x, godlike.tx)) {
			if (godlike.x > godlike.tx) {
				godlike.x -= godlike.vx * world.delta;
				if (godlike.x < godlike.tx) godlike.x = godlike.tx;
			} else {
				godlike.x += godlike.vx * world.delta;
				if (godlike.x > godlike.tx) godlike.x = godlike.tx;
			}
		}
		if (!MathUtils.isEqual(godlike.y, godlike.ty)) {
			if (godlike.y > godlike.ty) {
				godlike.y -= godlike.vy * world.delta;
				if (godlike.y < godlike.ty) godlike.y = godlike.ty;
			} else {
				godlike.y += godlike.vy * world.delta;
				if (godlike.y > godlike.ty) godlike.y = godlike.ty;
			}
		}

		if (MathUtils.isEqual(godlike.x, godlike.tx) && MathUtils.isEqual(godlike.y, godlike.ty)) {
			godlike.atTarget = true;
		}
	}

	public boolean atTarget (int eid) {
		Godlike godlike = mGodlike.get(eid);
		return MathUtils.isEqual(godlike.x, godlike.tx) && MathUtils.isEqual(godlike.y, godlike.ty);
	}

	public boolean isUnreachable (int eid) {
		// lets pretend we can check that here
		return false;
	}
}
