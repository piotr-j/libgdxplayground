package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Comparator;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIResolutionChangeTest extends BaseScreen {
	public static final String TAG = UIResolutionChangeTest.class.getSimpleName();
	public UIResolutionChangeTest (GameReset game) {
		super(game);

		/* NOTE
			stuff we want
			- fullscreen, windowed, borderless
			- set a preset resolution, from monitor
			- input custom resolution?
			- select monitor
		 */

		final VisSelectBox<DisplayHelper.DisplayMode> modes = new VisSelectBox<>();
		Array<DisplayHelper.DisplayMode> valid = DisplayHelper.getModes();
		modes.setItems(valid);
		int currentIndex = DisplayHelper.getCurrentIndex(valid);
		if (currentIndex >= 0) {
			modes.setSelectedIndex(currentIndex);
		}
		modes.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				DisplayHelper.DisplayMode selected = modes.getSelected();
				Gdx.app.log(TAG, "Selected " + selected);
			}
		});
		root.add(modes);
		root.row();
		VisTextButton apply = new VisTextButton("Apply");
		apply.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {

			}
		});
		root.add(apply);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	public static class DisplayHelper {
		public static Array<DisplayMode> getModes() {
			Graphics.DisplayMode[] modes = Gdx.graphics.getDisplayModes();
			Array<DisplayMode> valid = new Array<>();
			for (Graphics.DisplayMode mode : modes) {
				if (mode.refreshRate < 60) continue;
				if (mode.width < 800) continue;
				if (mode.height < 600) continue;
				valid.add(new DisplayMode(mode));
			}

			valid.sort(new Comparator<DisplayMode>() {
				@Override public int compare (DisplayMode o1, DisplayMode o2) {
					if (o2.width == o1.width) {
						return o2.height - o1.height;
					}
					return o2.width - o1.width;
				}
			});
			return valid;
		}

		public static int getCurrentIndex (Array<DisplayMode> modes) {
			Graphics.DisplayMode current = Gdx.graphics.getDisplayMode();
			for (int i = 0; i < modes.size; i++) {
				DisplayMode mode = modes.get(i);
				if (mode.eq(current)) {
					return i;
				}
			}
			return -1;
		}

		public static class DisplayMode {
			public Graphics.DisplayMode mode;
			public int width;
			public int height;
			public int refresh;

			public DisplayMode (Graphics.DisplayMode mode) {
				this.mode = mode;
				width = mode.width;
				height = mode.height;
				refresh = mode.refreshRate;
			}

			@Override public String toString () {
				return width + "x" + height + " (" + refresh + "hz)";
			}

			public boolean eq (DisplayMode mode) {
				if (width != mode.width) return false;
				if (height != mode.height) return false;
				if (refresh != mode.refresh) return false;
				return true;
			}

			public boolean eq (Graphics.DisplayMode current) {
				if (width != mode.width) return false;
				if (height != mode.height) return false;
				if (refresh != mode.refreshRate) return false;
				return true;
			}
		}
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UIResolutionChangeTest.class);
	}
}
