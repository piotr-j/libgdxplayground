package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
@SuppressWarnings("Duplicates")
public class CellularAutomataWater2Test extends BaseScreen {
	BitmapFont font;
	GlyphLayout glyphs;

	public final static int WIDTH = (int)VP_WIDTH;
	public final static int HEIGHT = (int)VP_HEIGHT;
	public final static int EMPTY = 0;
	public final static int BLOCK = 1;
	public final static int WATER = 2;
	public final static float MIN_VALUE = 0.001f;
	public final static float MAX_VALUE = 1;
	public final static float MIN_FLOW = 0.01f;
	public final static float MAX_SPEED = 1;
	public final static float MAX_COMPRESS = 0.02f;
	public final static float MIN_DRAW_VALUE = 0.01f;
	public final static float MAX_DRAW_VALUE = 1.1f;

	public static int[] types;
	public static float[] values;
	public static float[] nextValues;
	private boolean drawText;

	public CellularAutomataWater2Test (GameReset game) {
		super(game);
		BitmapFont visFont = VisUI.getSkin().getFont("default-font");
		font = new BitmapFont(new BitmapFont.BitmapFontData(visFont.getData().fontFile, false), visFont.getRegions(), false);
		font.setUseIntegerPositions(false);
		font.getData().setScale(INV_SCALE);
		glyphs = new GlyphLayout();
		clear.set(Color.GRAY);
		types = new int[WIDTH * HEIGHT];
		values = new float[WIDTH * HEIGHT];
		nextValues = new float[WIDTH * HEIGHT];
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				int index = x + y * WIDTH;
//				int type = MathUtils.random(EMPTY, WATER);
//				types[index] = type;
//				if (type == WATER) {
//					values[index] = 1;
//					nextValues[index] = 1;
//				}
			}
		}
	}
	Color water = new Color(Color.WHITE);
	float tick;
	float tickDelta = 1/60f;
	@Override public void render (float delta) {
		super.render(delta);
		// handle input
		int mx = (int)cs.x;
		int my = (int)cs.y;
		if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {
			int index = mx + my * WIDTH;
			if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
				types[index] = BLOCK;
				values[index] = 0;
				nextValues[index] = 0;
			} else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
				types[index] = WATER;
				values[index] += MAX_VALUE;
				nextValues[index] += MAX_VALUE;
			} else if (Gdx.input.isKeyPressed(Input.Keys.E)) {
				types[index] = EMPTY;
				values[index] = 0;
				nextValues[index] = 0;
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
			drawText = !drawText;
		}
		// simulate
		tick += delta;
		if (tick >= tickDelta) {
			tick -= tickDelta;
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					int index = x + y * WIDTH;
					int type = types[index];
					if (type == BLOCK) continue;

					float value = values[index];
					if (value <= 0) continue;

					// below
					if (y > 0) {
						int belowId = x + (y-1) * WIDTH;
						if (types[belowId] != BLOCK) {
							float flow = (values[index] - values[belowId])/4f;
							if (flow > MIN_FLOW) {
								flow *= .5f;
							}
							flow = MathUtils.clamp(flow, 0, value);

							nextValues[index] -= flow;
							nextValues[belowId] += flow;
							value -= flow;
						}
					}
					if (value <= 0) continue;

					// left
					if (x > 0) {
						int leftId = (x - 1) + y * WIDTH;
						if (types[leftId] != BLOCK) {
							float flow = (values[index] - values[leftId])/4f;
							if (flow > MIN_FLOW) {
								flow *= .5f;
							}
							flow = MathUtils.clamp(flow, 0, value);

							nextValues[index] -= flow;
							nextValues[leftId] += flow;
							value -= flow;
						}
					}
					if (value <= 0) continue;

					// right
					if (x < WIDTH -1) {
						int rightId = (x + 1) + y * WIDTH;
						if (types[rightId] != BLOCK) {
							float flow = (values[index] - values[rightId])/4f;
							if (flow > MIN_FLOW) {
								flow *= .5f;
							}
							flow = MathUtils.clamp(flow, 0, value);

							nextValues[index] -= flow;
							nextValues[rightId] += flow;
							value -= flow;
						}
					}
					if (value <= 0) continue;

					// up
					if (y < HEIGHT -1) {
						int upId = x + (y + 1) * WIDTH;
						float flow = (values[index] - values[upId])/4f;
						if (flow > MIN_FLOW) {
							flow *= .5f;
						}
						flow = MathUtils.clamp(flow, 0, value);

						nextValues[index] -= flow;
						nextValues[upId] += flow;
						value -= flow;
					}
				}
			}

			//Copy the new mass values to the mass array
			for (int i = 0; i < WIDTH * HEIGHT; i++) {
				values[i] = nextValues[i];
			}

			// remove water from the edges
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					if (x == 0 || x == WIDTH -1 || y == 0 || y == HEIGHT -1) {
						int index = x + y * WIDTH;
						if(types[index] == BLOCK) continue;
						values[index] = Math.max(values[index] - MAX_VALUE * tickDelta, 0);
						nextValues[index] = Math.max(nextValues[index] - MAX_VALUE * tickDelta, 0);
					}
				}
			}

			// update flags
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					int index = x + y * WIDTH;
					if(types[index] == BLOCK) continue;
					if (values[index] > MIN_VALUE){
						types[index] = WATER;
					} else {
						types[index] = EMPTY;
					}
				}
			}

			// clear water from edges?
		}
		// draw stuff
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				int index = x + y * WIDTH;
				switch (types[index]) {
				case BLOCK: {
					renderer.setColor(Color.BROWN);
					renderer.rect(x, y, 1, 1);
				} break;
				case WATER: {
					renderer.setColor(Color.NAVY);
					float value = values[index];
					if (value > MIN_DRAW_VALUE) {
						renderer.setColor(getWaterColor(value, water));
						renderer.rect(x, y, 1, 1);
					}
				} break;
				}

			}
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
					switch (types[index]) {
					case WATER: {
						float value = values[index];
						if (value > MIN_DRAW_VALUE) {
							font.setColor(Color.BLACK);
							glyphs.setText(font, String.format("%.2f", values[index]));
							font.draw(batch, glyphs, x + .5f - glyphs.width / 2, y + .5f + glyphs.height / 2);
						}
					}
					break;
					}

				}
			}
			batch.end();
		}
	}

	private Color getWaterColor(float value, Color out){
		value = MathUtils.clamp(value, MIN_DRAW_VALUE, MAX_DRAW_VALUE);

		if (value < 1){
			out.r = map(value, 0.01f, 1, .94f, .2f);
			out.r = MathUtils.clamp(out.r, .2f, .94f);
			out.g = out.r;
			out.b = map(value, 0.01f, 1, 1f, .78f);
		} else {
			out.r = .2f;
			out.g = .2f;
			out.b = map(value, 1, 1.1f, .75f, .55f);
		}
		out.b = MathUtils.clamp(out.b, .55f, 1);
		return out;
	}

	private float map (float value, float start1, float end1, float start2, float end2) {
		return start2 + ((end2 - start2) / (end1 - start1)) * (value - start1);
	}

	@Override public boolean keyDown (int keycode) {
		switch(keycode) {
		case Input.Keys.SPACE: {

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
		PlaygroundGame.start(args, CellularAutomataWater2Test.class);
	}
}
