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
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);
		stage = new Stage(gameViewport, batch);

		Table table = new Table();
		stage.addActor(table);
		table.setSize(VP_WIDTH, VP_HEIGHT);

		texture = new Texture("badlogic.jpg");
		table.add(new Image(texture)).expand().fill();
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.YELLOW);
		renderer.rect(-100, -100, VP_WIDTH + 200, VP_HEIGHT + 200);
		renderer.setColor(Color.GREEN);
		renderer.rect(0, 0, VP_WIDTH, VP_HEIGHT);
		renderer.setColor(Color.MAGENTA);
		renderer.rect(
			gameCamera.position.x - gameCamera.viewportWidth/2,
			gameCamera.position.y - gameCamera.viewportHeight/2,
			gameCamera.viewportWidth,
			gameCamera.viewportHeight);
		renderer.end();
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}

	@Override public void resize (int width, int height) {
		gameViewport.update(width, height, true);
		gameCamera.position.x += -(gameCamera.viewportWidth - VP_WIDTH)/2;
		gameCamera.position.y += -(gameCamera.viewportHeight - VP_HEIGHT)/2;
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width = 720/2 + 40;
		config.height = 1280/2;
		PlaygroundGame.start(args, config, ExtendViewportTest2.class);
	}
}
