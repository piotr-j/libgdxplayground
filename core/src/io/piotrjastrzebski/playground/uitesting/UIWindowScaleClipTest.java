package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

public class UIWindowScaleClipTest extends BaseScreen {

	public UIWindowScaleClipTest (GameReset game) {
		super(game);
		{
			Window scaled_window = new Window("Window .75x", skin);
			scaled_window.add(new Label("VERY LONG LABEL;VERY LONG LABEL;VERY LONG LABEL;VERY LONG LABEL;VERY LONG LABEL;", skin));
			root.addActor(scaled_window);
			scaled_window.setScale(.75f);
			scaled_window.setPosition(200, 200);
		}
		{
			Window scaled_window = new Window("Window 1x", skin);
			scaled_window.add(new Label("VERY LONG LABEL;VERY LONG LABEL;VERY LONG LABEL;VERY LONG LABEL;VERY LONG LABEL;", skin));
			root.addActor(scaled_window);
			scaled_window.setPosition(400, 200);
		}
		{
			Window scaled_window = new Window("Window 1.5x", skin);
			scaled_window.add(new Label("VERY LONG LABEL;VERY LONG LABEL;VERY LONG LABEL;VERY LONG LABEL;VERY LONG LABEL;", skin));
			root.addActor(scaled_window);
			scaled_window.setScale(1.5f);
			scaled_window.setPosition(600, 200);
		}
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIWindowScaleClipTest.class);
	}
}
