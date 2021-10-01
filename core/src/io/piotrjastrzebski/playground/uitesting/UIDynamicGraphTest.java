package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDynamicGraphTest extends BaseScreen {
	ShapeDrawer drawer;
	Array<DynamicGraph> graphs;

	public UIDynamicGraphTest (GameReset game) {
		super(game);


		graphs = new Array<>();
		{
			DynamicGraph graph = new RendererDynamicGraph(skin, renderer);
			root.add(graph).grow().pad(50);
			graphs.add(graph);
		}
		{
			DynamicGraph graph = new DrawerDynamicGraph(skin, shapes);
			root.add(graph).grow().pad(50);
			graphs.add(graph);
		}
		if (false){
			// it seems it sets the uv to single pixel in middle or whatever :<
			Pixmap pixmap = new Pixmap(5, 5, Pixmap.Format.RGBA8888);
			pixmap.setColor(Color.CLEAR);
//			pixmap.setColor(Color.WHITE);
			pixmap.fill();
			pixmap.setColor(Color.WHITE);
			pixmap.fillRectangle(1, 1, 3, 3);
			drawer = new ShapeDrawer(batch, new TextureRegion(new Texture(pixmap)));
			DynamicGraph graph = new DrawerDynamicGraph(skin, drawer);
			root.add(graph).grow().pad(50);
			graphs.add(graph);
		}



	}

	float time;
	float source;
	float scale = 100;
	float sampleDelay;
	float sampleRate = .1f;
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
		sampleDelay += delta;
		if (sampleDelay < sampleRate) return;
		sampleDelay -= sampleRate;
		float sample = MathUtils.sin(source) * scale;
//		if (sample < 0) sample = -sample;
		for (DynamicGraph graph : graphs) {
			graph.addSample(time, sample);
		}

	}

	static class RendererDynamicGraph extends DynamicGraph {
		ShapeRenderer renderer;
		public RendererDynamicGraph (Skin skin, ShapeRenderer renderer) {
			super(skin);
			this.renderer = renderer;
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
				setDrawColor(1, .5f, 0, 1);
				drawGraph();
			}
			renderer.end();
			Gdx.gl.glLineWidth(1);
			batch.begin();

			drawLabels(batch);
		}

		@Override
		protected void setDrawColor (float r, float g, float b, float a) {
			renderer.setColor(r, g, b, a);
		}

		float lineWidth = 1;
		@Override
		protected void line (float x1, float y1, float x2, float y2, float width) {
			if (lineWidth != width) {
				lineWidth = width;
				renderer.flush();
				Gdx.gl.glLineWidth(width);
			}
			renderer.line(x1, y1, x2, y2);
		}
	}

	static class DrawerDynamicGraph extends DynamicGraph {
		ShapeDrawer drawer;

		public DrawerDynamicGraph (Skin skin, ShapeDrawer drawer) {
			super(skin);
			this.drawer = drawer;
		}

		FloatArray path = new FloatArray();
		@Override
		public void draw (Batch batch, float parentAlpha) {
			super.draw(batch, parentAlpha);
			batch.enableBlending();
			float x = getX();
			float y = getY();
			float width = getWidth();
			float height = getHeight();
			drawer.setColor(Color.CYAN);
			drawer.rectangle(x, y, width, height, 1f);
			drawGuides();
			if (samples.size >= 2) {
				setDrawColor(1, .5f, 0, .3f);
				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
				path.clear();
				drawGraph();
				drawPath();
				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			}

			drawLabels(batch);
		}

		private void drawPath () {
			// if the path is very dense, this will be quite slow depending on number of passes
			// 2-3 passes seems sufficient, unless path is very wide
			float width = pathWidth;
			int passes = 3;
			float step = width/passes;
			while (width > 0f) {
				drawer.path(path, width, JoinType.SMOOTH, true);
				width -= step;
			}
		}

		@Override
		protected void setDrawColor (float r, float g, float b, float a) {
			drawer.setColor(r, g, b, a);
		}

		float pathWidth = 1;
		@Override
		protected void line (float x1, float y1, float x2, float y2, float width) {
			if (width <= 1) {
				drawer.line(x1, y1, x2, y2, width);
				return;
			}
			pathWidth = width;
//			path.add(x1, y1, x2, y2);
			path.add(x1, y1);
		}
	}

	static abstract class DynamicGraph extends Widget {
		Skin skin;

		int sampleToShow = 100;
		float sampleOffset = 0;
		float min = 0;
		float max = 50;
		Array<Sample> samples = new Array<>();
		BitmapFont font;
		long lastPan = 0;

		public DynamicGraph (Skin skin) {
			this.skin = skin;
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

		protected void drawLabels (Batch batch) {
			float x = getX() + 5;
			float y = getY() + 25;
			float width = getWidth() - 10;
			float height = getHeight() - 50;

			float range = Math.abs(min) + Math.abs(max);
			// lets show 4 values, min max and two middle?

			setDrawColor(.5f, .5f, .5f, .5f);

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

		protected void drawGuides () {
			float x = getX() + 5;
			float y = getY() + 25;
			float width = getWidth() - 10;
			float height = getHeight() - 50;

			float range = Math.abs(min) + Math.abs(max);
			// lets show 4 values, min max and two middle?

			setDrawColor(.5f, .5f, .5f, .5f);
			{
				float n = normalize(0, height, min);
				line(x, y + n, x + width, y + n, 1);
			}
			{
				float n = normalize(0, height, max);
				line(x, y + n, x + width, y + n, 1);
			}
			for (float i = min + range/3; i < max; i += range/3) {
				float n = normalize(0, height, i);
				line(x, y + n, x + width, y + n, 1);
			}
		}

		protected void drawGraph () {
			float x = getX() + 25;
			float y = getY() + 25;
			float width = getWidth() - 50;
			float height = getHeight() - 50;


			float sampleLen = width / sampleToShow;
			int offset = MathUtils.round(sampleOffset);
			Sample first = samples.get(offset);
			for (int i = 1 + offset, n = Math.min(samples.size - 1, offset + 1 + sampleToShow); i < n; i++) {
				Sample second = samples.get(i);
				float sx1 = sampleLen * ( i - 1 - offset);
				float sy1 = normalize(0, height, first.value);
				float sx2 = sampleLen * (i - offset);
				float sy2 = normalize(0, height, second.value);

				line(x + sx1, y + sy1, x + sx2, y + sy2, 10);

				first = second;
			}
		}

		private float normalize (float y, float height, float value) {
			return MathUtils.map(min, max, y, height, value);
		}

		protected abstract void setDrawColor(float r, float g, float b, float a);
		protected abstract void line(float x1, float y1, float x2, float y2, float width);
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
		config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
//		config.setWindowedMode(1280/3 * 2, 720/3 * 2);
		PlaygroundGame.start(args, config, UIDynamicGraphTest.class);
	}
}
