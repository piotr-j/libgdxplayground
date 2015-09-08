package io.piotrjastrzebski.playground.ecs.deferredsystemtest;

import com.artemis.EntityEdit;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class DeferredSystemTest extends BaseScreen {
	World world;

	public DeferredSystemTest (GameReset game) {
		super(game);
		WorldConfiguration cfg = new WorldConfiguration();

		DeferredManagerSystem deferred;
		cfg.setSystem(deferred = new DeferredManagerSystem());
		cfg.setSystem(new SubSystemA(deferred));
		cfg.setSystem(new SubSystemB(deferred));
		world = new World(cfg);

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
