package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Sort;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;

import java.util.Comparator;

/**
 * Created by PiotrJ on 05/08/15.
 */
@Wire
@com.artemis.annotations.Profile(using = SystemProfiler.class, enabled = SystemProfiler.ENABLED)
public class ProfilerGUISystem extends BaseSystem implements ProfilerConfig {
	private final static String TAG = ProfilerGUISystem.class.getSimpleName();

	GUISystem guiSystem;

	SystemProfiler total = new SystemProfiler("Profiled");

	@Wire ShapeRenderer renderer;
	@Wire(name = "gui") OrthographicCamera camera;

	public ProfilerGUISystem () {

	}

	VisWindow window;
	Graph graph;
	VisTable profilersContainer;
	@Override protected void initialize () {
		super.initialize();

		SystemProfiler.add(total).setColor(1, 1, 0, 1);
		SystemProfiler.add(new SystemProfiler("Logic")).setColor(1, 0, 0, 1);
		SystemProfiler.add(new SystemProfiler("Render")).setColor(1, 0, 0, 1);
		SystemProfiler.add(new SystemProfiler("Frame")).setColor(1, 0, 0, 1);

		window = new VisWindow("Profiling");
		window.setResizeBorder(16);
		VisTable root = new VisTable(true);
//		window.addCloseButton();
		VisTable graphContainer = new VisTable();
		VisTable labels = new VisTable();
		labels.add(label("32")).expandY().center().row();
		labels.add(label("16")).expandY().center().row();
		labels.add(label("8")).expandY().center().row();
		labels.add(label("4")).expandY().center().row();
		labels.add(label("2")).expandY().center().row();
		labels.add(label("1")).expandY().center().row();
		labels.add(label("0")).expandY().center().row();
		graphContainer.add(labels).expandY().fillY();
		graph = new Graph();
//		graph.setSize(200, 200);

		graphContainer.add(graph).expand().fill();

		profilersContainer = new VisTable();
//		row("Profiler 1", profilersContainer);
//		row("Profiler 2", profilersContainer);
//		row("Profiler 3", profilersContainer);
//		row("ProfilerProfiler 4", profilersContainer);
//		row("Profiler 5", profilersContainer);
//		row("Profiler 6", profilersContainer);
//		row("Profiler 7", profilersContainer);
//		row("ProfilerProfiler 8", profilersContainer);
//		row("Profiler 9", profilersContainer);
//		row("ProfilerProfiler 10", profilersContainer);

		VisScrollPane pane = new VisScrollPane(profilersContainer);
		pane.setScrollingDisabled(true, false);
		root.add(graphContainer).expand(3, 1).fill().pad(10, 10, 10, 0);
		root.add(pane).expandX().fillX().top().pad(10);

		window.add(root).expand().fill();
		window.setPosition(0, 0);
		window.setResizable(true);
		window.pack();

//		window.debugAll();
		guiSystem.addActor(window);
	}

	Comparator<SystemProfiler> byAvg = new Comparator<SystemProfiler>() {
		@Override
		public int compare(SystemProfiler o1, SystemProfiler o2) {
			return (int)(o2.getAverage() - o1.getAverage());
		}
	};


	private VisLabel label(String text) {
		return new VisLabel(text, "small");
	}

	Vector2 temp = new Vector2();
	@Override
	protected void processSystem() {
		if (!isEnabled()) {
			return;
		}

		updateProfilers();
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);

		graph.localToStageCoordinates(temp.setZero());
		drawGraph(temp.x, temp.y, graph.getWidth(), graph.getHeight());
		renderer.end();
	}

	Array<ProfilerRow> rows = new Array<>();
	private float REFRESH_RATE = 0.25f;
	float refreshTime = REFRESH_RATE;
	private void updateProfilers () {
		refreshTime += world.delta;
		if (refreshTime < REFRESH_RATE) return;
		refreshTime -= REFRESH_RATE;

		Sort.instance().sort(SystemProfiler.get(), byAvg);
		Array<SystemProfiler> profilers = SystemProfiler.get();
		Pools.freeAll(rows);
		profilersContainer.clear();
		for (SystemProfiler profiler : profilers) {
			ProfilerRow row = Pools.obtain(ProfilerRow.class);
			rows.add(row);
			row.init(profiler, profilersContainer);
		}
		window.invalidate();
	}

	private Actor row (SystemProfiler profiler, VisTable container) {
		return container;
	}

	private void drawGraph (float x, float y, float width, float height) {
		Sort.instance().sort(SystemProfiler.get());
		// we do this so the logical 0 and top are in the middle of the labels
		drawGraphAxis(x, y, width, height);
		float sep = height / 7;
		y += sep /2;
		height -= sep;
		long frameTotal = 0;
		for (int i = 0; i < SystemProfiler.sixe() && i < TOP_N; i++) {
			frameTotal += SystemProfiler.get(i).getCurrentSample();
		}
		total.sample(frameTotal - total.getCurrentSample());
		graphProfileTimes(x, y, width, height, SystemProfiler.get());
//		graphTotalTime(x, y, width, height, frameTotal);
//		renderGraph(x, y, width, height, total);
	}

//	private static final float SAMPLE_LENGTH = 300f / SystemProfiler.SAMPLES;
//	private static final int CHART_Y_SCALE = 30;
	private void drawGraphAxis (float x, float y, float width, float height) {
		float sep = height / 7;
		y += sep / 2;
		renderer.setColor(0.6f, 0.6f, 0.6f, 1);
		renderer.line(x, y, x, y + height - sep);
		renderer.line(x + width, y, x + width, y + height - sep);

		renderer.setColor(0.25f, 0.25f, 0.25f, 1);
		for (int i = 0; i < 7; i++) {
			renderer.line(x, y + i * sep , x + width, y + i * sep);
		}
	}

	private void graphTotalTime (float x, float y, float width, float height, long frameTotal) {
		total.sample(frameTotal - total.getCurrentSample());
//		renderGraph(x, y, width, height, total);
//		renderGraph(x, y, width, height, SystemProfiler.RENDER);
//		renderGraph(x, y, width, height, SystemProfiler.GAME_LOGIC);
//		renderGraph(x, y, width, height, SystemProfiler.FRAME);
	}

	private static final int TOP_N = 15;
	private static final float NANO_MULTI = 1 / 1000000f;
	private void graphProfileTimes(float x, float y, float width, float height, Array<SystemProfiler> profilers) {
		for (int id = 0; id < profilers.size && id < TOP_N; id++) {
			SystemProfiler profiler = profilers.get(id);
			renderer.setColor(profiler.color);
			float sampleLen = width/SystemProfiler.SAMPLES;
			int current = profiler.index;
			for (int i = profiler.times.length - 1; i >= 1 ; i--) {
				int prev = current == 0 ? profiler.times.length - 1 : current - 1;
				float x1 = profiler.times[prev] * NANO_MULTI;
				float prevPoint = getPoint(x1);
				float x2 = profiler.times[current] * NANO_MULTI;
				float currentPoint = getPoint(x2);
				renderer.line(x + (i - 1) * sampleLen,
					y + prevPoint * height/6,
					x + i * sampleLen,
					y + currentPoint * height/6);
				current = prev;
			}
		}
	}


	private float getPoint(float sampleValue) {
		return sampleValue < 1 ? sampleValue : (MathUtils.log2(sampleValue) + 1);
	}

	@Override
	public boolean isEnabled() {
		return SystemProfiler.SHOW;
	}

	@Override protected void dispose () {
		super.dispose();
		SystemProfiler.dispose();
	}

	@Override public Type getType () {
		return Type.DEBUG;
	}

	@Override public float getRefreshRate () {
		return 0;
	}

	@Override public void setColor (Color color) {

	}

	@Override public String getName () {
		return TAG;
	}

	public static class ProfilerRow implements Pool.Poolable {
		VisLabel name, max, localMax, avg;
		public ProfilerRow () {
			name = new VisLabel("", "small");
			max = new VisLabel("", "small");
			localMax = new VisLabel("", "small");
			avg = new VisLabel("", "small");
		}

		public void init (SystemProfiler profiler, VisTable container) {
			name.setText(profiler.getName());
			name.setColor(profiler.getColor());
			max.setText(String.format("%.2f", profiler.getMax()));
			max.setColor(profiler.getColor());
			localMax.setText(String.format("%.2f", profiler.getLocalMax()));
			localMax.setColor(profiler.getColor());
			avg.setText(String.format("%.2f", profiler.getMovingAvg()));
			avg.setColor(profiler.getColor());

			container.add(name).expandX().fill();
			container.add(max).right().padRight(10);
			container.add(localMax).right().padRight(10);
			container.add(avg).right();
			container.row();
		}

		@Override public void reset () {

		}
	}


	private class Graph extends Table {
		public Graph () {

		}

		@Override public float getMinWidth () {
			return 300;
		}

		@Override public float getMinHeight () {
			return 200;
		}

		public float x() {
			return getX();
		}
	}
}
