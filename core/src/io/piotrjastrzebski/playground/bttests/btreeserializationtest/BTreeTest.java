package io.piotrjastrzebski.playground.bttests.btreeserializationtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.StreamUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.bttests.btreeserializationtest.dog.Dog;

import java.io.Reader;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class BTreeTest extends BaseScreen {

	private MyBTree<Dog> dogBehaviorTreeA;
	private MyBTree<Dog> dogBehaviorTreeB;

	Json json;
	BehaviorTree<Dog> dogBehaviorTreeArchetype;
	public BTreeTest (GameReset game) {
		super(game);


		dogBehaviorTreeArchetype = null;

		Reader reader = null;
		try {
			reader = Gdx.files.internal("btree/dog.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE) {
				protected BehaviorTree<Dog> createBehaviorTree (Task<Dog> root, Dog object) {
					if (debug > BehaviorTreeParser.DEBUG_LOW) printTree(root, 0);
					return new MyBTree<>(root, object);
				}
			};
			dogBehaviorTreeArchetype = parser.parse(reader, null);
		} finally {
			StreamUtils.closeQuietly(reader);
		}

		if (dogBehaviorTreeArchetype != null) {
			dogBehaviorTreeA = (MyBTree<Dog>)dogBehaviorTreeArchetype.cloneTask();
			dogBehaviorTreeA.setObject(new Dog("Dog A"));

			dogBehaviorTreeB = (MyBTree<Dog>)dogBehaviorTreeArchetype.cloneTask();
			dogBehaviorTreeB.setObject(new Dog("Dog B"));
		}

		json = new Json();
		json.setOutputType(JsonWriter.OutputType.json);
		json.setUsePrototypes(false);

		json.setSerializer(BehaviorTree.class, new Json.Serializer<BehaviorTree>() {
			@Override public void write (Json json, BehaviorTree object, Class knownType) {
				Gdx.app.log("", "tree: " + object);
			}

			@Override public BehaviorTree read (Json json, JsonValue jsonData, Class type) {
				return null;
			}
		});

		json.setSerializer(MyBTree.class, new Json.Serializer<MyBTree>() {
			@Override public void write (Json json, MyBTree tree, Class knownType) {
				Gdx.app.log("", "my tree: "+tree);
				// what do we need to save here?
				// probably some id of the tree, ie dog.tree
				// running stuff only? amd running stuff of that etc?

				Task runningTask = tree.getRunningTask();
				Gdx.app.log("", "running task "+runningTask);
				for (int i = 0; i < tree.getChildCount(); i++) {
					Task child = tree.getChild(i);
					Gdx.app.log("", "child task "+child);
					if (child == runningTask) {
						Gdx.app.log("", "running id: " + i);
					}
				}
				Gdx.app.log("", "end");
			}

			@Override public MyBTree read (Json json, JsonValue jsonData, Class type) {
				// how does reading work?
				// must be cast to correct type at some point, and object set
				// String path = json.readString()...;
				// MyBTree tree = trees.obtain(path);

				// lets pretend this is correct tree
				MyBTree myBTree = (MyBTree)dogBehaviorTreeArchetype.cloneTask();

				// now we need to read the json shit and figure out what should run?

				return myBTree;
			}
		});

		json.setSerializer(Task.class, new Json.Serializer<Task>() {
			@Override public void write (Json json, Task object, Class knownType) {
				Gdx.app.log("", "Task: "+object);
			}

			@Override public Task read (Json json, JsonValue jsonData, Class type) {
				return null;
			}
		});
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

	String jsonA;
	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.F5:
			Gdx.app.log("", "save");
//			Task<Dog> runningTask = dogBehaviorTreeA.getRunningTask();
			jsonA = json.toJson(dogBehaviorTreeA);
			Gdx.app.log("", json.prettyPrint(jsonA));

			break;
		case Input.Keys.F9:
			Gdx.app.log("", "load");
			dogBehaviorTreeA = json.fromJson(MyBTree.class, jsonA);
			break;
		}
		return super.keyDown(keycode);
	}
}
