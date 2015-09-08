package io.piotrjastrzebski.playground.ecs.profilerv2;

import com.artemis.*;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.ecs.profilerv2.lib.ProfilerInvocationStrategy;
import io.piotrjastrzebski.playground.ecs.profilerv2.lib.SystemProfiler;

/**
 * Created by PiotrJ on 14/08/15.
 */
public class ECSProfilerTest extends BaseScreen {
	World world;

	public ECSProfilerTest (GameReset game) {
		super(game);
		SystemProfiler.resume();

		WorldConfiguration config = new WorldConfiguration();
		config.register("game", gameCamera);
		config.register("gui", guiCamera);
		config.register(gameViewport);
		config.register(renderer);
		config.register(stage);

		config.setSystem(new Mover());
		config.setSystem(new Bounder());
		config.setSystem(new Renderer());
		config.setSystem(new Stager());
		config.setSystem(new ProfilerSystem());

		world = new World(config);
		world.setInvocationStrategy(new ProfilerInvocationStrategy(world));

		float w = gameViewport.getWorldWidth() / 2;
		float h = gameViewport.getWorldHeight() / 2;
		for (int i = 0; i < 50000; i++) {
			EntityEdit edit = world.createEntity().edit();

			Size size = edit.create(Size.class);
			size.width = MathUtils.random(0.1f, 0.25f);
			size.height = MathUtils.random(0.1f, 0.25f);

			Transform transform = edit.create(Transform.class);
			transform.x = MathUtils.random(-w, w - size.width);
			transform.y = MathUtils.random(-h, h - size.height);

			if (MathUtils.random() > 0.25f) {
				Velocity velocity = edit.create(Velocity.class);
				velocity.x = MathUtils.random(-1f, 1f);
				velocity.y = MathUtils.random(-1f, 1f);
			}
			edit.create(DebugRenderable.class);
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.delta = delta;
		world.process();
	}

	@Override public void dispose () {
		super.dispose();
		world.dispose();
	}

	@Wire
	public static class Stager extends BaseSystem {
		@Wire Stage stage;
		@Override protected void processSystem () {
			stage.act(world.delta);
			stage.draw();
		}
	}

	public static class Transform extends Component {
		public float x, y;
	}

	public static class Size extends Component {
		public float width, height;
	}

	public static class Velocity extends Component {
		public float x, y;
	}

	public static class DebugRenderable extends Component {
	}
}
