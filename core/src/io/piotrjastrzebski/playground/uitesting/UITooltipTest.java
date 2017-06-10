package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.IntMap;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UITooltipTest extends BaseScreen {
	private final static String TAG = UITooltipTest.class.getSimpleName();

	public UITooltipTest (GameReset game) {
		super(game);

		TooltipManager tooltipManager = TooltipManager.getInstance();
		tooltipManager.offsetX = 0;
		tooltipManager.offsetY = -70;
		tooltipManager.edgeDistance = 7;
		tooltipManager.initialTime = 1;

		VisTable table = new VisTable(true);
		VisLabel label = new VisLabel("Test");
		table.add(label);

		table.row();

		table.add(createBtn("Btn1", "Btn1 tt", new BtnAction(Input.Keys.A) {
			@Override public void execute () {
				super.execute();
				Gdx.app.log("", "Btn1 exec");
			}
		}));
		table.row();
		table.add(createBtn("Btn2", "Btn2 tt", new BtnAction(Input.Keys.S) {
			@Override public void execute () {
				super.execute();
				Gdx.app.log("", "Btn2 exec");
			}
		}));
		root.add(table);

		clear.set(.5f, .5f, .5f, 1);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	private VisTextButton createBtn (String text, String tt, final BtnAction btnAction) {
		VisTextButton button = new VisTextButton(text);
		btnAction.setOwner(button);
//		new Tooltip(button, tt + " shortcut: " + Input.Keys.toString(btnAction.keyCode));
//		Tooltip build = new Tooltip.Builder("welp").target(button).build();
		TextTooltip tooltip = new TextTooltip(tt, VisUI.getSkin());
		button.addListener(tooltip);
		button.addListener(new ActorGestureListener(){
			@Override public void tap (InputEvent event, float x, float y, int count, int button) {
				btnAction.execute();
			}

			@Override public boolean longPress (Actor actor, float x, float y) {
				// show tooltip?
				Gdx.app.log(TAG, "long press?");
				return true;
			}
		});
		// todo handle modifiers
		btnActions.put(btnAction.keyCode, btnAction);
		return button;
	}

	IntMap<BtnAction> btnActions = new IntMap<>();

	@Override public boolean keyDown (int keycode) {
		BtnAction action = btnActions.get(keycode, null);
		if (action != null) {
			action.execute();
			return true;
		}
		return super.keyDown(keycode);
	}

	private abstract class BtnAction {
		int keyCode;
		private VisTextButton owner;

		public BtnAction (int keyCode) {
			this.keyCode = keyCode;
		}

		public void execute() {
			Gdx.app.log("", ""+owner);
//			FocusManager.getFocus(owner);
		}

		public void setOwner (VisTextButton owner) {
			this.owner = owner;
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UITooltipTest.class);
	}
}
