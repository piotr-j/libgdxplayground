package io.piotrjastrzebski.playground.tiledgentest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class MapWidget extends Widget {
	MapData data;
	TextureRegion white;
	float size;

	public MapWidget (TextureRegion white) {
		this.white = white;
		size = 1;

		addListener(new InputListener(){
			@Override public boolean scrolled (InputEvent event, float x, float y, float amountX, float amountY) {
				size -= amountX * 0.1f;
				invalidateHierarchy();
				return true;
			}
		});
		addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				int mx = (int)(x / size);
				int my = (int)(y / size);
				if (mx < 0 || mx > data.width) return;
				if (my < 0 || my > data.height) return;
				Gdx.app.log("", "Clicked " + data.tiles[mx][my]);
			}
		});
	}

	public void setSize(float size) {
		this.size = size;
	}

	public void setData(MapData data) {
		this.data = data;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		validate();
		if (data == null) return;
		float sX = getX();
		float sY = getY();

		for (int x = 0; x < data.width; x++) {
			for (int y = 0; y < data.height; y++) {
				batch.setColor(data.tiles[x][y].color);
				batch.draw(white, sX + x * size, sY + y * size, size, size);
			}
		}
	}

	public float getWidth () {
		return getPrefWidth();
	}

	public float getHeight () {
		return getPrefHeight();
	}

	public float getMinWidth () {
		return getPrefWidth();
	}

	public float getMinHeight () {
		return getPrefHeight();
	}

	public float getPrefWidth () {
		return data.width * size;
	}

	public float getPrefHeight () {
		return data.height * size;
	}

	public float getMaxWidth () {
		return data.width * size;
	}

	public float getMaxHeight () {
		return data.height * size;
	}
}
