package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.AddAction;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 * Created by PiotrJ on 20/06/15.
 */
public class UIActionLeakTest extends BaseScreen {
	public UIActionLeakTest (GameReset game) {
		super(game);
		Label hello = new Label("hello", skin);
		ColorAction color = Actions.color(Color.ORANGE, .5f, Interpolation.fade);
		AddAction add = Actions.addAction(color);
		hello.addAction(add);
		// this frees pooled action added to the actor, but not action added to AddAction
		hello.clearActions();
		if (color.getColor() != null) {
			PLog.error("color.getColor() != null");
		}
		// we expect pooled action here, but we get new one
		ColorAction color2 = Actions.color(Color.ORANGE, .5f, Interpolation.fade);
		if (color != color2) {
			PLog.error("color != color2");
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}


	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setWindowedMode(1280/2, 720/2);
		PlaygroundGame.start(args, config, UIActionLeakTest.class);
	}
}
