package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Self contained test for proper touch/mouse handling
 *
 * Created by PiotrJ on 05/10/15.
 */
public class ShapeRotateTest extends ApplicationAdapter {
	// we will use 32px/unit in world
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	// this is our "target" resolution, not that the window can be any size, it is not bound to this one
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;

	private OrthographicCamera camera;
	private ExtendViewport viewport;
	private ShapeRenderer shapes;

	@Override public void create () {
		camera = new OrthographicCamera();
		// pick a viewport that suits your thing, ExtendViewport is a good start
		viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera);
		// ShapeRenderer so we can see our touch point
		shapes = new ShapeRenderer();
	}

	float deg;
	@Override public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shapes.setProjectionMatrix(camera.combined);
		shapes.begin(ShapeRenderer.ShapeType.Filled);
		float rectW = 10;
		float rectH = 10;
		shapes.rect(
			VP_WIDTH/2 - rectW/2, VP_HEIGHT/2 - rectH/2,
			rectW/2, rectH/2,
			rectW, rectH,
			1, 1,
			deg += Gdx.graphics.getDeltaTime() * 90
		);
		shapes.end();
	}


	@Override public void resize (int width, int height) {
		// viewport must be updated for it to work properly
		viewport.update(width, height, true);
	}

	@Override public void dispose () {
		// disposable stuff must be disposed
		shapes.dispose();
	}


	public static void main (String[] arg) {
		PlaygroundGame.start(new ShapeRotateTest());
	}
}
