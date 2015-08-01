package io.piotrjastrzebski.playground.ecs;

import com.artemis.*;
import com.artemis.systems.EntityProcessingSystem;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

public class ECSPooledCompTest extends BaseScreen {

	World world;
	public ECSPooledCompTest (PlaygroundGame game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();
		config.setSystem(new Adder());
		world = new World(config);

		for (int i = 0; i < 50000; i++) {
			world.createEntity().edit().create(SomeData.class);
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.process();
	}

	public static class Adder extends EntityProcessingSystem {
		public Adder () {
			super(Aspect.all(SomeData.class));
		}
		@Override protected void process (Entity e) {
			// removing first fixes the pooling
			// e.edit().remove(Flag.class);
			e.edit().create(Flag.class);
		}
	}

	public static class Flag extends PooledComponent {
		@Override protected void reset () {}
	}

	public static class SomeData extends Component {}
}
