package io.piotrjastrzebski.playground.shaders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Adapted from http://forums.tigsource.com/index.php?topic=48508.msg1163762#msg1163762
 *
 * Created by EvilEntity on 07/06/2015.
 */
public class ShaderFireWallTest extends ShaderTestBase {
	Texture noise;

	public ShaderFireWallTest (PlaygroundGame game) {
		super(game);
	}

	@Override protected void init () {
		noise = new Texture("shaders/noise2.png");
		noise.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

		Sprite sprite;
		sprite = createSprite("shaders/rect-bg.png");
		sprite.setPosition(-VP_WIDTH / 2, -VP_HEIGHT / 2);
		sprite.setSize(VP_WIDTH, VP_HEIGHT);

		sprite = createSprite("shaders/rect-128.png");
		sprite.setPosition(-6, 3);
		sprite = createSprite("shaders/rect-128.png");
		sprite.setPosition(6 -sprite.getWidth(), 3);

		sprite = createSprite("shaders/rect-128x64.png");
		sprite.setPosition(-6, 0);
		sprite = createSprite("shaders/rect-128x64.png");
		sprite.setPosition(6 -sprite.getWidth(), 0);

		sprite = createSprite("shaders/rect-64.png");
		sprite.setPosition(-4, -3);
		sprite = createSprite("shaders/rect-64.png");
		sprite.setPosition(4 - sprite.getWidth(), -3);
		createShader("fire");
	}

	float time;
	@Override protected void preRender (float delta) {
		Shader shader = shaders.get("fire");
		if (shader == null) {
			batch.setShader(null);
		} else {
			ShaderProgram fire = shader.get();
			batch.setShader(fire);
			fire.begin();
			fire.setUniformf("u_time", time += delta);
			fire.setUniformi("u_noise", 1);
			fire.end();

			noise.bind(1);
		}
	}

	@Override public void dispose () {
		super.dispose();
		noise.dispose();
	}
}
