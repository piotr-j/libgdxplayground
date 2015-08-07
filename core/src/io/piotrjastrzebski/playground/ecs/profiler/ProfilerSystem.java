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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Sort;

import java.util.Comparator;

/**
 * Created by PiotrJ on 05/08/15.
 */
@Wire
@com.artemis.annotations.Profile(using = SystemProfiler.class, enabled = SystemProfiler.ENABLED)
public class ProfilerSystem extends BaseSystem {
	private static final float SAMPLE_LENGTH = 300f / SystemProfiler.SAMPLES;
	private static final int CHART_Y_SCALE = 30;
	private static int chartXStart;
	private static int textXStart;
	private static int yStart;
	private static int yChartStart;
	private static final float NANO_MULTI = 1 / 1000000f;
	private static final int TOP_N = 15;
	private static final int TEXT_Y_SPACE = 30;

	@Wire ShapeRenderer renderer;
	@Wire SpriteBatch batch;
	@Wire BitmapFont font;
	@Wire(name = "gui") OrthographicCamera camera;

	Comparator<SystemProfiler> byAvg = new Comparator<SystemProfiler>() {
		@Override
		public int compare(SystemProfiler o1, SystemProfiler o2) {
			return (int)(o2.getAverage() - o1.getAverage());
		}
	};

	public float update;
	Color color = new Color();
	SystemProfiler total = new SystemProfiler("Profiled");

	public ProfilerSystem() {

	}

	@Override
	protected void processSystem() {
		if (!isEnabled()) {
			return;
		}
		chartXStart = (int)(camera.viewportWidth * 0.05f);
		textXStart = (int)(camera.viewportWidth * 0.05f);
		yStart = (int)(camera.viewportHeight - 200);
		yChartStart = (int)(camera.viewportHeight - 650);

		batch.setProjectionMatrix(camera.combined);
		renderer.setProjectionMatrix(camera.combined);
		update += world.getDelta();
//		Sort.instance().sort(SystemProfiler.get());

		drawGraph(SystemProfiler.get());
//		Sort.instance().sort(SystemProfiler.get(), byAvg);
		drawText();
	}

	private void drawText() {
		batch.begin();

		drawChartYLabel(0, " 0");
		drawChartYLabel(1, " 1");
		drawChartYLabel(2, " 2");
		drawChartYLabel(3, " 4");
		drawChartYLabel(4, " 8");
		drawChartYLabel(5, "16");
		drawChartYLabel(6, "32");

		drawTopTimesText(SystemProfiler.get());

		batch.end();
	}

	private void drawTopTimesText(Array<SystemProfiler> profilers) {
		for (int i = 0; i < profilers.size && i < TOP_N; i++) {
			SystemProfiler profiler = profilers.get(i);
			if (update > 0.25f) {
//				profiler.updateMovingString();
			}
			setDrawColor(profiler);
			renderProfileText(color, i, profiler);
		}

		if (update > 0.25f) {
			update = 0;
//			total.updateMovingString();
//			SystemProfiler.FRAME.updateMovingString();
//			SystemProfiler.RENDER.updateMovingString();
//			SystemProfiler.GAME_LOGIC.updateMovingString();
		}

//		renderProfileText(color.set(1, 1, 1, 1), -4, SystemProfiler.FRAME);
		renderProfileText(color.set(1, 1, 0, 1), -3, total);
//		renderProfileText(color.set(1, 0, 0, 1), -2, SystemProfiler.RENDER);
//		renderProfileText(color.set(0, 1, 0, 1), -1, SystemProfiler.GAME_LOGIC);
	}

	private void drawChartYLabel(int ySpot, String label) {
		font.draw(batch, label, chartXStart - 35, yChartStart + 10 + ySpot * CHART_Y_SCALE);
	}

	private void setDrawColor(SystemProfiler profiler) {
		color.set(profiler.getColor());
	}

	private void drawGraph(Array<SystemProfiler> profilers) {
		renderer.begin(ShapeRenderer.ShapeType.Line);
		drawGraphAxis();
		long frameTotal = graphProfileTimes(profilers);
		graphTotalTime(frameTotal);
		renderer.end();
	}

	private void graphTotalTime(long frameTotal) {
		total.sample(frameTotal);
		renderGraph(chartXStart, yChartStart, color.set(1, 1, 0, 1), total);
//		renderGraph(chartXStart, yChartStart, color.set(1, 0, 0, 1), SystemProfiler.RENDER);
//		renderGraph(chartXStart, yChartStart, color.set(0, 1, 0, 1), SystemProfiler.GAME_LOGIC);
//		renderGraph(chartXStart, yChartStart, color.set(1, 1, 1, 1), SystemProfiler.FRAME);
	}

	private long graphProfileTimes(Array<SystemProfiler> profilers) {
		long frameTotal = 0;
		for (int i = 0; i < profilers.size && i < TOP_N; i++) {
			SystemProfiler profiler = profilers.get(i);
			setDrawColor(profiler);
			frameTotal += renderGraph(chartXStart, yChartStart, color, profiler);
		}
		return frameTotal;
	}

	private void drawGraphAxis() {
		renderer.setColor(0.6f, 0.6f, 0.6f, 1);
		renderer.line(chartXStart - 10, yChartStart,
			chartXStart + SystemProfiler.SAMPLES * SAMPLE_LENGTH, yChartStart);
		renderer.line(chartXStart, yChartStart - 10, chartXStart, yChartStart + 7 * CHART_Y_SCALE);

		renderer.setColor(0.25f, 0.25f, 0.25f, 1);
		for (int i = 1; i < 7; i++) {
			drawHorizontalLine(i);
		}
	}

	private void drawHorizontalLine(int ySpot) {
		renderer.line(chartXStart, yChartStart + ySpot * CHART_Y_SCALE, chartXStart + SystemProfiler.SAMPLES * SAMPLE_LENGTH,
			yChartStart + ySpot * CHART_Y_SCALE);
	}

	private long renderGraph(float x, float y, Color color, SystemProfiler profiler) {
		renderer.setColor(color);
		int current = profiler.index;
		for (int i = profiler.times.length - 1; i >= 1 ; i--) {
			int prev = current == 0 ? profiler.times.length - 1 : current - 1;
			float x1 = profiler.times[prev] * NANO_MULTI;
			float prevPoint = getPoint(x1);
			float x2 = profiler.times[current] * NANO_MULTI;
			float currentPoint = getPoint(x2);
			renderer.line(x + (i - 1) * SAMPLE_LENGTH,
				y + prevPoint * CHART_Y_SCALE,
				x + i * SAMPLE_LENGTH,
				y + currentPoint * CHART_Y_SCALE);
			current = prev;

		}
		return profiler.times[Math.abs(profiler.index - 1)];
	}

	private float getPoint(float sampleValue) {
		return sampleValue < 1 ? sampleValue : (MathUtils.log2(sampleValue) + 1);
	}

	private void renderProfileText(Color color, int i, SystemProfiler profiler) {
		font.setColor(color);
		font.draw(batch, profiler.toString(), textXStart,
			yStart - TEXT_Y_SPACE * i);
//		font.draw(batch, profiler.maxString, textXStart + 300,
//			yStart - TEXT_Y_SPACE * i);
//		font.draw(batch, profiler.localMaxString, textXStart + 400,
//			yStart - TEXT_Y_SPACE * i);
//		font.draw(batch, profiler.movingString, textXStart + 500,
//			yStart - TEXT_Y_SPACE * i);
	}


	@Override
	public boolean isEnabled() {
		return SystemProfiler.RUNNING;
	}
}
