package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDoublePaneTest extends BaseScreen {
	Actor lastFocus = null;
	public UIDoublePaneTest (GameReset game) {
		super(game);
		VisTable data = new VisTable();
		for (int i = 1; i <= 30; i++) {
			final VisTextButton button = new VisTextButton("Some data " + i + " extra long stuff is extra long!");
			VisScrollPane inner = new VisScrollPane(button);
			inner.setScrollingDisabled(false, true);
			data.add(inner).width(200).height(100);
			data.row();
			button.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					Gdx.app.log("", "Clicked " + button);
				}
			});
			inner.setFadeScrollBars(false);
		}
		VisWindow window = new VisWindow("Pane");
		final VisScrollPane pane;
		window.add(pane = new VisScrollPane(data));
		pane.setFadeScrollBars(false);
		root.add(window).pad(200);
		pane.setCancelTouchFocus(false);
		pane.setScrollingDisabled(true, false);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}
}
