package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIContextMenuTest2 extends BaseScreen {
	VisTable contextMenu;
	public UIContextMenuTest2 (PlaygroundGame game) {
		super(game);
		contextMenu = new VisTable();
		contextMenu.add(new VisTextButton("Context action " + lastId++)).row();
		contextMenu.add(new VisTextButton("Context action " + lastId++)).row();;
		contextMenu.add(new VisTextButton("Context action " + lastId++)).row();;
		contextMenu.add(new VisTextButton("Context action " + lastId++)).row();;
		contextMenu.setVisible(false);
		// TODO play with this
		root.addActor(contextMenu);
	}
	int lastId;
	Vector3 pos = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT) {
			hideContextMenu();
		} else if (button == Input.Buttons.RIGHT) {
			guiCamera.unproject(pos.set(screenX, screenY, 0));
			showContextMenu(pos.x, pos.y);
			// stays in center with either
			contextMenu.clearChildren();
			contextMenu.add(new VisTextButton("Context action " + lastId++));
		}
		return false;
	}

	private void showContextMenu (float x, float y) {
		contextMenu.setPosition(x, y);
		contextMenu.setVisible(true);
	}

	private void hideContextMenu () {
		contextMenu.setVisible(false);
	}
}
