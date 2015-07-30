package io.piotrjastrzebski.playground.simplelights;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Simple lights
 * http://techblog.orangepixel.net/2015/07/shine-a-light-on-it
 * Created by EvilEntity on 28/07/2015.
 */
public class SimpleLightTest extends BaseScreen {
	private final static String TAG = SimpleLightTest.class.getSimpleName();
	FrameBuffer lightBuffer;
	TextureRegion lightBufferRegion;;
	int lowDisplayW;
	int displayW;
	int lowDisplayH;
	int displayH;
	Color ambiance;
	Texture bg;
	Array<Light> lights = new Array<>();
	Light current;
	TextureRegion lightRegion;
	TextureRegion coneLightRegion;

	public SimpleLightTest (PlaygroundGame game) {
		super(game);
		Texture lightTex = new Texture("simplelighttest/light.png");
		lightRegion = new TextureRegion(lightTex);
		coneLightRegion = new TextureRegion(lightTex, 0, 0, 64, 64);
		coneLightRegion.flip(true, false);
		bg = new Texture("simplelighttest/spacegrunts.png");

		ambiance = new Color(0.3f, 0.38f, 0.4f, 1);
		createLight(0, 0);

		Gdx.app.log(TAG, "Click to create a light");
		Gdx.app.log(TAG, "Scroll to rotate cone lights");
		Gdx.app.log(TAG, "Space to clear lights");
	}

	private void createLight(float x, float y) {
		lights.add(new Light(x, y, MathUtils.randomBoolean()?lightRegion : coneLightRegion));
		current = lights.get(lights.size-1);
	}

	@Override public void render (float delta) {
		super.render(delta);
		// default batch blending
		batch.disableBlending();
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		batch.draw(bg, -bg.getWidth() / 2 * INV_SCALE, -bg.getHeight() / 2 * INV_SCALE, bg.getWidth() * INV_SCALE,
			bg.getHeight() * INV_SCALE);
		batch.end();

		// start rendering to the lightBuffer
		lightBuffer.begin();

		// setup the right blending
		batch.enableBlending();
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

		// set the ambient color values, this is the "global" light of your scene
		// imagine it being the sun.  Usually the alpha value is just 1, and you change the darkness/brightness with the Red, Green and Blue values for best effect
		Gdx.gl.glClearColor(ambiance.r, ambiance.g, ambiance.b, ambiance.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//		batch.setProjectionMatrix(lightCamera.combined);
		// start rendering the lights to our spriteBatch
		batch.begin();
		for (Light light : lights) {
			batch.setColor(light.color);
			// note: this is very dumb
			if (light.region == coneLightRegion) {
				batch.draw(light.region, light.x, light.y, 0, 0, light.width, light.height, 1, 1, light.rotation);
			} else {
				batch.draw(light.region, light.x, light.y, light.width, light.height);
			}
		}
		batch.end();
		lightBuffer.end();

		batch.setProjectionMatrix(gameCamera.combined);
		// now we render the lightBuffer to the default "frame buffer"
		// with the right blending !
		batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
		// reset the batch color or we will get last light color tint
		batch.setColor(Color.WHITE);
		batch.begin();
		batch.draw(lightBufferRegion, -VP_WIDTH / 2, -VP_HEIGHT / 2, VP_WIDTH, VP_HEIGHT);
		batch.end();

		// draw fbo without fancy blending, for debug
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.begin();
		batch.draw(lightBufferRegion, VP_WIDTH / 4, -VP_HEIGHT/2, VP_WIDTH / 4, VP_HEIGHT / 4);
		batch.end();

		// post light-rendering
		// you might want to render your status bar stuff here
	}

	Vector3 pos = new Vector3();
	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(pos.set(screenX, screenY, 0));
		if (current != null) {
			current.x = pos.x;
			current.y = pos.y;
		}
		return true;
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(pos.set(screenX, screenY, 0));
		createLight(pos.x, pos.y);
		return true;
	}

	@Override public boolean scrolled (int amount) {
		if (current != null) {
			current.rotation += amount * 15;
		}
		return true;
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.SPACE) {
			lights.clear();
			createLight(pos.x, pos.y);
		}
		return super.keyDown(keycode);
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		displayW = width;
		displayH = height;
		lowDisplayW = width;
		lowDisplayH = height;

		// Fakedlight system (alpha blending)

		// if lightBuffer was created before, dispose, we recreate a new one
		if (lightBuffer!=null)
			lightBuffer.dispose();
		lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, MathUtils.nextPowerOfTwo(lowDisplayW), MathUtils.nextPowerOfTwo(lowDisplayH), false);
		lightBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		lightBufferRegion = new TextureRegion(lightBuffer.getColorBufferTexture(), 0, lightBuffer.getHeight() - lowDisplayH, lowDisplayW, lowDisplayH);
		lightBufferRegion.flip(false, true);
	}

	@Override public void dispose () {
		super.dispose();
		lightBuffer.dispose();
		lightRegion.getTexture().dispose();
	}

	private static class Light {
		public float x = 0;
		public float y = 0;
		public float width = 16;
		public float height = 16;
		public float rotation;
		public Color color = new Color(0.9f, 0.4f, 0f, 1f);
		public TextureRegion region;

		public Light (float x, float y, TextureRegion region) {
			this.x = x;
			this.y = y;
			this.region = region;

			width = height = MathUtils.random(2, 8);
			color.set(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
//			rotation = MathUtils.random(360);
		}
	}
}
