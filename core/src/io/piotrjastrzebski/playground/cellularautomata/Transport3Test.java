package io.piotrjastrzebski.playground.cellularautomata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Comparator;

/**
 * We want to implement transport belts or something
 * each belt hast 2 lanes items can move on, with at most 2 items per lanes, for 4 total
 * items should travel along the belts direction
 * we want different types of belts, straight, elbows,
 * Created by EvilEntity on 25/01/2016.
 */
@SuppressWarnings("Duplicates")
public class Transport3Test extends BaseScreen {
	private final static int SAMPLE_POINTS = 100;
	private final static float SAMPLE_POINT_DISTANCE = 1f / SAMPLE_POINTS;
	BitmapFont font;
	GlyphLayout layout;

	public final static int WIDTH = (int)VP_WIDTH;
	public final static int HEIGHT = (int)VP_HEIGHT;

	private boolean drawText;
	private boolean simEnabled = true;

	Array<Path<Vector2>> paths = new Array<>();
	Bezier<Vector2> bezier = new Bezier<>();
	Array<Item> items = new Array<>();
	public Transport3Test (GameReset game) {
		super(game);
		BitmapFont visFont = VisUI.getSkin().getFont("small-font");
		font = new BitmapFont(new BitmapFont.BitmapFontData(visFont.getData().fontFile, false), visFont.getRegions(), false);
		font.setUseIntegerPositions(false);
		font.getData().setScale(INV_SCALE);
		layout = new GlyphLayout();
		clear.set(Color.GRAY);


		paths.add(new Bezier<>(v2(0.3f, 0), v2(0.3f, 1)));
		paths.add(new Bezier<>(v2(0.7f -1, 0), v2(0.7f -1, 1)));
		paths.add(new Bezier<>(
			v2(0.3f, 0),
			v2(0.25f, .75f),
			v2(1f, .7f)
		));
		paths.add(new Bezier<>(
			v2(0.7f -1, 0),
			v2(0.675f -1, .325f),
			v2(1f -1, .3f)
		));


		setBelt(13, 7, BeltType.EW, -1);
		setBelt(14, 7, BeltType.EW, -1);
		setBelt(15, 7, BeltType.NS, 1);
		setBelt(4, 8, BeltType.NS, 1);
		setBelt(6, 8, BeltType.NS, -1);
		setBelt(8, 8, BeltType.EW, 1);
		setBelt(10, 8, BeltType.EW, -1);
		setBelt(13, 8, BeltType.NS, -1);
		setBelt(15, 8, BeltType.NS, 1);
		setBelt(13, 9, BeltType.NS, -1);
		setBelt(14, 9, BeltType.EW, 1);
		setBelt(15, 9, BeltType.EW, 1);
		setBelt(4, 10, BeltType.SW, 1);
		setBelt(6, 10, BeltType.SW, -1);
		setBelt(8, 10, BeltType.SE, 1);
		setBelt(10, 10, BeltType.SE, -1);
		setBelt(16, 11, BeltType.EW, -1);
		setBelt(17, 11, BeltType.EW, -1);
		setBelt(18, 11, BeltType.EW, -1);
		setBelt(19, 11, BeltType.EW, -1);
		setBelt(4, 12, BeltType.NW, 1);
		setBelt(6, 12, BeltType.NW, -1);
		setBelt(8, 12, BeltType.NE, 1);
		setBelt(10, 12, BeltType.NE, -1);
		setBelt(16, 12, BeltType.EW, 1);
		setBelt(17, 12, BeltType.EW, 1);
		setBelt(18, 12, BeltType.EW, 1);
		setBelt(19, 12, BeltType.EW, 1);
		setBelt(8, 16, BeltType.SW, 1);
		setBelt(9, 16, BeltType.EW, 1);
		setBelt(10, 16, BeltType.EW, 1);
		setBelt(11, 16, BeltType.SE, 1);
		setBelt(12, 16, BeltType.SW, -1);
		setBelt(13, 16, BeltType.EW, -1);
		setBelt(14, 16, BeltType.EW, -1);
		setBelt(15, 16, BeltType.SE, -1);
		setBelt(0, 17, BeltType.SW, 1);
		setBelt(1, 17, BeltType.EW, 1);
		setBelt(2, 17, BeltType.SE, 1);
		setBelt(4, 17, BeltType.SW, -1);
		setBelt(5, 17, BeltType.EW, -1);
		setBelt(6, 17, BeltType.SE, -1);
		setBelt(8, 17, BeltType.NW, 1);
		setBelt(9, 17, BeltType.SE, -1);
		setBelt(10, 17, BeltType.SW, -1);
		setBelt(11, 17, BeltType.NE, 1);
		setBelt(12, 17, BeltType.NW, -1);
		setBelt(13, 17, BeltType.SE, 1);
		setBelt(14, 17, BeltType.SW, 1);
		setBelt(15, 17, BeltType.NE, -1);
		setBelt(0, 18, BeltType.NS, 1);
		setBelt(2, 18, BeltType.NS, -1);
		setBelt(4, 18, BeltType.NS, -1);
		setBelt(6, 18, BeltType.NS, 1);
		setBelt(9, 18, BeltType.NS, 1);
		setBelt(10, 18, BeltType.NS, -1);
		setBelt(13, 18, BeltType.NS, -1);
		setBelt(14, 18, BeltType.NS, 1);
		setBelt(0, 19, BeltType.NW, 1);
		setBelt(1, 19, BeltType.EW, -1);
		setBelt(2, 19, BeltType.NE, 1);
		setBelt(4, 19, BeltType.NW, -1);
		setBelt(5, 19, BeltType.EW, 1);
		setBelt(6, 19, BeltType.NE, -1);
		setBelt(9, 19, BeltType.NW, 1);
		setBelt(10, 19, BeltType.NE, 1);
		setBelt(13, 19, BeltType.NW, -1);
		setBelt(14, 19, BeltType.NE, -1);
	}

	Belt[] belts = new Belt[WIDTH * HEIGHT];

	private Belt setBelt (int x, int y, BeltType type) {
		System.out.println("Belt " + x + ", " + y +", "+ type);
		int index = x + y * WIDTH;
		Belt belt = belts[index];
		if (belt == null) {
			belt = new Belt(x, y, type);
			belts[index] = belt;
		} else {
			if (belt.type == type) {
				if (belt.direction == Belt.DIRECTION_CW) {
					belt.direction = Belt.DIRECTION_CCW;
				} else {
					belt.direction = Belt.DIRECTION_CW;
				}
			} else {
				belt.setType(type);
			}
		}
		return belt;
	}

	private void setBelt (int x, int y, BeltType type, int direction) {
		Belt belt = setBelt(x, y, type);
		belt.direction = direction;
	}

	private void clearBelt (int x, int y) {
		System.out.println("Clear " + x + ", " + y);
		int index = x + y * WIDTH;
		Belt belt = belts[index];
		if (belt != null) {
			belt.clear();
		}
		belts[index] = null;
	}

	public void spawnItem (float x, float y) {
		Item item = new Item(x, y);
		items.add(item);
		addItem(item);
	}

	public void addItem (Item item) {
		Belt belt = getBelt(item.position.x, item.position.y);
		if (belt != null) {
			belt.add(item);
		}
	}

	public Belt getBelt (float x, float y) {
		int gx = (int)x;
		int gy = (int)y;
		int index = gx + gy * WIDTH;
		return belts[index];
	}


	static final Vector2 tmpV = new Vector2();
	static final Vector2 tmpV2 = new Vector2();
	float alpha;
	@Override public void render (float delta) {
		super.render(delta);
		// handle input
		int mx = (int)cs.x;
		int my = (int)cs.y;
		if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {
			if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
				setBelt(mx, my, BeltType.NW);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
				setBelt(mx, my, BeltType.NE);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
				setBelt(mx, my, BeltType.SW);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
				setBelt(mx, my, BeltType.SE);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
			 	setBelt(mx, my, BeltType.NS);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
				setBelt(mx, my, BeltType.EW);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
				clearBelt(mx, my);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
				spawnItem(cs.x, cs.y);
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
			drawText = !drawText;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
			simEnabled = !simEnabled;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
			for (Belt belt : belts) {
				if (belt != null) {
					System.out.println("setBelt("+belt.x + ", " + belt.y + ", BeltType." + belt.type + ", " + belt.direction+");");
				}
			}

		}

		if (simEnabled) {
			for (Belt belt : belts) {
				if (belt != null) {
					belt.update(delta);
				}
			}
			for (Item item : items) {
				item.update(delta);
			}
		}

		// draw stuff
		enableBlending();
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();

		batch.end();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);


		renderer.end();
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.BLACK);
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				renderer.rect(x, y, 1, 1);
			}
		}

//		float ox = 5;
//		for (Path<Vector2> path : paths) {
//			renderer.setColor(Color.CYAN);
//			drawPath(renderer, path, ox, 10, 1);
//			renderer.setColor(Color.RED);
//			path.valueAt(tmp, alpha);
//			renderer.circle(ox + tmp.x, 10 + tmp.y, .15f, 16);
//			ox += 1;
//		}
//		alpha+=delta;
//		if (alpha > 1) alpha-=1;

//		float sx = 10;
//		for (BeltType beltType : BeltType.values()) {
//			draw(beltType, sx, 10, true);
//			sx += 2;
//		}
//
//		draw(BeltType.NW, 5f, 6f, false);
//		draw(BeltType.NE, 6f, 6f, false);
//		draw(BeltType.SW, 5f, 5f, false);
//		draw(BeltType.SE, 6f, 5f, false);
		for (Belt belt : belts) {
			if (belt != null) {
				belt.draw(renderer);
			}
		}
		renderer.end();
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (Item item : items) {
			item.draw(renderer);
		}
		renderer.end();


		if (drawText) {
			batch.setProjectionMatrix(gameCamera.combined);
			batch.begin();
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					int index = x + y * WIDTH;

				}
			}
			batch.end();
		}
	}

	private void draw (BeltType beltType, float x, float y, boolean points) {
		renderer.setColor(.75f, 0, 0, 1);
		bezier.set(beltType.lane0);
		drawPath(renderer, bezier, x, y, 1, points);
		renderer.setColor(0, .75f, 0, 1);
		bezier.set(beltType.lane1);
		drawPath(renderer, bezier, x, y, 1, points);
	}

	private static void drawPath(ShapeRenderer renderer, Path<Vector2> path, float x, float y, float scale, boolean points) {
		float val = 0f;
		while (val <= 1f - SAMPLE_POINT_DISTANCE) {
			path.valueAt(tmpV, val);
			path.valueAt(tmpV2, val + SAMPLE_POINT_DISTANCE);
			renderer.line(x + tmpV.x * scale, y + tmpV.y * scale, x + tmpV2.x * scale, y + tmpV2.y * scale);
			val += SAMPLE_POINT_DISTANCE;
		}
		if (points) {
			if (path instanceof Bezier) {
				Bezier<Vector2> bezier = (Bezier<Vector2>)path;
				for (Vector2 v2 : bezier.points) {
					renderer.circle(x + v2.x * scale, y + v2.y * scale, 0.075f, 8);
				}
			}
		}
	}

	protected enum BeltType {
		NS(v2s(v2(.3f, 0f), v2(.3f, 1f)), v2s(v2(.7f, 0f), v2(.7f, 1f))),
		EW(v2s(v2(1f, .3f), v2(0f, .3f)), v2s(v2(1f, .7f), v2(0f, .7f))),
		NW(v2s(v2(.3f, 0), v2(.25f, .75f), v2(1f, .7f)), v2s(v2(.7f, 0), v2(.675f, .325f), v2(1f, .3f))),
		NE(v2s(v2(0f, .7f), v2(.75f, .75f), v2(.7f, .0f)), v2s(v2(0f, .3f), v2(.325f, .325f), v2(.3f, 0f))),
		SE(v2s(v2(.7f, 1f), v2(.75f, .25f), v2(0f, .3f)), v2s(v2(.3f, 1), v2(.325f, .675f), v2(0f, .7f))),
		SW(v2s(v2(1f, 0.3f), v2(.25f, .25f), v2(.3f, 1f)), v2s(v2(1f, .7f), v2(.675f, .675f), v2(.7f, 1f)));

		public final Vector2[] lane0;
		public final Vector2[] lane1;
		BeltType (Vector2[] lane0, Vector2[] lane1) {
			this.lane0 = lane0;
			this.lane1 = lane1;
		}
	}

	protected static class Belt {
		public static final int SAMPLES = 4;
		public static final float SAMPLE_DST = 1f/SAMPLES;
		public static final int DIRECTION_CW = 1;
		public static final int DIRECTION_CCW = -1;
		public int x;
		public int y;
		public BeltType type;
		public int direction = DIRECTION_CW;
		Bezier<Vector2> bezier = new Bezier<>();
		Array<Item> lane0Items = new Array<>(true, 4);
		Array<Item> lane1Items = new Array<>(true, 4);
		Array<Item> items = new Array<>(true, 4);
		// time in seconds it takes an item to travel over this belt
		public float speed = 1;
		private boolean drawCircles = false;
		private boolean drawItems = true;

		public Belt (int x, int y, BeltType type) {
			this.x = x;
			this.y = y;
			setType(type);
		}

		public void setType (BeltType type) {
			this.type = type;
		}

		public void setDirection (int direction) {
			this.direction = direction;
		}

		float a = 0;
		public void update(float delta) {
			a += delta;
			if (a > 1) a -= 1;
		}

		static Vector2 tmp = new Vector2();
		static Vector2 tmp1 = new Vector2();
		static Vector2 tmp2 = new Vector2();
		static Vector2 tmp3 = new Vector2();
		static Vector2 tmp4 = new Vector2();
		Color lane0Color = new Color(.75f, 0, 0, 1);
		Color lane1Color = new Color(0, .75f, 0, 1);
		public void draw (ShapeRenderer renderer) {
			if (direction == DIRECTION_CW) {
				bezier.set(type.lane0);
				drawPath(renderer, lane0Color);
				bezier.set(type.lane1);
				drawPath(renderer, lane1Color);
			} else if (direction == DIRECTION_CCW) {
				bezier.set(type.lane0);
				drawPath(renderer, lane1Color);
				bezier.set(type.lane1);
				drawPath(renderer, lane0Color);
			}

			if (drawCircles) {
				if (direction == DIRECTION_CW) {
					bezier.set(type.lane0);
					bezier.valueAt(tmp, a);
					renderer.setColor(lane0Color);
					renderer.circle(x + tmp.x, y + tmp.y, .1f, 16);
					bezier.set(type.lane1);
					bezier.valueAt(tmp, a);
					renderer.setColor(lane1Color);
					renderer.circle(x + tmp.x, y + tmp.y, .1f, 16);
				} else if (direction == DIRECTION_CCW) {
					bezier.set(type.lane0);
					bezier.valueAt(tmp, 1 - a);
					renderer.setColor(lane1Color);
					renderer.circle(x + tmp.x, y + tmp.y, .1f, 16);
					bezier.set(type.lane1);
					bezier.valueAt(tmp, 1 - a);
					renderer.setColor(lane0Color);
					renderer.circle(x + tmp.x, y + tmp.y, .1f, 16);
				}
			}
			if (drawItems) {
				Color color = direction == DIRECTION_CW ? lane0Color : lane1Color;
				for (int i = 0; i < lane0Items.size; i++) {
//					float a = .25f + i/lane0Items.size * .75f;
					float a = i/(float)lane0Items.size;
					Item item = lane0Items.get(i);
//					renderer.setColor(color.r, color.g, color.b, a);
					renderer.setColor(a, 0, 1 - a, 1);
					renderer.circle(item.position.x, item.position.y, .175f, 16);
				}
				color = direction == DIRECTION_CW ? lane1Color : lane0Color;
				for (int i = 0; i < lane1Items.size; i++) {
//					float a = .25f + i/lane1Items.size * .75f;
					float a = i/(float)lane1Items.size;
					Item item = lane1Items.get(i);
//					renderer.setColor(color.r, color.g, color.b, a);
					renderer.setColor(a, 0, 1 - a, 1);
					renderer.circle(item.position.x, item.position.y, .175f, 16);
				}
			}
		}

		private void drawPath(ShapeRenderer renderer, Color color) {
			float val = 0f;
			while (val <= 1f - SAMPLE_DST) {
				color.a = .5f + val/2;
				bezier.valueAt(tmpV, val);
				bezier.valueAt(tmpV2, val + SAMPLE_DST);
				renderer.setColor(color);
				renderer.line(x + tmpV.x, y + tmpV.y, x + tmpV2.x, y + tmpV2.y);
				val += SAMPLE_DST;
			}
		}

		public void clear () {

		}

		public void add (Item item) {
			tmp.set(item.position).sub(x, y);
			if (dst(type.lane0, tmp) < dst(type.lane1, tmp)) {
				item.lane = direction == DIRECTION_CW?0 : 1;
				bezier.set(type.lane0);
				lane0Items.add(item);
			} else {
				item.lane = direction == DIRECTION_CW?1 : 0;
				bezier.set(type.lane1);
				lane1Items.add(item);
			}
			items.add(item);
			float progress = bezier.locate(tmp);
			bezier.valueAt(tmp1, progress);
			item.position.set(tmp1).add(x, y);
			item.progress = direction == DIRECTION_CW?progress:1-progress;
			item.direction = direction;
			item.belt = this;

			if (direction == DIRECTION_CW) {
				lane0Items.sort(cmpCW);
				lane1Items.sort(cmpCW);
			} else {
				lane0Items.sort(cmpCCW);
				lane1Items.sort(cmpCCW);
			}
		}

		Comparator<Item> cmpCW = new Comparator<Item>() {
			@Override public int compare (Item o1, Item o2) {
				return Float.compare(o2.progress, o1.progress);
//				return Float.compare(o1.progress, o2.progress);
			}
		};

		Comparator<Item> cmpCCW = new Comparator<Item>() {
			@Override public int compare (Item o1, Item o2) {
				return Float.compare(o1.progress, o2.progress);
//				return Float.compare(o1.progress, o2.progress);
			}
		};

		public Array<Item> getItems (Item item) {
			if (direction == DIRECTION_CW) {
				if (item.lane == 0) {
					return lane0Items;
				} else {
					return lane1Items;
				}
			} else {
				if (item.lane == 1) {
					return lane0Items;
				} else {
					return lane1Items;
				}
			}
		}

		private float dst (Vector2[] lane, Vector2 point) {
			bezier.set(lane);
			float val = 0f;
			float nearest = 99;
			while (val <= 1f - SAMPLE_DST) {
				bezier.valueAt(tmp1, val);
				bezier.valueAt(tmp2, val + SAMPLE_DST);
				float dst = Intersector.nearestSegmentPoint(tmp1, tmp2, point, tmp3).dst(point);
				if (dst < nearest) nearest = dst;
				val += SAMPLE_DST;
			}
			return nearest;
		}

		public void remove (Item item) {
			lane0Items.removeValue(item, true);
			lane1Items.removeValue(item, true);
			items.removeValue(item, true);
		}
	}

	protected class Item {
		public Vector2 position = new Vector2();
		// 0-1 progress on the current belt
		public float progress;
		public int lane = -1;
		public int direction = 1;
		public Belt belt;

		public Item (float x, float y) {
			position.set(x, y);
		}
		Vector2 tmp = new Vector2();
		public void draw (ShapeRenderer renderer) {
			renderer.setColor(Color.YELLOW);
			// lanes are swapped on ccw direction
			float a = .25f + progress * .75f;
			if (lane == 0) {
				renderer.setColor(a, 0, 0, 1);
			} else if (lane == 1) {
				renderer.setColor(0, a, 0, 1);
			}
			renderer.circle(position.x, position.y, .1f, 16);
		}

		public void update (float delta) {
			if (belt == null) return;
			float p = progress;
			p += delta;
			if (belt.direction == Belt.DIRECTION_CW) {
				if (lane == 0) {
					belt.bezier.set(belt.type.lane0);
				} else if (lane == 1) {
					belt.bezier.set(belt.type.lane1);
				}
				belt.bezier.valueAt(tmp, p);
			} else {
				if (lane == 0) {
					belt.bezier.set(belt.type.lane1);
				} else if (lane == 1) {
					belt.bezier.set(belt.type.lane0);
				}
				belt.bezier.valueAt(tmp, 1 - p);
			}
			tmp.add(belt.x, belt.y);
			Array<Item> items = belt.getItems(this);
			int index = items.indexOf(this, true);
			if (index == -1) throw new AssertionError("");
			if (index > 0) {
				Item other = items.get(index - 1);
				if (other.position.dst2(tmp) > .2f * .2f) {
					position.set(tmp);
					progress = p;
				}
			} else {
				Belt nextBelt = getBelt(tmp.x, tmp.y);
				if (nextBelt != null && nextBelt != belt) {
					boolean overlap = false;
					for (Item other : nextBelt.items) {
						if (other.position.dst2(tmp) <= .2f * .2f) {
							overlap = true;
							break;
						}
					}
					if (!overlap) {
						position.set(tmp);
						progress = p;
					}
				} else if (nextBelt == belt){
					position.set(tmp);
					progress = p;
				}
			}

			if (progress > 1) {
				belt.remove(this);
				belt = null;
				addItem(this);
			}
		}
	}

	protected static float map (float value, float start1, float end1, float start2, float end2) {
		return start2 + ((end2 - start2) / (end1 - start1)) * (value - start1);
	}

	protected static Vector2[] v2s (Vector2... v2s) {
		return v2s;
	}

	protected static Vector2 v2 (float x, float y) {
		return new Vector2(x, y);
	}


	Vector2 tmp = new Vector2();
	public void rect (float x1, float y1, float x2, float y2, float width, Color color1, Color color2) {
		Vector2 t = tmp.set(y2 - y1, x1 - x2).nor();
		width *= 0.5f;
		float tx = t.x * width;
		float ty = t.y * width;
		renderer.triangle(
			x1 + tx, y1 + ty,
			x1 - tx, y1 - ty,
			x2 + tx, y2 + ty,
			color1, color1, color2
		);
		renderer.triangle(
			x2 - tx, y2 - ty,
			x2 + tx, y2 + ty,
			x1 - tx, y1 - ty,
			color2, color2, color1
		);
	}

	@Override public boolean keyDown (int keycode) {
		switch(keycode) {
		case Input.Keys.SPACE: {
			simEnabled = !simEnabled;
		} break;
		case Input.Keys.Z: {
			for (int i = 0; i < WIDTH * HEIGHT; i++) {

			}
		} break;
		}
		return true;
	}

	@Override public void resize (int width, int height) {
		gameViewport.update(width, height, true);
		guiViewport.update(width, height, true);
	}

	@Override public void dispose () {
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, Transport3Test.class);
	}
}
