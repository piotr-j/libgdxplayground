package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIRotateTestTest extends BaseScreen {
	public UIRotateTestTest (GameReset game) {
		super(game);
		VisLabel normalLabel = new VisLabel("Doesnt rotate! LIBGDX IS BAD!");
		normalLabel.addAction(Actions.forever(Actions.rotateBy(2.5f)));
		root.add(normalLabel);
		root.row();
		VisTable table = new VisTable(true);
		table.setTransform(true);
		VisLabel label = new VisLabel("This rotates! What is this?!");
		table.add(label);
		root.add(table);
		root.invalidateHierarchy();
		table.addAction(Actions.forever(Actions.rotateBy(-2.5f)));

	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}
}
