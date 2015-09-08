package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class CameraMoveTest extends BaseScreen {
	Sprite sprite;
	public CameraMoveTest (GameReset game) {
		super(game);
		sprite = new Sprite(new Texture("white.png"));
		sprite.setSize(1, 1);
	}

	@Override public void render (float delta) {
		super.render(delta);
		gameCamera.update();
		// use gui cam as the effect is not setup for scaled drawing
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		sprite.draw(batch);
		batch.end();
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.W:
			gameCamera.position.add(0, 0.1f, 0);
			break;
		case Input.Keys.S:
			gameCamera.position.add(0, -0.1f, 0);
			break;
		case Input.Keys.A:
			gameCamera.position.add(0.1f, 0, 0);
			break;
		case Input.Keys.D:
			gameCamera.position.add(-0.1f, 0, 0);
			break;
		case Input.Keys.UP:
			sprite.setPosition(sprite.getX(), sprite.getY() + 0.1f);
			break;
		case Input.Keys.DOWN:
			sprite.setPosition(sprite.getX(), sprite.getY() - 0.1f);
			break;
		case Input.Keys.LEFT:
			sprite.setPosition(sprite.getX() + 0.1f, sprite.getY());
			break;
		case Input.Keys.RIGHT:
			sprite.setPosition(sprite.getX() - 0.1f, sprite.getY());
			break;
		}
		return super.keyDown(keycode);
	}
}
