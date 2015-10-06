package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UITAPaneTest extends BaseScreen {
	VisTextArea textArea;
	Actor lastFocus = null;
	public UITAPaneTest (GameReset game) {
		super(game);

		textArea = new VisTextArea("some text some text some text some text some text some text some text some text some text some text");
		VisWindow window = new VisWindow("TA Pane");
		window.setResizable(true);

		final VisScrollPane pane;
		window.add(pane = new VisScrollPane(textArea)).fill().expand();
		pane.setScrollingDisabled(true, false);
		pane.setScrollbarsOnTop(true);
		stage.addActor(window);
		window.setSize(200, 200);
		window.centerWindow();
		window.debugAll();
		pane.setCancelTouchFocus(false);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}
}
