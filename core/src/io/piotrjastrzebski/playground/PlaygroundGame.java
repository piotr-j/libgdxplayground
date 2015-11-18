package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.asyncscreentest.AsyncScreenTest;
import io.piotrjastrzebski.playground.box2dtest.*;
import io.piotrjastrzebski.playground.box2dtest.lights.Box2dRayTest;
import io.piotrjastrzebski.playground.bttests.btedittest.BTEditTest;
import io.piotrjastrzebski.playground.bttests.btedittest2.BTEditTest2;
import io.piotrjastrzebski.playground.bttests.btreeserializationtest.BTreeTest;
import io.piotrjastrzebski.playground.bttests.savetest.BTSaveTest;
import io.piotrjastrzebski.playground.bttests.simplishedittest.SimpleBTEditTest;
import io.piotrjastrzebski.playground.clientserver.CSTest;
import io.piotrjastrzebski.playground.clientserverv2.CSTestV2;
import io.piotrjastrzebski.playground.dungeon.DungeonGeneratorTest;
import io.piotrjastrzebski.playground.ecs.AshleyPhysicsTest;
import io.piotrjastrzebski.playground.ecs.ECSOrderTest;
import io.piotrjastrzebski.playground.ecs.aijobs.ECSAIJobsTest;
import io.piotrjastrzebski.playground.ecs.aitest.AIECSTest;
import io.piotrjastrzebski.playground.ecs.ECSPooledCompTest;
import io.piotrjastrzebski.playground.ecs.assettest.ECSAssetTest;
import io.piotrjastrzebski.playground.ecs.deferredsystemtest.DeferredSystemTest;
import io.piotrjastrzebski.playground.ecs.entityedittest.EntityEditTest;
import io.piotrjastrzebski.playground.ecs.entityonecomptest.EntityOneCompTest;
import io.piotrjastrzebski.playground.ecs.fancywalltest.ECSFancyWallsTest;
import io.piotrjastrzebski.playground.ecs.jobs.ECSJobsTest;
import io.piotrjastrzebski.playground.ecs.profiler.ECSPolyProfilerTest;
import io.piotrjastrzebski.playground.ecs.profiler.ECSShapeProfilerTest;
import io.piotrjastrzebski.playground.ecs.profilerv2.ECSProfilerTest;
import io.piotrjastrzebski.playground.ecs.quadtreetest.QuadTreeTest;
import io.piotrjastrzebski.playground.ecs.sat.SATTest;
import io.piotrjastrzebski.playground.ecs.tagtest.TagTest;
import io.piotrjastrzebski.playground.ecs.worldiotest.ECSWorldIOTest;
import io.piotrjastrzebski.playground.fogofwar.FogOfWarTest;
import io.piotrjastrzebski.playground.gpushadows.GpuShadowTest;
import io.piotrjastrzebski.playground.gpushadows.GpuShadows;
import io.piotrjastrzebski.playground.isotiled.*;
import io.piotrjastrzebski.playground.jobtest.AIECSJobTest;
import io.piotrjastrzebski.playground.jobtest.ECSJobTest;
import io.piotrjastrzebski.playground.jobtest.JobTest;
import io.piotrjastrzebski.playground.particletest.ParticleFaceTest;
import io.piotrjastrzebski.playground.shaders.Shader2dRainTest;
import io.piotrjastrzebski.playground.shaders.ShaderFireWallTest;
import io.piotrjastrzebski.playground.shaders.ShaderPortalTest;
import io.piotrjastrzebski.playground.shortcuts.ShortcutTest;
import io.piotrjastrzebski.playground.simple.*;
import io.piotrjastrzebski.playground.simplelights.SimpleLightTest;
import io.piotrjastrzebski.playground.tiledgentest.*;
import io.piotrjastrzebski.playground.tiledmapwidget.TMWTest;
import io.piotrjastrzebski.playground.uitesting.*;
import light2dtest.ShadowTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

public class PlaygroundGame extends Game implements GameReset {

	// should be a class that extends BaseScreen
	final Class[] testScreens = new Class[]{DungeonGeneratorTest.class, SATTest.class, ParticleFaceTest.class, SimpleTiledCropTest.class,
		SimpleBox2dTest.class, Box2dLoaderTest.class, RadialSpriteTest.class, Box2dSensorTest.class, UIReAddTest.class, FogOfWarTest.class,
		ShaderFireWallTest.class, ShaderPortalTest.class, Shader2dRainTest.class, ECSWorldIOTest.class, ECSOrderTest.class, JarUtilsTest.class,
		SimpleBTEditTest.class, BTEditTest2.class, SimpleTiledBrickTest.class, BTSaveTest.class, PixelArtUpsampleTest.class, UIUndoRedoTest.class,
		UIDaDSpriteTest.class, UILabelWrapTest.class, UIColorPickerTest.class, AshleyPhysicsTest.class, UISImpleGraphTest.class, ResolutionResolverTest.class,
		ECSJobsTest.class, ECSAIJobsTest.class, UISpriteDrawableTest.class, SimpleTiledTest.class, ParticleTest.class, CameraMoveTest.class,
		TiledGenTest.class, TemperatureTest.class, BiomeTest.class, Box2dInterpolationTest.class, AssetReloadTest.class, MouseDragTest.class,
		Box2dTiledTest.class, ParticleGUITest.class, ShortcutTest.class, SimpleTiledNonPotTest.class, UIDaDTest.class, UIReplaceFontTest.class,
		RainTest.class, BlurTest.class, CompositeGenTest.class, UITest.class, UISortTest.class, UISort2Test.class, CSTest.class, PathTest.class,
		UIFBOTransitionTest.class, QuadTreeTest.class, RandomTest.class, ECSAssetTest.class, GpuShadowTest.class, UIDoublePaneTest.class,
		ECSPooledCompTest.class, ECSProfilerTest.class, ECSShapeProfilerTest.class, ECSPolyProfilerTest.class, SplineTrailTest.class,
		CSTestV2.class, TagTest.class, EntityEditTest.class, EntityOneCompTest.class, DeferredSystemTest.class, AtlasSaveTest.class,
		AsyncScreenTest.class, UIPaneTest.class, UIContextMenuTest.class, UIContextMenuTest2.class, UIFillTest.class, UIDPITest.class,
		UIDialogTest.class, UITableBuilderTest.class, JobTest.class, ECSJobTest.class, AIECSJobTest.class, AIECSTest.class, InterpolationTest.class,
		ShadowTest.class, GpuShadows.class, CircleTest.class, ShapeRendererTest.class, BTreeTest.class, FitVPTest.class, VPTest.class,
		SimpleLightTest.class, UIImageButtonTest.class, IsoTest.class, UISelectRowTest.class, TMWTest.class, Box2dRayTest.class,
		ECSFancyWallsTest.class, PuncherTest.class, UIRotateTestTest.class, SimpleTouchTest.class, UITAPaneTest.class, BTEditTest.class,
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
		// enable markup so we can colorize parts of text
		VisUI.getSkin().get("default-font", BitmapFont.class)
			.getData().markupEnabled = true;
		reset();
	}

	public void reset () {
		setScreen(new TestSelectScreen(this));
	}

	private class TestSelectScreen extends BaseScreen {
		Array<VisTextButton> buttons = new Array<>();
		VisScrollPane pane;
		VisTable data;
		ObjectMap<VisTextButton, ClickListener> clickers = new ObjectMap<>();
		public TestSelectScreen (PlaygroundGame game) {
			super(game);
			root.add(new VisLabel("Select test to run, ESC to go back"));
			root.row();
			final VisTextField filter = new VisTextField("");
			filter.addListener(new ChangeListener() {
				 @Override public void changed (ChangeEvent event, Actor actor) {
					 filter(filter.getText());
				 }
			 });
			root.add(filter);
			root.row();

			data = new VisTable();
			Arrays.sort(testScreens, new Comparator<Class>() {
				@Override public int compare (Class o1, Class o2) {
					return o1.getSimpleName().compareTo(o2.getSimpleName());
				}
			});
			for (final Class cls : testScreens) {
				final VisTextButton button;
				button = new VisTextButton(cls.getSimpleName());
				ClickListener clicker;
				button.addListener(clicker = new ClickListener() {
					@Override public void clicked (InputEvent event, float x, float y) {
						try {
							Constructor constructor = cls.getConstructor(GameReset.class);
							setScreen((BaseScreen)constructor.newInstance(PlaygroundGame.this));
						} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
							e.printStackTrace();
						}
					}
				});
				clickers.put(button, clicker);
				buttons.add(button);
			}
			root.add(pane = new VisScrollPane(data));
			stage.setScrollFocus(pane);
			stage.setKeyboardFocus(filter);
			filter("");
		}

		private Array<VisTextButton> visible = new Array<>();
		private void filter (String text) {
			text = text.toLowerCase();
			data.clear();
			visible.clear();
			for (VisTextButton button : buttons) {
				if (button.getText().toString().toLowerCase().contains(text)) {
					visible.add(button);
					data.add(button);
					data.row();
				}
			}
		}

		int lastKey;
		@Override public boolean keyDown (int keycode) {
			if (keycode == Input.Keys.ESCAPE && lastKey == Input.Keys.ESCAPE) {
				Gdx.app.exit();
			} else if (keycode == Input.Keys.ENTER && visible.size > 0) {
				clickers.get(visible.first()).clicked(null, 0, 0);
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
