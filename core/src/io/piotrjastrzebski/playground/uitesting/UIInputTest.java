package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
import com.kotcrab.vis.ui.widget.VisImageButton;
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

		final Array<Focusable> focusables = new Array<>();
		{
			final VisTextButton button = new VisTextButton("Dialog-A");
			button.setName("::"+button.getText()+"::");
			button.getListeners().insert(0, new FocusableListener(button, focusables));
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog("Dialog-A", 1);
				}
			});
			root.add(button).pad(8).row();

			FocusManager.switchFocus(null, button);
			stage.setKeyboardFocus(button);
		}
		{
			final VisTextButton button = new VisTextButton("Dialog-B");
			button.setName("::"+button.getText()+"::");
			button.getListeners().insert(0, new FocusableListener(button, focusables));
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog("Dialog-B", 1);
				}
			});
			root.add(button).pad(8).row();
		}
	}

	void showDialog (final String name, final int depth) {
		final FDialog dialog = new FDialog(name);
		dialog.addCloseButton();
		dialog.closeOnEscape();

		Table content = dialog.getContentTable();
		{
			final VisTextButton button = new VisTextButton(name + "A");
			button.setName("::"+button.getText()+"::");
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog(name + "A", depth + 1);
				}
			});
			content.add(button).row();
			dialog.addFocusable(button);
		}
		content.add().height(16).row();
		{
			final VisTextButton button = new VisTextButton(name + "B");
			button.setName("::"+button.getText()+"::");
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog(name + "B", depth + 1);
				}
			});
			content.add(button).row();
			dialog.addFocusable(button);
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

		Actor focus = stage.getKeyboardFocus();
		if (focus != null) {
			Vector2 pos = focus.localToStageCoordinates(new Vector2());

			enableBlending();
			renderer.setProjectionMatrix(stage.getCamera().combined);
			renderer.begin(ShapeRenderer.ShapeType.Filled);
			renderer.setColor(1f, .85f, 0f, .5f);
			renderer.rect(pos.x + 2, pos.y + 2, focus.getWidth() - 4, focus.getHeight() - 4);
			renderer.setColor(Color.ORANGE);
			renderer.rect(pos.x, pos.y, focus.getWidth(), 2);
			renderer.rect(pos.x, pos.y, 2, focus.getHeight());
			renderer.rect(pos.x, pos.y + focus.getHeight() - 2, focus.getWidth(), 2);
			renderer.rect(pos.x + focus.getWidth() - 2, pos.y, 2, focus.getHeight());
			renderer.end();
		}
	}

	private static class FL extends FocusListener {
		private final String tag;

		public FL (String tag) {
			this.tag = tag;
		}

		@Override public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
			Actor relatedActor = event.getRelatedActor();
			if (!focused) return;
			if (relatedActor != null) {
				PLog.log(tag + ": focus change: " + relatedActor.getName() + " -> " + actor.getName());
			} else {
				PLog.log(tag + ": focus change: null -> " + actor.getName());
			}
			if (true) return;
			if (relatedActor != null) {
				if (focused) {
					// relative actor is actor that lost focus
					PLog.log(tag + ": focus gained: " + actor.getName() + ", lost: " + relatedActor.getName());
				} else {
					// relative actor is actor that gained focus
					PLog.log(tag + ": focus lost  : " + actor.getName() + ", gained: " + relatedActor.getName());
				}
			} else {
				if (focused) {
					PLog.log(tag + ": focus gained: " + actor.getName());
				} else {
					PLog.log(tag + ": focus lost  : " + actor.getName());
				}
			}
		}

		@Override public void scrollFocusChanged (FocusEvent event, Actor actor, boolean focused) {
//			PLog.log(tag + ": SC focus '" + actor.getName() + "': " + focused);
		}
	}

	/**
	 * This listener should be inserted as first or it will break dialog focus
	 */
	private static class FocusableListener extends ClickListener {
		private final String tag;
		private final Focusable owner;
		private final Array<Focusable> focusables;

		/**
		 *
		 * @param owner owner of this listeners
		 * @param focusables array of all widgets that will be participate in focus traversal
		 */
		public FocusableListener (Focusable owner, Array<Focusable> focusables) {
			this.tag = ((Actor)owner).getName();
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
			FocusManager.switchFocus(null, owner);
			event.getStage().setKeyboardFocus((Actor)owner);
		}

		@Override public boolean keyUp (InputEvent event, int keycode) {
			if (keycode == Input.Keys.TAB) {
//				PLog.log("Next focus from '" + tag + "'");
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
				FocusManager.switchFocus(null, target);
				// we need to set this for keyTyped to work
				stage.setKeyboardFocus((Actor)target);
				return true;
			} else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
//				PLog.log("Prev focus from '" + tag + "'");
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

	private static class FDialog extends VisDialog {
		Array<Focusable> focusables = new Array<>();
		boolean showing;
		boolean hasCloseButton;

		public FDialog (final String name) {
			super(name);
			setName(name);

			addListener(new FL(name) {
				@Override public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
					super.keyboardFocusChanged(event, actor, focused);
					if (showing) return;
					// if dialog gets focus, delegate to first of our own focusables
					if (focused && actor == FDialog.this) {
						focusFirst();
					}
				}
			});
		}

		@Override public void addCloseButton () {
			if (hasCloseButton) return;
			super.addCloseButton();
			for (Actor child : getTitleTable().getChildren()) {
				if (child instanceof VisImageButton) {
					hasCloseButton = true;
					// we want it to be first
					focusables.insert(0, (Focusable)child);
					child.getListeners().insert(0, new FocusableListener((Focusable)child, focusables));
					break;
				}
			}
		}

		private void focusFirst () {
			if (focusables.size == 0) return;
			// grab second if we have exit button
			Focusable focusable;
			if (hasCloseButton && focusables.size > 1) {
				focusable = focusables.get(1);
			} else {
				focusable = focusables.get(0);
			}

			FocusManager.switchFocus(null, focusable);
			Actor newFocus = (Actor)focusable;
			PLog.log(getName() + ": focusing " + newFocus.getName());
			getStage().setKeyboardFocus(newFocus);
		}

		public FDialog addFocusable (Focusable focusable) {
			focusables.add(focusable);
			Actor actor = (Actor)focusable;
			actor.getListeners().insert(0, new FocusableListener(focusable, focusables));
			return this;
		}

		@Override public VisDialog show (Stage stage) {
			showing = true;
			super.show(stage);
			showing = false;
			focusFirst();
			PLog.log(getName()+": show");
			PLog.log("------------");
			return this;
		}

		@Override protected void close () {
			// esc/x calls close which calls fadeOut, but that doesnt do quite what we want in dialog...
			hide();
		}

		@Override public void hide () {
			super.hide();
			PLog.log(getName()+": hide");
			PLog.log("------------");
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIInputTest.class);
	}

}
