package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.nio.ByteBuffer;

/**
 * Shader based outline drawing
 * http://www.allpiper.com/2d-selection-outline-shader-in-libgdx/
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class ShaderOutlineTest extends BaseScreen {
	private static final String TAG = ShaderOutlineTest.class.getSimpleName();

	private Texture source;
	private Sprite sourceSprite;

	ShaderProgram outlineShader;
	public ShaderOutlineTest (GameReset game) {
		super(game);

		outlineShader = new ShaderProgram(Gdx.files.internal("shaders/outline.vert"), Gdx.files.internal("shaders/outline.frag"));
		if (!outlineShader.isCompiled()) {
			throw new AssertionError(TAG + " : Shader not compiled!\n" + outlineShader.getLog());
		}

		outlineShader.begin();
		outlineShader.setUniformf("u_viewportInverse", 1f / Gdx.graphics.getWidth(), 1f / Gdx.graphics.getHeight());
		outlineShader.setUniformf("u_thickness", 8);
		outlineShader.setUniformf("u_step", Math.min(1f, Gdx.graphics.getWidth() / 70f));
		outlineShader.setUniformf("u_color", 1f, 0f, 0f);
		outlineShader.end();

		source = new Texture("test-shape.png");
		source.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		sourceSprite = new Sprite(source);
		sourceSprite.setSize(source.getWidth() * INV_SCALE * 4, source.getHeight() * INV_SCALE * 4);
		sourceSprite.setPosition(-3, -sourceSprite.getHeight()/2);

	}


	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0f, .75f, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setShader(null);
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		sourceSprite.draw(batch);
		batch.end();
		batch.setProjectionMatrix(gameCamera.combined);
		batch.setShader(outlineShader);
		batch.begin();
		sourceSprite.draw(batch);
		batch.end();
	}

	@Override public void dispose () {
		super.dispose();
		source.dispose();
		outlineShader.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, ShaderOutlineTest.class);
	}
}
