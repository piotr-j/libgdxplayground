package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Self contained test for proper touch/mouse handling
 *
 * Created by PiotrJ on 05/10/15.
 */
public class PolyBatchTest extends ApplicationAdapter {
	PolygonSpriteBatch batch;
	@Override public void create () {
		Pixmap white = new Pixmap(3, 3, Pixmap.Format.RGBA8888);
		white.setColor(Color.WHITE);
		white.drawRectangle(0, 0, 3, 3);
		Texture texture = new Texture(white);
		white.dispose();
		PolygonRegion region = new PolygonRegion(new TextureRegion(texture), new float[]{0, 0, 0, 3, 3, 3, 3, 0}, new short[]{0,1,2,0,3,2});
		// 6, so we cant fit 2 regions
		batch = new PolygonSpriteBatch(6);
		batch.begin();
		batch.draw(region, 0, 0);
		batch.draw(region, 0, 0);
		// java.lang.ArrayIndexOutOfBoundsException: 30
		batch.end();
		texture.dispose();
		batch.dispose();
	}

	public static void main (String[] arg) {
		PlaygroundGame.start(new PolyBatchTest());
	}
}
