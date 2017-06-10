package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class ArcTest extends BaseScreen {
	private static final String TAG = ArcTest.class.getSimpleName();
	Arc arc;
	public ArcTest (GameReset game) {
		super(game);
		arc = new Arc();
	}

	protected abstract static class Action3<T1, T2, T3> {
		public abstract void call(T1 p1, T2 p2, T3 p3);
	}

	@Override public void render (float delta) {
		super.render(delta);
		enableBlending();

		arc.setProjectionMatrix(gameCamera.combined);

		arc.begin(ShapeRenderer.ShapeType.Line);
		arc.setColor(Color.CYAN);
		arc.arc(0, 0, 10, 45, 90, 24);

		arc.end();

		arc.begin(ShapeRenderer.ShapeType.Filled);
		arc.setColor(Color.MAGENTA);

		arc.end();

	}

	static class Arc extends ShapeRenderer {

		private final ImmediateModeRenderer renderer;
		private final Color color = new Color(1, 1, 1, 1);

		public Arc(){
			renderer = super.getRenderer();
		}

		/** Draws an arc using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
		public void arc (float x, float y, float radius, float start, float degrees) {
			int segments = (int)(6 * (float)Math.cbrt(radius) * (degrees / 360.0f));
			arc(x, y, radius, start, degrees, segments);
		}

		public void arc (float x, float y, float radius, float start, float degrees, int segments ) {
			if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
			float colorBits = color.toFloatBits();
			float theta = (2 * MathUtils.PI * (degrees / 360.0f)) / segments;
			float cos = MathUtils.cos(theta);
			float sin = MathUtils.sin(theta);
			float cx = radius * MathUtils.cos(start * MathUtils.degreesToRadians);
			float cy = radius * MathUtils.sin(start * MathUtils.degreesToRadians);

			for (int i = 0; i < segments; i++) {
				renderer.color(colorBits);
				renderer.vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				renderer.color(colorBits);
				renderer.vertex(x + cx, y + cy, 0);
			}
		}
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.samples = 4;
		PlaygroundGame.start(args, config, ArcTest.class);
	}
}
