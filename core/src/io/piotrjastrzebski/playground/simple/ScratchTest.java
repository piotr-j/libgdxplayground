package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class ScratchTest extends BaseScreen {
	private static final String TAG = ScratchTest.class.getSimpleName();
	TextureRegion bg;
	TextureRegion clearBrush;
	FrameBuffer scratchFBO;
	TextureRegion scratchRegion;
	Color scratchColor = new Color(Color.GRAY);
	public ScratchTest (final GameReset game) {
		super(game);
		int w = 33;
		int h = 33;
		Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
		float size = w/2f - .5f;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				float dst = Vector2.dst(x, y, size, size);
				if (dst < size) {
					// why does this not blend correctly :/
					pm.setColor(1, 1, 1, (float)(1 - Math.pow(dst/size, 10)));
					pm.drawPixel(x, y);
				}
			}
		}
		clearBrush = new TextureRegion(new Texture(pm));
		pm.dispose();

		bg = new TextureRegion(new Texture("badlogic.jpg"));

		scratchFBO = new FrameBuffer(Pixmap.Format.RGBA8888, bg.getRegionWidth(), bg.getRegionHeight(), false);
		scratchRegion = new TextureRegion(scratchFBO.getColorBufferTexture());
		scratchRegion.flip(false, true);
		scratchFBO.begin();
		Gdx.gl.glClearColor(scratchColor.r, scratchColor.g, scratchColor.b, scratchColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		scratchFBO.end();
	}

	Vector2 lcs = new Vector2(-9999, 9999);
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1f, 1f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			scratchFBO.begin();
			Gdx.gl.glClearColor(scratchColor.r, scratchColor.g, scratchColor.b, scratchColor.a);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			scratchFBO.end();
		}

		if (!lcs.epsilonEquals(cs, .05f)) {
			scratchFBO.begin();
			batch.getProjectionMatrix().setToOrtho2D(-4, -4, 8, 8);
			batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
			batch.enableBlending();
			batch.begin();
			batch.draw(clearBrush, cs.x - .5f, cs.y - .5f, 2, 2);
			batch.end();
			scratchFBO.end();
			lcs.set(cs);
		}

		batch.setProjectionMatrix(gameCamera.combined);
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.enableBlending();
		batch.begin();
		batch.setColor(Color.WHITE);
		batch.draw(bg, -4, -4, 8, 8);
		batch.draw(scratchRegion, -4, -4, 8, 8);

		batch.setColor(Color.FOREST);
		batch.draw(clearBrush, cs.x - .5f, cs.y - .5f, 2, 2);
		batch.end();
	}

	@Override public void dispose () {
		super.dispose();
		bg.getTexture().dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, ScratchTest.class);
	}
}
