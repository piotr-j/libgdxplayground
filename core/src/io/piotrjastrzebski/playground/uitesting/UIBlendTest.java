package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIBlendTest extends BaseScreen {

	Texture atlas;
	TextureRegion track, thumb;
	FrameBuffer fbo;
	TextureRegion fboRegion;

	SpriteBatch batch;
	OrthographicCamera cam;

	public UIBlendTest (GameReset game) {
		super(game);

		atlas = new Texture(Gdx.files.internal("slider.png"));

		// ideally you would use a region packer like in LibGDX
		track = new TextureRegion(atlas, 0, 0, 64, 256);
		thumb = new TextureRegion(atlas, 65, 0, 64, 128);

		// for this demo we'll make our frame buffer the same size as our screen
		// if our device doesn't support NPOT textures, we might get an error
		// here
		// also if the screen resizes we'll need to accomodate for that...

		// the reason we make it the same size is so that we don't need to
		// upload a new projection
		// matrix to our SpriteBatch when rendering to the FBO
		fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight(), false);

		// this will just be the size of our slider track
		fboRegion = new TextureRegion(fbo.getColorBufferTexture(), 0, 0,
				track.getRegionWidth(), track.getRegionHeight());
		fboRegion.flip(false, true); // FBO uses lower left, TextureRegion uses
		// upper-left

		batch = new SpriteBatch();
		cam = new OrthographicCamera(Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		cam.setToOrtho(false);

		renderSlider();
	}

	protected void renderSlider() {
		// make our offscreen FBO the current buffer
		fbo.begin();

		// we need to first clear our FBO with transparent black
		Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// start our batch
		batch.begin();

		// render some sprites to our offscreen FBO
		float x = 0;
		float y = 0;
		int val = 40; // example value amount

		// use -1 to ignore.. somebody should fix this in LibGDX :\
		batch.setBlendFunction(-1, -1);

		// setup our alpha blending to avoid blending twice
		Gdx.gl20.glBlendFuncSeparate(GL20.GL_SRC_ALPHA,
				GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);

		// draw sprites
		batch.setColor(1f, 1f, 1f, 1f);
		batch.draw(track, x, y);
		batch.draw(thumb, x, y + val);

		// end (flush) our batch
		batch.end();

		// unbind the FBO
		fbo.end();

		// now let's reset blending to the default...
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	public void render(float delta) {
		// nice smooth background color
		float L = 233 / 255f;
		Gdx.gl.glClearColor(L, L, L, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// whenever the slider moves we will need to call renderSlider() to
		// update the offscreen region

		// render the offscreen region with "premultiplied alpha" blending
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

		batch.begin();

		// due to our different blend funcs we need to use RGBA to specify opacity
		// tinting becomes unavailable with this solution
		float a = 0.5f;
		batch.setColor(a, a, a, a);

		batch.draw(fboRegion, 0, 0);

		batch.setColor(1, 1, 1, 1);

		batch.draw(fboRegion, 128, 0);

		batch.end();


		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.begin();
		a = 0.5f;
		batch.setColor(a, a, a, a);

		batch.draw(fboRegion, 0, 256);

		batch.setColor(1, 1, 1, 1);

		batch.draw(fboRegion, 128, 256);

		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		if (cam != null) {
			cam.setToOrtho(false, width, height);
			batch.setProjectionMatrix(cam.combined);
		}
	}

	@Override public void dispose () {
		super.dispose();
		atlas.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIBlendTest.class);
	}
}
