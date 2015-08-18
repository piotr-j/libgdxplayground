package io.piotrjastrzebski.playground.ecs.worldiotest;

import com.artemis.*;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.IdentityHashMap;

@Wire
public class ECSWorldIOTest extends BaseScreen {

	World world;
	AspectSubscriptionManager subscriptions;

	public ECSWorldIOTest (PlaygroundGame game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();

		world = new World(config);
		world.inject(this);

		EntitySubscription subscription = subscriptions.get(Aspect.all());

		EntityEdit ee = world.createEntity().edit();
		ee.create(ComponentX.class).text = "hello";
		ee.create(ComponentY.class).text = "whatever";
		ee.create(ReusedComponent.class);

		Entity e = ee.getEntity();

		world.process();

		Json json = new Json(JsonWriter.OutputType.javascript);
//		json.setSerializer(IdentityHashMap.class, new ComponentLookupSerializer(world));
//		EntitySerializer serializer = new EntitySerializer(world, new ReferenceTracker());
//		json.setSerializer(Entity.class, serializer);

		String s = json.prettyPrint(e);
		Gdx.app.log("", s);
		deleteAll(subscription);

		Entity entity = json.fromJson(Entity.class, s);
		world.process();


	}

	private void deleteAll(EntitySubscription sub) {
		IntBag entities = sub.getEntities();
		for (int i = 0; entities.size() > i; i++) {
			world.deleteEntity(entities.get(i));
		}
		world.process();
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.process();
	}

	public static class ComponentX extends Component {
		public String text;
	}
	public static class ComponentY extends Component {
		public String text;
	}
	public static class ReusedComponent extends Component {
		public String text;
	}
}
