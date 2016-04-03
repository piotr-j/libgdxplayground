package io.piotrjastrzebski.playground.simple;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class AshleyRemoveTest extends BaseScreen {
	private static final String TAG = AshleyRemoveTest.class.getSimpleName();

	public AshleyRemoveTest (GameReset game) {
		super(game);
		Engine engine = new Engine();

		for(int i = 1; i <= 5; i++){
			Entity entity = new Entity();
			StringComponent sc = new StringComponent();
			sc.name = "Entity #: " + i;
			entity.add(sc);
			System.out.println("Created: " + sc.name);
			engine.addEntity(entity);
		}

		engine.update(1);

		ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(StringComponent.class).get());
		ComponentMapper<StringComponent> sm = ComponentMapper.getFor(StringComponent.class);
		System.out.println("//======");

		for(Entity e: entities){
			StringComponent sc = sm.get(e);
			System.out.println("Removed: " + sc.name);
			engine.removeEntity(e);
		}
	}

	protected class StringComponent implements Component {
		public String name;
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	@Override public void resize (int width, int height) {
		gameViewport.update(width, height, true);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, AshleyRemoveTest.class);
	}
}
