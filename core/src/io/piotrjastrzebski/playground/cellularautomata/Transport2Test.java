package io.piotrjastrzebski.playground.cellularautomata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.cellularautomata.Transport2Test.FlowField.FFType;

import java.util.Comparator;

/**
 * We want to implement transport belts or something
 * each belt hast 2 lanes items can move on, with at most 2 items per lanes, for 4 total
 * items should travel along the belts direction
 * we want different types of belts, straight, elbows,
 * Created by EvilEntity on 25/01/2016.
 */
@SuppressWarnings("Duplicates")
public class Transport2Test extends BaseScreen {
	BitmapFont font;
	GlyphLayout layout;

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

	ObjectMap<FFType, FlowField> fields = new ObjectMap<>();
	Texture fieldTexture;
	Texture fieldViewTexture;
	Texture fieldLanesTexture;
	Texture fieldProgressTexture;
	Array<FFType> types = new Array<>();
	private boolean testDrawFields;
	private boolean testDrawFieldsLables;

	public Transport2Test (GameReset game) {
		super(game);
		BitmapFont visFont = VisUI.getSkin().getFont("small-font");
		font = new BitmapFont(new BitmapFont.BitmapFontData(visFont.getData().fontFile, false), visFont.getRegions(), false);
		font.setUseIntegerPositions(false);
		font.getData().setScale(INV_SCALE);
		layout = new GlyphLayout();
		clear.set(Color.GRAY);
		fieldTexture = new Texture(Gdx.files.internal("flow.bmp"));
		fieldViewTexture = new Texture(Gdx.files.internal("flow-view.bmp"));
		fieldTexture.getTextureData().prepare();
		Pixmap flowData = fieldTexture.getTextureData().consumePixmap();
		fieldLanesTexture = new Texture(Gdx.files.internal("flow-lanes.bmp"));
		fieldLanesTexture.getTextureData().prepare();
		Pixmap laneData = fieldLanesTexture.getTextureData().consumePixmap();
		fieldProgressTexture = new Texture(Gdx.files.internal("flow-progress.bmp"));
		fieldProgressTexture.getTextureData().prepare();
		Pixmap progressData = fieldProgressTexture.getTextureData().consumePixmap();
		// flow data is made 64 x 64 pixels
		// layout:
		// E, 		E, 		E, 		E
		// CW_NW, 	CW_NE, 	CCW_NW, 	CCW_NE,
		// CW_SW, 	CW_SE, 	CCW_SW, 	CCW_SE,
		// W_E,		S_N,	 	E_W,		N_S
		Color tmp = new Color();
		Vector2 v2 = new Vector2();
		int eid = 0;
		types.addAll(FFType.values());
		types.sort(new Comparator<FFType>() {
			@Override public int compare (FFType o1, FFType o2) {
				return o1.sort > o2.sort?1:-1;
			}
		});
		for (int fy = 0; fy < 4; fy++) {
			for (int fx = 0; fx < 4; fx++) {
				FlowField ff = new FlowField(types.get(eid));
				ff.flowRegion = new TextureRegion(fieldTexture, fx * FlowField.SIZE, 32 - fy * FlowField.SIZE, 8, -8);
				ff.flowRegion.flip(false, true);
				ff.laneRegion = new TextureRegion(fieldLanesTexture, fx * FlowField.SIZE, 32 - fy * FlowField.SIZE, 8, -8);
				ff.laneRegion.flip(false, true);
				ff.progressRegion = new TextureRegion(fieldProgressTexture, fx * FlowField.SIZE, 32 - fy * FlowField.SIZE, 8, -8);
				ff.progressRegion.flip(false, true);
				ff.region = new TextureRegion(fieldViewTexture, fx * FlowField.SIZE, 32 - fy * FlowField.SIZE, 8, -8);
				ff.region.flip(false, true);
				eid++;
				fields.put(ff.type, ff);
				for (int x = 0; x < FlowField.SIZE; x++) {
					for (int y = 0; y < FlowField.SIZE; y++) {
						int flowPixel = flowData.getPixel(fx * FlowField.SIZE + x, 32 - 1 -(fy * FlowField.SIZE + y));
						Color.rgba8888ToColor(tmp, flowPixel);
						float vx = (tmp.r - .5f) * 2;
						float vy = (tmp.b - .5f) * 2;
						v2.set(vx, vy).nor();
						int index = x + y  * FlowField.SIZE;
						ff.values[index * 2] = v2.x;
						ff.values[index * 2 + 1] = v2.y;

						int lanePixel = laneData.getPixel(fx * FlowField.SIZE + x, 32 - 1 -(fy * FlowField.SIZE + y));
						Color.rgba8888ToColor(tmp, lanePixel);
						int lane = 0;
						if (tmp.r > .5f) {
							lane = 0;
						} else if (tmp.g > .5f) {
							lane = 1;
						} else {
							if (fy != 3)
								Gdx.app.log("", "No lane data for " + ff.type + " at " +x + " " + y) ;
						}
						ff.lanes[index] = lane;

						int progressPixel = progressData.getPixel(fx * FlowField.SIZE + x, 32 - 1 -(fy * FlowField.SIZE + y));
						Color.rgba8888ToColor(tmp, progressPixel);
						ff.lanesProgress[index] = tmp.r;
					}
				}
			}
		}
		flowData.dispose();
		laneData.dispose();
		progressData.dispose();

		setBelt(9, 0, FFType.CW_SW);
		setBelt(10, 0, FFType.W_E);
		setBelt(11, 0, FFType.CW_SE);
		setBelt(14, 0, FFType.CW_SW);
		setBelt(15, 0, FFType.W_E);
		setBelt(16, 0, FFType.CW_SE);
		setBelt(9, 1, FFType.E_W);
		setBelt(11, 1, FFType.CW_NW);
		setBelt(12, 1, FFType.W_E);
		setBelt(13, 1, FFType.W_E);
		setBelt(14, 1, FFType.CCW_NE);
		setBelt(16, 1, FFType.S_N);
		setBelt(9, 2, FFType.CW_NW);
		setBelt(10, 2, FFType.CW_SE);
		setBelt(15, 2, FFType.CW_SW);
		setBelt(16, 2, FFType.CW_NE);
		setBelt(10, 3, FFType.S_N);
		setBelt(15, 3, FFType.S_N);
		setBelt(10, 4, FFType.S_N);
		setBelt(15, 4, FFType.S_N);
		setBelt(9, 5, FFType.CCW_SW);
		setBelt(10, 5, FFType.CCW_NE);
		setBelt(15, 5, FFType.CW_NW);
		setBelt(16, 5, FFType.CCW_SE);
		setBelt(9, 6, FFType.S_N);
		setBelt(11, 6, FFType.CW_SW);
		setBelt(12, 6, FFType.N_S);
		setBelt(13, 6, FFType.N_S);
		setBelt(14, 6, FFType.CW_SE);
		setBelt(16, 6, FFType.S_N);
		setBelt(9, 7, FFType.CCW_NW);
		setBelt(10, 7, FFType.N_S);
		setBelt(11, 7, FFType.CCW_NE);
		setBelt(14, 7, FFType.CCW_NW);
		setBelt(15, 7, FFType.N_S);
		setBelt(16, 7, FFType.CCW_NE);
		setBelt(4, 9, FFType.CW_SW);
		setBelt(5, 9, FFType.CW_SE);
		setBelt(6, 9, FFType.CW_SW);
		setBelt(7, 9, FFType.CW_SE);
		setBelt(10, 9, FFType.CW_SW);
		setBelt(11, 9, FFType.CW_SE);
		setBelt(14, 9, FFType.CW_SW);
		setBelt(15, 9, FFType.CW_SE);
		setBelt(4, 10, FFType.S_N);
		setBelt(5, 10, FFType.CCW_NW);
		setBelt(6, 10, FFType.CCW_NE);
		setBelt(7, 10, FFType.E_W);
		setBelt(10, 10, FFType.S_N);
		setBelt(11, 10, FFType.CCW_NW);
		setBelt(12, 10, FFType.N_S);
		setBelt(13, 10, FFType.N_S);
		setBelt(14, 10, FFType.CCW_NE);
		setBelt(15, 10, FFType.E_W);
		setBelt(4, 11, FFType.S_N);
		setBelt(5, 11, FFType.CCW_SW);
		setBelt(6, 11, FFType.CCW_SE);
		setBelt(7, 11, FFType.E_W);
		setBelt(10, 11, FFType.S_N);
		setBelt(11, 11, FFType.CCW_SW);
		setBelt(12, 11, FFType.W_E);
		setBelt(13, 11, FFType.W_E);
		setBelt(14, 11, FFType.CCW_SE);
		setBelt(15, 11, FFType.E_W);
		setBelt(4, 12, FFType.CW_NW);
		setBelt(5, 12, FFType.CW_NE);
		setBelt(6, 12, FFType.CW_NW);
		setBelt(7, 12, FFType.CW_NE);
		setBelt(10, 12, FFType.CW_NW);
		setBelt(11, 12, FFType.CW_NE);
		setBelt(14, 12, FFType.CW_NW);
		setBelt(15, 12, FFType.CW_NE);
		setBelt(10, 14, FFType.W_E);
		setBelt(11, 14, FFType.S_N);
		setBelt(12, 14, FFType.E_W);
		setBelt(13, 14, FFType.N_S);
		setBelt(10, 15, FFType.CW_SW);
		setBelt(11, 15, FFType.CW_SE);
		setBelt(12, 15, FFType.CCW_SW);
		setBelt(13, 15, FFType.CCW_SE);
		setBelt(10, 16, FFType.CW_NW);
		setBelt(11, 16, FFType.CW_NE);
		setBelt(12, 16, FFType.CCW_NW);
		setBelt(13, 16, FFType.CCW_NE);
		setBelt(9, 0, FFType.CCW_SW);
		setBelt(10, 0, FFType.W_E);
		setBelt(11, 0, FFType.CCW_SE);
		setBelt(14, 0, FFType.CCW_SW);
		setBelt(15, 0, FFType.W_E);
		setBelt(16, 0, FFType.CCW_SE);
		setBelt(9, 1, FFType.E_W);
		setBelt(11, 1, FFType.CW_NW);
		setBelt(12, 1, FFType.W_E);
		setBelt(13, 1, FFType.W_E);
		setBelt(14, 1, FFType.CW_NE);
		setBelt(16, 1, FFType.S_N);
		setBelt(9, 2, FFType.CCW_NW);
		setBelt(10, 2, FFType.CW_SE);
		setBelt(15, 2, FFType.CW_SW);
		setBelt(16, 2, FFType.CCW_NE);
		setBelt(10, 3, FFType.E_W);
		setBelt(15, 3, FFType.S_N);
		setBelt(10, 4, FFType.E_W);
		setBelt(15, 4, FFType.S_N);
		setBelt(9, 5, FFType.CCW_SW);
		setBelt(10, 5, FFType.CW_NE);
		setBelt(15, 5, FFType.CW_NW);
		setBelt(16, 5, FFType.CCW_SE);
		setBelt(9, 6, FFType.E_W);
		setBelt(11, 6, FFType.CW_SW);
		setBelt(12, 6, FFType.N_S);
		setBelt(13, 6, FFType.N_S);
		setBelt(14, 6, FFType.CW_SE);
		setBelt(16, 6, FFType.S_N);
		setBelt(9, 7, FFType.CCW_NW);
		setBelt(10, 7, FFType.N_S);
		setBelt(11, 7, FFType.CCW_NE);
		setBelt(14, 7, FFType.CCW_NW);
		setBelt(15, 7, FFType.N_S);
		setBelt(16, 7, FFType.CCW_NE);
		setBelt(4, 9, FFType.CW_SW);
		setBelt(5, 9, FFType.CW_SE);
		setBelt(6, 9, FFType.CW_SW);
		setBelt(7, 9, FFType.CW_SE);
		setBelt(10, 9, FFType.CW_SW);
		setBelt(11, 9, FFType.CW_SE);
		setBelt(14, 9, FFType.CW_SW);
		setBelt(15, 9, FFType.CW_SE);
		setBelt(4, 10, FFType.S_N);
		setBelt(5, 10, FFType.CCW_NW);
		setBelt(6, 10, FFType.CCW_NE);
		setBelt(7, 10, FFType.E_W);
		setBelt(10, 10, FFType.S_N);
		setBelt(11, 10, FFType.CCW_NW);
		setBelt(12, 10, FFType.N_S);
		setBelt(13, 10, FFType.N_S);
		setBelt(14, 10, FFType.CCW_NE);
		setBelt(15, 10, FFType.E_W);
		setBelt(4, 11, FFType.S_N);
		setBelt(5, 11, FFType.CCW_SW);
		setBelt(6, 11, FFType.CCW_SE);
		setBelt(7, 11, FFType.E_W);
		setBelt(10, 11, FFType.S_N);
		setBelt(11, 11, FFType.CCW_SW);
		setBelt(12, 11, FFType.W_E);
		setBelt(13, 11, FFType.W_E);
		setBelt(14, 11, FFType.CCW_SE);
		setBelt(15, 11, FFType.E_W);
		setBelt(4, 12, FFType.CW_NW);
		setBelt(5, 12, FFType.CW_NE);
		setBelt(6, 12, FFType.CW_NW);
		setBelt(7, 12, FFType.CW_NE);
		setBelt(10, 12, FFType.CW_NW);
		setBelt(11, 12, FFType.CW_NE);
		setBelt(14, 12, FFType.CW_NW);
		setBelt(15, 12, FFType.CW_NE);
		setBelt(10, 14, FFType.W_E);
		setBelt(11, 14, FFType.S_N);
		setBelt(12, 14, FFType.E_W);
		setBelt(13, 14, FFType.N_S);
		setBelt(10, 15, FFType.CW_SW);
		setBelt(11, 15, FFType.CW_SE);
		setBelt(12, 15, FFType.CCW_SW);
		setBelt(13, 15, FFType.CCW_SE);
		setBelt(10, 16, FFType.CW_NW);
		setBelt(11, 16, FFType.CW_NE);
		setBelt(12, 16, FFType.CCW_NW);
		setBelt(13, 16, FFType.CCW_NE);

	}

	static class FlowField {
		public static final int SIZE = 8;
		public TextureRegion region;
		public TextureRegion flowRegion;
		public TextureRegion laneRegion;
		public TextureRegion progressRegion;
		public int[] lanes = new int[SIZE * SIZE];
		public float[] lanesProgress = new float[SIZE * SIZE];

		public enum FFType {
			// N - North
			// E - East
			// S - South
			// W - West
			// CW - ClockWise
			// CCW - CounterClockWise
			_E1(90), _E2(91), _E3(92), _E4(93),
			CW_NW(8), CW_NE(9), CCW_NW(10), CCW_NE(11),
			CW_SW(4), CW_SE(5), CCW_SW(6), CCW_SE(7),
			W_E(0), S_N(1), N_S(2), E_W(3),;

			public final int sort;
			FFType (int sort) {
				this.sort = sort;
			}

		}
		public FFType type;
		public float[] values = new float[SIZE * SIZE * 2];

		public FlowField (FFType type) {
			this.type = type;
		}

		public Vector2 get (int x, int y, Vector2 out) {
			int index = x + y  * FlowField.SIZE;
			out.x = values[index * 2];
			out.y = values[index * 2 + 1];
			return out;
		}

		public int getLane(int x, int y) {
			int index = x + y  * FlowField.SIZE;
			return lanes[index];
		}

		public float getProgress (int x, int y) {
			int index = x + y  * FlowField.SIZE;
			return lanesProgress[index];
		}
	}

	void setBelt(int x, int y, FFType type) {
		int index = x + y * WIDTH;
		Belt belt = belts[index];
		if (belt == null) {
			belt = belts[index] = new Belt(this);
			belt.init(x, y);
		}
		belt.setType(type);
	}

	void removeBelt(int x, int y) {
		int index = x + y * WIDTH;
		belts[index] = null;
	}

	@Override public void render (float delta) {
		super.render(delta);
		// handle input
		int mx = (int)cs.x;
		int my = (int)cs.y;
		if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {
			if (Gdx.input.isKeyPressed(Input.Keys.T)) {
				removeBelt(mx, my);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
				addItem(cs.x, cs.y);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
				setBelt(mx, my, FFType.CW_NW);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
				setBelt(mx, my, FFType.CW_NE);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
				setBelt(mx, my, FFType.CW_SW);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
				setBelt(mx, my, FFType.CW_SE);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
				setBelt(mx, my, FFType.CCW_NW);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
				setBelt(mx, my, FFType.CCW_NE);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
				setBelt(mx, my, FFType.CCW_SW);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
				setBelt(mx, my, FFType.CCW_SE);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
				setBelt(mx, my, FFType.W_E);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
				setBelt(mx, my, FFType.S_N);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
				setBelt(mx, my, FFType.E_W);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
				setBelt(mx, my, FFType.N_S);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
				for (Belt belt : belts) {
					if (belt == null) continue;
					System.out.println("setBelt(" +belt.x + ", " + belt.y + ", FFType." + belt.type +");");
				}

			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
			drawText = !drawText;
		}
		// simulate
		for (Item item : items) {
			item.update(delta);
		}

		for (Belt belt : belts) {
			if (belt == null) continue;
//			belt.update(delta, belts, items);
		}

		// draw stuff
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (Belt belt : belts) {
			if (belt == null) continue;
			belt.draw(batch);
		}
		batch.end();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		enableBlending();

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
		if (testDrawFields) {
			renderer.begin(ShapeRenderer.ShapeType.Filled);
			int count = 0;
			int sx = 0;
			int sy = 0;
			for (FFType fft : types) {
				float[] values = fields.get(fft).values;
				for (int y = 0; y < FlowField.SIZE; y++) {
					for (int x = 0; x < FlowField.SIZE; x++) {
						int index = x + y * FlowField.SIZE;
						float vx = values[index * 2]/4f;
						float vy = values[index * 2 + 1]/4f;
						renderer.setColor(1, 0, 1, .5f);
	//					renderer.circle(sx + x/2f + .25f, sy + y/2f + .25f, .25f, 16);
						renderer.setColor(Color.CYAN);
						renderer.rectLine(
							sx + x/2f + .25f,
							sy + y/2f + .25f,
							sx + x/2f + .25f + vx,
							sy + y/2f + .25f + vy, .05f
						);
						renderer.setColor(Color.MAGENTA);
						renderer.circle(sx + x/2f + .25f,
							sy + y/2f + .25f, .075f, 12);
	//					renderer.line(sx + x/2f + .5f, sy + y/2f + .5f, sx + x/2f + .5f + .25f, sy + y/2f + .5f + .25f);
					}
				}
				count++;
				sx += FlowField.SIZE/2 + 1;
				if (count % 4 == 0 && count > 0) {
					sy += FlowField.SIZE/2 + 1;
					sx -= FlowField.SIZE/2 * 4 + 4;
				}
			}
			renderer.end();
			if (testDrawFieldsLables) {
				batch.setProjectionMatrix(gameCamera.combined);
				batch.begin();
				count = 0;
				sx = 1;
				sy = 1;
				for (FFType fft : types) {
					layout.setText(font, fft.name());
					font.draw(batch, layout, sx, sy);
					count++;
					sx += FlowField.SIZE/2 + 1;
					if (count % 4 == 0 && count > 0) {
						sy += FlowField.SIZE/2 + 1;
						sx -= FlowField.SIZE/2 * 4 + 4;
					}
				}
				batch.end();
			}
		}

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
		Item item = new Item(this, x, y);
		items.add(item);
	}

	static class Belt {
		public static final int SIZE = 8;
		public FFType type;
		public int index;
		public int x;
		public int y;
		public float speed = 1;
		public FlowField ff;
		private Transport2Test owner;
		Array<Array<Item>> items = new Array<>(2);

		public Belt (Transport2Test owner) {
			this.owner = owner;
			setType(FFType.S_N);
			items.add(new Array<Item>());
			items.add(new Array<Item>());
		}

		public Belt setType (FFType type) {
			this.type = type;
			ff = owner.fields.get(type);
			return this;
		}

		public void draw (SpriteBatch batch) {
			batch.draw(ff.region, x, y, 1, 1);
//			batch.draw(ff.laneRegion, x, y, 1, 1);
//			batch.draw(ff.progressRegion, x, y, 1, 1);
			int count = items.get(0).size + items.get(1).size;
			if (count > 0) {
				GlyphLayout layout = owner.layout;
				owner.font.setColor(Color.BLACK);
				layout.setText(owner.font, Integer.toString(count));
				owner.font.draw(batch, layout, x + .5f - layout.width/2, y + .5f + layout.height/2);
			}
		}

		public void drawDebug (SpriteBatch batch) {
			batch.draw(ff.flowRegion, x, y, 1, 1);
		}

		public void init (int x, int y) {
			this.x = x;
			this.y = y;
			index = x + y * WIDTH;
		}

		public Vector2 getFlow (float x, float y, Vector2 out) {
			int nx = (int)((x - this.x) * SIZE);
			int ny = (int)((y - this.y) * SIZE);
			ff.get(nx, ny, out);
			return out;
		}

		public int getLane (float x, float y) {
			int nx = (int)((x - this.x) * SIZE);
			int ny = (int)((y - this.y) * SIZE);
			return ff.getLane(nx, ny);
		}

		public void add (Item item, int lane) {
			if (lane < 0 || lane > 1) return;
			Array<Item> items = this.items.get(lane);
			items.add(item);
			items.sort(new Comparator<Item>() {
				@Override public int compare (Item o1, Item o2) {
					return Float.compare(o2.progress, o1.progress);
				}
			});
		}

		public void remove (Item item, int lane) {
			if (lane < 0 || lane > 1) return;
			items.get(lane).removeValue(item, true);
		}

		public float getProgress (float x, float y) {
			int nx = (int)((x - this.x) * SIZE);
			int ny = (int)((y - this.y) * SIZE);
			return ff.getProgress(nx, ny);
		}
	}

	static class Item {
		public int type;
		private Transport2Test owner;
		public float x;
		public float y;
		public int lane = -1;
		public float progress = 0;
		public Circle bounds = new Circle();
		public static Circle tmpBounds = new Circle();
		public Belt onBelt = null;

		public Item (Transport2Test owner, float x, float y) {
			this.owner = owner;
			this.x = x;
			this.y = y;
			bounds.set(x, y, .15f);
		}

		public void draw (ShapeRenderer renderer) {
			if (lane == 0) {
				renderer.setColor(.5f +progress * .5f, 0, 0, 1);
			} else if (lane == 1) {
				renderer.setColor(0, .5f +progress * .5f, 0, 1);
			} else {
				renderer.setColor(Color.CYAN);
			}
			renderer.circle(bounds.x, bounds.y, bounds.radius, 12);
		}

		static Vector2 tmp = new Vector2();
		public void update (float delta) {
			Belt belt = owner.getBeltAt(x, y);
			if (belt == null) {
				if (onBelt != null) {
					onBelt.remove(this, lane);
					onBelt = null;
				}
				lane = -1;
				return;
			}
			lane = belt.getLane(x, y);
			progress = belt.getProgress(x, y);
			if (belt != onBelt) {
				if (onBelt != null) {
					onBelt.remove(this, lane);
				}
				belt.add(this, lane);
				onBelt = belt;
			}
			belt.getFlow(x, y, tmp);
			tmp.scl(delta);

			Array<Item> items = belt.items.get(lane);
			int thisId = items.indexOf(this, true);
			float nx = x;
			float ny = y;
			if (thisId > 0) {
				Item next = items.get(thisId - 1);
				tmpBounds.set(bounds);
				tmpBounds.x += tmp.x;
				tmpBounds.y += tmp.y;
				if (!next.bounds.overlaps(tmpBounds)) {
					nx += tmp.x;
					ny += tmp.y;
				}
			} else {
				// TODO need to check next belt
				belt.getFlow(x, y, tmp);
				nx += tmp.x * delta;
				ny += tmp.y * delta;
				belt = owner.getBeltAt(x + tmp.x * bounds.radius, y + tmp.y * bounds.radius);
				if (belt != null && belt != onBelt) {
					items = belt.items.get(lane);
					if (items.size > 0) {
						Item next = items.get(items.size - 1);
						tmpBounds.set(bounds);
						tmpBounds.x += tmp.x * delta;
						tmpBounds.y += tmp.y * delta;
//						tmpBounds.radius += bounds.radius/2;
						if (next.bounds.overlaps(tmpBounds)) {
							nx -= tmp.x * delta;
							ny -= tmp.y * delta;
						}
					}
				}
			}
			belt = owner.getBeltAt(nx, ny);
			if (belt != null) {
				x = nx;
				y = ny;
				bounds.setPosition(x, y);
			}
		}
	}

	private Belt getBeltAt (float x, float y) {
		int gx = (int)x;
		int gy = (int)y;
		return belts[gx + gy * WIDTH];
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

	@Override public void dispose () {
		super.dispose();
		fieldTexture.dispose();
		fieldViewTexture.dispose();
		fieldLanesTexture.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, Transport2Test.class);
	}
}
