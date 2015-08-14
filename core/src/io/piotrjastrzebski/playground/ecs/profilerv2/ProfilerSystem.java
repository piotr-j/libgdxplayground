package io.piotrjastrzebski.playground.ecs.profilerv2;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.playground.ecs.profilerv2.lib.SystemProfiler;
import io.piotrjastrzebski.playground.ecs.profilerv2.lib.SystemProfilerGUI;

/**
 * Created by PiotrJ on 05/08/15.
 */
@Wire
public class ProfilerSystem extends BaseSystem {
	@Wire ShapeRenderer renderer;
	@Wire(name = "gui") OrthographicCamera camera;
	@Wire Stage stage;

	public ProfilerSystem () {
	}

	SystemProfilerGUI gui;

	@Override protected void initialize () {
		super.initialize();

		// add some global profilers
		SystemProfiler.add(new SystemProfiler("CrazyLongSystemNameProfilerStuff")).setColor(1, 0, 0, 1);
//		SystemProfiler.add(new SystemProfiler("Render")).setColor(1, 0, 0, 1);
		// resume profiling
		SystemProfiler.resume();
		SystemProfilerGUI.STYLE_SMALL = "small";
		gui = new SystemProfilerGUI(VisUI.getSkin(), "default");
		gui.show(stage);
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

	@Override protected void dispose () {
		super.dispose();
		SystemProfiler.dispose();
	}
}
