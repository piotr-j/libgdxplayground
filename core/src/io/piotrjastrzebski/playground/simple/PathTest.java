package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class PathTest extends BaseScreen {
	private final static String TAG = PathTest.class.getSimpleName();
	int SAMPLE_POINTS = 100;
	float SAMPLE_POINT_DISTANCE = 1f / SAMPLE_POINTS;
	float ZIGZAG_SCALE;

	ImmediateModeRenderer20 renderer;
	Sprite obj;
	Sprite obj2;
	Array<Path<Vector2>> paths = new Array<>();
	int currentPath = 0;
	float t;
	float t2;
	float zt;
	float speed = 0.2f;
	float zspeed = 1.0f;
	float wait = 0f;
	boolean zigzag = false;

	public PathTest (GameReset game) {
		super(game);

		renderer = new ImmediateModeRenderer20(false, false, 0);
		obj = new Sprite(new Texture("box2d/box32.png"));
		obj.setSize(40, 40);
		obj.setOriginCenter();
		obj2 = new Sprite(new Texture("box2d/circle32.png"));
		obj2.setSize(40, 40);
		obj2.setOriginCenter();
		ZIGZAG_SCALE = Gdx.graphics.getDensity() * 96; // 96DP

		float w = Gdx.graphics.getWidth() - obj.getWidth();
		float h = Gdx.graphics.getHeight() - obj.getHeight();

		paths.add(new Bezier<>(new Vector2(0, 0), new Vector2(w, h)));
		paths.add(new Bezier<>(new Vector2(0, 0), new Vector2(0, h), new Vector2(w, h)));
		paths.add(new Bezier<>(new Vector2(0, 0), new Vector2(w, 0), new Vector2(0, h), new Vector2(w, h)));

		Vector2 cp[] = new Vector2[] {new Vector2(0, 0), new Vector2(w * 0.25f, h * 0.5f), new Vector2(0, h),
				new Vector2(w * 0.5f, h * 0.75f), new Vector2(w, h), new Vector2(w * 0.75f, h * 0.5f), new Vector2(w, 0),
				new Vector2(w * 0.5f, h * 0.25f)};
		paths.add(new BSpline<>(cp, 3, true));

		paths.add(new CatmullRomSpline<>(cp, true));

		pathLength = paths.get(currentPath).approxLength(500);
		avg_speed = speed * pathLength;
	}

	final Vector2 tmpV = new Vector2();
	final Vector2 tmpV2 = new Vector2();
	final Vector2 tmpV3 = new Vector2();
	final Vector2 tmpV4 = new Vector2();

	float pathLength;
	float avg_speed;

	@Override public void render (float delta) {
		super.render(delta);if (wait > 0)
			wait -= Gdx.graphics.getDeltaTime();
		else {
			t += speed * Gdx.graphics.getDeltaTime();
			zt += zspeed * Gdx.graphics.getDeltaTime();
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
			t2 += avg_speed * Gdx.graphics.getDeltaTime() / tmpV3.len();

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
			obj2.setPosition(tmpV4.x, tmpV4.y);

		}

		renderer.begin(batch.getProjectionMatrix(), GL20.GL_LINE_STRIP);
		float val = 0f;
		while (val <= 1f) {
			renderer.color(0f, 0f, 0f, 1f);
			paths.get(currentPath).valueAt(/* out: */tmpV, val);
			renderer.vertex(tmpV.x, tmpV.y, 0);
			val += SAMPLE_POINT_DISTANCE;
		}
		renderer.end();

		batch.begin();
		obj.draw(batch);
		obj2.draw(batch);
		batch.end();
	}

	private void touch (int x, int y) {
		t = paths.get(currentPath).locate(tmpV.set(x, Gdx.graphics.getHeight() - y));
		paths.get(currentPath).valueAt(tmpV, t);
		obj.setPosition(tmpV.x, tmpV.y);
		wait = 0.2f;
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		touch(screenX, screenY);
		return super.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		touch(screenX, screenY);
		return super.touchDragged(screenX, screenY, pointer);
	}

	@Override public void dispose () {
		super.dispose();
		obj.getTexture().dispose();
		obj2.getTexture().dispose();
	}
}
