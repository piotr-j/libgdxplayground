package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDialogTest extends BaseScreen {
	private final static String TAG = UIDialogTest.class.getSimpleName();

	public UIDialogTest (GameReset game) {
		super(game);

		final VisTextButton button = new VisTextButton("Show dialog");
		button.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				showDialog();
				button.addAction(
					Actions.sequence(
						Actions.moveBy(10, 0, .1f, Interpolation.bounce),
						Actions.moveBy(-10, 0, .1f, Interpolation.bounce)
					));
			}
		});


		root.add(button);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	enum Result {YES, NO}
	private void showDialog() {
		final VisDialog dialog = new VisDialog("Yes or no?!") {
			@Override
			protected void result(Object object) {
				if (object.equals(Result.YES)) {
					Gdx.app.log(TAG, "YES");
				} else {
					Gdx.app.log(TAG, "NO");
				}
			}
		};
		dialog.key(Input.Keys.Y, Result.YES);
		dialog.key(Input.Keys.N, Result.NO);
		dialog.button("yes! (Y)", Result.YES);
		dialog.button("no! (N)", Result.NO);
		dialog.show(stage);
		dialog.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				dialog.hide();
			}
		});
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIDialogTest.class);
	}

}
