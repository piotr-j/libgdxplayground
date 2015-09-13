package io.piotrjastrzebski.playground.ecs.profilerv2.lib;

import com.artemis.BaseSystem;
import com.artemis.SystemInvocationStrategy;
import com.artemis.World;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 *
 *
 * @author piotr-j
 */
public class ProfilerInvocationStrategy extends SystemInvocationStrategy {
	SystemProfiler total;
	SystemProfiler[] profilers;

	public ProfilerInvocationStrategy() {

	}

	boolean initialized;
	@Override protected void process (Bag<BaseSystem> systems) {
		if (!initialized) initialize();
		total.start();
		Object[] systemsData = systems.getData();
		for (int i = 0; i < systems.size(); i++) {
			updateEntityStates();

			BaseSystem system = (BaseSystem)systemsData[i];
			if (!system.isPassive()) {
				SystemProfiler profiler = profilers[i];
				if (profiler != null) profiler.start();
				system.process();
				if (profiler != null) profiler.stop();
			}
		}
		total.stop();
	}

	private void initialize () {
		initialized = true;
		total = SystemProfiler.create("Frame");
		total.setColor(1, 1, 0, 1);

		ImmutableBag<BaseSystem> systems = world.getSystems();
		profilers = new SystemProfiler[systems.size()];
		for (int i = 0; i < systems.size(); i++) {
			BaseSystem system = systems.get(i);
			SystemProfiler old = SystemProfiler.getFor(system);
			if (old == null) {
				profilers[i] = SystemProfiler.createFor(system, world);
			}
		}
	}
}
