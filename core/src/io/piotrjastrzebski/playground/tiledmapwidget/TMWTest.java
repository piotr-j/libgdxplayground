package io.piotrjastrzebski.playground.tiledmapwidget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class TMWTest extends BaseScreen {
	MapWidget map;
	public TMWTest (GameReset game) {
		super(game);
		map = new MapWidget(
			new TextureRegion(new Texture(Gdx.files.internal("white.png"))));

		Table container = new Table();
		// put it in container so it is always centered in the pane
		container.add(map).fill();
		// map widget inside scroll pane for movement
		MyScrollPane pane = new MyScrollPane(container, skin);
		pane.setOverscroll(false, false);
		pane.setKnobsEnabled(false, false);
		pane.setMouseScrollEnabled(false);

		pane.setCancelTouchFocus(true);

		root.add(pane).expand().fill().pad(200);
		root.getStage().setScrollFocus(map);

		MapData data = new MapData();
		data.water = 0.5f;
		data.width = 512;
		data.height = 256;
		data.data = new double[data.width][data.height];
		for (int x = 0; x < data.width; x++) {
			for (int y = 0; y < data.height; y++) {
				data.data[x][y] = MathUtils.random(1.f);
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
}
