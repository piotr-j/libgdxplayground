package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIContextMenuTest extends BaseScreen {
	VisTable contextMenu;
	Vector2 temp = new Vector2();
	public UIContextMenuTest (GameReset game) {
		super(game);
		contextMenu = new VisTable();
		contextMenu.setTransform(true);
		for (int i = 1; i <= 10; i++) {
			final VisTextButton action;
			contextMenu.add(action = new VisTextButton("Context action " + i));
			contextMenu.row();
			action.setFocusBorderEnabled(false);
			action.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					action.localToParentCoordinates(temp.set(x, y));
					hideContextMenu(temp.x, temp.y);
				}
			});
		}
		root.add(contextMenu).width(150);
		contextMenu.addAction(Actions.alpha(0));
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	Vector3 pos = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT) {
			hideContextMenu();
		} else if (button == Input.Buttons.RIGHT) {
			guiCamera.unproject(pos.set(screenX, screenY, 0));
			showContextMenu(pos.x, pos.y);
		}
		return false;
	}

	public final static float ANIM_SPEED = 0.15f;

	boolean flipped;
	private void showContextMenu (float x, float y) {
		contextMenu.setOrigin(0, 0);

		float w = contextMenu.getWidth();
		if (x + w > guiCamera.viewportWidth) {
			x -= w;
			contextMenu.setOriginX(w);
		}

		float h = contextMenu.getHeight();
		boolean flipY = false;
		if (y + h > guiCamera.viewportHeight) {
			y -= h;
			contextMenu.setOriginY(h);
			flipY = true;
		}

		contextMenu.setPosition(x, y);

		if (flipY && !flipped) {
			flipped = true;
			contextMenu.getCells().reverse();
			contextMenu.invalidate();
		} else if (flipped && !flipY) {
			flipped = false;
			contextMenu.getCells().reverse();
			contextMenu.invalidate();
		}

		contextMenu.clearActions();
		contextMenu.addAction(
			Actions.sequence(Actions.alpha(0), Actions.scaleTo(0.1f, 0.1f),
				Actions.parallel(Actions.fadeIn(ANIM_SPEED), Actions.scaleTo(1, 1, ANIM_SPEED))));
	}

	private void hideContextMenu () {
		hideContextMenu(contextMenu.getWidth()/2, contextMenu.getHeight()/2);
	}

	private void hideContextMenu (float x, float y) {
		contextMenu.clearActions();
		contextMenu.setOrigin(x, y);
		contextMenu.addAction(Actions.parallel(Actions.fadeOut(ANIM_SPEED), Actions.scaleTo(0.1f, 0.1f, ANIM_SPEED)));
	}
}
