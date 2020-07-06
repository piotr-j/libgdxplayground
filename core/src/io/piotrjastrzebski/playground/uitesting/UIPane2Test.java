package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIPane2Test extends BaseScreen {

	public UIPane2Test (GameReset game) {
		super(game);

		// pane will use prefSize of the Image and we cant easily change that cus its taken from the drawable
		// so we will return regular size that we can change easily
		Group container = new Group();
		Image image = new Image(new Texture("badlogic.jpg")) {
			@Override
			public float getPrefWidth () {
				return getWidth();
			}

			@Override
			public float getPrefHeight () {
				return getHeight();
			}
		};
//		image.setTouchable(Touchable.enabled);
		image.setSize(image.getDrawable().getMinWidth(), image.getDrawable().getMinHeight());
		container.debugAll();
		container.setSize(image.getPrefWidth(), image.getPrefHeight());
		container.addActor(image);

		// if its disabled, image will be visible outside of the scroll pane
		final boolean clipEnabled = true;
		final VisScrollPane pane = new VisScrollPane(container) {
			@Override
			public boolean clipBegin (float x, float y, float width, float height) {
				if (clipEnabled) {
					return super.clipBegin(x, y, width, height);
				}
				return true;
			}

			@Override
			public void clipEnd () {
				if (clipEnabled) {
					super.clipEnd();
				}
			}
		};
		image.addListener(new InputListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				// we want scroll events
				image.getStage().setScrollFocus(image);
				return super.touchDown(event, x, y, pointer, button);
			}

			@Override
			public boolean scrolled (InputEvent event, float x, float y, int amount) {
				float toAdd = amount * 10;
				image.setSize(image.getWidth() + toAdd, image.getHeight() + toAdd);
				// pane wont notice unless we tell it
//				image.invalidateHierarchy();
				container.setSize(image.getWidth(), image.getHeight());
				pane.invalidateHierarchy();
				return true;
			}
		});
//		pane.debug();
		// remove last listener, should be scroll handler
		// we dont want the pane to move when we scroll as we
		pane.getListeners().removeIndex(pane.getListeners().size - 1);
//		window.add(pane).grow();
//		root.add(window).pad(200);
		if (false) {
			// we need to tell the pane how bit it is supposed to be
			pane.setBounds(100, 100, 200, 200);
			root.addActor(pane);
		} else {
			// it can also be a child of some other actor
			VisWindow window = new VisWindow("Pane");
			window.setResizable(true);
			// pane will be the size of windows content
			window.add(pane).grow();
			root.addActor(window);
		}
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIPane2Test.class);
	}
}
