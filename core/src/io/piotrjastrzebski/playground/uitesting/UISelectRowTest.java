package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISelectRowTest extends BaseScreen {
	public UISelectRowTest (PlaygroundGame game) {
		super(game);
		VisTable table = new VisTable(true);
		table.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				Gdx.app.log("", "clicked!");
			}
		});
		row(table, "row A");
		row(table, "row B");
		row(table, "row C");
		row(table, "row D");
		row(table, "row E");

		root.add(table);
	}

	private void row(VisTable container, final String text) {
		VisTable row = new VisTable(true);
		row.add(new VisLabel(text + 1));
		row.add(new VisLabel(text + 2));
		row.add(new VisLabel(text + 3));
		row.add(new VisLabel(text + 4));
		row.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				Gdx.app.log("", "clicked row " + text);
			}
		});
		container.add(row).row();
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}
}
