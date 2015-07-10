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
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.Utils;
import io.piotrjastrzebski.playground.tiledgentest.generators.TerrainGen;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class TiledGenTest extends BaseScreen {
	MapWidget map;
	MapData data;
	Interpolation interp;
	float gain;
	public TiledGenTest (PlaygroundGame game) {
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

		final Label iLabel = new Label("Interpolation ", skin);
		settings.add(iLabel);
		settings.row();
		final SelectBox<Interp> sbInterp = new SelectBox<>(skin);
		Array<Interp> items = new Array<>();
		items.add(new Interp(Interpolation.fade, "fade"));
		items.add(new Interp(Interpolation.linear, "linear"));
		items.add(new Interp(Interpolation.pow2, "pow2"));
		items.add(new Interp(Interpolation.pow2In, "pow2In"));
		items.add(new Interp(Interpolation.pow2Out, "pow2Out"));
		items.add(new Interp(Interpolation.pow3, "pow3"));
		items.add(new Interp(Interpolation.pow3In, "pow3In"));
		items.add(new Interp(Interpolation.pow3Out, "pow3Out"));
		items.add(new Interp(Interpolation.pow4, "pow4"));
		items.add(new Interp(Interpolation.pow4In, "pow4In"));
		items.add(new Interp(Interpolation.pow4Out, "pow4Out"));
		items.add(new Interp(Interpolation.pow5, "pow5"));
		items.add(new Interp(Interpolation.pow5In, "pow5In"));
		items.add(new Interp(Interpolation.pow5Out, "pow5Out"));
		items.add(new Interp(Interpolation.sine, "sine"));
		items.add(new Interp(Interpolation.sineIn, "sineIn"));
		items.add(new Interp(Interpolation.sineOut, "sineOut"));
		items.add(new Interp(Interpolation.exp10, "exp10"));
		items.add(new Interp(Interpolation.exp10In, "exp10In"));
		items.add(new Interp(Interpolation.exp10Out, "exp10Out"));
		items.add(new Interp(Interpolation.exp5, "exp5"));
		items.add(new Interp(Interpolation.exp5In, "exp5In"));
		items.add(new Interp(Interpolation.exp5Out, "exp5Out"));
		items.add(new Interp(Interpolation.circle, "circle"));
		items.add(new Interp(Interpolation.circleIn, "circleIn"));
		items.add(new Interp(Interpolation.circleOut, "circleOut"));
		items.add(new Interp(Interpolation.elastic, "elastic"));
		items.add(new Interp(Interpolation.elasticIn, "elasticIn"));
		items.add(new Interp(Interpolation.elasticOut, "elasticOut"));
		items.add(new Interp(Interpolation.swing, "swing"));
		items.add(new Interp(Interpolation.swingIn, "swingIn"));
		items.add(new Interp(Interpolation.swingOut, "swingOut"));
		items.add(new Interp(Interpolation.bounce, "bounce"));
		items.add(new Interp(Interpolation.bounceIn, "bounceIn"));
		items.add(new Interp(Interpolation.bounceOut, "bounceOut"));
		sbInterp.setItems(items);
		sbInterp.setSelected(items.first());
		sbInterp.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				interp = sbInterp.getSelected().interpolation;
				refresh();
			}
		});
		settings.add(sbInterp);
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
		float[][] terrainData = TerrainGen.generate(data.seed, data.width, data.height);

		float max = 1.0f;
		for (int mx = 0; mx < data.width; mx++) {
			for (int my = 0; my < data.height; my++) {
				MapData.Tile tile = data.tiles[mx][my];
				float val = terrainData[mx][my];
				tile.value = val;

				if (val > max) max = val;
				tile.setColor(val, val, val);

				if (data.waterEnabled) {
					if (val < data.water) {
						if (val < data.water * 0.7f) {
							tile.setColor(0.2f, 0.5f, 0.9f);
						} else {
							tile.setColor(0.4f, 0.7f, 1);
						}
					} else {
						// normalize val so 0 is at water level
						val = (val - data.water) / (max - data.water);

						// todo interp so higher values go up faster
						tile.elevation = val * 200;
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
