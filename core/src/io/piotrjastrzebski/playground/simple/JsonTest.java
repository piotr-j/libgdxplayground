package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.particletest.ParticleFaceTest;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class JsonTest extends BaseScreen {
	private static final String TAG = JsonTest.class.getSimpleName();

	public JsonTest (GameReset game) {
		super(game);

		ObjectMap<String, Item> items = new ObjectMap<>();
		Item item1 = new Item("Item 1", "textures/tex1.png", 10, 12);
		Item item2 = new Item("Item 2", "textures/tex1.png", 10, 12);
		Item item3 = new Item("Item 3", "textures/tex1.png", 10, 12);
		Item item4 = new Item("Item 4", "textures/tex1.png", 10, 12);
		items.put(item1.getName(), item1);
		items.put(item2.getName(), item2);
		items.put(item3.getName(), item3);
		items.put(item4.getName(), item4);
		Gdx.app.log(TAG, items.get("Item 1").toString());

		Json json = new Json();
		String itemsJson = json.toJson(items);
		ObjectMap<String, Item> itemsFromJson = json.fromJson(ObjectMap.class, Item.class, itemsJson);
		Gdx.app.log(TAG, itemsFromJson.get("Item 1").toString());

		JsonReader reader = new JsonReader();
		JsonValue value = reader.parse(itemsJson);
		Gdx.app.log(TAG, value.get("Item 1").getString("name"));
	}

	public static class Item {

		private String name;
		private String texture;
		private int damageMin;
		private int damageMax;

		public Item () {
		}

		public Item (String name, String texture, int dmgMin, int dmgMax) {
			this.name = name;
			this.texture = texture;
			this.damageMin = dmgMin;
			this.damageMax = dmgMax;
		}

		public void setName (String n) {
			this.name = n;
		}

		public String getName () {
			return name;
		}

		public int getDamageMin () {
			return damageMin;
		}

		public void setDamageMin (int damageMin) {
			this.damageMin = damageMin;
		}

		public int getDamageMax () {
			return damageMax;
		}

		public void setDamageMax (int damageMax) {
			this.damageMax = damageMax;
		}

		@Override public String toString () {
			return "Item{" +
				"name='" + name + '\'' +
				", texture='" + texture + '\'' +
				", damageMin=" + damageMin +
				", damageMax=" + damageMax +
				'}';
		}
	}
	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, JsonTest.class);
	}
}
