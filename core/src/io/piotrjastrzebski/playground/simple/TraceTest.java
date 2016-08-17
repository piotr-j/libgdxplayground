package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class TraceTest extends BaseScreen {
	private static final String TAG = TraceTest.class.getSimpleName();

	CompositeTrace compositeTrace;
	public TraceTest (GameReset game) {
		super(game);
		clear.set(0, 0.05f, 0.1f, 1);
		enableBlending();
		compositeTrace = new CompositeTrace();
		compositeTrace.position.set(0, -5);
		compositeTrace.add(new Trace(0f, 0f, 0, 5, 0, 5, 0f, 10f));
		compositeTrace.add(new Trace(0f, 10f, 4, 10, 4, 6, 0f, 6f));
		compositeTrace.add(new Trace(0f, 6f, 5, 6, 5, 0, 0f, 0f));
	}

	private static class CompositeTrace {
		Vector2 position = new Vector2();
		Array<Trace> traces = new Array<>();
		int current = 0;

		void add(Trace trace) {
			traces.add(trace);
		}

		public void draw (ShapeRenderer renderer) {
			for (Trace trace : traces) {
				trace.draw(position, renderer);
			}
			for (Trace trace : traces) {
				trace.drawTrace(position, renderer);
			}
		}

		public boolean update (float x, float y) {
			Trace trace = traces.get(current);
			boolean valid = trace.valid(x - position.x, y - position.y);
			if (valid && trace.status == Trace.TraceStatus.SUCCESS) {
				if (current < traces.size -1) {
					current++;
				}
			}
			return valid;
		}

		public void resetCurrent () {
			Trace trace = traces.get(current);
			if (trace.status != Trace.TraceStatus.SUCCESS) {
				trace.status = Trace.TraceStatus.IDLE;
				trace.points.clear();
			}
		}

		public Trace.TraceStatus status () {
			return traces.get(current).status;
		}
	}

	// represents some shape that can be traced over
	private static class Trace {
		private static final String TAG = Trace.class.getSimpleName();
		enum TraceStatus {IDLE, RUNNING, SUCCESS, FAILURE}
		static Circle tmpCircle = new Circle();
		static Vector2 tmp = new Vector2();
		static Vector2 tmp2 = new Vector2();
		Array<Vector2> lines = new Array<>();
		Array<Vector2> points = new Array<>();
		Vector2 start = new Vector2();
		Vector2 end = new Vector2();
		Bezier<Vector2> path;
		TraceStatus status = TraceStatus.IDLE;
		float radius = .5f;

		public Trace (float x1, float y1, float hx1, float hy1, float hx2, float hy2, float x2, float y2) {
			start.set(x1, y1);
			end.set(x2, y2);
			path = new Bezier<>(new Vector2(x1, y1), new Vector2(hx1, hy1), new Vector2(hx2, hy2), new Vector2(x2, y2));
			float at = 0;
			float step = 1f/100f;
			path.valueAt(tmp, at);
			lines.add(new Vector2(tmp));
			while (at <= 1) {
				at += step;
				path.valueAt(tmp, at);
				lines.add(new Vector2(tmp));
			}
		}

		public void draw (Vector2 o, ShapeRenderer renderer) {
			renderer.setColor(Color.FOREST);
//			renderer.rectLine(o.x + start.x, o.y + start.y, o.x + end.x, o.y + end.y, radius * 2);
			tmp.set(lines.get(0));
			for (int i = 1; i < lines.size; i++) {
				tmp2.set(lines.get(i));
				renderer.rectLine(o.x + tmp.x, o.y + tmp.y, o.x + tmp2.x, o.y + tmp2.y, radius * 2);
				renderer.circle(o.x + tmp2.x, o.y + tmp2.y, radius, 16);
				tmp.set(tmp2);
			}

			renderer.setColor(Color.GREEN);
			renderer.circle(o.x + start.x, o.y + start.y, radius, 16);
			renderer.setColor(Color.OLIVE);
			renderer.circle(o.x + end.x, o.y + end.y, radius, 16);
			renderer.setColor(Color.BLUE);

		}

		public void drawTrace(Vector2 o, ShapeRenderer renderer) {
			renderer.setColor(Color.WHITE);
			if (points.size > 0) {
				tmp.set(points.get(0));
				renderer.circle(o.x + tmp.x, o.y + tmp.y, radius/2, 16);
				for (int i = 1; i < points.size; i++) {
					tmp2.set(points.get(i));
					renderer.rectLine(o.x + tmp.x, o.y + tmp.y, o.x + tmp2.x, o.y + tmp2.y, radius);
					renderer.circle(o.x + tmp2.x, o.y + tmp2.y, radius/2, 16);
					tmp.set(tmp2);
				}
			}
		}

		public boolean valid (float x, float y) {
			switch (status) {
			case IDLE:
				tmpCircle.set(start.x, start.y, radius);
				if (tmpCircle.contains(x, y)) {
					status = TraceStatus.RUNNING;
					return true;
				}
				return false;
			case RUNNING:
				tmpCircle.set(end.x, end.y, radius);
				if (tmpCircle.contains(x, y)) {
					points.add(new Vector2(x, y));
					status = TraceStatus.SUCCESS;
					return true;
				}
				if (!contains(x, y)) {
					status = TraceStatus.FAILURE;
					return false;
				}
				return true;
			case SUCCESS:
				points.add(new Vector2(x, y));
				return true;
			case FAILURE:
				return false;
			}
			return false;
		}

		private boolean contains (float x, float y) {
			tmp.set(lines.get(0));
			for (int i = 1; i < lines.size; i++) {
				tmp2.set(lines.get(i));
				float dst = Intersector.distanceSegmentPoint(tmp.x, tmp.y, tmp2.x, tmp2.y, x, y);
				if (dst <= radius) {
					points.add(new Vector2(x, y));
					return true;
				}
				tmp.set(tmp2);
			}
			points.clear();
			return false;
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		compositeTrace.draw(renderer);
		if (isDragging){
			compositeTrace.update(cs.x, cs.y);
			switch (compositeTrace.status()) {
			case IDLE:
				renderer.setColor(Color.RED);
				break;
			case RUNNING:
				renderer.setColor(Color.YELLOW);
				break;
			case SUCCESS:
				renderer.setColor(Color.GREEN);
				break;
			case FAILURE:
				renderer.setColor(Color.RED);
				break;
			}
			renderer.circle(cs.x, cs.y, .3f, 16);
		} else {
			compositeTrace.resetCurrent();
		}
		renderer.end();
	}

	boolean isDragging;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		if (pointer == 0 || button == Input.Buttons.LEFT) {
			isDragging = true;
		}
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		return super.touchDragged(screenX, screenY, pointer);
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		super.touchUp(screenX, screenY, pointer, button);
		if (pointer == 0 || button == Input.Buttons.LEFT) {
			isDragging = false;
		}
		return true;
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, TraceTest.class);
	}
}
