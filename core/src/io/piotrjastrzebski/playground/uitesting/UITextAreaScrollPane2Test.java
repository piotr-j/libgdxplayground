package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.StringBuilder;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UITextAreaScrollPane2Test extends BaseScreen {
	TestTT area;
	public UITextAreaScrollPane2Test (GameReset game) {
		super(game);

		StringBuilder dummyText = new StringBuilder();
		{
			dummyText.setLength(0);
			dummyText.append("|jg Text Area\nEssentially, a text field\nwith\nmultiple\nlines.\n");
			// we need a bunch of lines to demonstrate that prefHeight is way too large
			for (int i = 0; i < 30; i++) {
				dummyText.append("It cant even handle very loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong lines.\n");
			}
		}
		area = new TestTT(dummyText.toString(), skin);

		// we need to set the width of the area so getLines() calculation works
		Table wrapper = new Table() {
			@Override protected void sizeChanged () {
				super.sizeChanged();
				area.setWidth(getWidth());
			}
		};
		ScrollPane pane = new ScrollPane(wrapper, skin);
		pane.setScrollingDisabled(true, false);
		pane.setFadeScrollBars(false);

		// height of the pane should match desired minimum number of lines probably
		root.add(pane).width(PlaygroundGame.WIDTH/2f).maxHeight(PlaygroundGame.HEIGHT/2f);

		wrapper.add(area).growX();
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	static class TestTT extends PTextArea {
		public TestTT (String text, Skin skin) {
			super(text, skin);
			addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					invalidateHierarchy();
				}
			});
		}

		@Override public void setText (String str) {
			super.setText(str);
			invalidateHierarchy();
		}

		@Override public void appendText (String str) {
			super.appendText(str);
			invalidateHierarchy();
		}

		@Override public void layout () {
			super.layout();

			sizeChanged();
			// we need this so fake internal TextArea scrolling doesnt mess this up
			setFirstLineShowing(0);
		}

		float lastPrefHeight = 0;
		@Override public float getPrefHeight () {
			if (getWidth() > 0) calculateOffsets();
			int lines = Math.max(getLines(), 5);

			TextField.TextFieldStyle style = getStyle();
			float prefHeight = MathUtils.ceil(style.font.getLineHeight() * lines);
			if (style.background != null) {
				prefHeight = Math.max(prefHeight + style.background.getBottomHeight() + style.background.getTopHeight(),
					style.background.getMinHeight());
			}
			if (!MathUtils.isEqual(lastPrefHeight, prefHeight)) {
				lastPrefHeight = prefHeight;
				// make sure parent notices
				invalidateHierarchy();
			}
			return prefHeight;
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UITextAreaScrollPane2Test.class);
	}
}
