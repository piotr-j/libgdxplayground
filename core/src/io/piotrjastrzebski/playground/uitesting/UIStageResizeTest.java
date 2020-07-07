package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIStageResizeTest extends BaseScreen {
	protected final static float WIDTH = 1280;
	protected final static float HEIGHT = 720;
	protected OrthographicCamera camera;
	protected ExtendViewport viewport;


	protected Stage resizingStage;
	public UIStageResizeTest (GameReset game) {
		super(game);
		camera = new OrthographicCamera();
		// size should be fairly large, scene 2d works in pixels more or less
		viewport = new ExtendViewport(WIDTH, HEIGHT, camera);

		resizingStage = new Stage(viewport, batch);
		resizingStage.getRoot().setSize(WIDTH, HEIGHT);

		resizingStage.getRoot().debug();

//		Group root = new Group();
//		root.setSize(WIDTH, HEIGHT);
//		resizingStage.addActor(root);
//		root.debug();


		multiplexer.clear();
		multiplexer.addProcessor(this);
		multiplexer.addProcessor(resizingStage);
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		viewport.update(width, height);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		resizingStage.act(delta);
		resizingStage.draw();
	}

	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setWindowedMode(1280/2, 720/2);
		PlaygroundGame.start(args, config, UIStageResizeTest.class);
	}
}
