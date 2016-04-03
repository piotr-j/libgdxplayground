package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class BlendTest extends BaseScreen {
	private static final String TAG = BlendTest.class.getSimpleName();

	private TextureRegion ball;
	private TextureRegion bg;
	private FrameBuffer fbo;
	private TextureRegion fboReg;
	public BlendTest (GameReset game) {
		super(game);
		ball = new TextureRegion(new Texture("blend/crystalball.png"));
		bg = new TextureRegion(new Texture("blend/flamingobg.jpg"));
		fbo = new FrameBuffer(Pixmap.Format.RGBA8888, bg.getRegionWidth(), bg.getRegionHeight(), false);
		fboReg = new TextureRegion(fbo.getColorBufferTexture());
		fboReg.flip(false, true);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		fbo.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());
		batch.begin();
		batch.draw(ball, 0, 0);
		batch.end();
		batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA);
		batch.begin();
		batch.draw(bg, 0, 0);
		batch.end();
		fbo.end();

		batch.setProjectionMatrix(guiCamera.combined);
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.setProjectionMatrix(guiCamera.combined);
		batch.begin();
		batch.draw(fboReg, Gdx.graphics.getWidth()/2 - fboReg.getRegionWidth()/2, Gdx.graphics.getHeight()/2 - fboReg.getRegionHeight()/2);
		batch.end();
	}

	@Override public void dispose () {
		super.dispose();
		ball.getTexture().dispose();
		bg.getTexture().dispose();
		fbo.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, BlendTest.class);
	}
}
