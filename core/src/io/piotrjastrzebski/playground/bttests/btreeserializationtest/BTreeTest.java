package io.piotrjastrzebski.playground.bttests.btreeserializationtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.utils.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.OutputChunked;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.bttests.dog.Dog;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.Reader;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class BTreeTest extends BaseScreen {

	private BehaviorTree<Dog> dogBehaviorTreeA;
	private BehaviorTree<Dog> dogBehaviorTreeB;

	BehaviorTree<Dog> dogBehaviorTreeArchetype;

	public BTreeTest (GameReset game) {
		super(game);


		dogBehaviorTreeArchetype = null;

		Reader reader = null;
		try {
			reader = Gdx.files.internal("btree/dog.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE) {
				protected BehaviorTree<Dog> createBehaviorTree (Task<Dog> root, Dog object) {
					if (debugLevel > BehaviorTreeParser.DEBUG_LOW) printTree(root, 0);
					return new BehaviorTree<>(root, object);
				}
			};
			dogBehaviorTreeArchetype = parser.parse(reader, null);
		} finally {
			StreamUtils.closeQuietly(reader);
		}

		KryoUtils.initKryo();

		if (dogBehaviorTreeArchetype != null) {
			dogBehaviorTreeA = (BehaviorTree<Dog>)dogBehaviorTreeArchetype.cloneTask();
			dogBehaviorTreeA.setObject(new Dog("Dog A"));

			dogBehaviorTreeB = (BehaviorTree<Dog>)dogBehaviorTreeArchetype.cloneTask();
			dogBehaviorTreeB.setObject(new Dog("Dog B"));
		}
	}

	float elapsedTime;
	int step;
	@Override public void render (float delta) {
		super.render(delta);

		elapsedTime += delta;

		if (elapsedTime > 1) {
			System.out.println("\nStep: " + (++step));
			dogBehaviorTreeA.step();
			dogBehaviorTreeB.step();
			elapsedTime -= 1;
		}
	}

	public void save () {
		Array<BehaviorTree.Listener<Dog>> listeners = dogBehaviorTreeA.listeners;
		dogBehaviorTreeA.listeners = null;

		KryoUtils.save(new SaveObject<>(dogBehaviorTreeA, step));

		dogBehaviorTreeA.listeners = listeners;

	}

	public void load () {
		@SuppressWarnings("unchecked")
		SaveObject<Dog> saveObject = KryoUtils.load(SaveObject.class);
		BehaviorTree<Dog> oldTree = dogBehaviorTreeA;
		dogBehaviorTreeA = saveObject.tree;
		dogBehaviorTreeA.listeners = oldTree.listeners;

		step = saveObject.step;
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.F5:
			Gdx.app.log("", "save");
			save();
			break;
		case Input.Keys.F9:
			Gdx.app.log("", "load");
			load();
			break;
		}
		return super.keyDown(keycode);
	}

	static class SaveObject<T> {
		BehaviorTree<T> tree;
		int step;

		SaveObject (BehaviorTree<T> tree, int step) {
			this.tree = tree;
			this.step = step;
		}
	}

	public static final class KryoUtils {

		private static Kryo kryo;
		private static final OutputChunked output = new OutputChunked();

		private KryoUtils () {
		}

		public static void initKryo () {
			if (kryo == null) {
				kryo = new Kryo();
				kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
				kryo.register(BehaviorTree.class);
				// FieldSerializer fieldSerializer = new FieldSerializer(kryo, BehaviorTree.class);
				// fieldSerializer.removeField("object");
				// kryo.register(BehaviorTree.class, fieldSerializer);
			}
		}

		public static void save (Object obj) {
			output.clear();
			kryo.writeObjectOrNull(output, obj, obj.getClass());
			// System.out.println(output.total());
		}

		public static <T> T load (Class<T> type) {
			com.esotericsoftware.kryo.io.Input input = new ByteBufferInput(output.getBuffer());
			return kryo.readObjectOrNull(input, type);
		}
	}
}
