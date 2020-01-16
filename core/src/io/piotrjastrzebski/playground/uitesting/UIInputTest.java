package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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

		final Array<Focusable> focusables = new Array<>();
		stage.addListener(new FL("Stage") {
			@Override public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
				super.keyboardFocusChanged(event, actor, focused);
				if (!focused && event.getRelatedActor() == null) {
					PLog.log("No focus?");
				}
//					Focusable focusable = focusables.get(0);
//					FocusManager.switchFocus(stage, focusable);
//					Actor newFocus = (Actor)focusable;
//					stage.setKeyboardFocus(newFocus);
//				}
			}
		});

		{
			final VisTextButton button = new VisTextButton("DialogA: 1");
			button.setName("B::"+button.getText().toString());
			button.addListener(new FocusableListener(button, focusables));
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog("DialogA", 1);
				}
			});
			root.add(button).pad(8).row();

			FocusManager.switchFocus(stage, button);
			stage.setKeyboardFocus(button);
		}
		{
			final VisTextButton button = new VisTextButton("DialogB: 1");
			button.setName("B::"+button.getText().toString());
			button.addListener(new FocusableListener(button, focusables));
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog("DialogB", 1);
				}
			});
			root.add(button).pad(8).row();
		}
	}

	void showDialog (final String name, final int depth) {
		String tag = name + ": " + depth;
		final VisDialog dialog = new VisDialog(tag);
		dialog.setName("D::"+tag);

		dialog.addCloseButton();
		dialog.closeOnEscape();
		Table content = dialog.getContentTable();

		final Array<Focusable> focusables = new Array<>();
		{

			final VisTextButton button = new VisTextButton("Spawn: " + name + "A: " + (depth + 1));
			button.setName("B::"+button.getText().toString());
			button.addListener(new FocusableListener(button, focusables));
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
			button.setName("B::"+button.getText().toString());
			button.addListener(new FocusableListener(button, focusables));
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					showDialog(name + "B", depth + 1);
				}
			});
			content.add(button).row();
		}

		dialog.addListener(new FL(tag) {
			@Override public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
				super.keyboardFocusChanged(event, actor, focused);
				// if dialog gets focus, delegate to first of our own focusables
				if (focused && actor == dialog) {
					Focusable focusable = focusables.get(0);
					FocusManager.switchFocus(stage, focusable);
					Actor newFocus = (Actor)focusable;
					stage.setKeyboardFocus(newFocus);
				}
			}
		});


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

		enableBlending();
		renderer.setProjectionMatrix(stage.getCamera().combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
//		renderer.setColor(Color.GOLD);
		renderer.setColor(1f, .85f, 0f, .5f);
		Actor focus = stage.getKeyboardFocus();
		if (focus != null) {
			Vector2 pos = focus.localToStageCoordinates(new Vector2());
			renderer.rect(pos.x, pos.y, focus.getWidth(), focus.getHeight());
		}
		renderer.end();
	}

	private static class FL extends FocusListener {
		private final String tag;

		public FL (String tag) {
			this.tag = tag;
		}

		@Override public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
			Actor relatedActor = event.getRelatedActor();
			if (relatedActor != null) {
				if (focused) {
					// relative actor is actor that lost focus
					PLog.log(tag + ": focus gained: '" + actor.getName() + "', lost: " + relatedActor.getName());
				} else {
					// relative actor is actor that gained focus
					PLog.log(tag + ": focus lost  : '" + actor.getName() + "', gained: " + relatedActor.getName());
				}
			} else {
				if (focused) {
					PLog.log(tag + ": focus gained: '" + actor.getName() + "'");
				} else {
					PLog.log(tag + ": focus lost  : '" + actor.getName() + "'");
				}
			}
		}

		@Override public void scrollFocusChanged (FocusEvent event, Actor actor, boolean focused) {
//			PLog.log(tag + ": SC focus '" + actor.getName() + "': " + focused);
		}
	}

	private static class FocusableListener  extends ClickListener {
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
			event.getStage().setKeyboardFocus((Actor)owner);
		}

		@Override public boolean keyUp (InputEvent event, int keycode) {
			if (keycode == Input.Keys.TAB) {
				PLog.log("Next focus from '" + tag + "'");
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
				PLog.log("Prev focus from '" + tag + "'");
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
