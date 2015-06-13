package io.piotrjastrzebski.playground.tiledgentest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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
			@Override public boolean scrolled (InputEvent event, float x, float y, int amount) {
				size -= amount * 0.1f;
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
				Gdx.app.log("", "Clicked " + data.data[mx][my]);
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
				float val = (float)data.data[x][y];
				val = MathUtils.clamp(val, 0, 1);
				if (val < data.water) {
					if (val < data.water * 0.8f) {
						batch.setColor(0.2f, 0.5f, 0.9f, 1);
					} else {
						batch.setColor(0.4f, 0.7f, 1, 1);
					}
				} else {
					val = (val - data.water) / (1 - data.water);
					batch.setColor(val, val, val, 1);
				}
				batch.draw(
					white,
					// center in the parent
					sX + x * size, // + pWidth / 2 - data.width * size / 2,
					sY + y * size, // + pHeight / 2 - data.height * size / 2,
					size, size
				);
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
