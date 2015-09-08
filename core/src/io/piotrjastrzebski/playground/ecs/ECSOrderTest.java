package io.piotrjastrzebski.playground.ecs;

import com.artemis.*;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class ECSOrderTest extends BaseScreen {
	private final static String TAG = ECSOrderTest.class.getSimpleName();

	World world;
	public ECSOrderTest (GameReset game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();

		config.setSystem(new Selector());
		config.setSystem(new Reactor());
		// adding this system changes stuff
		config.setSystem(new Dummy());

		world = new World(config);

		Entity e1 = world.createEntity();
		e1.edit().create(Spec1.class);
		e1.edit().create(Spec2.class);
		Gdx.app.log(TAG, "Process 1");
		world.process();
		Gdx.app.log(TAG, "Process 2");
		world.process();
		Gdx.app.log(TAG, "Process 3");
		world.process();
		Gdx.app.log(TAG, "Process 4");
		world.process();

	}

	public static class Selector extends EntityProcessingSystem {
		private final static String TAG = "  "+Selector.class.getSimpleName();

		public Selector () {
			super(Aspect.all(Spec1.class).exclude(Selected.class));
		}

		@Override protected void inserted (int entityId) {
			Gdx.app.log(TAG, "inserted e:" + entityId);
		}

		@Override protected void process (Entity e) {
			Gdx.app.log(TAG, "process e:" + e.id);
			e.edit().create(Selected.class);
		}

		@Override protected void removed (int entityId) {
			Gdx.app.log(TAG, "removed e:" + entityId);
		}
	}

	public static class Reactor extends EntityProcessingSystem {
		private final static String TAG = "  "+Reactor.class.getSimpleName();

		public Reactor () {
			super(Aspect.all(Spec1.class, Selected.class));
		}

		@Override protected void inserted (int entityId) {
			Gdx.app.log(TAG, "inserted e:" + entityId);
		}

		@Override protected void process (Entity e) {
			Gdx.app.log(TAG, "process e:" + e.id);
			e.edit().remove(Selected.class);
		}

		@Override protected void removed (int entityId) {
			Gdx.app.log(TAG, "removed e:" + entityId);
		}
	}
	public static class Dummy extends EntityProcessingSystem {
		public Dummy () {
			super(Aspect.all(Spec1.class, Selected.class));
		}

		@Override protected void process (Entity e) {
		}
	}

	public static class Selected extends Component {
		@Override public String toString () {
			return "Selected{}";
		}
	}

	public static class Spec1 extends Component {
		@Override public String toString () {
			return "Spec1{}";
		}
	}

	public static class Spec2 extends Component {
		@Override public String toString () {
			return "Spec2{}";
		}
	}
}
