package io.piotrjastrzebski.playground.packs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

public class PackColorTest extends BaseScreen {
	private static final String TAG = PackColorTest.class.getSimpleName();
	Pixmap src;
	Pixmap dst;
	public PackColorTest (GameReset game) {
		super(game);
		clear.set(Color.PINK);

		TexturePacker.Settings settings = new TexturePacker.Settings();
		String in = Gdx.files.internal("pack_in").file().getAbsolutePath();
		String out = Gdx.files.internal("pack_out").file().getAbsolutePath();
		TexturePacker.process(settings, in, out, "colors.atlas");

		src = new Pixmap(Gdx.files.internal("pack_in/colors.png"));

		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("pack_out/colors.atlas"));
		TextureAtlas.AtlasRegion colors = atlas.findRegion("colors");
		TextureData textureData = colors.getTexture().getTextureData();
		textureData.prepare();
		Pixmap rawDst = textureData.consumePixmap();
		dst = new Pixmap(src.getWidth(), src.getHeight(), src.getFormat());
		dst.setBlending(Pixmap.Blending.None);
		dst.drawPixmap(rawDst, 0, 0, colors.getRegionX(), colors.getRegionY(), colors.getRegionWidth(), colors.getRegionHeight());
		compare(src, dst);
		atlas.dispose();
	}

	private void compare (Pixmap src, Pixmap dst) {
		Color srcColor = new Color();
		Color dstColor = new Color();
		for (int x = 0; x < src.getWidth(); x++) {
			for (int y = 0; y < src.getHeight(); y++) {
				int srcPixel = src.getPixel(x, y);
				int dstPixel = dst.getPixel(x, y);
				if (srcPixel != dstPixel) {
					Color.rgba8888ToColor(srcColor, srcPixel);
					Color.rgba8888ToColor(dstColor, dstPixel);
					Gdx.app.log(TAG, "pixels dont match at " + x + "x" + y + ", " + srcColor + " != " + dstColor);
					return;
				}
			}
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		Texture srcTexture = new Texture(src);
		Texture dstTexture = new Texture(dst);
		batch.setProjectionMatrix(guiCamera.combined);
		batch.enableBlending();
		batch.begin();
		float scale = 4;
		batch.draw(srcTexture, 100, 100, srcTexture.getWidth() * scale, srcTexture.getHeight() * scale);
		batch.draw(dstTexture, 100 + (10 + srcTexture.getWidth()) * scale, 100, dstTexture.getWidth() * scale, dstTexture.getHeight() * scale);
		batch.end();
//		renderer.setProjectionMatrix(guiCamera.combined);
//		renderer.begin(ShapeRenderer.ShapeType.Line);
//		renderer.setColor(Color.MAGENTA);
//		renderer.rect(100, 100, srcTexture.getWidth() * scale, srcTexture.getHeight() * scale);
//		renderer.setColor(Color.CYAN);
//		renderer.rect(100 + (10 + srcTexture.getWidth()) * scale, 100, dstTexture.getWidth() * scale, dstTexture.getHeight() * scale);
//		renderer.end();
		srcTexture.dispose();
		dstTexture.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, PackColorTest.class);
	}
}
