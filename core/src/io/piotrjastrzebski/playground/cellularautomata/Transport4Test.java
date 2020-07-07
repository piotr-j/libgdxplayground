package io.piotrjastrzebski.playground.cellularautomata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * We want to implement transport belts or something
 * each belt hast 2 lanes items can move on, with at most 2 items per lanes, for 4 total
 * items should travel along the belts direction
 * we want different types of belts, straight, elbows,
 * Created by EvilEntity on 25/01/2016.
 */
@SuppressWarnings("Duplicates")
public class Transport4Test extends BaseScreen {
	BitmapFont font;
	GlyphLayout layout;

	public final static int WIDTH = (int)VP_WIDTH;
	public final static int HEIGHT = (int)VP_HEIGHT;

	private boolean simEnabled = true;

	Array<Item> items = new Array<>();
	public Transport4Test (GameReset game) {
		super(game);
		BitmapFont visFont = VisUI.getSkin().getFont("small-font");
		font = new BitmapFont(new BitmapFont.BitmapFontData(visFont.getData().fontFile, false), visFont.getRegions(), false);
		font.setUseIntegerPositions(false);
		font.getData().setScale(INV_SCALE);
		layout = new GlyphLayout();
		clear.set(Color.GRAY);

		setBelt(13, 7, BeltType.EW, -1);
		setBelt(14, 7, BeltType.EW, -1);
		setBelt(15, 7, BeltType.SN, 1);
		setBelt(4, 8, BeltType.SN, 1);
		setBelt(6, 8, BeltType.SN, -1);
		setBelt(8, 8, BeltType.EW, 1);
		setBelt(10, 8, BeltType.EW, -1);
		setBelt(13, 8, BeltType.SN, -1);
		setBelt(15, 8, BeltType.SN, 1);
		setBelt(13, 9, BeltType.SN, -1);
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
		setBelt(0, 18, BeltType.SN, 1);
		setBelt(2, 18, BeltType.SN, -1);
		setBelt(4, 18, BeltType.SN, -1);
		setBelt(6, 18, BeltType.SN, 1);
		setBelt(9, 18, BeltType.SN, 1);
		setBelt(10, 18, BeltType.SN, -1);
		setBelt(13, 18, BeltType.SN, -1);
		setBelt(14, 18, BeltType.SN, 1);
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

	protected enum BeltType {
		SN(v2s(.3f, .15f, .3f, .5f, .3f, .85f),  v2s(.7f, .15f, .7f, .5f, .7f, .85f),   v2(0, 1),  v2(0, -1)),
		EW(v2s(.85f, .3f,.5f, .3f, .15f, .3f),   v2s(.85f, .7f, .5f, .7f, .15f, .7f),   v2(-1, 0), v2(1, 0)),
		NW(v2s(.3f, .15f,.4f, .6f, .85f, .7f),   v2s(.7f, .15f, .75f, .25f, .85f, .3f), v2(1, 0),  v2(0, -1)),
		NE(v2s(.15f, .7f,.6f, .6f, .7f, .15f),   v2s(.15f, .3f, .25f, .25f, .3f, .15f), v2(0, -1), v2(-1, 0)),
		SE(v2s(.7f, .85f,.6f, .4f, .15f, .3f),   v2s(.3f, .85f, .25f, .75f, .15f, .7f), v2(-1, 0), v2(0, 1)),
		SW(v2s(.85f, 0.3f, .4f, .4f, .3f, .85f), v2s(.85f, .7f, .75f, .75f, .7f, .85f), v2(0, 1),  v2(1, 0));

		public final Vector2[] lane0;
		public final Vector2[] lane1;
		public final Vector2 nextCW;
		public final Vector2 nextCCW;
		BeltType (Vector2[] lane0, Vector2[] lane1, Vector2 nextCW, Vector2 nextCCW) {
			this.lane0 = lane0;
			this.lane1 = lane1;
			this.nextCW = nextCW;
			this.nextCCW = nextCCW;
		}
	}

	Belt[] belts = new Belt[WIDTH * HEIGHT];

	private Belt setBelt (int x, int y, BeltType type) {
		System.out.println("Belt " + x + ", " + y +", "+ type);
		int index = x + y * WIDTH;
		Belt belt = belts[index];
		if (belt == null) {
			belt = new Belt(x, y, type, this);
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
		belts[index] = null;
	}

	public void spawnItem (float x, float y) {
		Item item = new Item(x, y);
		items.add(item);
		addItem(item, false);
	}

	public void addItem (Item item, boolean snapToSlot) {
		Belt belt = getBelt(item.x, item.y);
		item.belt = belt;
		if (belt != null) {
			belt.add(item, snapToSlot);
		}
	}

	public Belt getBelt (float x, float y) {
		int gx = (int)x;
		int gy = (int)y;
		int index = gx + gy * WIDTH;
		return belts[index];
	}

	@Override public void render (float delta) {
		super.render(delta);
		// handle input
		int mx = (int)cs.x;
		int my = (int)cs.y;
		if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
				setBelt(mx, my, BeltType.NW);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
				setBelt(mx, my, BeltType.NE);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
				setBelt(mx, my, BeltType.SW);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
				setBelt(mx, my, BeltType.SE);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
			 	setBelt(mx, my, BeltType.SN);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
				setBelt(mx, my, BeltType.EW);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
				clearBelt(mx, my);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
				spawnItem(cs.x, cs.y);
			}
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

		for (Belt belt : belts) {
			if (belt != null) {
				belt.update(delta);
			}
		}

		enableBlending();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.BLACK);
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				renderer.rect(x, y, 1, 1);
			}
		}
		renderer.end();
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (Belt belt : belts) {
			if (belt != null) {
				belt.drawBase(renderer);
			}
		}
		for (Belt belt : belts) {
			if (belt != null) {
				belt.draw(renderer);
			}
		}
		for (Belt belt : belts) {
			if (belt != null) {
				belt.drawDots(renderer);
			}
		}
		for (Item item : items) {
			item.draw(renderer);
		}

		renderer.end();
	}


	protected static class Belt {
		public static final int DIRECTION_CW = 1;
		public static final int DIRECTION_CCW = -1;

		public final int x;
		public final int y;
		public final float speed = 3/1f;
		private final Transport4Test owner;
		public BeltType type;
		public int direction = DIRECTION_CW;
		public Slot[] lane0 = new Slot[3];
		public Slot[] lane1 = new Slot[3];

		public Belt (int x, int y, BeltType type, Transport4Test owner) {
			this.x = x;
			this.y = y;
			this.owner = owner;
			lane0[0] = new Slot();
			lane0[1] = new Slot();
			lane0[2] = new Slot();
			lane1[0] = new Slot();
			lane1[1] = new Slot();
			lane1[2] = new Slot();
			setType(type);
		}

		public void setType (BeltType type) {
			this.type = type;
			for (int i = 0; i < 3; i++) {
				lane0[i].init(type.lane0[i]);
				lane1[i].init(type.lane1[i]);
			}
		}

		Vector2 tmp = new Vector2();
		public void add (Item item, boolean snapToSlot) {
			Slot nearest = getSlot(item.x, item.y);
			if (nearest == null) throw new AssertionError("null nearest!");
			nearest.queue.add(item);
			item.target.set(x + nearest.x, y + nearest.y);
			if (snapToSlot) {
				item.pos(x + nearest.x, y + nearest.y);

			}
		}

		public Slot getSlot (float x, float y) {
			tmp.set(x, y).sub(this.x, this.y);
			Slot nearest = null;
			float nearestDst = 2;
			for (Slot slot : lane0) {
				float dst = tmp.dst2(slot.x, slot.y);
				if (dst < nearestDst) {
					nearest = slot;
					nearestDst = dst;
				}
			}
			for (Slot slot : lane1) {
				float dst = tmp.dst2(slot.x, slot.y);
				if (dst < nearestDst) {
					nearest = slot;
					nearestDst = dst;
				}
			}
			return nearest;
		}

		public void update (float delta) {
			if (direction == DIRECTION_CW) {
				for (int i = 0; i < 3; i++) {
					Slot slot0 = lane0[i];
					Slot slot01 = i == 2?null:lane0[i + 1];
					updateSlots(slot0, slot01, delta);
					Slot slot1 = lane1[i];
					Slot slot11 = i == 2?null:lane1[i + 1];
					updateSlots(slot1, slot11, delta);
				}
			} else {
					for (int i = 2; i >= 0; i--) {
					Slot slot0 = lane0[i];
					Slot slot01 = i == 0?null:lane0[i - 1];
					updateSlots(slot0, slot01, delta);
					Slot slot1 = lane1[i];
					Slot slot11 = i == 0?null:lane1[i - 1];
					updateSlots(slot1, slot11, delta);
				}
			}
		}

		private void updateSlots (Slot update, Slot next, float delta) {
			if (update.queue.size == 0) return;
			if (next != null) {
				if (next.queue.size == 0) {
					update.progress += delta * speed;
					Item item = update.queue.get(0);
					if (direction == DIRECTION_CW) {
						item.alpha = update.progress;
					} else {
//						item.alpha = 1 - update.progress;
						item.alpha = update.progress;
					}
					item.target.set(x + next.x, y + next.y);
					if (update.progress > 1) {
						update.progress -= 1;
						item = update.queue.removeIndex(0);
						next.queue.add(item);
						item.pos(x + next.x, y + next.y);
					}
				}
			} else {
				Belt belt = null;
				if (direction == DIRECTION_CW) {
					belt = owner.getBelt(x + .5f + type.nextCW.x, y + .5f + type.nextCW.y);
				} else {
					belt = owner.getBelt(x + .5f + type.nextCCW.x, y + .5f + type.nextCCW.y);
				}
				if (belt == null) return;
				Item item = update.queue.get(0);

				if (direction == DIRECTION_CW) {
					next = belt.getSlot(x + update.x + type.nextCW.x * .1f, y + update.y + type.nextCW.y * .33f);
				} else {
					next = belt.getSlot(x + update.x + type.nextCCW.x * .1f, y + update.y + type.nextCCW.y * .33f);
				}
				if (next.queue.size > 0) return;
				update.progress += delta * speed;
				item.alpha = update.progress;
				item.target.set(belt.x + next.x, belt.y + next.y);
				if (update.progress > 1) {
					update.progress -= 1;
					item = update.queue.removeIndex(0);
					if (direction == DIRECTION_CW) {
						item.pos(item.x + type.nextCW.x * .33f, item.y + type.nextCW.y * .33f);
					} else {
						item.pos(item.x + type.nextCCW.x * .33f, item.y + type.nextCCW.y * .33f);
					}
					owner.addItem(item, true);
				}
			}
		}

		boolean drawNextIndicator = false;
		boolean drawSlots = false;
		boolean drawSlotsConnections = true;

		public void drawBase (ShapeRenderer renderer) {
				renderer.setColor(Color.YELLOW);
				renderer.rect(x, y, 1, 1);
		}

		public void draw (ShapeRenderer renderer) {
			float ss = .3f;
			if (direction == DIRECTION_CW) {
				if (drawNextIndicator) {
					renderer.setColor(Color.CYAN);
					renderer.rectLine(x + .5f, y + .5f, x + .5f + type.nextCW.x, y + .5f + type.nextCW.y, .1f);
				}
				if (drawSlots) {
					for (Slot slot : lane0) {
						renderer.setColor(1, 0, 0, .25f + slot.progress * .75f);
						renderer.rect(x + slot.x - ss / 2, y + slot.y - ss / 2, ss, ss);
					}
					for (Slot slot : lane1) {
						renderer.setColor(0, 1, 0, .25f + slot.progress * .75f);
						renderer.rect(x + slot.x - ss / 2, y + slot.y - ss / 2, ss, ss);
					}
				}
				if (drawSlotsConnections) {
					for (int i = 0; i < 3; i++) {
						Slot slot0 = lane0[i];
						Slot slot01 = i == 2 ? null : lane0[i + 1];
						if (slot01 == null) {
							renderer.setColor(Color.SKY);
							renderer.rectLine(x + slot0.x, y + slot0.y, x + slot0.x + type.nextCW.x * .33f, y + slot0.y + type.nextCW.y * .33f, .1f);
						} else {
							renderer.setColor(Color.NAVY);
							renderer.rectLine(x + slot0.x, y + slot0.y, x + slot01.x, y + slot01.y, .1f);
						}
						renderer.circle(x + slot0.x, y + slot0.y, .075f, 12);
						Slot slot1 = lane1[i];
						Slot slot11 = i == 2 ? null : lane1[i + 1];
						if (slot11 == null) {
							renderer.setColor(Color.SKY);
							renderer.rectLine(x + slot1.x, y + slot1.y, x + slot1.x + type.nextCW.x * .33f, y + slot1.y + type.nextCW.y * .33f, .1f);
						} else {
							renderer.setColor(Color.NAVY);
							renderer.rectLine(x + slot1.x, y + slot1.y, x + slot11.x, y + slot11.y, .1f);
						}
						renderer.circle(x + slot1.x, y + slot1.y, .075f, 12);
					}
				}
			} else {
				if (drawNextIndicator) {
					renderer.setColor(Color.CYAN);
					renderer.rectLine(x + .5f, y + .5f, x + .5f + type.nextCCW.x, y + .5f + type.nextCCW.y, .1f);
				}
				if (drawSlots) {
					for (Slot slot : lane0) {
						renderer.setColor(0, 1, 0, .25f + slot.progress * .75f);
						renderer.rect(x + slot.x - ss / 2, y + slot.y - ss / 2, ss, ss);
					}
					for (Slot slot : lane1) {
						renderer.setColor(1, 0, 0, .25f + slot.progress * .75f);
						renderer.rect(x + slot.x - ss / 2, y + slot.y - ss / 2, ss, ss);
					}
				}
				if (drawSlotsConnections) {
					for (int i = 2; i >= 0; i--) {
						Slot slot0 = lane0[i];
						Slot slot01 = i == 0 ? null : lane0[i - 1];
						if (slot01 == null) {
							renderer.setColor(Color.SKY);
							renderer.rectLine(x + slot0.x, y + slot0.y, x + slot0.x + type.nextCCW.x * .33f,
								y + slot0.y + type.nextCCW.y * .33f, .1f);

						} else {
							renderer.setColor(Color.NAVY);
							renderer.rectLine(x + slot0.x, y + slot0.y, x + slot01.x, y + slot01.y, .1f);
						}
						Slot slot1 = lane1[i];
						Slot slot11 = i == 0 ? null : lane1[i - 1];
						if (slot11 == null) {
							renderer.setColor(Color.SKY);
							renderer.rectLine(x + slot1.x, y + slot1.y, x + slot1.x + type.nextCCW.x * .33f,
								y + slot1.y + type.nextCCW.y * .33f, .1f);
						} else {
							renderer.setColor(Color.NAVY);
							renderer.rectLine(x + slot1.x, y + slot1.y, x + slot11.x, y + slot11.y, .1f);
						}
					}
				}
			}
		}

		public void drawDots (ShapeRenderer renderer) {
			if (direction == DIRECTION_CW) {
				if (drawSlotsConnections) {
					for (int i = 0; i < 3; i++) {
						Slot slot0 = lane0[i];
						if (i == 2) {
							renderer.setColor(Color.SKY);
						} else {
							renderer.setColor(Color.NAVY);
						}
						renderer.circle(x + slot0.x, y + slot0.y, .075f, 12);
						Slot slot1 = lane1[i];
						if (i == 2) {
							renderer.setColor(Color.SKY);
						} else {
							renderer.setColor(Color.NAVY);
						}
						renderer.circle(x + slot1.x, y + slot1.y, .075f, 12);
					}
				}
			} else {
				if (drawSlotsConnections) {
					for (int i = 2; i >= 0; i--) {
						Slot slot0 = lane0[i];
						if (i == 0) {
							renderer.setColor(Color.SKY);
						} else {
							renderer.setColor(Color.NAVY);
						}
						renderer.circle(x + slot0.x, y + slot0.y, .075f, 12);
						Slot slot1 = lane1[i];
						if (i == 0) {
							renderer.setColor(Color.SKY);
						} else {
							renderer.setColor(Color.NAVY);
						}
						renderer.circle(x + slot1.x, y + slot1.y, .075f, 12);
					}
				}
			}
		}

		public static class Slot {
			// offset from belt position
			public float x;
			public float y;
			public Array<Item> queue = new Array<>(false, 4);
			// progress of the first item in the queue
			public float progress;

			public void init (Vector2 position) {
				x = position.x;
				y = position.y;
			}
		}
	}

	protected class Item {
		public float x;
		public float y;
		public float alpha;
		public Vector2 draw = new Vector2();
		public Vector2 target = new Vector2();
		public Belt belt;

		public Item (float x, float y) {
			this.x = x;
			this.y = y;
			target.set(x, y);
			draw.set(target);
		}

		public void pos (float x, float y) {
			this.x = x;
			this.y = y;
			target.set(x, y);
		}

		public void draw (ShapeRenderer renderer) {
			renderer.setColor(Color.CYAN);
			renderer.circle(target.x, target.y, .1f, 16);
			draw.set(x, y).interpolate(target, alpha, Interpolation.linear);
			renderer.setColor(Color.MAGENTA);
			renderer.circle(draw.x, draw.y, .15f, 16);
		}
	}

	protected static float map (float value, float start1, float end1, float start2, float end2) {
		return start2 + ((end2 - start2) / (end1 - start1)) * (value - start1);
	}

	protected static Vector2[] v2s (float x1, float y1, float x2, float y2, float x3, float y3) {
		return new Vector2[]{new Vector2(x1, y1), new Vector2(x2, y2), new Vector2(x3, y3)};
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

	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 4);
		PlaygroundGame.start(args, config, Transport4Test.class);
	}
}
