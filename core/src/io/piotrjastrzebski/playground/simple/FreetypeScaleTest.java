package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 26.05.2017.
 */
public class FreetypeScaleTest extends ApplicationAdapter {

	SpriteBatch batch;
	Texture img;

	BitmapFont font;
	OrthographicCamera camera;
	ExtendViewport viewport;

	@Override public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");

		camera = new OrthographicCamera();
		viewport = new ExtendViewport(128, 72, camera);

		float w=Gdx.graphics.getWidth();
		float h=Gdx.graphics.getHeight();

		float worldWidth=400;
		float worldHeight=h / (w / worldWidth);

		camera=new OrthographicCamera();
		camera.setToOrtho(false,worldWidth,worldHeight);

		FreeTypeFontGenerator freeTypeFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid-sans.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter ftp = new FreeTypeFontGenerator.FreeTypeFontParameter();
		ftp.size = 400;
		font = freeTypeFontGenerator.generateFont(ftp);

		font.getData().setScale(0.04f);
		font.setUseIntegerPositions(false);
	}

	@Override public void render () {

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		font.draw(batch, "LEADERBOARD", 8, 16);
		batch.end();
	}

	@Override public void resize (int width, int height) {
		viewport.update(width, height, true);
	}

	@Override public void dispose () {
		font.dispose();
		batch.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(new FreetypeScaleTest());
	}
}
