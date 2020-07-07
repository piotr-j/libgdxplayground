package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 *
 * The Minkowski distance is a metric in a normed vector space which can be considered as
 * a generalization of both the Euclidean distance and the Manhattan distance.
 *
 * http://en.wikipedia.org/wiki/Minkowski_distance
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class MinkowskyiDistanceTest extends BaseScreen {
	private static final String TAG = MinkowskyiDistanceTest.class.getSimpleName();

	private GlyphLayout layout;
	private BitmapFont font;
	public MinkowskyiDistanceTest (GameReset game) {
		super(game);
		// we want unit to be fairly large
		gameViewport = new ExtendViewport(VP_WIDTH/6, VP_HEIGHT/8, gameCamera);
		font = skin.get("default-font", BitmapFont.class);
		layout = new GlyphLayout();
	}

	private float timer;
	@Override public void render (float delta) {
		timer += delta * 0.5f;
		float p = (MathUtils.sin(timer) + 1) * 3 + .2f;

		Gdx.gl.glClearColor(.125f, .125f, .125f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		enableBlending();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(0, 1, 0, .5f);
		renderer.setColor(1, 1, 1, .5f);
		renderer.rect(-1, -1, 2, 2);
		renderer.line(-2, 0, 2, 0);
		renderer.line(0, -2, 0, 2);
//		renderer.end();
//		renderer.begin(ShapeRenderer.ShapeType.Point);
		renderer.setColor(Color.CYAN);
		plotUnitCircle(renderer, p);
		renderer.end();
		disableBlending();

		batch.setProjectionMatrix(guiCamera.combined);
		batch.begin();
		layout.setText(font, "p=" + p);
		font.draw(batch, layout, Gdx.graphics.getWidth()/2 - layout.width/2, Gdx.graphics.getHeight()/4);
		batch.end();

	}

	private final static int points = 256;
	private Vector2 tmp = new Vector2();
	private Vector2 tmp2 = new Vector2();
	private void plotUnitCircle (ShapeRenderer renderer, float p) {
		float step = 360f/points;
		for (int i = 0; i < points; i++) {
			float a = i * step;
			tmp.set(1, 0).rotate(a + step/2);
//			renderer.setColor(Color.GREEN);
//			renderer.point(tmpCircle.x, tmpCircle.y, 0);

			// p = 0.25 thingy
			// p = 1 rotated square
			// p = 2 circle
			// p = 4 almost square
			float unit = 1;
			float unitStep = unit;
			float lastDst = 999;
			float lastDiff = 999;
			// using very low iteration count produces cool effects!
			for (int j = 0; j < 16; j++) {
				tmp.set(unit, 0).rotate(a);
				// to plot a circle, we need to find point with distance = 1 from centre
				float distance = getDistance(tmp.x, tmp.y, 0, 0, p);
				if (MathUtils.isEqual(distance, 1, 0.001f)) {
					break;
				}
				// scale only if we get closer to the target
				float diff = Math.abs(lastDst - distance);
				if (lastDiff > diff) {
					unitStep /= 2;
				}
				lastDst = distance;
				lastDiff = diff;
				// this can be probably better
				if (distance > 1f) {
					unit -= unitStep;
				} else {
					unit += unitStep;
				}
			}
//			renderer.setColor(Color.CYAN);
//			renderer.point(tmpCircle.x, tmpCircle.y, 0);
			renderer.line(tmp, tmp2);
			tmp2.set(tmp);
		}
	}

	private float getDistance(float x1, float y1, float x2, float y2, float p) {
		float dx = abs(x1 - x2);
		float dy = abs(y1 - y2);
		double sum = pow(dx, p) + pow(dy, p);
		return (float)pow(sum, 1f / p);
	}

	private float getDistance(float x1, float y1, float z1, float x2, float y2, float z2, float p) {
		float dx = abs(x1 - x2);
		float dy = abs(y1 - y2);
		float dz = abs(z1 - z2);
		double sum = pow(dx, p) + pow(dy, p) + pow(dz, p);
		return (float)pow(sum, 1f / p);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 4);
		PlaygroundGame.start(args, MinkowskyiDistanceTest.class);
	}
}
