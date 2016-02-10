package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class Noise1DTest extends BaseScreen {
	private static final String TAG = Noise1DTest.class.getSimpleName();

	Noise1D noise1D = new Noise1D();
	public Noise1DTest (GameReset game) {
		super(game);

	}

	@Override public void render (float delta) {
		super.render(delta);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.GREEN);
		renderer.line(0, 50, 1000, 50);
		renderer.setColor(Color.WHITE);
		for (int x = 0; x < 1000; x++) {
			float y1 = noise1D.getVal2(x);
			float y2 = noise1D.getVal2(x + 1);
			renderer.line(x, y1 + 50, x + 1, y2 + 50);
		}

		renderer.setColor(Color.GREEN);
		renderer.line(0, 200, 1000, 200);
		renderer.setColor(Color.WHITE);
		for (int x = 0; x < 1000; x++) {
			float y1 = noise1D.getVal2(x + 100);
			float y2 = noise1D.getVal2(x + 101);
			renderer.line(x, y1 + 200, x + 1, y2 + 200);
		}
		renderer.end();
	}

	public static class Noise1D {
		int MAX_VERTICES = 256;
		int MAX_VERTICES_MASK = MAX_VERTICES - 1;
		float amplitude = 50;
		float scale = 0.1f;

		float r[] = new float[MAX_VERTICES];

		public Noise1D () {
			for(int i = 0; i< MAX_VERTICES;++i) {
				r[i] = MathUtils.random();
			}
		}

		float getVal ( float x ){
			float scaledX = x * scale;
			int xFloor = (int)Math.floor(scaledX);
			float t = scaledX - xFloor;
			float tRemapSmoothstep = t * t * ( 3 - 2 * t );

			/// Modulo using &
			int xMin = xFloor & MAX_VERTICES_MASK;
			int xMax = ( xMin + 1 ) & MAX_VERTICES_MASK;

			float y = lerp( r[ xMin ], r[ xMax ], tRemapSmoothstep );

			return y * amplitude;
		}

		float getVal2 (float x) {
			return getVal(x) - amplitude/2;
		}

		/**
		 * Linear interpolation function.
		 * @param a The lower integer value
		 * @param b The upper integer value
		 * @param t The value between the two
		 * @returns {number}
		 */
		float lerp (float a, float b, float t) {
			return a * ( 1 - t ) + b * t;
		};
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, Noise1DTest.class);
	}
}
