package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIFillTest extends BaseScreen {
	Array<CleverWindow> windows = new Array<>();
	public UIFillTest (PlaygroundGame game) {
		super(game);
		// undo BaseScreen stuff
		stage.clear();
		stage.setDebugAll(true);

		VisWindow window;
		stage.addActor(window = createWindow());
		window.add(new VisLabel("Label " + windowId)).top();
		stage.addActor(window = createWindow());
		window.add(new VisLabel("Label " + windowId)).bottom();
	}

	int windowId;
	private VisWindow createWindow() {
		VisWindow window = new VisWindow("Window " + (++windowId));
//		window.setBackground((Drawable)null);
//		window.setFillParent(true);
		window.setSize(300, 300);
		window.setPosition(200, 200);
		window.addCloseButton();
		window.fadeIn();
//		window.set
		return window;
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		// update position
		for (CleverWindow window: windows) {
			window.resize(width, height);
		}
	}

	public static class CleverWindow extends VisWindow {

		public CleverWindow (String title) {
			super(title);
		}

		public CleverWindow (String title, boolean showWindowBorder) {
			super(title, showWindowBorder);
		}

		public CleverWindow (String title, WindowStyle style) {
			super(title, style);
		}

		public void resize (int width, int height) {

		}
	}
}