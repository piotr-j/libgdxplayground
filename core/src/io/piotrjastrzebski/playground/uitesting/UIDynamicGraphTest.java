package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDynamicGraphTest extends BaseScreen {
	DynamicGraph graph;
	public UIDynamicGraphTest (GameReset game) {
		super(game);
		graph = new DynamicGraph(skin, renderer);
		root.add(graph).grow().pad(100);
	}

	float time;
	float source;
	float scale = 100;
	@Override public void render (float delta) {
		super.render(delta);

		renderer.setProjectionMatrix(guiCamera.combined);
		stage.act(delta);
		stage.draw();

		time += delta;
		source += delta;
		if (source > MathUtils.PI2) {
			source -= MathUtils.PI2;
			scale += 50;
		}
		float sample = MathUtils.sin(source) * scale;
//		if (sample < 0) sample = -sample;
		graph.addSample(time, sample);
	}

	static class DynamicGraph extends Widget {
		Skin skin;
		ShapeRenderer renderer;

		int sampleToShow = 1000;
		float sampleOffset = 0;
		float min = 0;
		float max = 50;
		Array<Sample> samples = new Array<>();
		BitmapFont font;
		long lastPan = 0;

		public DynamicGraph (Skin skin, ShapeRenderer renderer) {
			this.skin = skin;
			this.renderer = renderer;
			font = skin.getFont("default-font");
			addListener(new ActorGestureListener() {
				@Override
				public void pan (InputEvent event, float x, float y, float deltaX, float deltaY) {
					super.pan(event, x, y, deltaX, deltaY);
					if (getWidth() <= 0) return;

					float sampleLen = getWidth() / sampleToShow;
					float offset = deltaX/sampleLen;

					sampleOffset -= offset;
					sampleOffset = MathUtils.clamp(sampleOffset, 0, Math.max(samples.size -1, samples.size - sampleOffset));
					lastPan = TimeUtils.millis();
				}

				@Override
				public void panStop (InputEvent event, float x, float y, int pointer, int button) {
					super.panStop(event, x, y, pointer, button);
				}
			});
		}

		public void addSample (float time, float value) {
			//PLog.log("Sample: " + time + " -> " + value);
			samples.add(new Sample(time, value));
			if (value > max) max = value;
			if (value < min) min = value;
			if (samples.size > sampleToShow && TimeUtils.timeSinceMillis(lastPan) > 5000) {
				// smooth scroll?
				sampleOffset = samples.size - sampleToShow;
			}
			//samples.sort();
		}

		@Override
		public void draw (Batch batch, float parentAlpha) {
			super.draw(batch, parentAlpha);
			batch.end();
			renderer.begin(ShapeRenderer.ShapeType.Line);
			float x = getX();
			float y = getY();
			float width = getWidth();
			float height = getHeight();
			renderer.setColor(Color.CYAN);
			renderer.rect(x, y, width, height);
			drawGuides();
			if (samples.size >= 2) {
				drawGraph();
			}
			renderer.end();
			batch.begin();

			drawLabels(batch);
		}

		private void drawLabels (Batch batch) {
			float x = getX() + 5;
			float y = getY() + 25;
			float width = getWidth() - 10;
			float height = getHeight() - 50;

			float range = Math.abs(min) + Math.abs(max);
			// lets show 4 values, min max and two middle?

			renderer.setColor(.5f, .5f, .5f, .5f);

			{
				float n = normalize(0, height, min);
				String text = String.format("%.2f", min);
				font.draw(batch, text, x, y + n);
			}
			{
				float n = normalize(0, height, max);
				String text = String.format("%.2f", max);
				font.draw(batch, text, x, y + n);
			}
			for (float i = min + range/3; i < max; i += range/3) {
				float n = normalize(0, height, i);
				String text = String.format("%.2f", i);
				font.draw(batch, text, x, y + n);
			}
			// kinda want time at the bottom
			// probably dont want it to go nuts when we scrub

			int offset = MathUtils.round(sampleOffset);
			int showTimes = 5;
			int showStep = sampleToShow/showTimes;
			int start = offset/showStep;
			start *= showStep;
			float showStepWidth = getWidth()/showTimes;
			for (int i = start, n = Math.min(samples.size - 1, offset + 1 + sampleToShow); i < n; i+=showStep) {
				Sample sample = samples.get(i);
				float sx = showStepWidth * ( i - 1 - start)/showStep;

				String text = String.format("%.2f", sample.time);
				font.draw(batch, text, x + sx, y - 20);
			}
		}

		private void drawGuides () {
			float x = getX() + 5;
			float y = getY() + 25;
			float width = getWidth() - 10;
			float height = getHeight() - 50;

			float range = Math.abs(min) + Math.abs(max);
			// lets show 4 values, min max and two middle?

			renderer.setColor(.5f, .5f, .5f, .5f);
			{
				float n = normalize(0, height, min);
				renderer.line(x, y + n, x + width, y + n);
			}
			{
				float n = normalize(0, height, max);
				renderer.line(x, y + n, x + width, y + n);
			}
			for (float i = min + range/3; i < max; i += range/3) {
				float n = normalize(0, height, i);
				renderer.line(x, y + n, x + width, y + n);
			}
		}

		private void drawGraph () {
			float x = getX() + 25;
			float y = getY() + 25;
			float width = getWidth() - 50;
			float height = getHeight() - 50;


			renderer.setColor(Color.ORANGE);
			float sampleLen = width / sampleToShow;
			int offset = MathUtils.round(sampleOffset);
			Sample first = samples.get(offset);
			for (int i = 1 + offset, n = Math.min(samples.size - 1, offset + 1 + sampleToShow); i < n; i++) {
				Sample second = samples.get(i);
				float sx1 = sampleLen * ( i - 1 - offset);
				float sy1 = normalize(0, height, first.value);
				float sx2 = sampleLen * (i - offset);
				float sy2 = normalize(0, height, second.value);

				renderer.line(x + sx1, y + sy1, x + sx2, y + sy2);

				first = second;
			}
		}

		private float normalize (float y, float height, float value) {
			return MathUtils.map(min, max, y, height, value);
		}
	}

	static class Sample implements Comparable<Sample> {
		float time;
		float value;

		public Sample (float time, float value) {
			this.time = time;
			this.value = value;
		}

		@Override
		public int compareTo (Sample o) {
			return Float.compare(time, o.time);
		}
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setWindowedMode(1280/3 * 2, 720/3 * 2);
		PlaygroundGame.start(args, config, UIDynamicGraphTest.class);
	}
}
