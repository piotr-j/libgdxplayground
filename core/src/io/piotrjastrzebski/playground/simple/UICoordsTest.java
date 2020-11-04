package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 14/06/2016.
 */
public class UICoordsTest extends ApplicationAdapter implements InputProcessor {
	private final static String TAG = UICoordsTest.class.getSimpleName();
	public static float SCALE = 2f;
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
//		stage = new Stage(new ScreenViewport(), batch);
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));

		stage.addListener(new InputListener(){
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				PLog.log("Stage touch down x = " + x + ", y = " + y);
				return false;
			}
		});

		badlogic = new Texture("badlogic.jpg");

		float size = badlogic.getWidth() * INV_SCALE;

		Table root = new Table();
		root.setFillParent(true);
		stage.addActor(root);

		Table scrolled = new Table();
		scrolled.setTouchable(Touchable.childrenOnly);
		{
			Image image = new Image(badlogic);
			image.addListener(new ImageListener());
			scrolled.add(image).size(size).padRight(size * .5f);
		}
		{
			Image image = new Image(badlogic);
			image.addListener(new ImageListener());
			scrolled.add(image).size(size).row();
		}
		scrolled.add().height(size * .5f).row();
		{
			Image image = new Image(badlogic);
			image.addListener(new ImageListener());
			scrolled.add(image).size(size).padRight(size * .5f);
		}
		{
			Image image = new Image(badlogic);
			image.addListener(new ImageListener());
			scrolled.add(image).size(size);
		}

		// vis just for easy style
		VisUI.load();
		VisScrollPane pane = new VisScrollPane(scrolled);
		root.add(pane).size(size * 1.25f);

		float offset = 25;
		stage.addActor(image(offset, offset, Color.WHITE));
		stage.addActor(image(offset, HEIGHT - offset - size, Color.RED));
		stage.addActor(image(WIDTH - offset - size, offset, Color.GREEN));
		stage.addActor(image(WIDTH - offset - size, HEIGHT - offset - size, Color.BLUE));
	}

	private Actor image (float ix, float iy, Color color) {
		Image image = new Image(badlogic);
		image.setPosition(ix, iy);
		image.setScale(INV_SCALE);
		image.setColor(color);
		stage.addActor(image);
		image.addListener(new ImageListener());
		return image;
	}

	static class ImageListener extends InputListener {
		@Override
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
			PLog.log("Image touch down x = " + x + ", y = " + y);

			{
				Vector2 sc = event.getTarget().localToStageCoordinates(new Vector2(x, y));
				PLog.log("Image touch down stage coordinates x = " + sc.x + ", y = " + sc.y);
			}
			{
				Vector2 sc = event.getTarget().localToScreenCoordinates(new Vector2(x, y));
				PLog.log("Image touch down screen coordinates x = " + sc.x + ", y = " + sc.y);
			}
			return false;
		}
	}

	@Override public void render () {
		Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		HdpiUtils.glScissor(0, 0, (int)(Gdx.graphics.getWidth() * .85f), (int)(Gdx.graphics.getHeight() * .75f));
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

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
		PLog.log("------- -------------- -------");
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

	@Override
	public boolean scrolled (float amountX, float amountY) {
		return false;
	}

	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		PlaygroundGame.start(new UICoordsTest(), config);
	}
}
