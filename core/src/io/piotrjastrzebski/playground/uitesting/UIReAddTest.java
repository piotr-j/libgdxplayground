package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIReAddTest extends BaseScreen {
	VisTable table;
	public UIReAddTest (GameReset game) {
		super(game);

		table = new VisTable();
		table.add(new VisLabel("welp1")).row();
		table.add(new VisLabel("welp2")).row();
		table.add(new VisLabel("welp3")).row();
		table.add(new VisLabel("welp4")).row();
		final VisTextButton toggle = new VisTextButton("Toggle");
		toggle.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				if (table.getStage() == null) {
					root.addActor(table);
					table.setPosition(root.getWidth()/2 - 200, root.getHeight()/2);
				} else {
					table.remove();
				}
			}
		});
		root.add(toggle).expand();
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}
}
