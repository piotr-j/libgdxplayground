package io.piotrjastrzebski.playground.ecs.worldiotest;

import com.artemis.*;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.Wire;
import com.artemis.io.JsonArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Wire
public class ECSWorldIOTest extends BaseScreen {

	World world;
	AspectSubscriptionManager subscriptions;

	public ECSWorldIOTest (GameReset game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();



		WorldSerializationManager manager = new WorldSerializationManager();
		config.setSystem(manager);

		world = new World(config);
		world.inject(this);

		JsonArtemisSerializer backend = new JsonArtemisSerializer(world);
		backend.prettyPrint(true);
		manager.setSerializer(backend);

		EntitySubscription subscription = subscriptions.get(Aspect.all());

		ReusedComponent reusedComponent = new ReusedComponent();
		reusedComponent.text = "reused";

		EntityEdit ee;
		ee = world.createEntity().edit();
		Entity e0 = ee.getEntity();
		ComponentX componentX0 = ee.create(ComponentX.class);
		componentX0.text = "hello0";
		ee.create(ComponentY.class).text = "whatever0";
		ComponentRef componentRef0 = ee.create(ComponentRef.class);
		ee.add(reusedComponent);

		ee = world.createEntity().edit();
		Entity e1 = ee.getEntity();
		ComponentX componentX1 = ee.create(ComponentX.class);
		componentX1.text = "hello1";
		ee.create(ComponentY.class).text = "whatever1";
		ComponentRef componentRef1 = ee.create(ComponentRef.class);
		ee.add(reusedComponent);

		ee = world.createEntity().edit();
		Entity e2 = ee.getEntity();
		ComponentX componentX2 = ee.create(ComponentX.class);
		componentX2.text = "hello2";
		ee.create(ComponentY.class).text = "whatever2";
		ComponentRef componentRef2 = ee.create(ComponentRef.class);
		ee.add(reusedComponent);

		componentRef0.e = e1;
		componentRef0.id = e1.getId();
		componentRef0.x = componentX1;

		componentRef1.e = e2;
		componentRef1.id = e2.getId();
		componentRef1.x = componentX2;

		componentRef2.e = e0;
		componentRef2.id = e0.getId();
		componentRef2.x = componentX0;


		world.process();

		IntBag entities = subscription.getEntities();
		for (int i = 0; i < entities.size(); i++) {
			Gdx.app.log("", entityToStr(world, entities.get(i)));
		}

		SaveFileFormat save = new SaveFileFormat(subscription);
		StringWriter writer = new StringWriter();
//		manager.save(writer, save);
		String jsonSave = writer.toString();
		Gdx.app.log("saveData", jsonSave);

		deleteAll(subscription);

		ByteArrayInputStream is = new ByteArrayInputStream(jsonSave.getBytes(StandardCharsets.UTF_8));
		manager.load(is, SaveFileFormat.class);

		world.process();

		entities = subscription.getEntities();
		for (int i = 0; i < entities.size(); i++) {
			Gdx.app.log("", entityToStr(world, entities.get(i)));
		}

	}

	static Bag<Component> fill = new Bag<>();
	static StringBuilder sb = new StringBuilder();
	public static String entityToStr(World world, int e) {
		fill.clear();
		sb.setLength(0);
		sb.append("Entity{");
		sb.append(e);
		sb.append("}[\n");
		world.getEntity(e).getComponents(fill);
		for (int i = 0; fill.size() > i; i++) {
			if (i > 0) sb.append(",\n");
			sb.append("  ");
			sb.append(fill.get(i));
		}
		sb.append("\n]");
		return sb.toString();
	}

	private void deleteAll(EntitySubscription sub) {
		IntBag entities = sub.getEntities();
		for (int i = 0; entities.size() > i; i++) {
			world.delete(entities.get(i));
		}
		world.process();
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.process();
	}

	public static class ComponentX extends Component {
		public String text;

		@Override public String toString () {
			return "ComponentX{" +
				"text='" + text + '\'' +
				'}';
		}
	}
	public static class ComponentY extends Component {
		public String text;

		@Override public String toString () {
			return "ComponentY{" +
				"text='" + text + '\'' +
				'}';
		}
	}
	public static class ComponentRef extends Component {
		@EntityId
		public int id;
		public Entity e;
		public ComponentX x;

		@Override public String toString () {
			return "ComponentRef{" +
				"id=" + id +
				", x=" + x +
				", e=" + e +
				'}';
		}
	}
	public static class ReusedComponent extends Component {
		public String text;

		@Override public String toString () {
			return "ReusedComponent{" +
				"text='" + text + '\'' +
				'}';
		}
	}
}
