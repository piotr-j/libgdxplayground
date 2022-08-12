package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.widget.VisTextArea;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UITextAreaTest extends BaseScreen {
	VisTextArea visTextArea;
	TextArea textArea;
	Table table;
	public UITextAreaTest (GameReset game) {
		super(game);

		table = new Table();
		root.add(table);

		visTextArea = new VisTextArea("");
		textArea = new TextArea("", skin);

		visTextArea.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				String text = visTextArea.getText();
				if (textArea.getText().equals(text)) return;

				textArea.setProgrammaticChangeEvents(false);
				textArea.setText(text);
				textArea.setProgrammaticChangeEvents(true);
			}
		});
		textArea.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				String text = textArea.getText();
				if (visTextArea.getText().equals(text)) return;

				visTextArea.setProgrammaticChangeEvents(false);
				visTextArea.setText(text);
				visTextArea.setProgrammaticChangeEvents(true);
			}
		});

		table.add(visTextArea).growX().fill().width(100).height(50);
		table.add().width(20);
		table.add(textArea).growX().fill().width(100).height(50);

		root.debugAll();
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();

		resize(visTextArea);
		resize(textArea);
	}

	private void resize (VisTextArea textArea) {
		Integer prevLines = (Integer)textArea.getUserObject();
		int lines = MathUtils.clamp(textArea.getLines(), 1, 5);
		if (prevLines != null && prevLines == lines) return;

		float lineHeight = textArea.getPrefHeight();
		Drawable background = textArea.getStyle().background;
		if (background != null) {
			lineHeight -= background.getBottomHeight() + background.getTopHeight();
		}
		table.getCell(textArea).height(lineHeight * lines);
		table.setUserObject(lines);
		table.invalidateHierarchy();
	}

	private void resize (TextArea textArea) {
		Integer prevLines = (Integer)textArea.getUserObject();
		int lines = MathUtils.clamp(textArea.getLines(), 1, 5);
		if (prevLines != null && prevLines == lines) return;

		float lineHeight = textArea.getPrefHeight();
		Drawable background = textArea.getStyle().background;
		if (background != null) {
			lineHeight -= background.getBottomHeight() + background.getTopHeight();
		}
		table.getCell(textArea).height(lineHeight * lines);
		table.setUserObject(lines);
		table.invalidateHierarchy();
	}

	@Override public void dispose () {
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UITextAreaTest.class);
	}
}
