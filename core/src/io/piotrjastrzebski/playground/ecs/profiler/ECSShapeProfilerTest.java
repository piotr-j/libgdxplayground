package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.*;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Position;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Size;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Velocity;

/**
 * ECS graphical profiler with shape renderer
 * Created by EvilEntity on 28/07/2015.
 */
public class ECSShapeProfilerTest extends BaseScreen {
	World world;

	public ECSShapeProfilerTest (GameReset game) {
		super(game);

		SystemProfiler.resume();

		WorldConfiguration config = new WorldConfiguration();
		config.register("gui", guiCamera);
		config.register("game", gameCamera);
		config.register(gameViewport);
		config.register(batch);
		config.register(renderer);
		config.register(stage);
		config.register(VisUI.getSkin().get("default-font", BitmapFont.class));


		config.setSystem(new DebugDrawSystem());

		config.setSystem(new VelocitySystem());
		config.setSystem(new BoundsSystem());

		config.setSystem(new QTSystem());


		config.setSystem(new QTTestSystem());
		config.setSystem(new QTGetSystem());
		config.setSystem(new QTSelectSystem());

		config.setSystem(new GUISystem());
		config.setSystem(new ProfilerGUISystem());
		config.setInvocationStrategy(new ProfilerIS());

		world = new World(config);

		for (int i = 0; i < 17500; i++) {
			createEntity();
		}
	}

	private void createEntity( ){
		Entity entity = world.createEntity();
		EntityEdit edit = entity.edit();
		Position position = edit.create(Position.class);
		position.x = MathUtils.random(-8.f, 7.f);
		position.y = MathUtils.random(-8.f, 7.f);
		Size size = edit.create(Size.class);
		size.width = MathUtils.random(.25f, .5f);
		size.height = MathUtils.random(.25f, .5f);
		if (MathUtils.random() > 0.5f) {
			Velocity velocity = edit.create(Velocity.class);
			velocity.x = MathUtils.random(-2, 2);
			velocity.y = MathUtils.random(-2, 2);
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.delta = delta;
		world.process();
	}

	public static class ProfilerIS extends SystemInvocationStrategy {
		SystemProfiler total;
		SystemProfiler[] profilers;

		public ProfilerIS () {

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

	@Override public void dispose () {
		super.dispose();
		world.dispose();
	}
}
