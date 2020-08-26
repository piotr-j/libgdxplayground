package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 14/06/2016.
 */
public class UICoordsTest extends ApplicationAdapter implements InputProcessor {
	private final static String TAG = UICoordsTest.class.getSimpleName();
	public static float SCALE = 32f;
	public static float INV_SCALE = 1.f/ SCALE;
	public static float WIDTH = 1280 * INV_SCALE;
	public static float HEIGHT = 720 * INV_SCALE;

	OrthographicCamera gameCam;
	ExtendViewport gameVP;
	ShapeRenderer renderer;
	SpriteBatch batch;

	Stage stage;

	Texture badlogic;

	public UICoordsTest () {
		super();
	}

	@Override public void create () {
		super.create();
		gameCam = new OrthographicCamera();
		gameVP = new ExtendViewport(WIDTH, HEIGHT, gameCam);

		renderer = new ShapeRenderer();
		batch = new SpriteBatch();

		stage = new Stage(gameVP, batch);
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));

		stage.addListener(new InputListener(){
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				PLog.log("Stage touch down x = " + x + ", y = " + y);
				return true;
			}
		});

		badlogic = new Texture("badlogic.jpg");

		{ // bottom left
			Image image = new Image(badlogic);
			image.setPosition(1, 1);
			image.setScale(INV_SCALE);
			stage.addActor(image);
			image.addListener(new InputListener(){
				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					PLog.log("Image touch down x = " + x + ", y = " + y);
					return true;
				}
			});
		}

		float size = badlogic.getWidth() * INV_SCALE;
		stage.addActor(image(1, 1, Color.WHITE));
		stage.addActor(image(1, HEIGHT - 1 - size, Color.RED));
		stage.addActor(image(WIDTH - 1 - size, 1, Color.GREEN));
		stage.addActor(image(WIDTH - 1 - size, HEIGHT - 1 - size, Color.BLUE));
	}

	private Actor image (float ix, float iy, Color color) {
		Image image = new Image(badlogic);
		image.setPosition(ix, iy);
		image.setScale(INV_SCALE);
		image.setColor(color);
		stage.addActor(image);
		image.addListener(new InputListener(){
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				PLog.log("Image touch down x = " + x + ", y = " + y);

				{
					Vector2 sc = image.localToStageCoordinates(new Vector2(x, y));
					PLog.log("Image touch down stage coordinates x = " + sc.x + ", y = " + sc.y);
				}
				{
					Vector2 sc = image.localToScreenCoordinates(new Vector2(x, y));
					PLog.log("Image touch down screen coordinates x = " + sc.x + ", y = " + sc.y);
				}
				return true;
			}
		});
		return image;
	}

	@Override public void render () {
		Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

	}


	@Override public void resize (int width, int height) {
		super.resize(width, height);
		gameVP.update(width, height, true);
	}

	@Override public void dispose () {
		super.dispose();
		renderer.dispose();
		batch.dispose();
		badlogic.dispose();
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.ESCAPE) {
			Gdx.app.exit();
		}
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		PLog.log("raw touchDown x = " + screenX + ", y = " + screenY);
		return false;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		return false;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}

	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		PlaygroundGame.start(new UICoordsTest(), config);
	}
}
