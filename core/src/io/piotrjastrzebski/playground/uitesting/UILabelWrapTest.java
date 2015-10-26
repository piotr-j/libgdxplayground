package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UILabelWrapTest extends BaseScreen {
	VisTable table;
	public UILabelWrapTest (GameReset game) {
		super(game);
		stage.setDebugAll(true);
		table = new VisTable();
		table.add(create("Text")).expand().fill().row();
		table.add(create("TextText")).expand().fill().row();
		table.add(create("TextTextText")).expand().fill().row();
		table.add(create("TextTextTextText")).expand().fill().row();
		root.add(table).size(150, 200);
	}

	private Label create(String text) {
		VisLabel label = new VisLabel(text);
		label.setWrap(true);
		label.setEllipsis(true);
		label.setEllipsis("...");
		return label;
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}
}
