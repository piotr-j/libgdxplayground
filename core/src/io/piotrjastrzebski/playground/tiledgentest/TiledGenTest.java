package io.piotrjastrzebski.playground.tiledgentest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
		map.setSize(4);
		data = new MapData();

		data.water = 0.5f;
		data.largestFeature = 128;
		data.persistence = 0.35f;
		data.seed = MathUtils.random(Long.MAX_VALUE);
		data.width = 256;
		data.height = 128;

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

		settings.add(new Label("Largest Feature", skin));
		settings.row();
		final Slider largestFeature = new Slider(4, 256, 1, false, skin);
		largestFeature.setValue(data.largestFeature);
		largestFeature.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				data.largestFeature = (int)largestFeature.getValue();
				refresh();
			}
		});
		settings.add(largestFeature);
		settings.row();

		settings.add(new Label("Persistence", skin));
		settings.row();
		final Slider persistence = new Slider(0.1f, 1.5f, 0.05f, false, skin);
		persistence.setValue(data.persistence);
		persistence.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				data.persistence = persistence.getValue();
				refresh();
			}
		});
		settings.add(persistence);
		settings.row();

		settings.add(new Label("Water", skin));
		settings.row();
		final Slider water = new Slider(0.01f, 1.0f, 0.01f, false, skin);
		water.setValue(data.water);
		water.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				data.water = water.getValue();
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
		data.data = new double[xResolution][yResolution];
		for (int i = 0; i < xResolution; i++) {
			for (int j = 0; j < yResolution; j++) {
				int x = (int)(xStart + i * ((XEnd - xStart) / xResolution));
				int y = (int)(yStart + j * ((yEnd - yStart) / yResolution));
				// normalize
				data.data[i][j] = 0.5d + noise.getNoise(x, y);
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
