package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by PiotrJ on 05/08/15.
 */
@Wire
@com.artemis.annotations.Profile(using = SystemProfiler.class, enabled = SystemProfiler.ENABLED)
public class QTGetSystem extends BaseSystem {
	QTSystem qtSystem;
	QTTestSystem qtTestSystem;
	IntBag fill = new IntBag();
	@Override protected void processSystem () {
		Rectangle b = qtTestSystem.testBounds;
		fill.clear();
		qtSystem.getQuadTree().getExact(fill, b.x, b.y, b.width, b.height);
	}
}
