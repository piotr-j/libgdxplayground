package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.Focusable;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIInputTest extends BaseScreen {
	private final static String TAG = UIInputTest.class.getSimpleName();

	public UIInputTest (GameReset game) {
		super(game);
		// we care about vis widgets mainly for reasons...

		Array<Focusable> focusables = new Array<>();
		{
			final VisTextButton button = new VisTextButton("Dialog: 1");
			button.addListener(new FocusableListener(button, focusables));
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog("Dialog", 1);
				}
			});
			root.add(button).pad(8).row();

			FocusManager.switchFocus(stage, button);
			stage.setKeyboardFocus(button);
		}
		{
			final VisTextButton button = new VisTextButton("Dialog: 1");
			button.addListener(new FocusableListener(button, focusables));
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog("Dialog", 1);
				}
			});
			root.add(button).pad(8).row();
		}

		stage.addListener(new FL("Stage"));
	}

	private void showDialog (final String name, final int depth) {
		String tag = name + ": " + depth;
		VisDialog dialog = new VisDialog(tag);
		dialog.setName(tag);

		dialog.addCloseButton();
		Table content = dialog.getContentTable();
		{

			final VisTextButton button = new VisTextButton("Spawn: " + name + "A: " + (depth + 1));
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog(name + "A", depth + 1);
				}
			});
			content.add(button).row();
		}
		content.add().height(16).row();
		{
			final VisTextButton button = new VisTextButton("Spawn: " + name + "B: " + (depth + 1));
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog(name + "B", depth + 1);
				}
			});
			content.add(button).row();
		}

		dialog.show(stage);
		dialog.setPosition(
			MathUtils.random(root.getWidth() - dialog.getWidth()),
			MathUtils.random(root.getHeight() - dialog.getHeight())
		);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	private static class FL extends FocusListener {
		private final String tag;

		public FL (String tag) {
			this.tag = tag;
		}

		@Override public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
			Actor relatedActor = event.getRelatedActor();
			if (relatedActor != null) {
				PLog.log(tag + ": KB focus '" + actor.getName() + "': " + focused + ", rel: " + relatedActor.getName());
			} else {
				PLog.log(tag + ": KB focus '" + actor.getName() + "': " + focused);
			}
		}

		@Override public void scrollFocusChanged (FocusEvent event, Actor actor, boolean focused) {
//			PLog.log(tag + ": SC focus '" + actor.getName() + "': " + focused);
		}
	}

	private static class FocusableListener  extends ClickListener {
		private final Focusable owner;
		private final Array<Focusable> focusables;

		/**
		 *
		 * @param owner owner of this listeners
		 * @param focusables array of all widgets that will be participate in focus traversal
		 */
		public FocusableListener (Focusable owner, Array<Focusable> focusables) {
			this.owner = owner;
			this.focusables = focusables;
			if (!focusables.contains(owner, true)) {
				focusables.add(owner);
			}
			if (owner instanceof VisTextField) {
				// we handle that
				((VisTextField)owner).setFocusTraversal(false);
			}
		}

		@Override public void clicked (InputEvent event, float x, float y) {
			super.clicked(event, x, y);
			// not set automatically for non text fields
			event.getStage().setKeyboardFocus((Actor)owner);
		}

		@Override public boolean keyUp (InputEvent event, int keycode) {
			if (keycode == Input.Keys.TAB) {
				int index = focusables.indexOf(owner, true);
				if (index == -1) return false;
				Stage stage = event.getStage();
				if (UIUtils.shift()) { // uo
					index -= 1;
					if (index < 0) index += focusables.size;
				} else { // down
					index += 1;
				}
				Focusable target = focusables.get(index % focusables.size);
				FocusManager.switchFocus(stage, target);
				// we need to set this for keyTyped to work
				stage.setKeyboardFocus((Actor)target);
				return true;
			} else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
				// if we have focus and enter/space is pressed, do type specific action
				if (owner instanceof Button) {
					// got to use checked, also handles checkbox
					Button btn = (Button)owner;
					btn.setChecked(!btn.isChecked());
					return true;
				}
			}
			return super.keyUp(event, keycode);
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIInputTest.class);
	}

}
