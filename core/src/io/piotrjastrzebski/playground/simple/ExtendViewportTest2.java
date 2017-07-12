package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class ExtendViewportTest2 extends BaseScreen {
	private static final String TAG = ExtendViewportTest2.class.getSimpleName();
	public final static float SCALE = 1f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 720 * INV_SCALE;
	public final static float VP_HEIGHT = 1280 * INV_SCALE;

	protected OrthographicCamera gameCamera;
	protected ExtendViewport gameViewport;
	Stage stage;
	Texture texture;

	public ExtendViewportTest2 (GameReset game) {
		super(game);
		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, VP_WIDTH, VP_HEIGHT, gameCamera);
		stage = new Stage(gameViewport, batch);

		Table table = new Table();
		stage.addActor(table);
		table.setSize(VP_WIDTH, VP_HEIGHT);

		texture = new Texture("badlogic.jpg");
		table.add(new Image(texture)).expand().fill();
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.setColor(Color.GREEN);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.rect(0, 0, VP_WIDTH, VP_HEIGHT);
		renderer.end();
		stage.act(delta);
		stage.draw();

	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}

	@Override public void resize (int width, int height) {
		gameViewport.update(width, height, true);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width = 720/2 + 40;
		config.height = 1280/2;
		PlaygroundGame.start(args, config, ExtendViewportTest2.class);
	}
}
