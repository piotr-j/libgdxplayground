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
import io.piotrjastrzebski.playground.tiledgentest.generators.MountainGen;
import io.piotrjastrzebski.playground.tiledgentest.generators.RainGen;
import io.piotrjastrzebski.playground.tiledgentest.generators.TempGen;
import io.piotrjastrzebski.playground.tiledgentest.generators.TerrainGen;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class CompositeGenTest extends BaseScreen {
	MapWidget map;
	MapData data;
	Interpolation interp;

	boolean rainFall = false;
	private boolean temp = false;
	private boolean mountains = false;

	public CompositeGenTest (GameReset game) {
		super(game);
		map = new MapWidget(
			new TextureRegion(new Texture(Gdx.files.internal("white.png"))));
		map.setSize(2.75f);

		data = new MapData();

		interp = Interpolation.fade;

		// reasonable values for world map
		data.biomeEnabled = true;
		data.water = 0.4f;
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

		final CheckBox showRainFall = new CheckBox("Rainfall", skin);
		showRainFall.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				rainFall = showRainFall.isChecked();
				refresh();
			}
		});
		settings.add(showRainFall);
		settings.row();

		final CheckBox showTemp = new CheckBox("Temperature", skin);
		showTemp.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				temp = showTemp.isChecked();
				refresh();
			}
		});
		settings.add(showTemp);
		settings.row();

		final CheckBox showMount = new CheckBox("Mountains", skin);
		showMount.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				mountains = showMount.isChecked();
				refresh();
			}
		});
		settings.add(showMount);
		settings.row();

		root.add(settings).expandY().fillY();
		root.add(pane).expand().fill();
		root.getStage().setScrollFocus(map);

		refresh();
	}

	public void refresh() {
		float[][] terrainData = TerrainGen.generate(data.seed, data.width, data.height);
		float[][] mountainData = MountainGen.generate(data.seed, terrainData);
		float[][] rainfallData = RainGen.generate(terrainData, 97, data.water);
		float[][] temperatureData = TempGen.generate(data.seed, data.width, data.height);

		float max = 1.0f;
		for (int mx = 0; mx < data.width; mx++) {
			for (int my = 0; my < data.height; my++) {
				MapData.Tile tile = data.tiles[mx][my];
				tile.temp = temperatureData[mx][my];
				tile.rainfall = rainfallData[mx][my];

				float val = terrainData[mx][my];
				tile.value = val;

				if (val > max) max = val;
				tile.setColor(val, val, val);
				if (temp) {
					val = temperatureData[mx][my];
					tile.setColor(val, val, val);
				} else {
					if (val < data.water) {
						if (val < data.water * 0.7f) {
							tile.setColor(0.2f, 0.5f, 0.9f);
						} else {
							tile.setColor(0.4f, 0.7f, 1);
						}
						tile.mountains = 1;
					} else {
						// normalize val so 0 is at water level
						val = (val - data.water) / (max - data.water);

						tile.elevation = val;
						tile.mountains = mountainData[mx][my];

						tile.setColor(val, val, val);
						if (data.biomeEnabled) {
							// set color based on above the see level
							// beach, plain, forest, mountains etc
							tile.setColor(val, val, val);
							if (val < 0.1) {
								tile.setColor(Color.YELLOW);
							} else if (val < 0.3) {
								tile.setColor(Color.GREEN);
							} else if (val < 0.55) {
								tile.setColor(.1f, 0.8f, .2f);
							} else if (val < 0.8) {
								tile.setColor(Color.GRAY);
							} else {
								tile.setColor(Color.WHITE);
							}
						} else {
							tile.setColor(val, val, val);
						}
					}
					tile.mulColor(val, val, val);
					if (rainFall) {
						float val2 = rainfallData[mx][my];
						tile.mulColor(val2, val2, val2);
					}
					if (mountains) {
						tile.setColor(tile.mountains, tile.mountains, tile.mountains);
					}
				}

			}
		}
		map.setData(data);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
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
