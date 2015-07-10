package io.piotrjastrzebski.playground.entityonecomptest;

import com.artemis.Entity;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.badlogic.gdx.Gdx;
import io.piotrjastrzebski.playground.BaseScreen;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class EntityOneCompTest extends BaseScreen {
	World world;
	public EntityOneCompTest() {
		WorldConfiguration cfg = new WorldConfiguration();

		world = new World(cfg);
		world.setSystem(new OptionalTestSystem());
		world.initialize();

		world.process();

		Entity e1 = world.createEntity();
		Gdx.app.log("" + e1, "create MandatoryComponent");
		e1.edit().create(MAndatoryComponent.class);
		world.process();
		Gdx.app.log("" + e1, "create OptionalComponentA");
		e1.edit().create(OptionalComponentA.class);
		world.process();
		Gdx.app.log("" + e1, "create OptionalComponentB");
		e1.edit().create(OptionalComponentB.class);
		world.process();
		Gdx.app.log("" + e1, "remove OptionalComponentA");
		e1.edit().remove(OptionalComponentA.class);
		world.process();
		Gdx.app.log("" + e1, "remove OptionalComponentA");
		e1.edit().remove(OptionalComponentB.class);
		world.process();
//		e1.deleteFromWorld();
//		world.process();

		Entity e2 = world.createEntity();
		Gdx.app.log("" + e2, "create MandatoryComponent");
		e2.edit().create(MAndatoryComponent.class);
		Gdx.app.log("" + e2, "create OptionalComponentB");
		e2.edit().create(OptionalComponentB.class);
		world.process();
		Gdx.app.log("" + e2, "remove OptionalComponentB");
		e2.edit().remove(OptionalComponentB.class);
		world.process();

		Entity e3 = world.createEntity();
		Gdx.app.log("" + e3, "create MandatoryComponent");
		e3.edit().create(MAndatoryComponent.class);
		Gdx.app.log("" + e3, "create OptionalComponentA");
		e3.edit().create(OptionalComponentA.class);
		world.process();
		Gdx.app.log("" + e3, "remove OptionalComponentA");
		e3.edit().remove(OptionalComponentA.class);
		world.process();
		Gdx.app.log("" + e3, "create OptionalComponentB");
		e3.edit().create(OptionalComponentB.class);
		world.process();
		Gdx.app.log("" + e3, "remove OptionalComponentB");
		e3.edit().remove(OptionalComponentB.class);
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
