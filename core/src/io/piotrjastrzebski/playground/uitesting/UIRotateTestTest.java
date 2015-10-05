package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIRotateTestTest extends BaseScreen {
	public UIRotateTestTest (GameReset game) {
		super(game);

		VisTextButton clear = new VisTextButton("clear");
		VisLabel normalLabel = new VisLabel("Doesnt rotate! LIBGDX IS BAD!");
		normalLabel.addAction(Actions.forever(Actions.rotateBy(2.5f)));
		root.add(clear);
		root.row();
		root.add(normalLabel);
		root.row();
		final VisTable table = new VisTable(true);
		table.setTransform(true);
		VisLabel label = new VisLabel("This rotates! What is this?!");
		table.add(label);
		root.add(table);
		root.invalidateHierarchy();
		table.addAction(Actions.forever(Actions.rotateBy(-2.5f)));
		clear.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				table.clearActions();
			}
		});
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}
}
