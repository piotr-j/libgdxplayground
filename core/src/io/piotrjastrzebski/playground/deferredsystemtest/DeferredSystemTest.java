package io.piotrjastrzebski.playground.deferredsystemtest;

import com.artemis.EntityEdit;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class DeferredSystemTest extends BaseScreen {
	World world;

	public DeferredSystemTest (PlaygroundGame game) {
		super(game);
		WorldConfiguration cfg = new WorldConfiguration();

		world = new World(cfg);
		DeferredManagerSystem deferred = world.setSystem(new DeferredManagerSystem());
		world.setSystem(new SubSystemA(deferred));
		world.setSystem(new SubSystemB(deferred));
		world.initialize();
		EntityEdit editA = world.createEntity().edit();
		editA.create(CommonComp.class);
		editA.create(CompA.class);
		EntityEdit editB = world.createEntity().edit();
		editB.create(CommonComp.class);
		editB.create(CompB.class);
		world.process();
		editA.deleteEntity();
		world.process();
		editB.deleteEntity();
		world.process();

	}

	@Override
	public void render(float delta) {
		super.render(delta);
		world.process();
	}

	@Override
	public void dispose() {
		super.dispose();
		world.dispose();
	}
}
