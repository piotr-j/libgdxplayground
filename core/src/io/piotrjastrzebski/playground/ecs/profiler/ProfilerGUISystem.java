package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;

/**
 * Created by PiotrJ on 05/08/15.
 */
@Wire
@com.artemis.annotations.Profile(using = SystemProfiler.class, enabled = SystemProfiler.ENABLED)
public class ProfilerGUISystem extends BaseSystem {
	GUISystem guiSystem;

	@Wire ShapeRenderer renderer;
	@Wire(name = "gui") OrthographicCamera camera;

	public ProfilerGUISystem () {

	}

	SystemProfilerGUI gui;

	@Override protected void initialize () {
		super.initialize();

		// add some global profilers
		SystemProfiler.add(new SystemProfiler("Logic")).setColor(1, 0, 0, 1);
		SystemProfiler.add(new SystemProfiler("Render")).setColor(1, 0, 0, 1);

		SystemProfilerGUI.STYLE_SMALL = "small";
		gui = new SystemProfilerGUI(VisUI.getSkin(), "default");

		VisTextButton showProfiler = new VisTextButton("SHOW");
		showProfiler.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				toggleWindow();
			}
		});
		toggleWindow();
		guiSystem.addActor(showProfiler);
	}

	public boolean toggleWindow () {
		Group parent = gui.getParent();
		if (parent != null) {
			gui.hide();
			return false;
		} else {
			guiSystem.addActor(gui);
			gui.show();
			return true;
		}
	}

	@Override
	protected void processSystem() {
		if (!isEnabled() || gui.getParent() == null) {
			return;
		}

		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		gui.updateAndRender(world.delta, renderer);
		renderer.end();
	}

	@Override
	public boolean isEnabled() {
		return SystemProfiler.RUNNING;
	}

	@Override protected void dispose () {
		super.dispose();
		SystemProfiler.dispose();
	}
}
