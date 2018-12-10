package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.*;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIPerspectiveCameraTest extends BaseScreen {
	PerspectiveCamera guiPerspectiveCamera;
	Texture badlogic;
	public UIPerspectiveCameraTest (GameReset game) {
		super(game);
		clear.set(Color.GRAY);
		badlogic = new Texture("badlogic.jpg");
		guiPerspectiveCamera= new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

//		guiPerspectiveCamera.position.y = -100;
		guiPerspectiveCamera.position.set(0f, 8f, 8f);
		guiPerspectiveCamera.lookAt(0, 0, 0);
		guiPerspectiveCamera.near = 0.1f;
		guiPerspectiveCamera.far = 300f;
		guiPerspectiveCamera.update();
//		guiPerspectiveCamera.lookAt(0, 0, 0);
//		guiPerspectiveCamera.update();/
//		guiViewport = new ScreenViewport(guiPerspectiveCamera);
		VisWindow window = new VisWindow("Welp");
		stage.addActor(window);
		stage.setViewport(new ExtendViewport(0, 0, guiPerspectiveCamera));
//		stage.setViewport(new ScreenViewport(guiPerspectiveCamera));
//		stage.setViewport(new ScreenViewport(guiPerspectiveCamera));
//		stage.setViewport(new FillViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), guiPerspectiveCamera));
//		stage.setViewport(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), guiPerspectiveCamera));
		window.centerWindow();
	}


	@Override public void render (float delta) {
		super.render(delta);
		batch.setProjectionMatrix(guiPerspectiveCamera.combined);
		batch.begin();
//		batch.draw(badlogic, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		batch.draw(badlogic, -5f, -5f, 10f, 10f);
		batch.end();
		stage.act(delta);
		stage.draw();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
	}

	@Override public void dispose () {
		super.dispose();
		badlogic.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UIPerspectiveCameraTest.class);
	}
}
