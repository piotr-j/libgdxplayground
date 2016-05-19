package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class TriangulatorTest extends BaseScreen {
	private static final String TAG = TriangulatorTest.class.getSimpleName();

	public static final int SEED_COUNT = 20;
	FloatArray points = new FloatArray();
	ShortArray triangles;
	DelaunayTriangulator trianglulator = new DelaunayTriangulator();
	long seed = MathUtils.random.nextLong();

	public TriangulatorTest (GameReset game) {
		super(game);

		triangulate();
		System.out.println(seed);
	}

	void triangulate () {
		// seed = 4139368480425561099l;
		// seed = 6559652580366669361l;
		MathUtils.random.setSeed(seed);

		int pointCount = 100;
		points.clear();
		for (int i = 0; i < pointCount; i++) {
			float x;
			float y;
			do {
				x = MathUtils.random(-VP_WIDTH/2 + 1, VP_WIDTH/2-1);
				y = MathUtils.random(-VP_HEIGHT/2 + 1, VP_HEIGHT/2-1);
			} while (contains(points, x, y, 0.5f));
			points.add(x);
			points.add(y);
		}
		points.add(cs.x);
		points.add(cs.y);

		triangles = trianglulator.computeTriangles(points, false);
	}

	private boolean contains (FloatArray points, float x, float y, float margin) {
		for (int i = 0; i < points.size; i+=2) {
			float ox = points.get(i);
			float oy = points.get(i + 1);
			if (MathUtils.isEqual(ox, x, margin) && MathUtils.isEqual(oy, y, margin)) return true;
		}
		return false;
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		renderer.setProjectionMatrix(gameCamera.combined);

		renderer.setColor(Color.WHITE);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		for (int i = 0; i < triangles.size; i += 3) {
			int p1 = triangles.get(i) * 2;
			int p2 = triangles.get(i + 1) * 2;
			int p3 = triangles.get(i + 2) * 2;
			renderer.triangle( //
				points.get(p1), points.get(p1 + 1), //
				points.get(p2), points.get(p2 + 1), //
				points.get(p3), points.get(p3 + 1));
		}
		renderer.end();

		renderer.setColor(Color.BLACK);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (int i = 0; i < points.size; i += 2) {
			renderer.circle(points.get(i), points.get(i + 1), .15f, 12);
		}
		renderer.end();
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		seed = MathUtils.random.nextLong();
		System.out.println(seed);
		triangulate();
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		triangulate();
		return super.touchDragged(screenX, screenY, pointer);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		config.useHDPI = true;
		config.stencil = 8;
		// for mesh based sub pixel rendering multi sampling is required or post process aa
		config.samples = 4;
		PlaygroundGame.start(args, config, TriangulatorTest.class);
	}
}
