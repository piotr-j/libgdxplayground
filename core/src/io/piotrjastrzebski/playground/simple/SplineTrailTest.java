package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Simple trail behind touch drag
 * A lot of stuff left for optimizations
 */
public class SplineTrailTest extends BaseScreen {
	private final static int NUM_POINTS = 16;
	private Array<Vector2> points = new Array<>();
	private Array<Vector2> smoothed = new Array<>();

	public SplineTrailTest (GameReset game) {
		super(game);
	}

	@Override public void render (float delta) {
		super.render(delta);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(Color.RED);
		for (int i = 0; i < smoothed.size - 1; i++) {
			Vector2 p1 = smoothed.get(i);
			Vector2 p2 = smoothed.get(i + 1);
			renderer.rectLine(p1, p2, 0.05f);
		}
		renderer.end();
	}

	private void addPoint(float x, float y) {
		points.insert(0, new Vector2(x, y));
		points.truncate(NUM_POINTS);
		resolve(points, smoothed);
	}

	private void simplify(Array<Vector2> points, float sqTolerance, Array<Vector2> out) {
		int len = points.size;

		Vector2 prevPoint = points.get(0);
		Vector2 tmp = new Vector2();
		out.clear();
		out.add(prevPoint);

		for (int i = 1; i < len; i++) {
			tmp = points.get(i);
			if (tmp.dst2(prevPoint) > sqTolerance) {
				out.add(tmp);
				prevPoint = tmp;
			}
		}
		if (!prevPoint.equals(tmp)) {
			out.add(tmp);
		}
	}

	private void smooth(Array<Vector2> input, Array<Vector2> output) {
		//expected size
		output.clear();
		output.ensureCapacity(input.size*2);

		//first element
		output.add(input.get(0));
		//average elements
		for (int i=0; i<input.size-1; i++) {
			Vector2 p0 = input.get(i);
			Vector2 p1 = input.get(i+1);

			Vector2 Q = new Vector2(0.75f * p0.x + 0.25f * p1.x, 0.75f * p0.y + 0.25f * p1.y);
			Vector2 R = new Vector2(0.25f * p0.x + 0.75f * p1.x, 0.25f * p0.y + 0.75f * p1.y);
			output.add(Q);
			output.add(R);
		}

		//last element
		output.add(input.get(input.size-1));
	}

	public static int iterations = 2;
	public static float simplifyTolerance = 2f;
	private Array<Vector2> tmp = new Array<>();

	public void resolve(Array<Vector2> input, Array<Vector2> output) {
		output.clear();
		if (input.size<=2) { //simple copy
			output.addAll(input);
			return;
		}

		//simplify with squared tolerance
		if (simplifyTolerance>0 && input.size>3) {
			simplify(input, simplifyTolerance * simplifyTolerance, tmp);
			input = tmp;
		}

		//perform smooth operations
		if (iterations<=0) { //no smooth, just copy input to output
			output.addAll(input);
		} else if (iterations==1) { //1 iteration, smooth to output
			smooth(input, output);
		} else { //multiple iterations.. ping-pong between arrays
			int iters = iterations;
			//subsequent iterations
			do {
				smooth(input, output);
				tmp.clear();
				tmp.addAll(output);
				Array<Vector2> old = output;
				input = tmp;
				output = old;
			} while (--iters > 0);
		}
	}

	Vector3 tp = new Vector3();
	boolean dragging;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		dragging = true;
		points.clear();
		addPoint(tp.x, tp.y);
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		if (!dragging) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		addPoint(tp.x, tp.y);
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		dragging = false;
		addPoint(tp.x, tp.y);
		return true;
	}
}
