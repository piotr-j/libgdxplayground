package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Position;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Size;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Velocity;

/**
 * ECS graphical profiler with shape renderer
 * Created by EvilEntity on 28/07/2015.
 */
public class ECSPolyProfilerTest extends BaseScreen {
	private final static String TAG = ECSPolyProfilerTest.class.getSimpleName();

	PolygonSpriteBatch polyBatch;
	World world;
	public ECSPolyProfilerTest (PlaygroundGame game) {
		super(game);

		polyBatch = new PolygonSpriteBatch(10920);

		SystemProfiler.resume();

		WorldConfiguration config = new WorldConfiguration();
		config.register("gui", guiCamera);
		config.register("game", gameCamera);
		config.register(gameViewport);
		config.register(batch);
		config.register(polyBatch);
		config.register(renderer);
		config.register(VisUI.getSkin().get("default-font", BitmapFont.class));


		config.setSystem(new DebugDrawSystem());

		config.setSystem(new VelocitySystem());
		config.setSystem(new BoundsSystem());

		config.setSystem(new QTSystem());


		config.setSystem(new QTTestSystem());
		config.setSystem(new QTGetSystem());
		config.setSystem(new QTSelectSystem());

		config.setSystem(new PolyProfilerSystem());

		world = new World(config);

		for (int i = 0; i < 10000; i++) {
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
		long start = System.nanoTime();
		world.process();
		// probably should lazy init ot something
		SystemProfiler.get("Frame").sample(System.nanoTime() - start);
//		SystemProfiler.FRAME.sample(System.nanoTime() - start);
	}

	@Override public void dispose () {
		super.dispose();
		world.dispose();
		polyBatch.dispose();
	}
}
