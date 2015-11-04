package io.piotrjastrzebski.playground.ecs;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class AshleyPhysicsTest extends BaseScreen {
	private final static String TAG = ECSOrderTest.class.getSimpleName();
	Engine engine;
	World world;

	public AshleyPhysicsTest (GameReset game) {
		super(game);
		engine = new Engine();
		world = new World(new Vector2(), true);
		engine.addSystem(new PhysicsSystem());
		Entity entity = new Entity();
		entity.add(new PhysicsComp());
		engine.addEntity(entity);
		engine.update(1);
		entity.remove(PhysicsComp.class);
		engine.update(1);
		entity = new Entity();
		entity.add(new PhysicsComp());
		engine.addEntity(entity);
		engine.update(1);
		engine.removeEntity(entity);
		engine.update(1);
	}

	public static class PhysicsComp implements Component {
		public String welp = "welp";

		@Override public String toString () {
			return "PhysicsComp{"+welp+"}";
		}
	}

	public static class PhysicsSystem extends EntitySystem implements EntityListener {

		public PhysicsSystem () {

		}

		Family family;
		@Override public void addedToEngine (Engine engine) {
			family = Family.all(PhysicsComp.class).get();
			engine.addEntityListener(family, this);
		}

		@Override public void entityAdded (Entity entity) {
			PhysicsComp component = entity.getComponent(PhysicsComp.class);
			Gdx.app.log("added", "phys " + component);
		}

		@Override public void entityRemoved (Entity entity) {
			PhysicsComp component = entity.getComponent(PhysicsComp.class);
			Gdx.app.log("removed", "phys " + component);
		}
	}

	@Override public void dispose () {
		super.dispose();
		world.dispose();
	}
}
