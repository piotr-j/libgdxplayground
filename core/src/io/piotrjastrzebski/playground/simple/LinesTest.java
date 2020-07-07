package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.piotrjastrzebski.playground.PlaygroundGame;

public class LinesTest extends ApplicationAdapter {
	ScreenViewport viewport;
	Camera camera;
	ShapeRenderer shapes;

	@Override public void create () {
		viewport = new ScreenViewport(camera = new OrthographicCamera());
		shapes = new ShapeRenderer();
	}

	@Override public void render () {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// pixel perfect viewport
		shapes.setProjectionMatrix(camera.combined);
		shapes.begin(ShapeRenderer.ShapeType.Line);
		float cy = viewport.getWorldHeight()/2;
		for (int i = 0; i <= viewport.getWorldWidth(); i++){
			int off = MathUtils.random(25, 100);
			shapes.line(i, cy - off, i, cy + off);
		}
		shapes.end();
	}

	@Override public void resize (int width, int height) {
		viewport.update(width, height, true);
	}

	@Override public void dispose () {
		shapes.dispose();
	}

	public static void main (String[] arg) {
		PlaygroundGame.start(new LinesTest());
	}
}
