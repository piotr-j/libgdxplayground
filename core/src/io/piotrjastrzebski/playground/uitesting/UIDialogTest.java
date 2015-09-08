package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDialogTest extends BaseScreen {
	private final static String TAG = UIDialogTest.class.getSimpleName();

	public UIDialogTest (GameReset game) {
		super(game);

		VisTextButton button = new VisTextButton("Show dialog");
		button.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				showDialog();
			}
		});
		root.add(button);
	}

	enum Result {YES, NO}
	private void showDialog() {
		VisDialog dialog = new VisDialog("Yes or no?!") {
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
	}


}
