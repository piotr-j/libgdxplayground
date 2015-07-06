package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.*;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIPaneTest extends BaseScreen {
	Actor lastFocus = null;
	public UIPaneTest (PlaygroundGame game) {
		super(game);
		VisTable data = new VisTable();
		for (int i = 1; i <= 30; i++) {
			data.add(new VisLabel("Some data " + i));
			data.row();
		}

		final VisScrollPane pane;
		root.add(pane = new VisScrollPane(data)).pad(200);
		pane.addListener( new InputListener() {
			@Override public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				lastFocus = stage.getScrollFocus();
				stage.setScrollFocus(pane);
			}

			@Override public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
				stage.setScrollFocus(lastFocus);
			}
		});
	}
}
