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

		Item item1 = new Item("Item 1", "textures/tex1.png", 10, 12);
		Gdx.app.log(TAG, item1.toString());

		Json json = new Json(JsonWriter.OutputType.javascript);
		String item1Json = json.toJson(item1);
		Item item2 = json.fromJson(Item.class, item1Json);
		Gdx.app.log(TAG, item2.toString());

		JsonReader reader = new JsonReader();
		JsonValue value = reader.parse(item1Json);
		Gdx.app.log(TAG, value.getString("name"));
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
