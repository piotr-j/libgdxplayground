package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.tiledgentest.OpenNoise;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class LightningCurveTest extends BaseScreen {
	private static final String TAG = LightningCurveTest.class.getSimpleName();

	OpenNoise noise;
	int width = 512;
	int height = 512;
	float step = 1f/32f;
	float[][] values = new float[width][height];


	Array<Curve> curves = new Array<>();
	public LightningCurveTest (GameReset game) {
		super(game);
		clear.set(Color.DARK_GRAY);


		noise = new OpenNoise(9, .65, 6471423);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				values[x][y] = (float)noise.getNoise(x * step, y * step);
			}
		}

		curves.add(new Curve(noise));
	}

	@Override public void render (float delta) {
		super.render(delta);
		renderer.setProjectionMatrix(gameCamera.combined);

		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				float a = (values[x][y] + 1)/2;
				renderer.setColor(a, a, a, 1);
				renderer.rect(-width * step /2 + x * step, -height * step /2 + y *step, step, step);
			}
		}
		renderer.end();

		renderer.begin(ShapeRenderer.ShapeType.Line);
		for (Curve curve : curves) {
			curve.draw(renderer);
		}
		renderer.end();
		if(doubleClickTimer > 0) doubleClickTimer -= delta;
	}

	public static class Curve {
		private static final int ITERS = 32;
		private static final float STEP = 1f/ITERS;
		private static Array<Vector2> tmps = new Array<>();
		private static Vector2 tmp = new Vector2();
		private static Vector2 tmp2 = new Vector2();
		private static Vector2 dtmp = new Vector2();
		private static Vector2 dtmp2 = new Vector2();
		private static Vector2 tmpAngle = new Vector2();
		private static Vector2 tmpOut = new Vector2();
		private Bezier<Vector2> bezier;
		private Handle[] handles = new Handle[4];
		private Handle drag;
		private Vector2 dragOffset = new Vector2();
		private static Polygon polygon = new Polygon(new float[]{0, -.5f, 1, -.5f, 1, .5f, 0, .5f});

		public Curve prev;
		public Curve next;
		private boolean drawPolygon;
		private OpenNoise noise;
		private boolean rngCircle = false;

		public Curve (OpenNoise noise) {
			this.noise = noise;
			tmps.clear();
			tmps.add(new Vector2(0, 0));
			tmps.add(new Vector2(0, 3f));
			tmps.add(new Vector2(7, 5f));
			tmps.add(new Vector2(10, 5));
			bezier = new Bezier<>(tmps, 0, 4);
			handles[1] = new Handle(1, tmps.get(1));
			handles[0] = new Handle(0, tmps.get(0), handles[1]);
			handles[2] = new Handle(2, tmps.get(2));
			handles[3] = new Handle(3, tmps.get(3), handles[2]);
		}

		public float at(float a) {
			return 0;
		}
		float scroll;
		public void draw(ShapeRenderer renderer) {
			renderer.setColor(Color.GREEN);
			for (Handle handle : handles) {
				renderer.circle(handle.handle.x, handle.handle.y, handle.radius, 16);
			}
			renderer.line(handles[0].handle.x, handles[0].handle.y, handles[1].handle.x, handles[1].handle.y);
			renderer.line(handles[2].handle.x, handles[2].handle.y, handles[3].handle.x, handles[3].handle.y);


			float at = 0;
			bezier.valueAt(tmp, at);
			dtmp.set(tmp);
			renderer.setColor(Color.MAGENTA);
			renderer.circle(tmp2.x, tmp2.y, .1f, 8);
			scroll += Gdx.graphics.getDeltaTime();
			while (at < 1) {
				at += STEP;
				bezier.valueAt(tmp2, at);
				renderer.setColor(Color.MAGENTA);
				renderer.line(tmp, tmp2);
				renderer.circle(tmp2.x, tmp2.y, .1f, 8);
				tmp.set(tmp2);

				dtmp2.set(tmp2);
				// dst from start would be better then that
				float a = at < .5f?at * 2:2 * (1 - at);
				a = Interpolation.circle.apply(a);
				randomizePoint(dtmp2, a, tmpOut);
				dtmp2.set(tmpOut);
				renderer.setColor(Color.CYAN);
				renderer.line(dtmp, dtmp2);
				dtmp.set(dtmp2);
			}
		}

		private void randomizePoint (Vector2 point, float a, Vector2 out) {
			if (rngCircle) {
				rngPointInCircle(point.x, point.y, .5f * a, out);
			} else {
				float s = .5f * scroll;
				float ox = (float)noise.getNoise(point.x * s, point.y * s);
				float oy = (float)noise.getNoise((point.x + 100) * s, (point.y + 100) * s);
				out.set(point).add(ox * a, oy * a);
			}
		}

		public boolean touchDown (float x, float y) {
			for (Handle handle : handles) {
				if (handle.contains(x, y)) {
					drag = handle;
					dragOffset.set(drag.handle).sub(x, y);
					return true;
				}
			}
			return false;
		}

		public boolean touchDragged (float x, float y) {
			if (drag != null) {
				boolean symmetric = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
				drag.set(x + dragOffset.x, y + dragOffset.y);
				if (drag.id == 0 && prev != null) {
					Handle handle = prev.handles[3];
					handle.set(drag.handle.x, drag.handle.y);
				} else if (drag.id == 1 && prev != null) {
					if (symmetric) {
						tmp.set(handles[0].handle).sub(drag.handle);
						prev.handles[2].handle.set(handles[0].handle).add(tmp);
					}
				} else if (drag.id == 2 && next != null) {
					if (symmetric) {
						tmp.set(handles[3].handle).sub(drag.handle);
						next.handles[1].handle.set(handles[3].handle).add(tmp);
					}
				} else if (drag.id == 3 && next != null) {
					Handle handle = next.handles[0];
					handle.set(drag.handle.x, drag.handle.y);
				}
				return true;
			}
			return false;
		}

		public boolean touchUp (float x, float y) {
			if (drag != null) {
				drag = null;
				return true;
			}
			return false;
		}

		public boolean contains (float x, float y) {
			float at = 0;
			bezier.valueAt(tmp, at);
			while (at < 1) {
				at += STEP;
				bezier.valueAt(tmp2, at);
				polygon.setPosition(tmp.x, tmp.y);
				float angle = tmpAngle.set(tmp2).sub(tmp).angleDeg();
				polygon.setRotation(angle);
				polygon.setScale(tmp.dst(tmp2), .2f);
				if (polygon.contains(x, y)) return true;
				tmp.set(tmp2);
			}
			return false;
		}

		protected Vector2 rngPointInCircle (float x, float y, float range, Vector2 out) {
			float t = MathUtils.PI2 * MathUtils.random();
			float r = (float)Math.sqrt(MathUtils.random(2f));
			out.set(r * MathUtils.cos(t), r * MathUtils.sin(t));
			out.scl(range);
			out.add(x, y);
			return out;
		}

		private static class Handle {
			public int id;
			public Vector2 handle;
			public Handle child;
			public Vector2 childOffset = new Vector2();
			public float radius = .25f;

			public Handle (int id, Vector2 handle) {
				this(id, handle, null);
			}

			public Handle (int id, Vector2 handle, Handle child) {
				this.id = id;
				this.handle = handle;
				this.child = child;
			}

			public boolean contains (float x, float y) {
				x = handle.x - x;
				y = handle.y - y;
				return x * x + y * y <= radius * radius;
			}

			public void set (float x, float y) {
				if (child != null) {
					childOffset.set(child.handle).sub(handle);
				}
				handle.set(x, y);
				if (child != null) {
					child.set(x + childOffset.x, y + childOffset.y);
				}
			}
		}
	}

	private Vector2 tmp = new Vector2();
	private Vector2 tmp2 = new Vector2();
	float doubleClickTimer;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		if (doubleClickTimer > 0) {
			boolean merge = false;
			for (int i = curves.size - 1; i >= 0; i--) {
				Curve curve = curves.get(i);
				if (curve.touchDown(cs.x, cs.y)) {
					Curve prev = curve.prev;
					Curve next = curve.next;
					if (curve.drag.id == 0) {
						merge = true;
						curves.removeIndex(i);
						if (prev != null) {
							prev.next = next;
							if (next != null) {
								next.prev = prev;
							}
							prev.handles[2].handle.set(curve.handles[2].handle);
							prev.handles[3].handle.set(curve.handles[3].handle);
						}
					} else if (curve.drag.id == 3) {
						merge = true;
						curves.removeIndex(i);
						if (next != null) {
							next.prev = prev;
							if (prev != null) {
								prev.next = next;
							}
							next.handles[0].handle.set(curve.handles[0].handle);
							next.handles[1].handle.set(curve.handles[1].handle);
						}
					}
				}
			}
			if (!merge) {
				for (int i = curves.size - 1; i >= 0; i--) {
					Curve curve = curves.get(i);
					if (curve.contains(cs.x, cs.y)) {
						Curve c1 = new Curve(noise);
						Curve c2 = new Curve(noise);
						c1.prev = curve.prev;
						if (c1.prev != null) {
							c1.prev.next = c1;
						}
						c1.next = c2;
						c2.prev = c1;
						c2.next = curve.next;
						if (c2.next != null) {
							c2.next.prev = c2;
						}
						curves.set(i, c1);
						curves.ensureCapacity(i + 1);
						curves.insert(i + 1, c2);
						Array<Vector2> cps = curve.bezier.points;
						Array<Vector2> c1ps = c1.bezier.points;
						c1ps.get(0).set(cps.get(0));
						c1ps.get(1).set(cps.get(1));
						float at = curve.bezier.locate(cs);
						curve.bezier.valueAt(tmp, at - .05f);
						curve.bezier.valueAt(tmp2, at + .05f);
						tmp2.sub(tmp).nor();
						c1ps.get(3).set(cs.x, cs.y);
						c1ps.get(2).set(cs.x, cs.y).add(-tmp2.x, -tmp2.y);
						Array<Vector2> c2ps = c2.bezier.points;
						c2ps.get(0).set(cs.x, cs.y);
						c2ps.get(1).set(cs.x, cs.y).add(tmp2.x, tmp2.y);
						c2ps.get(2).set(cps.get(2));
						c2ps.get(3).set(cps.get(3));
					}
				}
			}
		} else {
			doubleClickTimer += .25f;
			for (Curve curve : curves) {
				if (curve.touchDown(cs.x, cs.y)) {
					break;
				}
			}
		}
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		super.touchDragged(screenX, screenY, pointer);
		for (Curve curve : curves) {
			if (curve.touchDragged(cs.x, cs.y)) break;
		}
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		super.touchUp(screenX, screenY, pointer, button);
		for (Curve curve : curves) {
			if (curve.touchUp(cs.x, cs.y)) {
				break;
			}
		}
		return true;
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, LightningCurveTest.class);
	}
}
