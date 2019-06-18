package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.kotcrab.vis.ui.widget.ScrollableTextArea;
import com.kotcrab.vis.ui.widget.VisTextArea;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UITextAreaResizeTest extends BaseScreen {
	public UITextAreaResizeTest (GameReset game) {
		super(game);

		// we want text area to sorta fit what we type in it
		// perhaps hidden input field and fit text into a square?
		// ...
		{
			final MyVisTextArea textArea = new MyVisTextArea("Hello");
//			root.add(textArea).width(60);
			root.add(textArea);
			textArea.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					textArea.resize();
				}
			});
		}
		root.add().width(100);
		{
			ScrollableTextArea textArea = new ScrollableTextArea("ASDF");
			root.add(textArea).width(50);
		}

		root.debugAll();
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UITextAreaResizeTest.class);
	}

	private static class MyVisTextArea extends VisTextArea {
		String lastText;
		float lastWidth;
		int lastLines = 0;
		int targetCursorPos = -1;
		public MyVisTextArea (String text) {
			super(text);
			resize();
		}

		@Override public void act (float delta) {
			super.act(delta);

			if (targetCursorPos >= 0) {
				setCursorPosition(targetCursorPos);
				targetCursorPos = -1;
			}
		}

		public void resize () {

			float min = 50;
			float max = 150;

			String text = getText();
			if (lastText != null && lastText.equals(text)) return;

			PLog.log("Text changed");
			lastText = text;
			Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
			GlyphLayout layout = layoutPool.obtain();

			int nl = text.indexOf("\n");
			if (nl == -1) {
				nl = text.indexOf("\r");
				if (nl == -1) {
					nl = text.length() - 1;
				}
			}
			VisTextFieldStyle style = getStyle();
			BitmapFont font = style.font;
			float extraWidth = font.getSpaceXadvance() * 4;
			layout.setText(font, text.substring(0, nl));
			// background could be null...
			lastWidth = MathUtils.clamp(layout.width + style.background.getRightWidth() + style.background.getLeftWidth(), min, max);

			layoutPool.free(layout);

			// need this for getLines to return current value
			calculateOffsets();
			int lines = getLines();
			if (lines != lastLines) {
				lastLines = lines;
				PLog.log("Lines = " + lines);
				setPrefRows(lines);
				// need to invalidate for rows to take
				invalidateHierarchy();
				// force view update so all text is visible
				targetCursorPos = getCursorPosition();
				moveCursorLine(0);
			}
		}

		@Override public float getWidth () {
			return lastWidth;
		}

		@Override public float getPrefWidth () {
			return getWidth();
		}
	}
}
