package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
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
	Vector2 out = new Vector2();
	FPSLogger fpsLogger = new FPSLogger();
	public CurveEdit2Test (GameReset game) {
		super(game);
		clear.set(Color.GRAY);
		gameCamera.zoom = .33f;
		gameCamera.update();
		curves = new Curves();
	}

	@Override public void render (float delta) {
		super.render(delta);
		fpsLogger.log();
		curves.update(delta);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		curves.draw(renderer);
		renderer.end();

		for (Curve curve : curves.curves) {
			out.setZero();
			if (curve.pointOnLine(cs.x, cs.y, out)) {
				renderer.begin(ShapeRenderer.ShapeType.Filled);
				renderer.setColor(Color.SCARLET);
				renderer.circle(cs.x, cs.y, .05f, 16);
				renderer.setColor(Color.ORANGE);
				renderer.circle(out.x, out.y, .025f, 16);
				renderer.setColor(Color.LIME);
				// this is more or less useless for anything but very straight curve
				curve.closestPointTo(cs.x, cs.y, out);
				renderer.circle(out.x, out.y, .025f, 16);
				renderer.end();
				break;
			}
		}
	}

	protected static class Curves {
		Array<Curve> curves = new Array<>();
		private Vector2 out = new Vector2();
		private float handleRadius = .25f;

		public Curves () {
			Curve curve = new Curve();
			curve.start.set(-3, -3);
			curve.startHandle.set(-3, 0);
			curve.endHandle.set(0, 3);
			curve.end.set(3, 3);
			curve.rebuild();
			curves.add(curve);
		}

		private float doubleClickDelay = .25f;
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
			tmpCircle.set(x, y, handleRadius);
			if (doubleClickTimer <= 0) {
				doubleClickTimer = doubleClickDelay;

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
				boolean onCurveHandle = false;
				Curve onCurve = null;
				Vector2 handle = null;
				for (Curve curve : curves) {
					if (tmpCircle.contains(curve.start)) {
						onCurveHandle = true;
						onCurve = curve;
						handle = curve.startHandle;
						break;
					}
					if (tmpCircle.contains(curve.end)) {
						onCurveHandle = true;
						onCurve = curve;
						handle = curve.startHandle;
						break;
					}
				}
				if (onCurveHandle) {
					// on end/start handle, remove handle, merge curves
					if (onCurve.next == null && onCurve.prev == null) {
						// not connected, just remove it
						curves.removeValue(onCurve, true);
					} else {
						if (onCurve.start == handle) {
							if (onCurve.prev != null) {
								// merge prev and this one
								onCurve.prev.next = onCurve.next;
								onCurve.prev.end.set(onCurve.end);
								onCurve.prev.endHandle.set(onCurve.endHandle);
								onCurve.rebuild();
							} else {
								onCurve.next.prev = null;
							}
							curves.removeValue(onCurve, true);
						} else if (onCurve.end == handle) {
							if (onCurve.next != null) {
								// merge next and this one
								onCurve.next.prev = onCurve.prev;
								onCurve.next.start.set(onCurve.start);
								onCurve.next.startHandle.set(onCurve.startHandle);
								onCurve.rebuild();
							} else {
								onCurve.prev.next = null;
							}
							curves.removeValue(onCurve, true);
						}
					}
				} else {
					boolean onCurvePath = false;
					out.setZero();
					for (Curve curve : curves) {
						if (curve.pointOnLine(x, y, out)) {
							onCurvePath = true;
							onCurve = curve;
							break;
						}
					}
					if (onCurvePath) {
						// on curve, split
						int at = curves.indexOf(onCurve, true);
						Curve curve = new Curve();
						curve.start.set(x, y);
						curve.startHandle.set(x, y + 1);
						curve.endHandle.set(onCurve.endHandle);
						curve.end.set(onCurve.end);
						curve.rebuild();
						curve.prev = onCurve;
						onCurve.next = curve;
						onCurve.end.set(x, y);
						onCurve.endHandle.set(x, y - 1);
						onCurve.rebuild();


						if (at == curves.size) {
							curves.add(curve);
						} else {
							curves.insert(at + 1, curve);
						}
					} else {
						// on empty space, create
						Curve curve = new Curve();
						curve.start.set(x, y);
						curve.startHandle.set(x, y + 1);
						curve.endHandle.set(x + 1, y + 1);
						curve.end.set(x + 1, y);
						curve.rebuild();
						curves.add(curve);
					}
				}
			}
			return true;
		}

		public boolean touchDragged (float x, float y) {
			// drag handle if we have one
			if (curveEdit.curve != null) {
				curveEdit.dragged.set(x, y).add(curveEdit.dragOffset);
				if (curveEdit.handle != null) {
					curveEdit.handle.set(curveEdit.dragged).add(curveEdit.handleOffset);
					if (curveEdit.curve.end == curveEdit.dragged && curveEdit.curve.next != null) {
						curveEdit.curve.next.start.set(curveEdit.curve.end);
						curveEdit.curve.next.rebuild();
					} else if (curveEdit.curve.start == curveEdit.dragged && curveEdit.curve.prev != null) {
						curveEdit.curve.prev.end.set(curveEdit.curve.start);
						curveEdit.curve.prev.rebuild();
					}
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
					if (curveEdit.curve.end == curveEdit.dragged && curveEdit.curve.next != null) {
						curveEdit.curve.next.rebuild();
					} else if (curveEdit.curve.start == curveEdit.dragged && curveEdit.curve.prev != null) {
						curveEdit.curve.prev.end.set(curveEdit.curve.start);
						curveEdit.curve.prev.rebuild();
					}
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

		protected Curve prev;
		protected Curve next;

		private Array<Vector2> cache = new Array<>();

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
//			tmp5.set(tmp2).sub(tmp).nor().scl(.25f).rotate(90);
//			renderer.line(tmp.x, tmp.y, tmp.x + tmp5.x, tmp.y + tmp5.y);
//			renderer.line(tmp.x, tmp.y, tmp.x - tmp5.x, tmp.y - tmp5.y);
			renderer.setColor(Color.RED);
			renderer.circle(start.x, start.y, .1f, 16);
			renderer.setColor(Color.BLUE);
			renderer.circle(end.x, end.y, .1f, 16);
			for (int i = 1; i < size; i++) {
				tmp2.set(cache.get(i));
				renderer.setColor(a, 1, 1 - a, 1);
				renderer.line(tmp, tmp2);

				renderer.setColor(Color.RED);
//				tmp5.set(tmp).sub(tmp2).nor().scl(.25f).rotate(90);
//				renderer.line(tmp2.x, tmp2.y, tmp2.x + tmp5.x, tmp2.y + tmp5.y);
//				renderer.line(tmp2.x, tmp2.y, tmp2.x - tmp5.x, tmp2.y - tmp5.y);

				a += step;

				tmp.set(tmp2);
				tmp3.set(tmp4);
			}

			renderer.setColor(Color.DARK_GRAY);
			for (int i = 0; i < indices.length -3; i+=3) {
				float x1 = vertices[indices[i] * 2];
				float y1 = vertices[indices[i] * 2 + 1];
				float x2 = vertices[indices[i + 1] * 2];
				float y2 = vertices[indices[i + 1] * 2 + 1];
				float x3 = vertices[indices[i + 2] * 2];
				float y3 = vertices[indices[i + 2] * 2 + 1];
				renderer.triangle(x1, y1, x2, y2, x3, y3);
			}
//			renderer.setColor(Color.MAGENTA);
//			renderer.polygon(polygon);
		}

		public boolean pointOnLine(float x, float y, Vector2 out) {
			int triangle = 0;
			int segment = 0;
			for (int i = 0; i < indices.length -3; i+=3) {
				float x1 = vertices[indices[i] * 2];
				float y1 = vertices[indices[i] * 2 + 1];
				float x2 = vertices[indices[i + 1] * 2];
				float y2 = vertices[indices[i + 1] * 2 + 1];
				float x3 = vertices[indices[i + 2] * 2];
				float y3 = vertices[indices[i + 2] * 2 + 1];
				triangle++;
				if (triangle > 1 && triangle %2 == 1) {
					segment++;
				}
				if (Intersector.isPointInTriangle(x, y, x1, y1, x2, y2, x3, y3)){
					if (out != null) {
						Vector2 p1;
						Vector2 p2;
						if (segment == cache.size -1) {
							p1 = cache.get(segment -1);
							p2 = cache.get(segment);
						} else {
							p1 = cache.get(segment);
							p2 = cache.get(segment + 1);
						}
						Intersector.nearestSegmentPoint(p1, p2, tmp.set(x, y), out);
					}
					return true;
				}
			}
			return false;
		}

		float polyScale = .1f;
		// unless we want just an outline, polygon is kinda useless
//		float[] polygon;
		float[] vertices;
		int[] indices;
		protected void rebuild() {
			float len = approxLength(APPROX_LEN_SAMPLES);
			// we want 10 samples per 1 unit of length
			int iters = (int)(len * SAMPLES_PER_UNIT_LEN);
			float step = 1f/iters;
			// we need extra start point, so we start before 0
			float at = 0;
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
			vertices = new float[cache.size * 4];
//			polygon = new float[cache.size * 4];
			int vid = 0;
//			int psid = 0;
//			int peid = polygon.length -1;

			tmp.set(cache.get(0));
			tmp2.set(cache.get(1));

			tmp3.set(tmp2).sub(tmp).nor().rotate(90).scl(polyScale);
			vertices[vid++] = tmp.x + tmp3.x;
			vertices[vid++] = tmp.y + tmp3.y;
			vertices[vid++] = tmp.x - tmp3.x;
			vertices[vid++] = tmp.y - tmp3.y;

//			polygon[psid++] = tmp.x + tmp3.x;
//			polygon[psid++] = tmp.y + tmp3.y;
//			polygon[peid--] = tmp.y - tmp3.y;
//			polygon[peid--] = tmp.x - tmp3.x;

			int size = cache.size;
			for (int i = 2; i < size; i++) {
				tmp3.set(cache.get(i));

				tmp4.set(tmp2).sub(tmp).nor();
				tmp5.set(tmp2).sub(tmp3).nor();
				float angle = tmp4.angle(tmp5);
				// flip it around so we are always on the same side of the curve
				if (angle < 0) {
					angle = 360 + angle;
				}
				angle = tmp4.angle() + angle/2;
				tmp5.set(1, 0).rotate(angle).nor().scl(polyScale);

				vertices[vid++] = tmp2.x + tmp5.x;
				vertices[vid++] = tmp2.y + tmp5.y;
				vertices[vid++] = tmp2.x - tmp5.x;
				vertices[vid++] = tmp2.y - tmp5.y;
//				polygon[psid++] = tmp2.x + tmp5.x;
//				polygon[psid++] = tmp2.y + tmp5.y;
//				polygon[peid--] = tmp2.y - tmp5.y;
//				polygon[peid--] = tmp2.x - tmp5.x;

				tmp.set(tmp2);
				tmp2.set(tmp3);
			}

			tmp.set(cache.get(cache.size-1));
			tmp2.set(cache.get(cache.size-2));
			tmp3.set(tmp2).sub(tmp).nor().rotate(90).scl(polyScale);
			vertices[vid++] = tmp.x - tmp3.x;
			vertices[vid++] = tmp.y - tmp3.y;
			vertices[vid++] = tmp.x + tmp3.x;
			vertices[vid++] = tmp.y + tmp3.y;

//			polygon[psid++] = tmp.x - tmp3.x;
//			polygon[psid++] = tmp.y - tmp3.y;
//			polygon[peid--] = tmp.y + tmp3.y;
//			polygon[peid--] = tmp.x + tmp3.x;

			int ox = 0;
			indices = new int[cache.size * 6];
			for (int i = 0; i < indices.length -6; i+=6) {
				// 0, 1, 2, 1, 2, 3
				indices[i] = ox;
				indices[i + 1] = 1 + ox;
				indices[i + 2] = 2 + ox;
				indices[i + 3] = 1 + ox;
				indices[i + 4] = 2 + ox;
				indices[i + 5] = 3 + ox;
				ox += 2;
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

		public Vector2 closestPointTo (float x, float y, Vector2 out) {
			Vector2 p1 = start;
			Vector2 p2 = end;
			Vector2 p3 = out.set(x, y);
			float l1Sqr = p1.dst2(p2);
			float l2Sqr = p3.dst2(p2);
			float l3Sqr = p3.dst2(p1);
			float l1 = (float)Math.sqrt(l1Sqr);
			float s = (l2Sqr + l1Sqr - l3Sqr) / (2 * l1);
			float at = MathUtils.clamp((l1 - s) / l1, 0f, 1f);
			return valueAt(out, at);
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
