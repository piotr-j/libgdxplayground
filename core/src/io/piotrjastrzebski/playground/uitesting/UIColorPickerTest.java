package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIColorPickerTest extends BaseScreen {
	ColorPicker colorPicker;
	public UIColorPickerTest (GameReset game) {
		super(game);
		stage.setDebugAll(true);

		colorPicker = new ColorPicker("", new ColorPickerListener() {
			@Override public void canceled () {
				Gdx.app.log("", "Cancelled");
			}

			@Override public void finished (Color newColor) {
				Gdx.app.log("", "Finished " + newColor);
			}
		});

		root.add(colorPicker);
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

	@Override public void dispose () {
		super.dispose();
		colorPicker.dispose();
	}
}
