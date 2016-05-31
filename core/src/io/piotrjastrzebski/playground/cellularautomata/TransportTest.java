package io.piotrjastrzebski.playground.cellularautomata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.sun.org.apache.bcel.internal.generic.FLOAD;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.simple.JsonTest;

import java.util.Comparator;

/**
 * We want to implement transport belts or something
 * each belt hast 2 lanes items can move on, with at most 2 items per lanes, for 4 total
 * items should travel along the belts direction
 * we want different types of belts, straight, elbows,
 * Created by EvilEntity on 25/01/2016.
 */
@SuppressWarnings("Duplicates")
public class TransportTest extends BaseScreen {
	BitmapFont font;
	GlyphLayout glyphs;

	public final static int WIDTH = (int)VP_WIDTH;
	public final static int HEIGHT = (int)VP_HEIGHT;
	public final static int EMPTY = 0;
	public final static int BLOCK = 1;
	public final static int SOURCE = 2;
	public final static int DRAIN = 3;

	private boolean drawText;
	private boolean simEnabled = true;

	Belt[] belts = new Belt[WIDTH * HEIGHT];
	Array<Item> items = new Array<>();

	public TransportTest (GameReset game) {
		super(game);
		BitmapFont visFont = VisUI.getSkin().getFont("small-font");
		font = new BitmapFont(new BitmapFont.BitmapFontData(visFont.getData().fontFile, false), visFont.getRegions(), false);
		font.setUseIntegerPositions(false);
		font.getData().setScale(INV_SCALE);
		glyphs = new GlyphLayout();
		clear.set(Color.GRAY);

	}

	void setBelt(int x, int y, int direction) {
		int index = x + y * WIDTH;
		Belt belt = belts[index];
		if (belt == null) {
			belt = belts[index] = new Belt();
			belt.init(x, y);
		}
		belt.setDirection(direction);
	}

	void removeBelt(int x, int y) {
		int index = x + y * WIDTH;
		belts[index] = null;
	}

	int tick;
	float tickTime;
	float tickDelta = 1/10f;
	@Override public void render (float delta) {
		super.render(delta);
		// handle input
		int mx = (int)cs.x;
		int my = (int)cs.y;
		if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {
			if (Gdx.input.isKeyPressed(Input.Keys.W)) {
				setBelt(mx, my, Belt.DIRECTION_NORTH);
			} else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
				setBelt(mx, my, Belt.DIRECTION_SOUTH);
			} else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
				setBelt(mx, my, Belt.DIRECTION_WEST);
			} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
				setBelt(mx, my, Belt.DIRECTION_EAST);
			} else if (Gdx.input.isKeyPressed(Input.Keys.R)) {
				removeBelt(mx, my);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
				addItem(cs.x, cs.y);
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
			drawText = !drawText;
		}
		// simulate
		for (Item item : items) {
			item.update(delta, belts);
		}

		for (Belt belt : belts) {
			if (belt == null) continue;
			belt.update(delta, belts, items);
		}

		// draw stuff
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		enableBlending();
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				int index = x + y * WIDTH;
				Belt belt = belts[index];
				if (belt == null) continue;
				belt.draw(renderer);
			}
		}
		for (Item item : items) {
			item.draw(renderer);
		}

		renderer.end();
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.BLACK);
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				renderer.rect(x, y, 1, 1);
			}
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

	private void addItem (float x, float y) {
		Item item = new Item(x, y);
		items.add(item);
	}

	static class Belt {
		public static final int SIZE = 8;
		public static final int DIRECTION_NORTH = 1;
		public static final int DIRECTION_EAST = 2;
		public static final int DIRECTION_SOUTH = 3;
		public static final int DIRECTION_WEST = 4;
		public int index;
		public int x;
		public int y;
		public int direction;
		public float speed = 1;
		// when facing north, lane0 is left, lane1 is right
		// TODO instead of all this crap, we could do a flow field maybe?
		Array<Vector2> lane0 = new Array<>();
		Array<Vector2> lane1 = new Array<>();
		float[] flow = new float[SIZE * SIZE];

		Array<Item> items0 = new Array<>(4);
		Array<Item> items1 = new Array<>(4);

		public Belt () {
			// TODO set these based on direction
			lane0.add(new Vector2());
			lane0.add(new Vector2());
			lane1.add(new Vector2());
			lane1.add(new Vector2());
		}

		public Belt setDirection (int direction) {
			this.direction = direction;
			// TODO we want to include type as well, ie straight vs elbow
			switch (direction) {
			case DIRECTION_NORTH: {
				lane0.get(0).set(x + .33f, y);
				lane0.get(1).set(x + .33f, y + 1);
				lane1.get(0).set(x + .66f, y);
				lane1.get(1).set(x + .66f, y + 1);
			} break;
			case DIRECTION_EAST: {
				lane0.get(0).set(x, y + .33f);
				lane0.get(1).set(x + 1, y + .33f);
				lane1.get(0).set(x, y + .66f);
				lane1.get(1).set(x + 1, y + .66f);
			} break;
			case DIRECTION_SOUTH: {
				lane0.get(0).set(x + .33f, y + 1);
				lane0.get(1).set(x + .33f, y);
				lane1.get(0).set(x + .66f, y + 1);
				lane1.get(1).set(x + .66f, y);
			} break;
			case DIRECTION_WEST: {
				lane0.get(0).set(x + 1, y + .33f);
				lane0.get(1).set(x, y + .33f);
				lane1.get(0).set(x + 1, y + .66f);
				lane1.get(1).set(x, y + .66f);
			} break;
			}
			return this;
		}

		public void draw(ShapeRenderer renderer) {
			renderer.setColor(Color.LIGHT_GRAY);
			renderer.rect(x + .15f, y, .7f, 1);
			renderer.rect(x, y + .15f, 1, .7f);
			switch (direction) {
			case DIRECTION_NORTH: {
				renderer.setColor(.6f, .6f, .6f, 1);
				renderer.rect(x + .25f, y, .2f, 1);
				renderer.rect(x + .55f, y, .2f, 1);
				renderer.setColor(1, 1, 0, .66f);
				renderer.triangle(x + .2f, y + .2f, x + .5f, y + .8f, x + .8f, y + .2f);
			} break;
			case DIRECTION_EAST: {
				renderer.setColor(.6f, .6f, .6f, 1);
				renderer.rect(x, y + .25f, 1, .2f);
				renderer.rect(x, y + .55f, 1, .2f);
				renderer.setColor(1, 1, 0, .66f);
				renderer.triangle(x + .2f, y + .2f, x +.8f, y + .5f, x + .2f, y + .8f);
			} break;
			case DIRECTION_SOUTH: {
				renderer.setColor(.6f, .6f, .6f, 1);
				renderer.rect(x + .25f, y, .2f, 1);
				renderer.rect(x + .55f, y, .2f, 1);
				renderer.setColor(1, 1, 0, .66f);
				renderer.triangle(x + .2f, y + .8f, x + .5f, y + .2f, x + .8f, y + .8f);
			} break;
			case DIRECTION_WEST: {
				renderer.setColor(.6f, .6f, .6f, 1);
				renderer.rect(x, y + .25f, 1, .2f);
				renderer.rect(x, y + .55f, 1, .2f);
				renderer.setColor(1, 1, 0, .66f);
				renderer.triangle(x + .8f, y + .2f, x +.2f, y + .5f, x + .8f, y + .8f);
			} break;
			}
			renderer.setColor(Color.VIOLET);
			for (int i = 0; i < lane0.size - 1; i++) {
				Vector2 p1 = lane0.get(i);
				Vector2 p2 = lane0.get(i + 1);
				renderer.rectLine(p1, p2, .1f);
			}
			for (int i = 0; i < lane1.size - 1; i++) {
				Vector2 p1 = lane1.get(i);
				Vector2 p2 = lane1.get(i + 1);
				renderer.rectLine(p1, p2, .1f);
			}
		}

		public void drawDebug (ShapeRenderer renderer) {

		}

		public void init (int x, int y) {
			this.x = x;
			this.y = y;
			index = x + y * WIDTH;
			direction = DIRECTION_NORTH;
		}

		Vector2 nearest0 = new Vector2();
		Vector2 nearest1 = new Vector2();
		Vector2 nearestTmp = new Vector2();
		public boolean accept (Item item) {
			if (item.x < x || item.x > x + 1 || item.y < y || item.y > y + 1) return false;
			item.onBelt = this;
			Gdx.app.log("Belt", "Accepted " + item);
			// TODO snap to closest rail

			float dst0 = Float.MAX_VALUE;
			for (int i = 0; i < lane0.size - 1; i++) {
				Vector2 p1 = lane0.get(i);
				Vector2 p2 = lane0.get(i + 1);
				Intersector.nearestSegmentPoint(p1.x, p1.y, p2.x, p2.y, item.x, item.y, nearestTmp);
				float dst2 = nearestTmp.dst2(item.x, item.y);
				if (dst2 < dst0) {
					dst0 = dst2;
					nearest0.set(nearestTmp);
				}
			}
			float dst1 = Float.MAX_VALUE;
			for (int i = 0; i < lane1.size - 1; i++) {
				Vector2 p1 = lane1.get(i);
				Vector2 p2 = lane1.get(i + 1);
				Intersector.nearestSegmentPoint(p1.x, p1.y, p2.x, p2.y, item.x, item.y, nearestTmp);
				float dst2 = nearestTmp.dst2(item.x, item.y);
				if (dst2 < dst1) {
					dst1 = dst2;
					nearest1.set(nearestTmp);
				}
			}
			if (dst0 < dst1) {
				items0.add(item);
				final Vector2 first = lane0.get(0);
				// NOTE this wont work for curves, need to sort per segment or something
				items0.sort(new Comparator<Item>() {
					@Override public int compare (Item o1, Item o2) {
						return -Float.compare(first.dst2(o1.x, o1.y), first.dst2(o2.x, o2.y)) ;
					}
				});
			} else {
				items1.add(item);
				final Vector2 first = lane0.get(0);
				items1.sort(new Comparator<Item>() {
					@Override public int compare (Item o1, Item o2) {
						return -Float.compare(first.dst2(o1.x, o1.y), first.dst2(o2.x, o2.y)) ;
					}
				});
			}
			return true;
		}

		Vector2 vel = new Vector2();
		public void update(float delta, Belt[] belts, Array<Item> items) {
			for (int i = 0; i < items0.size; i++) {
				Item item = items0.get(i);
				float dst = Float.MAX_VALUE;
				// find nearest segment
				int near = -1;
				for (int j = 0; j < lane0.size - 1; j++) {
					Vector2 p1 = lane0.get(j);
					Vector2 p2 = lane0.get(j + 1);
					Intersector.nearestSegmentPoint(p1.x, p1.y, p2.x, p2.y, item.x, item.y, nearestTmp);
					float dst2 = nearestTmp.dst2(item.x, item.y);
					if (dst2 <= dst) {
						dst = dst2;
						nearest0.set(nearestTmp);
						near = j;
					}
				}
				// travel along it
				if (near != -1) {
					vel.set(lane0.get(near + 1)).sub(lane0.get(near));
					vel.nor().scl(speed * delta);
					float vx = vel.x;
					float vy = vel.y;
					// TODO handle end if path, we dont want to throw stuff on the ground
					if (dst > 0.001f) {
						vel.set(nearest0).sub(item.x, item.y);
						vel.nor().scl(speed * delta);
						vx += vel.x;
						vy += vel.y;
					}
					item.x += vx;
					item.y += vy;
					item.bounds.set(item.x, item.y, .2f, .2f);
					if (i > 0) {
						Item prev = items0.get(i - 1);
						if (item.bounds.overlaps(prev.bounds)) {
							item.x -= vx;
							item.y -= vy;
							item.bounds.set(item.x, item.y, .2f, .2f);
						}
					} else {
						Belt other = null;
						switch (direction) {
						case DIRECTION_NORTH: {
							other = belts[x + (y + 1) * WIDTH];
						}
						break;
						case DIRECTION_EAST: {
							other = belts[x + 1 + y * WIDTH];
						}
						break;
						case DIRECTION_SOUTH: {
							other = belts[x + (y - 1) * WIDTH];
						}
						break;
						case DIRECTION_WEST: {
							other = belts[x - 1 + y * WIDTH];
						}
						break;
						}
						if (other != null) {
							if (other.items0.size > 0) {
								Item prev = other.items0.get(other.items0.size - 1);
								if (item.bounds.overlaps(prev.bounds)) {
									item.x -= vx;
									item.y -= vy;
									item.bounds.set(item.x, item.y, .2f, .2f);
								}
							}
							if (other.items1.size > 0) {
								Item prev = other.items1.get(other.items1.size - 1);
								if (item.bounds.overlaps(prev.bounds)) {
									item.x -= vx;
									item.y -= vy;
									item.bounds.set(item.x, item.y, .2f, .2f);
								}
							}
						}
					}

					if (item.x < x && belts[x - 1 + y * WIDTH] == null) {
						item.x = x;
					}
					if (item.x > x + 1&& belts[x + 1 + y * WIDTH] == null) {
						item.x = x + 1;
					}
					if (item.y < y && belts[x + (y -1) * WIDTH] == null) {
						item.y = y;
					}
					if (item.y > y + 1&& belts[x + (y +1) * WIDTH] == null) {
						item.y = y + 1;
					}
				}
			}
//			for (Item item : items1) {
			for (int i = 0; i < items1.size; i++) {
				Item item = items1.get(i);
				float dst = Float.MAX_VALUE;
				// find nearest segment
				int near = -1;
				for (int j = 0; j < lane1.size - 1; j++) {
					Vector2 p1 = lane1.get(j);
					Vector2 p2 = lane1.get(j + 1);
					Intersector.nearestSegmentPoint(p1.x, p1.y, p2.x, p2.y, item.x, item.y, nearestTmp);
					float dst2 = nearestTmp.dst2(item.x, item.y);
					if (dst2 <= dst) {
						dst = dst2;
						nearest1.set(nearestTmp);
						near = j;
					}
				}
				// travel along it
				if (near != -1) {
					vel.set(lane1.get(near + 1)).sub(lane1.get(near));
					vel.nor().scl(speed * delta);
					float vx = vel.x;
					float vy = vel.y;
					// TODO handle end if path, we dont want to throw stuff on the ground
					if (dst > 0.001f) {
						vel.set(nearest1).sub(item.x, item.y);
						vel.nor().scl(speed * delta);
						vx += vel.x;
						vy += vel.y;
					}
					item.x += vx;
					item.y += vy;
					item.bounds.set(item.x, item.y, .2f, .2f);
//					if (id > 0) {
					if (i > 0) {
						Item prev = items1.get(i - 1);
						if (item.bounds.overlaps(prev.bounds)) {
							item.x -= vx;
							item.y -= vy;
							item.bounds.set(item.x, item.y, .2f, .2f);
						}
					} else {
						Belt other = null;
						switch (direction) {
						case DIRECTION_NORTH: {
							other = belts[x + (y + 1) * WIDTH];
						} break;
						case DIRECTION_EAST: {
							other = belts[x + 1 + y * WIDTH];
						} break;
						case DIRECTION_SOUTH: {
							other = belts[x + (y - 1) * WIDTH];
						} break;
						case DIRECTION_WEST: {
							other = belts[x - 1 + y * WIDTH];
						} break;
						}
						if (other != null) {
							if (other.items0.size > 0) {
								Item prev = other.items0.get(other.items0.size - 1);
								if (item.bounds.overlaps(prev.bounds)) {
									item.x -= vx;
									item.y -= vy;
									item.bounds.set(item.x, item.y, .2f, .2f);
								}
							}
							if (other.items1.size > 0) {
								Item prev = other.items1.get(other.items1.size - 1);
								if (item.bounds.overlaps(prev.bounds)) {
									item.x -= vx;
									item.y -= vy;
									item.bounds.set(item.x, item.y, .2f, .2f);
								}
							}
						}
					}
//					}
					if (item.x < x && belts[x - 1 + y * WIDTH] == null) {
						item.x = x;
					}
					if (item.x > x + 1&& belts[x + 1 + y * WIDTH] == null) {
						item.x = x + 1;
					}
					if (item.y < y && belts[x + (y -1) * WIDTH] == null) {
						item.y = y;
					}
					if (item.y > y + 1&& belts[x + (y +1) * WIDTH] == null) {
						item.y = y + 1;
					}
				}
			}
		}

		public void remove (Item item) {
			items0.removeValue(item, true);
			items1.removeValue(item, true);
		}
	}

	static class Item {
		public int type;
		public float x, y;
		public boolean searched;
		public Belt onBelt;
		public Rectangle bounds = new Rectangle();

		public Item (float x, float y) {
			this.x = x;
			this.y = y;
			bounds.set(x, y, .33f, .33f);
		}

		public void draw (ShapeRenderer renderer) {
			renderer.setColor(Color.CYAN);
			renderer.rect(x - .1f, y - .1f, .2f, .2f);
		}

		public void update (float delta, Belt[] belts) {
			bounds.set(x, y, .2f, .2f);
			if (onBelt == null) {
				if (!searched) {
					searched = true;
					for (Belt belt : belts) {
						if (belt != null && belt.accept(this))
							break;
					}
				}
			} else {
				if (x < onBelt.x || x > onBelt.x + 1 || y < onBelt.y || y > onBelt.y + 1) {
					onBelt.remove(this);
					onBelt = null;
					searched = false;
				}
			}
		}
	}

	private float map (float value, float start1, float end1, float start2, float end2) {
		return start2 + ((end2 - start2) / (end1 - start1)) * (value - start1);
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
	
	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, TransportTest.class);
	}
}
