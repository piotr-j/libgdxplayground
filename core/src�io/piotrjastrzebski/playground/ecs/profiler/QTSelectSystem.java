package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Position;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Selected;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Size;
import net.mostlyoriginal.api.utils.pooling.ObjectPool;
import net.mostlyoriginal.api.utils.pooling.Poolable;
import net.mostlyoriginal.api.utils.pooling.Pools;

@Wire
@com.artemis.annotations.Profile(using = SystemProfiler.class, enabled = SystemProfiler.ENABLED)
public class QTSelectSystem extends BaseSystem {
	QTGetSystem qtGetSystem;
	public QTSelectSystem () {
		super();
	}

	@Override protected void initialize () {
		super.initialize();
	}

	@Override protected void processSystem () {
		IntBag fill = qtGetSystem.fill;
		for (int i = 0; i < fill.size(); i++) {
			Entity entity = world.getEntity(fill.get(i));
			entity.edit().create(Selected.class);
		}
	}
}
