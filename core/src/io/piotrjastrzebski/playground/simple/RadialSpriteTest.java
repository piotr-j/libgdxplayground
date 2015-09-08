package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 09/09/15.
 */
public class RadialSpriteTest extends BaseScreen {
	Texture texture;
	RadialSprite sprite;
	public RadialSpriteTest (GameReset game) {
		super(game);
		texture = new Texture("badlogic.jpg");
		sprite = new RadialSprite(new TextureRegion(texture));
	}

	float angle;
	@Override public void render (float delta) {
		super.render(delta);
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		angle += delta * 90;
		sprite.draw(batch, 0, 0, 5, 5, angle);
		sprite.draw(batch, -6, 0, 5, 5, -angle);
		batch.end();
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}
}
