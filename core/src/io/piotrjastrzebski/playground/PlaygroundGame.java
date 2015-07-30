package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.asyncscreentest.AsyncScreenTest;
import io.piotrjastrzebski.playground.btreeserializationtest.BTreeTest;
import io.piotrjastrzebski.playground.clientserver.CSTest;
import io.piotrjastrzebski.playground.clientserverv2.CSTestV2;
import io.piotrjastrzebski.playground.deferredsystemtest.DeferredSystemTest;
import io.piotrjastrzebski.playground.entityedittest.EntityEditTest;
import io.piotrjastrzebski.playground.entityonecomptest.EntityOneCompTest;
import io.piotrjastrzebski.playground.gpushadows.GpuShadows;
import io.piotrjastrzebski.playground.jobtest.JobTest;
import io.piotrjastrzebski.playground.simplelights.SimpleLightTest;
import io.piotrjastrzebski.playground.superkoalio.ECSKoalioTest;
import io.piotrjastrzebski.playground.superkoalio.SimpleKoalioTest;
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
		UIDialogTest.class, JobTest.class,
		ShadowTest.class, GpuShadows.class, CircleTest.class, ShapeRendererTest.class, BTreeTest.class, FitVPTest.class, VPTest.class,
		SimpleKoalioTest.class, ECSKoalioTest.class, SimpleLightTest.class,
	};

	PlatformBridge bridge;
	public PlaygroundGame (PlatformBridge bridge) {
		this.bridge = bridge;
	}

	@Override
	public void create () {
		if (bridge.getPixelScaleFactor() > 1.5f) {
			VisUI.load(VisUI.SkinScale.X2);
		} else {
			VisUI.load(VisUI.SkinScale.X1);
		}
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

			VisTable data = new VisTable();
			for (int i = 0; i < testScreens.length; i++) {
				final Class cls = testScreens[i];
				final VisTextButton button;
				data.add(button = new VisTextButton("Run " + cls.getSimpleName()));
				data.row();
				button.addListener(new ClickListener() {
					@Override public void clicked (InputEvent event, float x, float y) {
						try {
							Constructor constructor = cls.getConstructor(PlaygroundGame.class);
							setScreen((BaseScreen)constructor.newInstance(PlaygroundGame.this));
						} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
							e.printStackTrace();
						}
					}
				});
			}
			VisScrollPane pane;
			root.add(pane = new VisScrollPane(data));
			stage.setScrollFocus(pane);
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
