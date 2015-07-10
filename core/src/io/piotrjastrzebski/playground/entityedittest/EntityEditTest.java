package io.piotrjastrzebski.playground.entityedittest;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class EntityEditTest extends BaseScreen {
	World world;
	public EntityEditTest (PlaygroundGame game) {
		super(game);
		WorldConfiguration cfg = new WorldConfiguration();

		world = new World(cfg);
		world.setSystem(new TestSystem());
		world.initialize();
		Entity entity = world.createEntity();
		Gdx.app.log("", "e: "+ world.getEntity(entity.id));
		world.process();
		Gdx.app.log("", "e: "+ world.getEntity(entity.id));
		entity.edit().create(TestComponentA.class).data = "first A";
		entity.edit().create(TestComponentB.class).data = "first B";
		world.process(); // TestSystem: inserted, process
		// entity.edit().remove(TestComponentB.class);
		// world.process(); // TestSystem: removed
		// replace/edit component
		entity.edit().create(TestComponentB.class).data = "second B";
		world.process(); // TestSystem: process, TestComponentB changed
		entity.deleteFromWorld();
		world.process(); // TestSystem: removed

		Json json = new Json();
		Gdx.app.log("",""+Gdx.files.external("welp"));
		Gdx.files.external("whatever.json").writeString(json.toJson("some string crap"), false);
	}

	@Override
	public void dispose() {
		super.dispose();
		world.dispose();
	}

	static Bag<Component> cFill = new Bag<>();
	public static String entityToStr(Entity e) {
		cFill.clear();
		e.getComponents(cFill);
		String str =  "Entity{id="+e.getId();
		for(Component c: cFill) {
			// can be null when it was removed
			if (c == null) continue;
			str+=", " + c.toString();
		}
		return str + "}";
	}
}
