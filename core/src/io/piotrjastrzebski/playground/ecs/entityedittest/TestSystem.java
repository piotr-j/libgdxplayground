package io.piotrjastrzebski.playground.ecs.entityedittest;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;

/**
 * Created by EvilEntity on 09/07/2015.
 */
@Wire
public class TestSystem extends IteratingSystem {

	public TestSystem () {
		super(Aspect.all(TestComponentA.class, TestComponentB.class));
	}

	@Override protected void inserted (int e) {
		Gdx.app.log("Inserted","" + EntityEditTest.entityToStr(world, e));
	}

	@Override protected void process (int e) {
		Gdx.app.log("Process","" + EntityEditTest.entityToStr(world, e));

	}
	private ComponentMapper<TestComponentA> mTestComponentA;
	private ComponentMapper<TestComponentB> mTestComponentB;
	@Override protected void removed (int e) {
		Gdx.app.log("Removed","" + EntityEditTest.entityToStr(world, e));
		Gdx.app.log("R", "A: " + mTestComponentA.get(e));
		Gdx.app.log("R", "B: " + mTestComponentB.get(e));
	}
}
