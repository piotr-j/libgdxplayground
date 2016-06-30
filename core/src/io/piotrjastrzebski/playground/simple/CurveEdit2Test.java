package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class CurveEdit2Test extends BaseScreen {
	private static final String TAG = CurveEdit2Test.class.getSimpleName();

	Curves curves;
	public CurveEdit2Test (GameReset game) {
		super(game);
		clear.set(Color.GRAY);

		curves = new Curves();
	}

	@Override public void render (float delta) {
		super.render(delta);

		curves.update(delta);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		curves.draw(renderer);
		renderer.end();
	}

	protected static class Curves {
		Array<Curve> curves = new Array<>();

		public Curves () {
			Curve curve = new Curve();
			curve.startHandle.set(0, 3);
			curve.endHandle.set(3, 5);
			curve.end.set(5, 5);
			curve.rebuild();
			curves.add(curve);
		}

		private float doubleClickDelay = .1f;
		private float doubleClickTimer;
		public void update(float delta) {
			if (doubleClickTimer > 0) doubleClickTimer -= delta;
		}

		public void draw (ShapeRenderer renderer) {
			for (Curve curve : curves) {
				curve.draw(renderer);
			}
		}

		public boolean touchDown (float x, float y, int button) {
			// do we care about the buttons?
			if (doubleClickTimer <= 0) {
				doubleClickTimer = doubleClickDelay;



			} else {


			}
			return true;
		}

		public boolean touchDragged (float x, float y) {

			return true;
		}

		public boolean touchUp (float x, float y, int button) {

			return true;
		}
	}

	protected static class Curve {
		private static final int APPROX_LEN_SAMPLES = 50;
		private static final int SAMPLES_PER_UNIT_LEN = 3;
		private static final int BASE_ITERS = 32;
		private static final float BASE_STEP = 1f/BASE_ITERS;
		private float iters = BASE_ITERS;
		private float step = 1f/iters;

		protected Vector2 start = new Vector2();
		protected Vector2 startHandle = new Vector2();
		protected Vector2 endHandle = new Vector2();
		protected Vector2 end = new Vector2();
		private Vector2 tmp = new Vector2();
		private Vector2 tmp2 = new Vector2();
		private Vector2 tmp3 = new Vector2();

		private Array<Vector2> cache = new Array<>();
		private static Pool<Vector2> cachePool = new Pool<Vector2>() {
			@Override protected Vector2 newObject () {
				return new Vector2();
			}
		};

		public Curve () {

		}

		public void draw(ShapeRenderer renderer) {
			if (cache.size == 0) return;
			renderer.setColor(Color.CYAN);
			tmp.set(cache.get(0));
			renderer.circle(tmp.x, tmp.y, .1f, 8);
			for (int i = 1; i < cache.size; i++) {
				tmp2.set(cache.get(i));
				renderer.line(tmp, tmp2);
				renderer.circle(tmp2.x, tmp2.y, .1f, 8);
				tmp.set(tmp2);
			}
		}

		protected void rebuild() {
			float len = approxLength(APPROX_LEN_SAMPLES);
			// we want 10 samples per 1 unit of length
			int iters = (int)(len * SAMPLES_PER_UNIT_LEN);
			float step = 1f/iters;
			float at = 0;
			cachePool.freeAll(cache);
			cache.clear();
			valueAt(tmp, at);
			cache.add(cachePool.obtain().set(tmp));
			while (at < 1) {
				at += step;
				valueAt(tmp2, at);
				cache.add(cachePool.obtain().set(tmp2));
				tmp.set(tmp2);
			}
		}

		protected float approxLength (int samples) {
			float tempLength = 0;
			for (int i = 0; i < samples; ++i) {
				tmp2.set(tmp3);
				valueAt(tmp3, (i) / ((float)samples - 1));
				if (i > 0) tempLength += tmp2.dst(tmp3);
			}
			return tempLength;
		}

		protected Vector2 valueAt (Vector2 out, float at) {
			return Bezier.cubic(out, at, start, startHandle, endHandle, end, tmp);
		}

		protected Vector2 derivativeAt (Vector2 out, float at) {
			return Bezier.cubic_derivative(out, at, start, startHandle, endHandle, end, tmp);
		}
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		return curves.touchDown(cs.x, cs.y, button);
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		super.touchDragged(screenX, screenY, pointer);
		return curves.touchDragged(cs.x, cs.y);
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		super.touchUp(screenX, screenY, pointer, button);
		return curves.touchUp(cs.x, cs.y, button);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, CurveEdit2Test.class);
	}
}
