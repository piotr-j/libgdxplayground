package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISmartWindowsTest extends BaseScreen {

	public UISmartWindowsTest (GameReset game) {
		super(game);
		SmartWindow sw1 = new SmartWindow("H1", "w1");
		stage.addActor(sw1);
		SmartWindow.restore(sw1);

		SmartWindow sw2 = new SmartWindow("H2", "w2");
		stage.addActor(sw2);
		SmartWindow.restore(sw2);

		SmartWindow sw3 = new SmartWindow("H3", "w3");
		stage.addActor(sw3);
		SmartWindow.restore(sw3);

		SmartWindow sw4 = new SmartWindow("H4", "w4");
		stage.addActor(sw4);
		SmartWindow.restore(sw4);

		SmartWindow sw5 = new SmartWindow("H5", "w4");
		stage.addActor(sw5);
		SmartWindow.restore(sw5);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	public static class SmartWindow extends VisWindow {
		private final static String TAG = SmartWindow.class.getSimpleName();

		protected static ObjectMap<String, SmartWindow> windows = new ObjectMap<>();

		public static void persist () {
			// TODO would be nice to persist when position or size changes
			Preferences prefs = Gdx.app.getPreferences("SmartWindowTest.pref");
			for (SmartWindow w : windows.values()) {
				prefs.putString(w.key, w.getX()+";"+w.getY()+";"+w.getWidth()+";"+w.getHeight());
			}
			prefs.flush();
			windows.clear();
		}

		public static void restore (SmartWindow window) {
			Preferences prefs = Gdx.app.getPreferences("SmartWindowTest.pref");
			String data = prefs.getString(window.key, null);
			if (data != null) {
				String[] split = data.split(";");
				if (split.length == 4) {
					try {
						window.setPosition(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
						window.setSize(Float.parseFloat(split[2]), Float.parseFloat(split[3]));
					} catch (NumberFormatException e) {
						Gdx.app.error(TAG, "Failed to restore window " + window.key + " from data '" + data + "'");
					}
				}
			}
			SmartWindow other = windows.get(window.key, null);
			if (other != null && other != window) {
				Gdx.app.error(TAG, "Duplicate key " + window.key);
			}
			windows.put(window.key, window);
		}

		private final String key;
		public SmartWindow (String title, String key) {
			super(title);
			this.key = key;
			setResizable(true);
		}
	}

	@Override public void dispose () {
		SmartWindow.persist();
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UISmartWindowsTest.class);
	}
}
