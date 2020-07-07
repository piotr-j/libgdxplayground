package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class PathsTest extends BaseScreen {
	private static final String TAG = PathsTest.class.getSimpleName();
	int SAMPLE_POINTS = 100;
	float SAMPLE_POINT_DISTANCE = 1f / SAMPLE_POINTS;
	float ZIGZAG_SCALE = Gdx.graphics.getDensity() * 4;

	Array<Path<Vector2>> paths = new Array<>();
	int currentPath = 0;
	float t;
	float t2;
	float zt;
	float speed = 0.2f;
	float zspeed = 3.0f;
	float wait = 0f;
	boolean zigzag = false;
	Circle obj = new Circle(0, 0, .33f);
	float objAngle;
	Circle obj2 = new Circle(0, 0, .33f);
	float obj2Angle;
	public PathsTest (GameReset game) {
		super(game);
		clear.set(Color.GRAY);

		float w = VP_WIDTH - 2;
		float h = VP_HEIGHT - 2;

		paths.add(new Bezier<>(new Vector2(1, 1), new Vector2(w, h)));
		paths.add(new Bezier<>(new Vector2(1, 1), new Vector2(1, h), new Vector2(w, h)));
		paths.add(new Bezier<>(new Vector2(1, 1), new Vector2(w, 1), new Vector2(1, h), new Vector2(w, h)));

		Vector2 cp[] = new Vector2[] {new Vector2(1, 1), new Vector2(w * 0.25f, h * 0.5f), new Vector2(1, h),
			new Vector2(w * 0.5f, h * 0.75f), new Vector2(w, h), new Vector2(w * 0.75f, h * 0.5f), new Vector2(w, 1),
			new Vector2(w * 0.5f, h * 0.25f)};
		paths.add(new BSpline<>(cp, 3, true));

		paths.add(new CatmullRomSpline<>(cp, true));

		pathLength = paths.get(currentPath).approxLength(500);

	}

	final Vector2 tmpV = new Vector2();
	final Vector2 tmpV2 = new Vector2();
	final Vector2 tmpV3 = new Vector2();
	final Vector2 tmpV4 = new Vector2();

	float pathLength;
	float avg_speed;

	@Override public void render (float delta) {
		super.render(delta);
		if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
			zigzag = !zigzag;
		}
		enableBlending();

		if (wait > 0)
			wait -= delta;
		else {
			t += speed * delta;
			zt += zspeed * delta;
			while (t >= 1f) {
				currentPath = (currentPath + 1) % paths.size;
				pathLength = paths.get(currentPath).approxLength(500);
				if (currentPath > 2) {
					avg_speed = speed * pathLength / 8.0f;
				} else {
					avg_speed = speed * pathLength;
				}
				if (currentPath == 0) {
					zigzag = !zigzag;
					zt = 0;
				}
				t -= 1f;
				t2 = 0f;
			}

			paths.get(currentPath).valueAt(tmpV, t);
			paths.get(currentPath).derivativeAt(tmpV2, t);

			paths.get(currentPath).derivativeAt(tmpV3, t2);
			t2 += avg_speed * delta / tmpV3.len();

			paths.get(currentPath).valueAt(tmpV4, t2);
			// obj.setRotation(tmpV2.angle());
			// obj2.setRotation(tmpV3.angle());

			if (zigzag) {
				tmpV2.nor();
				tmpV2.set(-tmpV2.y, tmpV2.x);
				tmpV2.scl((float)Math.sin(zt) * ZIGZAG_SCALE);
				tmpV.add(tmpV2);
			}

			obj.setPosition(tmpV.x, tmpV.y);
			objAngle = tmpV2.angle() - 90;
			obj2.setPosition(tmpV4.x, tmpV4.y);
			obj2Angle = tmpV3.angle() - 90;
		}

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		Path<Vector2> path = paths.get(currentPath);
		if (path instanceof Bezier) {
			renderer.setColor(0f, 1f, 1f, 1f);
			Bezier<Vector2> bezier = (Bezier<Vector2>)path;
			for (Vector2 point : bezier.points) {
				renderer.circle(point.x, point.y, .2f, 8);
			}
		} else if (path instanceof BSpline) {
			renderer.setColor(0f, 1f, 1f, 1f);
			BSpline<Vector2> bSpline = (BSpline<Vector2>)path;
			for (Vector2 cp : bSpline.controlPoints) {
				renderer.circle(cp.x, cp.y, .2f, 8);
			}
			renderer.setColor(1f, 1f, 0f, 1f);
			for (Vector2 knot : bSpline.knots) {
				renderer.circle(knot.x, knot.y, .2f, 8);
			}

		} else if (path instanceof CatmullRomSpline) {
			CatmullRomSpline<Vector2> catmullRomSpline = (CatmullRomSpline<Vector2>)path;
			renderer.setColor(0f, 1f, 1f, 1f);
			for (Vector2 cp : catmullRomSpline.controlPoints) {
				renderer.circle(cp.x, cp.y, .2f, 8);
			}
		}
		float val = 0f;
		while (val <= 1f - SAMPLE_POINT_DISTANCE) {
			renderer.setColor(0f, 0f, 0f, 1f);
			paths.get(currentPath).valueAt(/* out: */tmpV, val);
			paths.get(currentPath).valueAt(/* out: */tmpV2, val + SAMPLE_POINT_DISTANCE);
			renderer.line(tmpV.x, tmpV.y, tmpV2.x, tmpV2.y);
			val += SAMPLE_POINT_DISTANCE;
		}
		renderer.setColor(Color.RED);
		renderer.circle(obj.x, obj.y, obj.radius, 16);
		tmpV.set(0, 1).rotate(objAngle);
		renderer.line(obj.x, obj.y, obj.x + tmpV.x, obj.y + tmpV.y);
		renderer.setColor(Color.GREEN);
		renderer.circle(obj2.x, obj2.y, obj2.radius, 16);
		tmpV.set(0, 1).rotate(obj2Angle);
		renderer.line(obj2.x, obj2.y, obj2.x + tmpV.x, obj2.y + tmpV.y);
		renderer.end();
	}

	private void touch (float x, float y) {
		t = paths.get(currentPath).locate(tmpV.set(x, y));
		paths.get(currentPath).valueAt(tmpV, t);
		obj.setPosition(tmpV.x, tmpV.y);
		wait = 0.2f;
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		touch(cs.x, cs.y);
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		super.touchDragged(screenX, screenY, pointer);
		touch(cs.x, cs.y);
		return true;
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		gameViewport.update(width, height, true);
	}

	@Override public void dispose () {
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 4);
		PlaygroundGame.start(args, config, PathsTest.class);
	}
}
