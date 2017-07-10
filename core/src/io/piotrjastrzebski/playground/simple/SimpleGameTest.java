package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class SimpleGameTest extends Game {
	// we will use 32px/unit in world
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	// this is our "target" resolution, not that the window can be any size, it is not bound to this one
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;

	private ShapeRenderer shapes;
	private SpriteBatch batch;
	private final boolean reuseScreens = true;

	@Override public void create () {
		shapes = new ShapeRenderer();
		batch = new SpriteBatch();

		setScreenA();
	}

	private ScreenA screenA = null;
	void setScreenA () {
		if (reuseScreens) {
			if (screenA == null) screenA = new ScreenA(this);
			setScreen(screenA);
		} else {
			setScreen(new ScreenA(this));
		}
	}

	private ScreenB screenB = null;
	void setScreenB () {
		if (reuseScreens) {
			if (screenB == null) screenB = new ScreenB(this);
			setScreen(screenB);
		} else {
			setScreen(new ScreenB(this));
		}
	}

	@Override public void dispose () {
		super.dispose();
		batch.dispose();
		shapes.dispose();
	}

	static abstract class BaseScreen extends ScreenAdapter {
		protected SimpleGameTest game;
		protected ShapeRenderer shapes;
		protected SpriteBatch batch;
		protected OrthographicCamera camera;
		protected ExtendViewport viewport;

		public BaseScreen (SimpleGameTest game) {
			this.game = game;
			shapes = game.shapes;
			batch = game.batch;

			camera = new OrthographicCamera();
			viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera);
		}

		@Override public void resize (int width, int height) {
			viewport.update(width, height, false);
		}

		@Override public void hide () {
			if (!game.reuseScreens) {
				dispose();
			}
		}
	}

	static class ScreenA extends BaseScreen {
		Texture texture;
		TextureRegion region;
		public ScreenA (SimpleGameTest game) {
			super(game);
			texture = new Texture("badlogic.jpg");
			region = new TextureRegion(texture);
		}

		float rotation;
		@Override public void render (float delta) {
			Gdx.gl.glClearColor(1, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			rotation += 45 * delta;
			shapes.setProjectionMatrix(camera.combined);
			shapes.begin(ShapeRenderer.ShapeType.Filled);
			shapes.setColor(0, 1, 0, 1);
			shapes.rect(-6, -5, 5, 5, 10, 10, 1, 1, rotation);
			shapes.end();

			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			batch.setColor(0, 1, 0, 1);
			batch.draw(region, 6, -5, 5, 5, 10, 10, 1, 1, rotation);
			batch.end();

			if (Gdx.input.justTouched()) {
				game.setScreenB();
			}
		}

		@Override public void dispose () {
			texture.dispose();
		}
	}

	static class ScreenB extends BaseScreen {
		Texture texture;
		TextureRegion region;
		public ScreenB (SimpleGameTest game) {
			super(game);
			texture = new Texture("badlogic.jpg");
			region = new TextureRegion(texture);
		}

		float rotation;
		@Override public void render (float delta) {
			Gdx.gl.glClearColor(0, 1, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			rotation += 45 * delta;
			shapes.setProjectionMatrix(camera.combined);
			shapes.begin(ShapeRenderer.ShapeType.Filled);
			shapes.setColor(Color.RED);
			shapes.rect(6, -5, 5, 5, 10, 10, 1, 1, rotation);
			shapes.end();

			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			batch.setColor(1, 0, 0, 1);
			batch.draw(region, -6, -5, 5, 5, 10, 10, 1, 1, rotation);
			batch.end();

			if (Gdx.input.justTouched()) {
				game.setScreenA();
			}
		}

		@Override public void dispose () {
			texture.dispose();
		}
	}


	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		config.useHDPI = true;
		new LwjglApplication(new SimpleGameTest(), config);
	}
}
