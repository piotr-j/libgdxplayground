package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Json;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.io.IOException;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class PixmapSaveTest extends BaseScreen {
	private static final String TAG = PixmapSaveTest.class.getSimpleName();

	public PixmapSaveTest (GameReset game) {
		super(game);
//		Texture texture = new Texture(Gdx.files.internal("badlogic.jpg"));
		Texture texture = new Texture(new Pixmap(1024, 1024, Pixmap.Format.RGBA8888));
		TextureData data = texture.getTextureData();
		Pixmap pixmap = data.consumePixmap();
//		data.prepare();
		PixmapIO.writePNG(Gdx.files.external("test/test.png"), pixmap);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, PixmapSaveTest.class);
	}
}
