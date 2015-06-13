package io.piotrjastrzebski.playground.tiledgentest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.piotrjastrzebski.playground.BaseScreen;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class TiledGenTest extends BaseScreen {
	MapWidget map;
	public TiledGenTest () {
		super();
		map = new MapWidget(
			new TextureRegion(new Texture(Gdx.files.internal("white.png"))));
		map.setSize(4);

		Table container = new Table();
		// put it in container so it is always centered in the pane
		container.add(map).fill();
		// map widget inside scroll pane for movement
		MyScrollPane pane = new MyScrollPane(container, skin);
		pane.setOverscroll(false, false);
		pane.setKnobsEnabled(false, false);
		pane.setMouseScrollEnabled(false);

		pane.setCancelTouchFocus(true);

		root.add(pane).expand().fill();//.pad(200);
		root.getStage().setScrollFocus(map);

		refresh();
	}

	public void refresh() {
		MapData data = new MapData();
		data.water = 0.5f;
		data.largestFeature = 128;
		data.persistence = 0.35;
		data.seed = MathUtils.random(Long.MAX_VALUE);
		data.width = 256;
		data.height = 128;

		Gdx.app.log("", "refresh " + data.seed);

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
