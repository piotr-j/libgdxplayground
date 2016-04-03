package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UITextButtonTest extends BaseScreen {
	public UITextButtonTest (GameReset game) {
		super(game);

		BitmapFont font = VisUI.getSkin().getFont("default-font");

		TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
		style.font = font;
		style.fontColor = Color.WHITE;
		style.downFontColor = Color.BLUE;
		style.overFontColor = Color.GRAY;
		TextButton textButton = new TextButton("Test Button", style);
		stage.addActor(textButton);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UITextButtonTest.class);
	}
}
