package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
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
		private float handleRadius = .25f;

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
				drawHandles(renderer, curve);
				curve.draw(renderer);
			}
		}

		private void drawHandles (ShapeRenderer renderer, Curve curve) {
			renderer.setColor(Color.BLACK);
			renderer.circle(curve.start.x, curve.start.y, .2f, 16);
			renderer.circle(curve.startHandle.x, curve.startHandle.y, .25f, 16);
			renderer.line(curve.start.x, curve.start.y, curve.startHandle.x, curve.startHandle.y);
			renderer.circle(curve.endHandle.x, curve.endHandle.y, .25f, 16);
			renderer.circle(curve.end.x, curve.end.y, .2f, 16);
			renderer.line(curve.end.x, curve.end.y, curve.endHandle.x, curve.endHandle.y);
		}

		private CurveEdit curveEdit = new CurveEdit();
		private Circle tmpCircle = new Circle();
		public boolean touchDown (float x, float y, int button) {
			// do we care about the buttons?
			if (doubleClickTimer <= 0) {
				doubleClickTimer = doubleClickDelay;

				tmpCircle.set(x, y, handleRadius);
				// find handle to drag
				for (Curve curve : curves) {
					if (tmpCircle.contains(curve.startHandle)) {
						curveEdit.curve = curve;
						curveEdit.dragged = curve.startHandle;
						curveEdit.dragOffset.set(curve.startHandle).sub(x, y);
						System.out.println("got start handle");
						break;
					}
					if (tmpCircle.contains(curve.endHandle)) {
						curveEdit.curve = curve;
						curveEdit.dragged = curve.endHandle;
						curveEdit.dragOffset.set(curve.endHandle).sub(x, y);
						System.out.println("got end handle");
						break;
					}
				}
				if (curveEdit.curve == null) {
					// if not handle, get endpoint
					for (Curve curve : curves) {
						if (tmpCircle.contains(curve.start)) {
							curveEdit.curve = curve;
							curveEdit.dragged = curve.start;
							curveEdit.dragOffset.set(curve.start).sub(x, y);
							curveEdit.handle = curve.startHandle;
							curveEdit.handleOffset.set(curve.startHandle).sub(curve.start);
							System.out.println("got start");
							break;
						}
						if (tmpCircle.contains(curve.end)) {
							curveEdit.curve = curve;
							curveEdit.dragged = curve.end;
							curveEdit.dragOffset.set(curve.end).sub(x, y);
							curveEdit.handle = curve.endHandle;
							curveEdit.handleOffset.set(curve.endHandle).sub(curve.end);
							System.out.println("got end");
							break;
						}
					}
				}
			} else {
				// on empty space, create

				// on curve, split

				// on end/start handle, remove handle, merge curves
			}
			return true;
		}

		public boolean touchDragged (float x, float y) {
			// drag handle if we have one
			if (curveEdit.curve != null) {
				curveEdit.dragged.set(x, y).add(curveEdit.dragOffset);
				if (curveEdit.handle != null) {
					curveEdit.handle.set(curveEdit.dragged).add(curveEdit.handleOffset);
				}
				curveEdit.curve.rebuild();
			}
			return true;
		}

		public boolean touchUp (float x, float y, int button) {
			if (curveEdit.curve != null) {
				curveEdit.dragged.set(x, y).add(curveEdit.dragOffset);
				if (curveEdit.handle != null) {
					curveEdit.handle.set(curveEdit.dragged).add(curveEdit.handleOffset);
				}
				curveEdit.curve.rebuild();
				curveEdit.curve = null;
				curveEdit.dragged = null;
				curveEdit.handle = null;
			}
			return true;
		}

		protected static class CurveEdit {
			Curve curve;
			Vector2 dragged;
			Vector2 dragOffset = new Vector2();
			Vector2 handle;
			Vector2 handleOffset = new Vector2();
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
		private static Vector2 tmp = new Vector2();
		private static Vector2 tmp2 = new Vector2();
		private static Vector2 tmp3 = new Vector2();
		private static Vector2 tmp4 = new Vector2();
		private static Vector2 tmp5 = new Vector2();

		private Array<Vector2> cache = new Array<>();

		private Polygon polygon = new Polygon();
		private static Pool<Vector2> cachePool = new Pool<Vector2>() {
			@Override protected Vector2 newObject () {
				return new Vector2();
			}
		};

		public Curve () {

		}

		public void draw(ShapeRenderer renderer) {
			int size = cache.size;
			if (size == 0) return;
			float step = 1f/size;
			float a = 0;
			renderer.setColor(Color.CYAN);
			tmp.set(cache.get(0));
			tmp2.set(cache.get(1));

			renderer.setColor(Color.RED);
			tmp5.set(tmp2).sub(tmp).nor().scl(.25f).rotate(90);
			renderer.line(tmp.x, tmp.y, tmp.x + tmp5.x, tmp.y + tmp5.y);
			renderer.line(tmp.x, tmp.y, tmp.x - tmp5.x, tmp.y - tmp5.y);

			for (int i = 1; i < size; i++) {
				tmp2.set(cache.get(i));
				renderer.setColor(a, 1, 1 - a, 1);
				renderer.line(tmp, tmp2);

				renderer.setColor(Color.RED);
				tmp5.set(tmp).sub(tmp2).nor().scl(.25f).rotate(90);
				renderer.line(tmp2.x, tmp2.y, tmp2.x + tmp5.x, tmp2.y + tmp5.y);
				renderer.line(tmp2.x, tmp2.y, tmp2.x - tmp5.x, tmp2.y - tmp5.y);

				a += step;

				tmp.set(tmp2);
				tmp3.set(tmp4);
			}
		}

		protected void rebuild() {
			float len = approxLength(APPROX_LEN_SAMPLES);
			// we want 10 samples per 1 unit of length
			int iters = (int)(len * SAMPLES_PER_UNIT_LEN);
			float step = 1f/iters;
			// we need extra start point, so we start before 0
			float at = -step;
			at = 0;
			cachePool.freeAll(cache);
			cache.clear();
			valueAt(tmp, at);
			cache.add(cachePool.obtain().set(tmp));
			while (at < 1) {
				at += step;
				if (at > 1){
					at = 1;
					valueAt(tmp2, at);
					if (!cache.get(cache.size -1).epsilonEquals(tmp2, .01f)) {
						cache.add(cachePool.obtain().set(tmp2));
					}
				} else {
					valueAt(tmp2, at);
					cache.add(cachePool.obtain().set(tmp2));
				}
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

		private static Vector2 btmp = new Vector2();
		protected Vector2 valueAt (Vector2 out, float at) {
			return Bezier.cubic(out, at, start, startHandle, endHandle, end, btmp);
		}

		protected Vector2 derivativeAt (Vector2 out, float at) {
			return Bezier.cubic_derivative(out, at, start, startHandle, endHandle, end, btmp);
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
