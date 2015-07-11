package io.piotrjastrzebski.playground.deferredsystemtest;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Created by EvilEntity on 11/07/2015.
 */
public class DeferredManagerSystem extends BaseSystem implements DeferredSystem{

	private boolean dirty;

	public DeferredManagerSystem () {

	}

	private Array<Job> jobs = new Array<>();
	@Override public void inserted (Entity e, SubSystem system) {
		Gdx.app.log("deferred", "inserted "+ e);
		// not a whole lot of insert/remove cycles probably fine to not pool this
		jobs.add(new Job(e.getId(), system));
		dirty = true;
	}

	SubSystem active;
	@Override protected void processSystem () {
		if (dirty) {
			jobs.sort();
		}

		for (Job job : jobs) {
			if (active != job.system) {
				if (active != null) {
					active.end();
				}
				active = job.system;
				active.begin();
			}

			active.process(world.getEntity(job.eid));
		}

		if (active != null) {
			active.end();
		}
	}

	@Override public void removed (Entity e, SubSystem system) {
		Gdx.app.log("deferred", "remove "+ e);
		for (int i = 0; i < jobs.size; i++) {
			Job job = jobs.get(i);
			if (job.eid == e.id && job.system == system) {
				jobs.removeIndex(i);
				break;
			}
		}
		dirty = true;
	}

	public class Job implements Comparable<Job>{
		public final int eid;
		public final SubSystem system;

		public Job (int eid, SubSystem system) {
			this.eid = eid;
			this.system = system;
		}

		@Override public int compareTo (Job o) {
			return 0;
		}
	}
}
