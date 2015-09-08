package io.piotrjastrzebski.playground.ecs;

import com.artemis.*;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

import java.util.Comparator;

/**
 * Separating Axis Theorem Test
 * Created by EvilEntity on 28/07/2015.
 */
public abstract class ECSTestBase extends BaseScreen {
	public static final String WIRE_GUI_CAM = "gui-cam";
	public static final String WIRE_GUI_VP = "gui-vp";
	public static final String WIRE_GAME_CAM = "game-cam";
	public static final String WIRE_GAME_VP = "game-vp";

	protected World world;

	public ECSTestBase (GameReset game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();
		config.register(WIRE_GUI_CAM, guiCamera);
		config.register(WIRE_GUI_VP, guiViewport);
		config.register(WIRE_GAME_CAM, gameCamera);
		config.register(WIRE_GAME_VP, gameViewport);
		config.register(batch);
		config.register(renderer);
		config.register(stage);

		preInit(config);
		world = new World(config);
		postInit();

		Array<Input> inputs = new Array<>();
		input(inputs, world.getManagers());
		input(inputs, world.getSystems());
		inputs.sort(new Comparator<Input>() {
			@Override public int compare (Input o1, Input o2) {
				return o1.priority() - o2.priority();
			}
		});
		for (Input input : inputs) {
			multiplexer.addProcessor(input.get());
		}
	}

	/**
	 * Called before world is created, after common objects are added
	 */
	protected abstract void preInit (WorldConfiguration config);

	/**
	 * Called after world is created
	 */
	protected abstract void postInit ();

	private void input(Array<Input> inputs, ImmutableBag bag) {
		for (Object object : bag) {
			if (object instanceof Input) {
				inputs.add((Input)object);
			}
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.delta = delta;
		world.process();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		if (world == null) return;
		resize(world.getManagers(), width, height);
		resize(world.getSystems(), width, height);
	}

	private void resize(ImmutableBag bag, int width, int height) {
		for (Object object : bag) {
			if (object instanceof Resizing) {
				((Resizing)object).resize(width, height);
			}
		}
	}

	@Override public void dispose () {
		super.dispose();
		world.dispose();
	}

	static Bag<Component> fill = new Bag<>();
	static StringBuilder sb = new StringBuilder();
	public static String toString(int eid, World world) {
		fill.clear();
		sb.setLength(0);
		sb.append("Entity{");
		sb.append(eid);
		sb.append("}[\n");
		world.getEntity(eid).getComponents(fill);
		for (int i = 0; fill.size() > i; i++) {
			if (i > 0) sb.append(",\n");
			sb.append("  ");
			sb.append(fill.get(i));
		}
		sb.append("\n]");
		return sb.toString();
	}
}
