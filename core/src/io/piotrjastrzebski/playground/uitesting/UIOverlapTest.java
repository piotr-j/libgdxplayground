package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIOverlapTest extends BaseScreen {
	private final static String TAG = UIOverlapTest.class.getSimpleName();

	boolean eatEvent = true;
	public UIOverlapTest (GameReset game) {
		super(game);

		VisTextButton vtb1 = new VisTextButton("LARGE_LARGE_LARGE");
		vtb1.setPosition(Gdx.graphics.getWidth()/2 - vtb1.getWidth()/2, Gdx.graphics.getHeight()/2);
		vtb1.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				Gdx.app.log(TAG, "Clicked LARGE");
			}
		});
		stage.addActor(vtb1);
		final VisTextButton vtb2 = new VisTextButton("__SMALL__");
		vtb2.setPosition(Gdx.graphics.getWidth()/2 - vtb2.getWidth()/2, Gdx.graphics.getHeight()/2);
		vtb2.addListener(new ClickListener() {
			@Override public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				super.touchDown(event, x, y, pointer, button);
				return false;
			}

			@Override public void clicked (InputEvent event, float x, float y) {
				Gdx.app.log(TAG, "Clicked SMALL");
			}
		});
		stage.addActor(vtb2);
		vtb2.setColor(Color.GREEN);

		final VisTextButton toggle = new VisTextButton("EAT EVENT?", "toggle");
		toggle.setChecked(eatEvent);
		toggle.setPosition(Gdx.graphics.getWidth()/2 - toggle.getWidth()/2, Gdx.graphics.getHeight()/2 + 100);
		stage.addActor(toggle);

		toggle.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				eatEvent = toggle.isChecked();
			}
		});
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
		PlaygroundGame.start(args, UIOverlapTest.class);
	}
}
