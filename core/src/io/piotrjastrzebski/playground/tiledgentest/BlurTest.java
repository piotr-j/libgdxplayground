package io.piotrjastrzebski.playground.tiledgentest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class BlurTest extends BaseScreen {
	private boolean blurEnabled = true;
	private int blurDst = 10;
	MapWidget map;
	MapData data;
	Interpolation interp;
	float gain;
	public BlurTest (PlaygroundGame game) {
		super(game);
		map = new MapWidget(
			new TextureRegion(new Texture(Gdx.files.internal("white.png"))));
		map.setSize(2.75f);

		data = new MapData();

		interp = Interpolation.fade;

		// reasonable values for world map
		data.biomeEnabled = true;
		data.water = 0.4f;
		data.waterEnabled = true;
		data.largestFeature = 256;
		data.persistence = 0.55f;
		data.seed = MathUtils.random(Long.MAX_VALUE);
		// same aspect as default 720p screen
		data.width = 400;
		data.height = 225;

		data.tiles = new MapData.Tile[data.width][data.height];
		for (int mx = 0; mx < data.width; mx++) {
			for (int my = 0; my < data.height; my++) {
				MapData.Tile tile = new MapData.Tile();
				tile.x = mx;
				tile.y = my;
				data.tiles[mx][my] = tile;
			}
		}

		Table container = new Table();
		// put it in container so it is always centered in the pane
		container.add(map).fill();
		// map widget inside scroll pane for movement
		MyScrollPane pane = new MyScrollPane(container, skin);
		pane.setOverscroll(false, false);
		pane.setKnobsEnabled(false, false);
		pane.setMouseScrollEnabled(false);

		pane.setCancelTouchFocus(true);

		Table settings = new Table();
		TextButton randomSeed = new TextButton("ReSeed", skin);
		randomSeed.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				data.seed = MathUtils.random(Long.MAX_VALUE);
				refresh();
			}
		});
		settings.add(randomSeed);
		settings.row();
		final Label lfLabel = new Label("Max Feature " + data.largestFeature, skin);
		settings.add(lfLabel);
		settings.row();
		final Slider largestFeature = new Slider(16, 513, 1, false, skin);
		largestFeature.setValue(data.largestFeature);
		largestFeature.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				data.largestFeature = (int)largestFeature.getValue();
				lfLabel.setText(String.format("Max Feature %d", data.largestFeature));
				refresh();
			}
		});
		settings.add(largestFeature);
		settings.row();

		final Label pLabel = new Label("Persistence " + data.persistence, skin);
		settings.add(pLabel);
		settings.row();
		final Slider persistence = new Slider(0.1f, 1f, 0.01f, false, skin);
		persistence.setValue(data.persistence);
		persistence.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				data.persistence = persistence.getValue();
				pLabel.setText(String.format("Persistence %.2f", data.persistence));
				refresh();
			}
		});

		settings.add(persistence);
		settings.row();
		final CheckBox cbRain = new CheckBox("BlurDst " + blurDst, skin);
		cbRain.setChecked(blurEnabled);
		cbRain.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				blurEnabled = cbRain.isChecked();
				refresh();
			}
		});
		settings.add(cbRain);
		settings.row();

		final Slider rainS = new Slider(1f, 200.0f, 1f, false, skin);
		rainS.setValue(blurDst);
		rainS.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				blurDst = (int)rainS.getValue();
				cbRain.setText(String.format("RainDst %d", blurDst));
				refresh();
			}
		});
		settings.add(rainS);
		settings.row();

		root.add(settings).expandY().fillY();
		root.add(pane).expand().fill();
		root.getStage().setScrollFocus(map);

		refresh();
	}

	private class Interp {
		String text;
		Interpolation interpolation;

		public Interp (Interpolation interpolation, String text) {
			this.interpolation = interpolation;
			this.text = text;
		}

		@Override public String toString () {
			return text;
		}
	}

	public void refresh() {
		OpenNoise noise = new OpenNoise(data.largestFeature, data.persistence, data.seed);
		double xStart = 0;
		double XEnd = data.width;
		double yStart = 0;
		double yEnd = data.height * 2;

		int xResolution = data.width;
		int yResolution = data.height;
		float max = 1.0f;
		for (int mx = 0; mx < xResolution; mx++) {
			for (int my = 0; my < yResolution; my++) {
				int nx = (int)(xStart + mx * ((XEnd - xStart) / xResolution));
				int ny = (int)(yStart + my * ((yEnd - yStart) / yResolution));
				// normalize
				double dVal = 0.5d + noise.getNoise(nx, ny);
				MapData.Tile tile = data.tiles[mx][my];
				tile.value = dVal;
				tile.blur = 0;

				float val = (float)tile.value;
				if (val > max) max = val;
				if (val > 0.5f) {
					val = 1;
				} else {
					val = 0;
				}
				tile.rainfall = val;
				tile.setColor(val, val, val);
			}
		}

		for (int mx = 0; mx < xResolution; mx++) {
			for (int my = 0; my < yResolution; my++) {
				blur(data.tiles[mx][my], false);
			}
		}
		for (int mx = 0; mx < xResolution; mx++) {
			for (int my = 0; my < yResolution; my++) {
				MapData.Tile tile = data.tiles[mx][my];
				tile.rainfall += tile.blur;
				tile.blur = 0;
			}
		}

		for (int mx = 0; mx < xResolution; mx++) {
			for (int my = 0; my < yResolution; my++) {
				blur(data.tiles[mx][my], true);
			}
		}
		for (int mx = 0; mx < xResolution; mx++) {
			for (int my = 0; my < yResolution; my++) {
				MapData.Tile tile = data.tiles[mx][my];
				tile.rainfall += tile.blur;
				tile.blur = 0;
			}
		}
		// find max value so we can normalize
		float maxRF = 0;
		for (int mx = 0; mx < xResolution; mx++) {
			for (int my = 0; my < yResolution; my++) {
				MapData.Tile tile = data.tiles[mx][my];
				if (tile.rainfall > maxRF) maxRF = tile.rainfall;
			}
		}

		for (int mx = 0; mx < xResolution; mx++) {
			for (int my = 0; my < yResolution; my++) {
				MapData.Tile tile = data.tiles[mx][my];
				// normalize
				tile.rainfall /= maxRF;
				// remove some rainfall based on elevation
//				tile.rainfall -= Interpolation.exp10In.apply(tile.elevation);// * 0.5f;
				if (blurEnabled && tile.rainfall > 0.05f) {
					float c = tile.rainfall;
					tile.color.add(new Color(c, c, c, 1));
				}
			}
		}

		map.setData(data);
	}

	private void blur (MapData.Tile tile, boolean horizontal) {
		// do some magic to blur stuff a bit
		if (horizontal) {
			for (int x = -blurDst; x <= blurDst; x++) {
				if (x == 0) continue;
				int mx = tile.x + x;
				if (mx < 0 || mx >= data.width) continue;
				MapData.Tile other = data.tiles[mx][tile.y];
				if (other.blur >= 1) continue;
				int dx = x;
				if (dx < 0) dx = -dx;
				float rf = tile.rainfall * (1-(dx/(1.1f * blurDst)))/10;
				other.blur += rf;
			}
		} else {
			for (int y = -blurDst; y <= blurDst; y++) {
				if (y == 0) continue;
				int my = tile.y + y;
				if (my < 0 || my >= data.height) continue;
				MapData.Tile other = data.tiles[tile.x][my];
				if (other.blur >= 1) continue;
				int dy = y;
				if (dy < 0) dy = -dy;
				float rf = tile.rainfall * (1-(dy/(1.1f * blurDst)))/10;
				other.blur += rf;
			}
		}
	}

	@Override public void render (float delta) {
		super.render(delta);

	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);

	}

	@Override public void dispose () {
		super.dispose();
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.R) {
			refresh();
		}
		return super.keyDown(keycode);
	}
}
