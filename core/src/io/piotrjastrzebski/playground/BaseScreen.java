package io.piotrjastrzebski.playground;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public abstract class BaseScreen implements Screen, InputProcessor {
	private final static String TAG = "BaseScreen";
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;

	protected OrthographicCamera gameCamera;
	protected OrthographicCamera guiCamera;
	protected ExtendViewport gameViewport;
	protected ScreenViewport guiViewport;

	protected SpriteBatch batch;
	protected ShapeRenderer renderer;

	protected Stage stage;
	protected Table root;
	protected Skin skin;

	protected final InputMultiplexer multiplexer;

	boolean debugStage;
	GameReset game;
	public BaseScreen (GameReset game) {
		this.game = game;

		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);
		guiCamera = new OrthographicCamera();
		guiViewport = new ScreenViewport(guiCamera);

		batch = new SpriteBatch();
		renderer = new ShapeRenderer();

		skin = new Skin(Gdx.files.internal("gui/uiskin.json"));
		stage = new Stage(guiViewport, batch);
		stage.setDebugAll(debugStage);
		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		Gdx.input.setInputProcessor(multiplexer = new InputMultiplexer(stage, this));

		Gdx.app.log(TAG, "F1 - toggle stage debug");
	}

	@Override public void show () {

	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	public void enableBlending () {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	public void disableBlending () {
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	@Override public void resize (int width, int height) {
		gameViewport.update(width, height, false);
		guiViewport.update(width, height, true);
	}

	@Override public void pause () {

	}

	@Override public void resume () {

	}

	@Override public void hide () {
		dispose();
	}

	@Override public void dispose () {
		batch.dispose();
		renderer.dispose();
		stage.dispose();
		skin.dispose();
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.ESCAPE) {
			game.reset();
		}
		if (keycode == Input.Keys.F1) {
			debugStage = !debugStage;
			stage.setDebugAll(debugStage);
			Gdx.app.log(TAG, "F1 - Stage debug is " + (debugStage?"enabled":"disabled"));
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
		return false;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		return false;
	}

	protected Vector2 cs = new Vector2();
	protected Vector3 temp = new Vector3();
	@Override public boolean mouseMoved (int screenX, int screenY) {
		updateMousePosition(screenX, screenY);
		return true;
	}

	protected void updateMousePosition (int screenX, int screenY) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}
}
