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
import io.piotrjastrzebski.playground.GameReset;


/**
 * Created by EvilEntity on 07/06/2015.
 */
public class BlurTest extends BaseScreen {
	private boolean blurEnabled = true;
	private float blurDst = 5;
	MapWidget map;
	MapData data;
	MapData.Tile[][] tmpTiles;
	Interpolation interp;
	float gain;
	public BlurTest (GameReset game) {
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
		tmpTiles = new MapData.Tile[data.width][data.height];
		for (int mx = 0; mx < data.width; mx++) {
			for (int my = 0; my < data.height; my++) {
				MapData.Tile tile = new MapData.Tile();
				tile.x = mx;
				tile.y = my;
				data.tiles[mx][my] = tile;
				tile = new MapData.Tile();
				tile.x = mx;
				tile.y = my;
				tmpTiles[mx][my] = tile;
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

		final Slider rainS = new Slider(0f, 15.0f, .5f, false, skin);
		rainS.setValue(blurDst);
		rainS.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				blurDst = rainS.getValue();
				cbRain.setText(String.format("RainDst %.2f", blurDst));
				refresh();
			}
		});
		settings.add(rainS).width(300);
		settings.row();
		root.add(settings).expandY().fillY();
		root.add(pane).expand().fill();
		root.getStage().setScrollFocus(map);

		refresh();
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

		blur(data.tiles, tmpTiles, blurDst);

		for (int mx = 0; mx < xResolution; mx++) {
			for (int my = 0; my < yResolution; my++) {
				MapData.Tile tile = data.tiles[mx][my];
				// remove some rainfall based on elevation
//				tile.rainfall -= Interpolation.exp10In.apply(tile.elevation);// * 0.5f;
				if (blurEnabled) {
					float c = tile.rainfall;
//					tile.addColor(c, c, c);
					Color tc = tile.color;
					tc.r = (tc.r + c)/2;
					tc.g = (tc.g + c)/2;
					tc.b = (tc.b + c)/2;
//					tile.mulColor(c, c, c);
				}
			}
		}

		map.setData(data);
	}

	private void blur(MapData.Tile[][] src, MapData.Tile[][] dst, float str) {
		double[] kernel = create1DGK(str);
		int kernelRadius = kernel.length/2;
		int kernelSize = kernel.length;

		// horizontal
		int width = src.length;
		int height = src[0].length;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double rT = 0.0, kT = 0.0;

				for (int u = 0; u < kernelSize; u++) {
					int cX = x + u - kernelRadius;
					if (cX < 0 || cX > width - 1) {
						continue;
					}

					rT += src[cX][y].rainfall * kernel[u];
					kT += kernel[u];
				}

				dst[x][y].rainfall = (float)(rT / kT);
			}
		}
		// vertical
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double rT = 0.0, kT = 0.0;

				for (int v = 0; v < kernelSize; v++) {
					int cY = y + v - kernelRadius;
					if (cY < 0 || cY > height - 1) {
						continue;
					}

					rT += dst[x][cY].rainfall * kernel[v];
					kT += kernel[v];
				}

				src[x][y].rainfall = (float)(rT / kT);
			}
		}
	}

	private double[] create1DGK(double sd) {
		int radius = (int)Math.ceil(sd * 2.5);
		double[] kernel = new double[radius * 2 + 1];

		int kPos = 0;
		double norm = 1./Math.sqrt(2 * Math.PI * sd * sd);
		for (int u = -radius; u <= radius; u++) {
			kernel[kPos++] = norm * Math.exp(-(u * u) / (2 * sd * sd));
		}

		return kernel;
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.R) {
			refresh();
		}
		return super.keyDown(keycode);
	}
}
