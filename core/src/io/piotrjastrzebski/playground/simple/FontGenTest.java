package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class FontGenTest extends BaseScreen {
	private final FreeTypeFontGenerator fontGenerator;
	private final BitmapFont font;

	public FontGenTest (GameReset game) {
		super(game);
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid-sans.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 100;
		parameter.color = Color.WHITE;
		font = fontGenerator.generateFont(parameter);
		font.getData().setScale(0.05f);
		font.setUseIntegerPositions(false);
	}

	@Override public void render (float delta) {
		super.render(delta);
		// use gui cam as the effect is not setup for scaled drawing
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		font.draw(batch, "WELP", 0, 0);
		batch.end();
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.NUM_1:

			break;
		case Input.Keys.NUM_2:

			break;
		}
		return true;
	}

	@Override public void dispose () {
		super.dispose();
		fontGenerator.dispose();
		font.dispose();
	}
}
