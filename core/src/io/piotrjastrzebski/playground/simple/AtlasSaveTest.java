package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class AtlasSaveTest extends BaseScreen {
	private final static String TAG = AtlasSaveTest.class.getSimpleName();

	public AtlasSaveTest (GameReset game) {
		super(game);
		TextureAtlas atlas = new TextureAtlas("gui/uiskin.atlas");
		FileHandle save = Gdx.files.external("save");

		for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
			TextureData td = region.getTexture().getTextureData();
			td.prepare();
			Pixmap source = td.consumePixmap();
			Pixmap pixmap = new Pixmap(region.originalWidth, region.originalHeight, Pixmap.Format.RGBA8888);
			pixmap.drawPixmap(
				source,
				(int)(td.getWidth() * region.getU()), (int)(td.getHeight() * region.getV()),
				region.originalWidth, region.originalHeight,
				(int)region.offsetX, (int)region.offsetY,
				region.originalWidth, region.originalHeight);
			PixmapIO.writePNG(save.child(region.name), pixmap);
			pixmap.dispose();
		}
	}
}
