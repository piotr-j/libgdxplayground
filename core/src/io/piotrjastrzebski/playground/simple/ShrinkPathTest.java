package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class ShrinkPathTest extends BaseScreen {
	private static final String TAG = ShrinkPathTest.class.getSimpleName();

	String xmlBox = "M 0,952.36218 L 100,952.36218 L 100,1052.3622 L 0,1052.3622 Z";
	String xmlInner3 = "M 90.83974,-1.13887 L 81.45331,-3.01594 L 72.73228,-3.3219 L 68.89991,-2.16406 L 65.56821,0.25254 L 64.06045,2.62949 L 63.54977,5.39453 L 64.53819,11.75746 L 66.57065,18.67852 L 67.68433,25.49492 L 66.3274,30.08874 L 62.59216,33.0107 L 56.98193,34.55105 L 50,35 L 43.01808,34.55105 L 37.40784,33.0107 L 33.6726,30.08874 L 32.31567,25.49492 L 33.42935,18.67852 L 35.46181,11.75746 L 36.45024,5.39453 L 35.93955,2.62949 L 34.43179,0.25254 L 31.1001,-2.16406 L 27.26772,-3.3219 L 18.54669,-3.01594 L 9.16027,-1.13887 L 0,0 L 1.13887,9.16026 L 3.01594,18.54669 L 3.3219,27.26772 L 2.16406,31.10009 L -0.25254,34.43179 L -2.62949,35.93955 L -5.39453,36.45023 L -11.75746,35.46181 L -18.67852,33.42935 L -25.49492,32.31567 L -30.08874,33.6726 L -33.0107,37.40784 L -34.55105,43.01807 L -35,50 L -34.55105,56.98193 L -33.0107,62.59216 L -30.08874,66.3274 L -25.49492,67.68433 L -18.67852,66.57065 L -11.75746,64.53819 L -5.39453,63.54976 L -2.62949,64.06045 L -0.25254,65.56821 L 2.16406,68.89991 L 3.3219,72.73228 L 3.01594,81.453307 L 1.13887,90.839731 L 0,100 L 9.16027,101.13887 L 18.54669,103.01594 L 27.26772,103.3219 L 31.1001,102.16406 L 34.43179,99.74746 L 35.93955,97.37051 L 36.45024,94.605468 L 35.46181,88.242538 L 33.42935,81.321478 L 32.31567,74.505078 L 33.6726,69.91126 L 37.40784,66.9893 L 43.01808,65.44895 L 50,65 L 56.98193,65.44895 L 62.59216,66.9893 L 66.3274,69.91126 L 67.68433,74.505078 L 66.57065,81.321478 L 64.53819,88.242538 L 63.54977,94.605468 L 64.06045,97.37051 L 65.56821,99.74746 L 68.89991,102.16406 L 72.73228,103.3219 L 81.45331,103.01594 L 90.83974,101.13887 L 100,100 L 98.86113,90.839733 L 96.98407,81.453309 L 96.6781,72.732282 L 97.83594,68.8999 L 100.25254,65.56821 L 102.62949,64.06045 L 105.39453,63.54976 L 111.75746,64.53819 L 118.67852,66.57065 L 125.49492,67.68432 L 130.08874,66.3274 L 133.0107,62.59216 L 134.55105,56.98192 L 135,50 L 134.55105,43.01807 L 133.0107,37.40783 L 130.08874,33.6726 L 125.49492,32.31567 L 118.67852,33.42934 L 111.75746,35.46181 L 105.39453,36.45023 L 102.62949,35.93955 L 100.25254,34.43179 L 97.83594,31.10009 L 96.6781,27.26771 L 96.98407,18.54669 L 98.86113,9.16026 L 100,0 Z";
	Array<Piece> pieces = new Array<>();
	boolean drawGrid = true;

	public ShrinkPathTest (GameReset game) {
		super(game);
		clear.set(Color.GRAY);
		gameCamera.zoom = .25f;
		gameCamera.update();
//		pieces.add(new Piece(parsePath(xmlBox), 0f, 0f));
		pieces.add(new Piece(parsePath(xmlInner3), 0f, 0f));
	}

	protected static Array<Vector2> parsePath (String path) {
		Array<Vector2> points = new Array<>();
		char[] chars = path.toCharArray();
		char[] temp = new char[8 + 1 + 8];
		for (int i = 0; i < chars.length;) {
			char aChar = chars[i++];
			if (aChar == ' ') continue;
			if (aChar == 'M' || aChar == 'L') {
				// we have new point
				char next;
				int len = 0;
				while (i < chars.length) {
					next = chars[i++];
					if (next == ' ') continue;
					if (next == ',') {
						break;
					}
					temp[len++] = next;
				}
				float x = Float.parseFloat(new String(temp, 0, len));
				len = 0;
				while (i < chars.length) {
					next = chars[i++];
					if (next == ' ') {
						i--;
						break;
					}
					temp[len++] = next;
				}
				float y = Float.parseFloat(new String(temp, 0, len));
				// dunno wtf is up this this translate, needed to setPosition y to 0
//				points.add(new Vector2(x * INV_SCALE, (y -952.36216f) * INV_SCALE));
				// -y + 100 to flip it
//				points.add(new Vector2(x, (-y +952.36216f + 100)));
				points.add(new Vector2(x, (-y + 100)));
			} else if (aChar == 'Z') {
//				points.add(points.get(0).cpy());
			}
		}
		// BayazitDecomposer doesnt like duplicate points
		if (points.first().epsilonEquals(points.get(points.size -1), 0.001f)) {
			points.removeIndex(points.size -1);
		}
		IntArray remove = new IntArray();

		for (int i = 0; i < points.size; i++) {
			for (int j = 0; j < points.size; j++) {
				if (i == j) continue;
				Vector2 p1 = points.get(i);
				Vector2 p2 = points.get(j);
				if (p1.epsilonEquals(p2, .001f)) {
					if (!remove.contains(i) && !remove.contains(j))
						remove.add(i);
				}
			}
		}
		remove.sort();
		remove.reverse();
		for (int i = 0; i < remove.size; i++) {
			points.removeIndex(remove.get(i));
		}
		return points;
	}

	@Override public void render (float delta) {
		super.render(delta);
		enableBlending();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		if (drawGrid) {
			renderer.setColor(1, 1, 1, .25f);
			float sx = (int)(-VP_WIDTH/2);
			float sy = (int)(-VP_HEIGHT/2);
			int width = MathUtils.ceil(VP_WIDTH);
			int height = MathUtils.ceil(VP_HEIGHT);
			for (int x = 0; x <= width; x++) {
				renderer.line(sx + x, sy, sx + x, sy + height);
			}
			for (int y = 0; y <= height; y++) {
				renderer.line(sx, sy + y, sx + width, sy + y);
			}
		}

		for (Piece piece : pieces) {
			piece.draw(renderer);
		}

		renderer.end();
	}

	protected static class Piece {
		Vector2 pos = new Vector2();
		Array<Vector2> rawPoints = new Array<>();
		Array<Vector2> shrinkPoints = new Array<>();

		public Piece (Array<Vector2> rawPoints, float x, float y) {
			this.rawPoints.addAll(rawPoints);
			pos.set(x, y);
			for (int i = 0; i < rawPoints.size; i++) {
				Vector2 p1 = rawPoints.get(i % rawPoints.size);
				Vector2 p2 = rawPoints.get((i + 1) % rawPoints.size);
				Vector2 p3 = rawPoints.get((i + 2) % rawPoints.size);
				tmp1.set(p1).sub(p2);
				tmp2.set(p2).sub(p3);
				tmp3.set(0, 1).rotateDeg(tmp1.angleDeg(tmp2) / 2 + tmp1.angleDeg()).nor().scl(2f);
				shrinkPoints.add(new Vector2(p2).sub(tmp3));
			}

		}
		Vector2 tmp1 = new Vector2();
		Vector2 tmp2 = new Vector2();
		Vector2 tmp3 = new Vector2();
		float scale = .01f;
		public void draw (ShapeRenderer renderer) {
			Vector2 p1;
			Vector2 p2;
			Vector2 p3;
			renderer.setColor(Color.CYAN);
			for (int i = 0; i < rawPoints.size; i++) {
				p1 = rawPoints.get(i % rawPoints.size);
				p2 = rawPoints.get((i + 1) % rawPoints.size);
				renderer.line(pos.x + p1.x * scale, pos.y + p1.y * scale, pos.x + p2.x * scale, pos.y + p2.y * scale);
			}


//			for (int i = 1; i < rawPoints.size; i++) {
			for (int i = 0; i < rawPoints.size; i++) {
				renderer.setColor(Color.MAGENTA);
				p1 = rawPoints.get(i % rawPoints.size);
				p2 = rawPoints.get((i + 1) % rawPoints.size);
//				renderer.line(pos.x + p1.x * scale, pos.y + p1.y * scale, pos.x + p2.x * scale, pos.y + p2.y * scale);
				p3 = rawPoints.get((i + 2) % rawPoints.size);
//				renderer.line(pos.x + p2.x * scale, pos.y + p2.y * scale, pos.x + p3.x * scale, pos.y + p3.y * scale);
				tmp1.set(p1).sub(p2);
				tmp2.set(p2).sub(p3);
				tmp3.set(0, 1).rotateDeg(tmp1.angleDeg(tmp2) / 2 + tmp1.angleDeg()).nor().scl(.1f);
//				tmp3.set(0, 1).rotate(45).nor();
				renderer.line(p2.x * scale, p2.y * scale, p2.x * scale -tmp3.x, p2.y * scale - tmp3.y);
				renderer.setColor(Color.YELLOW);
//				renderer.line(p2.x * scale, p2.y * scale, p3.x * scale, p3.y * scale);
//				break;
			}
			renderer.setColor(Color.YELLOW);
			for (int i = 0; i < shrinkPoints.size; i++) {
				p1 = shrinkPoints.get(i % shrinkPoints.size);
				p2 = shrinkPoints.get((i + 1) % shrinkPoints.size);
				renderer.line(pos.x + p1.x * scale, pos.y + p1.y * scale, pos.x + p2.x * scale, pos.y + p2.y * scale);
			}

		}

	}

	private void draw (Array<Vector2> points, float ox, float oy) {
		for (int i = 0; i < points.size -1; i++) {
			Vector2 p1 = points.get(i);
			Vector2 p2 = points.get(i + 1);
			renderer.line(p1.x + ox, p1.y + oy, p2.x + ox, p2.y + oy);
		}
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, ShrinkPathTest.class);
	}
}
