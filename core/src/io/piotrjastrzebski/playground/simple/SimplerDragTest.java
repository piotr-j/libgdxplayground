package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Self contained test for proper touch/mouse handling
 *
 * Created by PiotrJ on 05/10/15.
 */
public class SimplerDragTest extends ApplicationAdapter implements InputProcessor {
	// we will use 32px/unit in world
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	// this is our "target" resolution, not that the window can be any size, it is not bound to this one
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;

	private OrthographicCamera camera;
	private ExtendViewport viewport;
	private ShapeRenderer shapes;
	private SpriteBatch batch;
	private Texture texture;
	private TextureRegion textureRegion;
	private Stage stage;
	private Image image;

	@Override public void create () {
		camera = new OrthographicCamera();
		// pick a viewport that suits your thing, ExtendViewport is a good start
		viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera);
		// ShapeRenderer so we can see our touch point
		shapes = new ShapeRenderer();
		batch = new SpriteBatch();
		texture = new Texture(Gdx.files.internal("badlogic.jpg"));
		textureRegion = new TextureRegion(texture);
		stage = new Stage(new ScreenViewport(), batch);
		image = new Image(textureRegion);
		image.setSize(64, 64);

		stage.addActor(image);
		image.addListener(new ActorGestureListener() {
			@Override
			public void pan (InputEvent event, float x, float y, float deltaX, float deltaY) {
				image.setPosition(image.getX() + deltaX, image.getY() + deltaY);
			}
		});

		Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
	}

	@Override public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shapes.setProjectionMatrix(camera.combined);
		shapes.begin(ShapeRenderer.ShapeType.Filled);
		shapes.rect(tp.x - .5f, tp.y - .5f, 1, 1);
		shapes.end();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(textureRegion, tp.x - .5f, tp.y - .5f, 1, 1);
		batch.end();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

		System.out.println("FPS:\t" + Gdx.graphics.getFramesPerSecond());
	}

	Vector3 tp = new Vector3();
	boolean dragging;
	@Override public boolean mouseMoved (int screenX, int screenY) {
		// we can also handle mouse movement without anything pressed
//		camera.unproject(tp.set(screenX, screenY, 0));
		return false;
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		// ignore if its not left mouse button or first touch pointer
		if (button != Input.Buttons.LEFT || pointer > 0) return false;
		camera.unproject(tp.set(screenX, screenY, 0));
		dragging = true;
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		if (!dragging) return false;
		camera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT || pointer > 0) return false;
		camera.unproject(tp.set(screenX, screenY, 0));
		dragging = false;
		return true;
	}

	@Override public void resize (int width, int height) {
		// viewport must be updated for it to work properly
		viewport.update(width, height, true);
		stage.getViewport().update(width, height, true);
	}

	@Override public void dispose () {
		// disposable stuff must be disposed
		shapes.dispose();
		batch.dispose();
		texture.dispose();
	}


	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override
	public boolean scrolled (float amountX, float amountY) {
		return false;
	}

	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
//		config.useVsync(false);
		PlaygroundGame.start(new SimplerDragTest(), config);
	}
}
