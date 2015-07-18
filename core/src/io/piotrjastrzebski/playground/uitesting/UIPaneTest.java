package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
			final VisTextButton button;
			data.add(button = new VisTextButton("Some data " + i));
			data.row();
			button.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					Gdx.app.log("", "Clicked " + button);
				}
			});
		}
		VisWindow window = new VisWindow("Pane");
		final VisScrollPane pane;
		window.add(pane = new VisScrollPane(data));
		root.add(window).pad(200);
		pane.addListener(new InputListener() {
			@Override public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				lastFocus = stage.getScrollFocus();
				stage.setScrollFocus(pane);
			}

			@Override public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
				stage.setScrollFocus(lastFocus);
			}
		});
		pane.addListener(new ActorGestureListener(){
			@Override public void fling (InputEvent event, float velocityX, float velocityY, int button) {
				Gdx.app.log("", "fling...");
			}
		});
		pane.setCancelTouchFocus(false);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}
}
