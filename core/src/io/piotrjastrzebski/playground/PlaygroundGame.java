package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import io.piotrjastrzebski.playground.asyncscreentest.AsyncScreenTest;
import io.piotrjastrzebski.playground.btreeserializationtest.BTreeTest;
import io.piotrjastrzebski.playground.clientserver.CSTest;
import io.piotrjastrzebski.playground.clientserverv2.CSTestV2;
import io.piotrjastrzebski.playground.deferredsystemtest.DeferredSystemTest;
import io.piotrjastrzebski.playground.entityedittest.EntityEditTest;
import io.piotrjastrzebski.playground.entityonecomptest.EntityOneCompTest;
import io.piotrjastrzebski.playground.gpushadows.GpuShadows;
import io.piotrjastrzebski.playground.tagtest.TagTest;
import io.piotrjastrzebski.playground.tiledgentest.*;
import io.piotrjastrzebski.playground.uitesting.*;
import light2dtest.ShadowTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PlaygroundGame extends Game {

	// should be a class that extends BaseScreen
	final Class[] testScreens = new Class[]{
		TiledGenTest.class, TemperatureTest.class, BiomeTest.class,
		RainTest.class, BlurTest.class, CompositeGenTest.class, UITest.class, CSTest.class,
		CSTestV2.class, TagTest.class, EntityEditTest.class, EntityOneCompTest.class, DeferredSystemTest.class,
		AsyncScreenTest.class, UIPaneTest.class, UIContextMenuTest.class, UIContextMenuTest2.class, UIFillTest.class, UIDPITest.class,
		ShadowTest.class, GpuShadows.class, CircleTest.class, ShapeRendererTest.class, BTreeTest.class, FitVPTest.class, VPTest.class
	};

	@Override
	public void create () {
		VisUI.load();
		reset();
	}

	int lastSelected = -1;

	public void reset () {
		setScreen(new TestSelectScreen(this));
	}

	private class TestSelectScreen extends BaseScreen {
		public TestSelectScreen (PlaygroundGame game) {
			super(game);
			root.add(new VisLabel("Select test to run, ESC to go back"));
			root.row();
			root.row();

			final VisList<TestScreen> screenSelect = new VisList<>();
			screenSelect.setItems(createScreens());
			screenSelect.setSelectedIndex(lastSelected);
			screenSelect.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					lastSelected = screenSelect.getSelectedIndex();
					screenSelect.getSelected().run();
				}
			});
			root.add(new VisScrollPane(screenSelect));
		}

		private Array<TestScreen> createScreens () {
			Array<TestScreen> screens = new Array<>();
			for (Class cls : testScreens) {
				if (!BaseScreen.class.isAssignableFrom(cls)) {
					Gdx.app.log("", "Invalid class: " + cls);
					continue;
				}
				screens.add(new TestScreen(cls));
			}
			return screens;
		}

		private class TestScreen {
			Class cls;

			public TestScreen (Class cls) {
				this.cls = cls;
			}

			public void run() {
				try {
					Constructor constructor = cls.getConstructor(PlaygroundGame.class);
					setScreen((BaseScreen)constructor.newInstance(PlaygroundGame.this));
				} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
					e.printStackTrace();
				}
			}

			@Override public String toString () {
				return cls.getSimpleName();
			}
		}

		@Override public boolean keyDown (int keycode) {
			return false;
		}
	}

	@Override public void dispose () {
		super.dispose();
		VisUI.dispose();
	}
}
