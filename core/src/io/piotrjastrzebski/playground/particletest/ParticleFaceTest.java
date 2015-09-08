package io.piotrjastrzebski.playground.particletest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ParticleFaceTest extends BaseScreen {
	MyEmitter emitter;
	public ParticleFaceTest (GameReset game) {
		super(game);

		InputStream input = Gdx.files.internal("particles/face.p").read();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(input), 512);
			emitter = new MyEmitter(reader);
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error loading effect: ", ex);
		} finally {
			StreamUtils.closeQuietly(reader);
		}

		emitter.setSprite(new Sprite(new Texture("particles/particle-drop.png")));

		guiCamera.unproject(pos.set(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0));
		moveEffect(pos.x, pos.y);
	}

	@Override public void render (float delta) {
		super.render(delta);
		// use gui cam as the effect is not setup for scaled drawing
		batch.setProjectionMatrix(guiCamera.combined);
		batch.begin();
		emitter.draw(batch, delta);
		batch.end();
	}

	private void moveEffect (float x, float y) {
		emitter.setPosition(x, y);
	}

	Vector3 pos = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		guiCamera.unproject(pos.set(screenX, screenY, 0));
		moveEffect(pos.x, pos.y);
		return super.touchDown(screenX, screenY, pointer, button);
	}
}
