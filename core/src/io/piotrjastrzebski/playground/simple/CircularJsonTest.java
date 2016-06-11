package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

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
		public static String REF_ID_KEY = "_id";
		protected static Array serialized = new Array<>();
		protected static Array deserialized = new Array<>();
		protected Class<?> cls;

		public CircularSerializer (Class<?> cls) {
			this.cls = cls;
			instances.add(this);
			Field[] fields = ClassReflection.getDeclaredFields(cls);
			for (Field field : fields) {
				if (field.getName().equals(REF_ID_KEY)) {
					throw new AssertionError("Field name in class " + cls.getSimpleName() + " collides with internal key name: " + REF_ID_KEY);
				}
			}
		}

		@Override public void write (Json json, T object, Class knownType) {
			int index = serialized.indexOf(object, true);
			if (index != -1) {
				json.writeObjectStart();
				json.writeValue(REF_ID_KEY, index);
				json.writeObjectEnd();
			} else {
				serialized.add(object);
				json.writeObjectStart();
				json.writeFields(object);
				json.writeObjectEnd();
			}
		}

		@Override public T read (Json json, JsonValue jsonData, Class type) {
			if (jsonData.has(REF_ID_KEY)) {
				int index = jsonData.getInt(REF_ID_KEY);
				return (T)deserialized.get(index);
			} else {
				try {
					T t = (T)ClassReflection.newInstance(cls);
					deserialized.add(t);
					json.readFields(t, jsonData);
					return t;
				} catch (ReflectionException e) {
					throw new GdxRuntimeException(e);
				}
			}
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
