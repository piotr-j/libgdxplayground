package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDoubleDialogTest extends BaseScreen {
	protected final static String TAG = UIDoubleDialogTest.class.getSimpleName();

	VisDialog dialogA;
	VisDialog dialogB;

	public UIDoubleDialogTest (GameReset game) {
		super(game);
		// TODO we want a clean way to show one dialog with single tap and another with 2 taps
		// TODO they should work as expected, ie be modal and close after tap outside;

		dialogA = new VisDialog("Dialog A");
		dialogA.addCloseButton();
		dialogA.getContentTable().add(new VisTextButton("AAAAAAAAA")).row();
		dialogA.getContentTable().add(new VisTextButton("AAAAAAAAA")).row();
		dialogA.getContentTable().add(new VisTextButton("AAAAAAAAA")).row();
		dialogA.getContentTable().add(new VisTextButton("AAAAAAAAA")).row();
		dialogA.pack();
//		dialogA.show(stage);
		dialogA.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				dialogA.hide();
			}
		});
//		dialogA.setModal(false);

		dialogB = new VisDialog("Dialog B");
		dialogB.addCloseButton();
		dialogB.getContentTable().add(new VisTextButton("BBBBBBBBB")).row();
		dialogB.getContentTable().add(new VisTextButton("BBBBBBBBB")).row();
		dialogB.getContentTable().add(new VisTextButton("BBBBBBBBB")).row();
		dialogB.getContentTable().add(new VisTextButton("BBBBBBBBB")).row();
		dialogB.pack();
//		dialogB.show(stage);
		dialogB.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				dialogB.hide();
			}
		});
//		dialogB.setModal(false);
		stage.addListener(new ActorGestureListener(){
			boolean showDialogA = true;
			@Override public void tap (InputEvent event, float x, float y, int count, int button) {
				Gdx.app.log(TAG, "tap " + count);
				if (count == 1) {
					showDialogA = true;
					// show dialogA
					root.addAction(Actions.sequence(Actions.delay(.5f, Actions.run(new Runnable() {
						@Override public void run () {
							if (showDialogA)
								dialogA.show(stage);
						}
					}))));
				} else if (count == 2) {
					// show dialogB
					showDialogA = false;
					dialogB.show(stage);
				}
			}
		});
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width *= .5f;
		config.height *= .5f;
		PlaygroundGame.start(args, config, UIDoubleDialogTest.class);
	}
}
