package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ArrayReflection;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Trivial outline thing
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class OutlineTest extends BaseScreen {
	private static final String TAG = OutlineTest.class.getSimpleName();

	private Texture source;
	private Sprite sourceSprite;
	private Texture outline;
	private Sprite outlineSprite;

	public OutlineTest (GameReset game) {
		super(game);
		source = new Texture("test-shape.png");
		source.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		sourceSprite = new Sprite(source);
		sourceSprite.setSize(source.getWidth() * INV_SCALE * 4, source.getHeight() * INV_SCALE * 4);
		sourceSprite.setPosition(-3, -sourceSprite.getHeight()/2);
		Pixmap out = buildOutline(source, 2, Color.WHITE, Color.GRAY);
		outline = new Texture(out);
		out.dispose();
		outline.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		outlineSprite = new Sprite(outline);
		outlineSprite.setSize(outline.getWidth() * INV_SCALE * 4, outline.getHeight() * INV_SCALE * 4);
		outlineSprite.setPosition(3, -outlineSprite.getHeight()/2);
	}

	private Pixmap buildOutline (Texture source, int outlineSize, Color fg, Color bg) {
		TextureData data = source.getTextureData();
		data.prepare();
		Pixmap src = data.consumePixmap();
		int srcWidth = src.getWidth();
		int srcHeight = src.getHeight();
		int width = srcWidth + outlineSize * 2;
		int height = srcHeight + outlineSize * 2;
		Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		int fgColor = Color.rgba8888(fg);
		int bgColor = Color.rgba8888(bg);
		// create outline
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (shouldAddFGAt(src, x, y, outlineSize)) {
					pixmap.drawPixel(x, y, fgColor);
				}
			}
		}
		// create outline shadow
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (pixmap.getPixel(x, y) == fgColor) {
					for (int oy = -1; oy >= -outlineSize; oy--) {
						if (pixmap.getPixel(x, y + oy) == 0) {
							pixmap.drawPixel(x, y + oy, bgColor);
						}
					}
				}
			}
		}
		flipPixmap(pixmap);
		src.dispose();
		return pixmap;
	}

	private boolean shouldAddFGAt (Pixmap src, int x, int y, int outlineSize) {
		int sx = x - outlineSize;
		int sy = y - outlineSize;
		if (sx >= 0 && sx < src.getWidth() && sy >= 0 && sy < src.getHeight()) {
			// skip pixels that are set in src img
			int pixel = src.getPixel(sx, sy);
			if (pixel != 0) {
				return false;
			}
		}
		// find if we should add something to this pixel
		for (int ox = -outlineSize; ox <= outlineSize; ox++) {
			for (int oy = -outlineSize; oy <= outlineSize; oy++) {
				int fx = sx + ox;
				int fy = sy + oy;
				if (fx < 0 || fx >= src.getWidth() || fy < 0 && fy >=src.getHeight()) continue;
				int pixel = src.getPixel(fx, fy);
				if (pixel != 0) {
					return true;
				}
			}
		}
		return false;
	}

	private void flipPixmap (Pixmap pixmap) {
		int w = pixmap.getWidth();
		int h = pixmap.getHeight();
		final ByteBuffer pixels = pixmap.getPixels();
		final int numBytes = w * h * 4;
		byte[] lines = new byte[numBytes];
		final int numBytesPerLine = w * 4;
		for (int i = 0; i < h; i++) {
			pixels.position((h - i - 1) * numBytesPerLine);
			pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
		}
		pixels.clear();
		pixels.put(lines);
		// clear again so texture doesnt explode
		pixels.clear();
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.75f, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//		Gdx.gl.glEnable(GL20.GL_BLEND);
//		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		sourceSprite.draw(batch);
		outlineSprite.draw(batch);
		batch.end();
	}

	@Override public void dispose () {
		super.dispose();
		source.dispose();
		outline.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, OutlineTest.class);
	}
}
