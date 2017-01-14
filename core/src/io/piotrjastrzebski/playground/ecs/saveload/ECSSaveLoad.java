package io.piotrjastrzebski.playground.ecs.saveload;

import com.artemis.*;
import com.artemis.annotations.Wire;
import com.artemis.io.JsonArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * We want to figure out a way to cleanly start a new game or load existing save
 *
 * the problem is when new game starts, we need to create a bunch of entities for basic things
 */
public class ECSSaveLoad extends BaseScreen {
	private final static String TAG = ECSSaveLoad.class.getSimpleName();
	private final static String PREFS_GLOBAL = "GLOBAL";
	private final static String PREFS_CURRENT_GAME = "CURRENT_GAME";

	private Preferences prefsGlobal;
	private Preferences prefsSave;
	private World world;
	private Array<LifeCycle> lifeCycles = new Array<>();
	private SaveData save;

	public ECSSaveLoad (GameReset game) {
		super(game);
		prefsGlobal = Gdx.app.getPreferences("ECSSaveLoad");

		boolean newGame = false;
		// now we initialize new world if needed
		int save_id = prefsGlobal.getInteger("save_id", -1);
		if (save_id == -1) {
			newGame = true;
			save_id = 0;
			prefsGlobal.putInteger("save_id", save_id);
			prefsGlobal.flush();
		}
		prefsSave = Gdx.app.getPreferences("ECSSaveLoad."+save_id);

		// we need to setup world either way
		WorldConfiguration config = new WorldConfiguration();
		config.register(PREFS_GLOBAL, prefsGlobal);
		config.register(PREFS_CURRENT_GAME, prefsSave);
		config.register(save = new SaveData());

		config.setSystem(new WorldSerializationManager());
		config.setSystem(new LCSystem());
		config.setSystem(new LCAssets());
		config.setSystem(new LCRenderer());

		world = new World(config);
		for (BaseSystem system : world.getSystems()) {
			if (system instanceof LifeCycle) {
				lifeCycles.add((LifeCycle)system);
			}
		}

		// initialize has been called at this point
		if (newGame) {
			// clear any existing data just in case
			prefsSave.clear();
			prefsSave.flush();
			for (LifeCycle lifeCycle : lifeCycles) {
				lifeCycle.create();
			}
		}
		// and load the previous state, its fine if its a new game
		for (LifeCycle lifeCycle : lifeCycles) {
			lifeCycle.load();
		}
	}

	@Override public void render (float delta) {
		super.render(delta);

		world.delta = delta;
		world.process();

	}

	@Override public void pause () {
		for (LifeCycle lifeCycle : lifeCycles) {
			lifeCycle.save();
			lifeCycle.pause();
		}
		prefsGlobal.flush();
		prefsSave.flush();
	}

	@Override public void resume () {
		for (LifeCycle lifeCycle : lifeCycles) {
			lifeCycle.resume();
		}
	}

	@Override public void dispose () {
		super.dispose();
		world.dispose();
	}

	interface LifeCycle {
		// called when _new_ game is created,
		void create();
		void resume();
		void pause();
		// called when old state should be loaded, if exists
		void load();
		// called when old state was loaded
//		void loaded();
		void save();
	}

	public static class LCPosition extends Component {
		public float x;
		public float y;
	}

	public static class LCAsset extends Component {
		public String path;
		public Color tint = new Color(Color.WHITE);
		public transient TextureRegion region;
	}

	static class LCAssets extends BaseEntitySystem {
		protected ComponentMapper<LCAsset> mLCAsset;
		private Texture texture;
		public LCAssets () {
			super(Aspect.all(LCAsset.class));
		}

		@Override protected void initialize () {
			texture = new Texture("badlogic.jpg");
		}

		@Override protected void inserted (int entityId) {
			LCAsset asset = mLCAsset.get(entityId);
			asset.region = new TextureRegion(texture);
		}

		@Override protected void processSystem () {

		}

		@Override protected void dispose () {
			texture.dispose();
		}
	}

	static class LCRenderer extends IteratingSystem {
		SpriteBatch batch;
		protected ComponentMapper<LCPosition> mLCPosition;
		protected ComponentMapper<LCAsset> mLCAsset;

		public LCRenderer () {
			super(Aspect.all(LCPosition.class, LCAsset.class));
		}

		@Override protected void initialize () {
			batch = new SpriteBatch();
		}

		@Override protected void begin () {
			batch.begin();
		}

		@Override protected void process (int entityId) {
			LCPosition pos = mLCPosition.get(entityId);
			LCAsset asset = mLCAsset.get(entityId);
			batch.setColor(asset.tint);
			batch.draw(asset.region, pos.x, pos.y);
		}

		@Override protected void end () {
			batch.end();
		}
	}

	static class LCSystem extends BaseSystem implements LifeCycle {
		private static final String TAG = LCSystem.class.getSimpleName();
		private final static String CHARSET = "UTF-8";
		@Wire(name = PREFS_CURRENT_GAME) Preferences prefs;
		@Wire SaveData saveData;
		@Wire WorldSerializationManager wsm;

		@Override protected void initialize () {
			JsonArtemisSerializer serializer = new JsonArtemisSerializer(world);
			serializer.prettyPrint(true);
			serializer.setUsePrototypes(false);
			serializer.register(Color.class, new Json.Serializer<Color>() {
				@Override public void write (Json json, Color object, Class knownType) {
					json.writeValue(Color.rgba8888(object), int.class);
				}

				@Override public Color read (Json json, JsonValue jsonData, Class type) {
					return new Color(jsonData.asInt());
				}
			});
			wsm.setSerializer(serializer);
		}

		@Override protected void processSystem () {

		}

		@Override public void create () {
			Gdx.app.log(TAG, "create");
			for (int i = 0; i < 20; i++) {
				EntityEdit edit = world.edit(world.create());
				edit.create(LCAsset.class).tint.set(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
				LCPosition pos = edit.create(LCPosition.class);
				pos.x = MathUtils.random(0, Gdx.graphics.getWidth() - 256);
				pos.y = MathUtils.random(0, Gdx.graphics.getHeight() - 256);
			}
		}

		@Override public void resume () {
			Gdx.app.log(TAG, "resume");
		}

		@Override public void pause () {
			Gdx.app.log(TAG, "pause");
		}

		@Override public void save () {
			Gdx.app.log(TAG, "save");

			final EntitySubscription allEntities = world.getSystem(AspectSubscriptionManager.class).get(Aspect.all());

			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
				saveData.init(allEntities.getEntities());
				wsm.save(baos, saveData);
				prefs.putString("save_data", baos.toString(CHARSET));
				prefs.flush();
			} catch (Exception e) {
				Gdx.app.error(TAG, "Save Failed", e);
			}

		}

		@Override public void load () {
			Gdx.app.log(TAG, "load");

			try {
				final String json = prefs.getString("save_data", null);
				if (json != null) {
					final ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes(CHARSET));
					saveData = world.getSystem(WorldSerializationManager.class).load(is, SaveData.class);
				}
			} catch (Exception e) {
				Gdx.app.error(TAG, "Load Failed", e);
			}
		}

		@Override protected void dispose () {
			Gdx.app.log(TAG, "dispose");
		}
	}

	static class SaveData extends SaveFileFormat {
		protected ObjectMap<String, Object> extras = new ObjectMap<>();
		public void init (IntBag entities) {
			this.entities = (entities != null) ? entities : new IntBag();
			componentIdentifiers = new ComponentIdentifiers();
			metadata = new Metadata();
			metadata.version = Metadata.LATEST;
		}

		public void putExtra(String name, Object value) {
			extras.put(name, value);
		}

		public Object getExtra(String name, Object defValue) {
			return extras.get(name, defValue);
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, ECSSaveLoad.class);
	}
}
