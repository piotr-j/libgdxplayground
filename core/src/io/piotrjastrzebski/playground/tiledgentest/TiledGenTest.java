package io.piotrjastrzebski.playground.tiledgentest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.piotrjastrzebski.playground.BaseScreen;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class TiledGenTest extends BaseScreen {
	MapWidget map;
	MapData data;
	public TiledGenTest () {
		super();
		map = new MapWidget(
			new TextureRegion(new Texture(Gdx.files.internal("white.png"))));
		map.setSize(2.75f);

		data = new MapData();

		// reasonable values for world map
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
		final Slider persistence = new Slider(0.1f, 1f, 0.05f, false, skin);
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

		final CheckBox waterEnabled = new CheckBox("Water " + data.water, skin);
		waterEnabled.setChecked(data.waterEnabled);
		waterEnabled.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				data.waterEnabled = waterEnabled.isChecked();
				refresh();
			}
		});
		settings.add(waterEnabled);
		settings.row();

		final Slider water = new Slider(0.01f, 1.0f, 0.01f, false, skin);
		water.setValue(data.water);
		water.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				data.water = water.getValue();
				waterEnabled.setText(String.format("Water %.2f", data.water));
				refresh();
			}
		});
		settings.add(water);
		settings.row();

		root.add(settings).expandY().fillY();
		root.add(pane).expand().fill();
		root.getStage().setScrollFocus(map);

		refresh();
	}

	public void refresh() {
		OpenNoise noise = new OpenNoise(data.largestFeature, data.persistence, data.seed);
		double xStart = 0;
		double XEnd = 500;
		double yStart = 0;
		double yEnd = 500;

		int xResolution = data.width;
		int yResolution = data.height;
		for (int mx = 0; mx < xResolution; mx++) {
			for (int my = 0; my < yResolution; my++) {
				int nx = (int)(xStart + mx * ((XEnd - xStart) / xResolution));
				int ny = (int)(yStart + my * ((yEnd - yStart) / yResolution));
				// normalize
				double dVal = 0.5d + noise.getNoise(nx, ny);
				MapData.Tile tile = data.tiles[mx][my];
				tile.value = dVal;

				float val = (float)tile.value;
				val = MathUtils.clamp(val, 0, 1);
				// shades of gray
				tile.setColor(val, val, val);
				if (data.waterEnabled) {
					if (val < data.water) {
						if (val < data.water * 0.8f) {
							tile.setColor(0.2f, 0.5f, 0.9f);
						} else {
							tile.setColor(0.4f, 0.7f, 1);
						}
					} else {
						// normalize val so 0 is at water level
						val = (val - data.water) / (1 - data.water);
						if (data.biomeEnabled) {
							// set color based on above the see level
							// beach, plain, forest, mountains etc

						} else {
							tile.setColor(val, val, val);
						}
					}
				}

			}
		}
		map.setData(data);
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
