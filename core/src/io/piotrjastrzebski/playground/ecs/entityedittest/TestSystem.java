package io.piotrjastrzebski.playground.ecs.entityedittest;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;

/**
 * Created by EvilEntity on 09/07/2015.
 */
@Wire
public class TestSystem extends EntityProcessingSystem {

	public TestSystem () {
		super(Aspect.all(TestComponentA.class, TestComponentB.class));
	}

	@Override protected void inserted (Entity e) {
		Gdx.app.log("Inserted","" + EntityEditTest.entityToStr(e));
	}

	@Override protected void process (Entity e) {
		Gdx.app.log("Process","" + EntityEditTest.entityToStr(e));

	}
	private ComponentMapper<TestComponentA> mTestComponentA;
	private ComponentMapper<TestComponentB> mTestComponentB;
	@Override protected void removed (Entity e) {
		Gdx.app.log("Removed","" + EntityEditTest.entityToStr(e));
		Gdx.app.log("R", "A: " + mTestComponentA.get(e));
		Gdx.app.log("R", "B: " + mTestComponentB.get(e));
	}
}
