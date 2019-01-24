package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.AlphaAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIShaderTest extends BaseScreen {
	private final static String TAG = UIShaderTest.class.getSimpleName();
	private Texture texture;

	public UIShaderTest (GameReset game) {
		super(game);
		ShaderProgram shader = new ShaderProgram(
			Gdx.files.internal("shaders/saturation.vert"), Gdx.files.internal("shaders/saturation.frag"));
		if (!shader.isCompiled()) {
			throw new AssertionError("shader not compiled " + shader.getLog());
		}

		texture = new Texture("badlogic.jpg");
		{
			Image image = new ShaderImage(new TextureRegionDrawable(new TextureRegion(texture)), shader);
			image.setOrigin(Align.center);
			image.addAction(Actions.forever(Actions.parallel(
					Actions.sequence(
						Actions.scaleTo(.5f, .5f, 2),
						Actions.scaleTo(2f, 2f, 2)
					),
					Actions.rotateBy(360, 4),
					Actions.sequence(
						Actions.moveBy(100, 0, 1),
						Actions.moveBy(-200, 0, 2),
						Actions.moveBy(100, 0, 1)
					)
				)));
			root.add(image).expand();
		}

		{
			Image image = new Image(new TextureRegion(texture));
			image.setOrigin(Align.center);
			image.addAction(Actions.forever(Actions.parallel(
				Actions.sequence(
					Actions.scaleTo(.5f, .5f, 2),
					Actions.scaleTo(2f, 2f, 2)
				),
				Actions.rotateBy(360, 4),
				Actions.sequence(
					Actions.moveBy(100, 0, 1),
					Actions.moveBy(-200, 0, 2),
					Actions.moveBy(100, 0, 1)
				)
			)));
			root.add(image).expand();
		}

		clear.set(.5f, .5f, .5f, 1);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}

	private static class ShaderImage extends Image {

		private final ShaderProgram shader;

		public ShaderImage (Drawable drawable, ShaderProgram shader) {
			super(drawable);
			this.shader = shader;
		}

		float saturation = 0;
		@Override public void act (float delta) {
			super.act(delta);
			saturation += delta;

		}

		@Override public void draw (Batch batch, float parentAlpha) {
			ShaderProgram old = batch.getShader();
			// this will flush the batch even if its the same shader
			// but we probably want that anyway, since we probably have per object uniforms
			batch.setShader(shader);
			shader.setUniformf("u_saturation", 1.5f + MathUtils.sin(saturation) * 1.5f);
			super.draw(batch, parentAlpha);
			batch.setShader(old);
		}
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width /= 2;
		config.height /= 2;
		PlaygroundGame.start(args, config, UIShaderTest.class);
	}
}
