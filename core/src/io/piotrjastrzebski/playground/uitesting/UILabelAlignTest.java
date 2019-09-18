package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

public class UILabelAlignTest extends BaseScreen {

	public UILabelAlignTest (GameReset game) {
		super(game);
		{
			VisLabel label = new VisLabel("Hello\naligned\nworld!");
			label.setAlignment(Align.left);
			root.add(label).pad(16).row();
		}
		{
			VisLabel label = new VisLabel("Hello\naligned\nworld!");
			label.setAlignment(Align.center);
			root.add(label).pad(16).row();
		}
		{
			VisLabel label = new VisLabel("Hello\naligned\nworld!");
			label.setAlignment(Align.right);
			root.add(label).pad(16).row();
		}
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UILabelAlignTest.class);
	}
}
