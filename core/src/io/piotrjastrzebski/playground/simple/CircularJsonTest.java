package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import com.badlogic.gdx.ai.utils.random.ConstantIntegerDistribution;
import com.badlogic.gdx.ai.utils.random.TriangularIntegerDistribution;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.bttests.dog.*;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class CircularJsonTest extends BaseScreen {
	private static final String TAG = CircularJsonTest.class.getSimpleName();

	public CircularJsonTest (GameReset game) {
		super(game);

		Root root = new Root();
		A a1 = new A();
		a1.data = "A1";
		A a2 = new A();
		a2.data = "A2";
		a1.a = a2;
		a2.a = a1;
		B b1 = new B();
		b1.data = "B1";
		B b2 = new B();
		b2.data = "B2";
		b1.b = b2;
		b2.b = b1;

		a1.b = b1;
		b1.a = a1;

		a2.b = b2;
		b2.a = a2;

		root.a1 = a1;
		root.b1 = b1;
		root.a2 = a2;
		root.b2 = b2;

		Json json = new Json();
		json.setSerializer(A.class, new CircularSerializer<A>(A.class));
		json.setSerializer(B.class, new CircularSerializer<B>(B.class));
		String jsonRoot = json.toJson(root);
		CircularSerializer.resetAll();
		System.out.println(jsonRoot);

		Root fromJson = json.fromJson(Root.class, jsonRoot);
		CircularSerializer.resetAll();
		System.out.println(fromJson);
		if (fromJson.a1 != fromJson.b1.a)
			System.err.println("a1 != b1.a");
		if (fromJson.a1 != fromJson.a2.a)
			System.err.println("a1 != a2.a");
		if (fromJson.b1 != fromJson.a1.b)
			System.err.println("b1 != a1.b");
		if (fromJson.b1 != fromJson.b2.b)
			System.err.println("b1 != b2.b");

		testBT();
	}

	private void testBT () {
		Selector<Dog> selector = new Selector<>();

		Parallel<Dog> parallel = new Parallel<>();
		selector.addChild(parallel);

		CareTask care = new CareTask();
		care.urgentProb = 0.8f;
		parallel.addChild(care);
		parallel.addChild(new AlwaysFail<>(new RestTask()));

		Sequence<Dog> sequence = new Sequence<>();
		selector.addChild(sequence);

		BarkTask bark1 = new BarkTask();
		bark1.times = new TriangularIntegerDistribution(1, 5, 2);
		sequence.addChild(bark1);
		sequence.addChild(new WalkTask());
		sequence.addChild(new BarkTask());
		sequence.addChild(new MarkTask());

		BehaviorTree<Dog> bt = new BehaviorTree<>(selector);
//		BehaviorTree<Dog> bt = new BehaviorTree<>(bark1);
		bt.setObject(new Dog("welp!"));
		Json json = new Json();
		json.setSerializer(BehaviorTree.class, new CircularSerializer<BehaviorTree>(BehaviorTree.class) {
			@Override protected void writeFields (Json json, BehaviorTree object) {
				json.writeField(object, "rootTask");
				json.writeField(object, "object");
			}

			@Override protected void readFields (BehaviorTree object, Json json, JsonValue jsonData) {
				json.readField(object, "rootTask", jsonData);
				json.readField(object, "object", jsonData);
			}
		});
		json.setSerializer(TriangularIntegerDistribution.class, new Json.Serializer<TriangularIntegerDistribution>() {
			@Override public void write (Json json, TriangularIntegerDistribution object, Class knownType) {
				json.writeObjectStart();
				json.writeType(TriangularIntegerDistribution.class);
				json.writeValue("low", object.getLow());
				json.writeValue("high", object.getHigh());
				json.writeValue("mode", object.getMode());
				json.writeObjectEnd();
			}

			@Override public TriangularIntegerDistribution read (Json json, JsonValue jsonData, Class type) {
				return new TriangularIntegerDistribution(jsonData.getInt("low", 0),jsonData.getInt("high", 0),jsonData.getFloat("mode", 0));
			}
		});
		json.setSerializer(ConstantIntegerDistribution.class, new Json.Serializer<ConstantIntegerDistribution>() {
			@Override public void write (Json json, ConstantIntegerDistribution object, Class knownType) {
				json.writeValue(object.getValue());
			}

			@Override public ConstantIntegerDistribution read (Json json, JsonValue jsonData, Class type) {
				return new ConstantIntegerDistribution(jsonData.asInt());
			}
		});
		json.setSerializer(Dog.class, new Json.Serializer<Dog>() {
			@Override public void write (Json json, Dog object, Class knownType) {
				json.writeObjectStart();
				json.writeType(Dog.class);
				json.writeFields(object);
				json.writeObjectEnd();
			}

			@Override public Dog read (Json json, JsonValue jsonData, Class type) {
				Dog dog = new Dog("");
				json.readFields(dog, jsonData);
				return dog;
			}
		});

		String jsonBt = json.toJson(bt);
		CircularSerializer.resetAll();
//		System.out.println(jsonBt);
		System.out.println(json.prettyPrint(jsonBt));

		BehaviorTree<Dog> fromJson = json.fromJson(BehaviorTree.class, jsonBt);
		CircularSerializer.resetAll();

	}

	public static class Root {
		public A a1;
		public B b1;
		public A a2;
		public B b2;

		@Override public String toString () {
			return "Root{" +
				"a1=" + a1 +
				", b1=" + b1 +
				"a2=" + a2 +
				", b2=" + b2 +
				'}';
		}
	}

	public static class A {
		public String data;
		public A a;
		public B b;

		@Override public String toString () {
			return "A{" +
				"data='" + data + '\'' +
				'}';
		}
	}

	public static class B {
		public String data;
		public A a;
		public B b;

		@Override public String toString () {
			return "B{" +
				"data='" + data + '\'' +
				'}';
		}

	}

	public static class CircularSerializer<T> implements Json.Serializer<T> {
		public static final Array<CircularSerializer> instances = new Array<>();
		protected static Array serialized = new Array<>();
		protected static Array deserialized = new Array<>();
		protected Class<?> cls;

		public CircularSerializer (Class<?> cls) {
			this.cls = cls;
			instances.add(this);
		}

		@Override public final void write (Json json, T object, Class knownType) {
			int index = serialized.indexOf(object, true);
			if (index != -1) {
				json.writeValue(index);
			} else {
				serialized.add(object);
				json.writeObjectStart();
				writeFields(json, object);
				json.writeObjectEnd();
			}
		}

		protected void writeFields (Json json, T object) {
			json.writeFields(object);
		}

		@Override public final T read (Json json, JsonValue jsonData, Class type) {
			if (jsonData.isLong()) {
				return (T)deserialized.get(jsonData.asInt());
			} else {
				try {
					T object = (T)ClassReflection.newInstance(cls);
					deserialized.add(object);
					readFields(object, json, jsonData);
					return object;
				} catch (ReflectionException e) {
					throw new GdxRuntimeException(e);
				}
			}
		}

		protected void readFields (T object, Json json, JsonValue jsonData) {
			json.readFields(object, jsonData);
		}

		public void reset() {
			serialized.clear();
			deserialized.clear();
		}

		public static void resetAll(){
			for (CircularSerializer instance : instances) {
				instance.reset();
			}
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, CircularJsonTest.class);
	}
}
