package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.asyncscreentest.AsyncScreenTest;
import io.piotrjastrzebski.playground.box2dtest.Box2dInterpolationTest;
import io.piotrjastrzebski.playground.btreeserializationtest.BTreeTest;
import io.piotrjastrzebski.playground.clientserver.CSTest;
import io.piotrjastrzebski.playground.clientserverv2.CSTestV2;
import io.piotrjastrzebski.playground.ecs.ECSOrderTest;
import io.piotrjastrzebski.playground.ecs.aijobs.ECSAIJobsTest;
import io.piotrjastrzebski.playground.ecs.aitest.AIECSTest;
import io.piotrjastrzebski.playground.ecs.ECSPooledCompTest;
import io.piotrjastrzebski.playground.ecs.deferredsystemtest.DeferredSystemTest;
import io.piotrjastrzebski.playground.ecs.entityedittest.EntityEditTest;
import io.piotrjastrzebski.playground.ecs.entityonecomptest.EntityOneCompTest;
import io.piotrjastrzebski.playground.ecs.jobs.ECSJobsTest;
import io.piotrjastrzebski.playground.ecs.profiler.ECSPolyProfilerTest;
import io.piotrjastrzebski.playground.ecs.profiler.ECSShapeProfilerTest;
import io.piotrjastrzebski.playground.ecs.profilerv2.ECSProfilerTest;
import io.piotrjastrzebski.playground.ecs.quadtreetest.QuadTreeTest;
import io.piotrjastrzebski.playground.ecs.sat.SATTest;
import io.piotrjastrzebski.playground.ecs.tagtest.TagTest;
import io.piotrjastrzebski.playground.ecs.worldiotest.ECSWorldIOTest;
import io.piotrjastrzebski.playground.gpushadows.GpuShadows;
import io.piotrjastrzebski.playground.isotiled.IsoTest;
import io.piotrjastrzebski.playground.isotiled.SimpleTiledCropTest;
import io.piotrjastrzebski.playground.isotiled.SimpleTiledTest;
import io.piotrjastrzebski.playground.jobtest.AIECSJobTest;
import io.piotrjastrzebski.playground.jobtest.ECSJobTest;
import io.piotrjastrzebski.playground.jobtest.JobTest;
import io.piotrjastrzebski.playground.particletest.ParticleFaceTest;
import io.piotrjastrzebski.playground.shaders.Shader2dRainTest;
import io.piotrjastrzebski.playground.shaders.ShaderFireWallTest;
import io.piotrjastrzebski.playground.shaders.ShaderPortalTest;
import io.piotrjastrzebski.playground.simplelights.SimpleLightTest;
import io.piotrjastrzebski.playground.tiledgentest.*;
import io.piotrjastrzebski.playground.uitesting.*;
import light2dtest.ShadowTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

public class PlaygroundGame extends Game {

	// should be a class that extends BaseScreen
	final Class[] testScreens = new Class[]{SATTest.class, ParticleFaceTest.class, SimpleTiledCropTest.class,
		ShaderFireWallTest.class, ShaderPortalTest.class, Shader2dRainTest.class, ECSWorldIOTest.class, ECSOrderTest.class,
		ECSJobsTest.class, ECSAIJobsTest.class, UISpriteDrawableTest.class, SimpleTiledTest.class, ParticleTest.class, CameraMoveTest.class,
		TiledGenTest.class, TemperatureTest.class, BiomeTest.class, Box2dInterpolationTest.class,
		RainTest.class, BlurTest.class, CompositeGenTest.class, UITest.class, UISortTest.class, UISort2Test.class, CSTest.class,
		UIFBOTransitionTest.class, QuadTreeTest.class, RandomTest.class,
		ECSPooledCompTest.class, ECSProfilerTest.class, ECSShapeProfilerTest.class, ECSPolyProfilerTest.class,
		CSTestV2.class, TagTest.class, EntityEditTest.class, EntityOneCompTest.class, DeferredSystemTest.class,
		AsyncScreenTest.class, UIPaneTest.class, UIContextMenuTest.class, UIContextMenuTest2.class, UIFillTest.class, UIDPITest.class,
		UIDialogTest.class, UITableBuilderTest.class, JobTest.class, ECSJobTest.class, AIECSJobTest.class, AIECSTest.class,
		ShadowTest.class, GpuShadows.class, CircleTest.class, ShapeRendererTest.class, BTreeTest.class, FitVPTest.class, VPTest.class,
		SimpleLightTest.class, UIImageButtonTest.class, IsoTest.class, UISelectRowTest.class,
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
			Arrays.sort(testScreens, new Comparator<Class>() {
				@Override public int compare (Class o1, Class o2) {
					return o1.getSimpleName().compareTo(o2.getSimpleName());
				}
			});
			for (final Class cls : testScreens) {
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
		int lastKey;
		@Override public boolean keyDown (int keycode) {
			if (keycode == Input.Keys.ESCAPE && lastKey == Input.Keys.ESCAPE) {
				Gdx.app.exit();
			}
			lastKey = keycode;
			return false;
		}

		@Override public void render (float delta) {
			super.render(delta);
			stage.act(delta);
			stage.draw();
		}
	}

	@Override public void dispose () {
		super.dispose();
		VisUI.dispose();
	}
}
