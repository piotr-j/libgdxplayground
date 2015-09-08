package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISpriteDrawableTest extends BaseScreen {
	public UISpriteDrawableTest (GameReset game) {
		super(game);

		Sprite white = new Sprite(new Texture("white.png"));
		GradientSpriteDrawable drawable = new GradientSpriteDrawable(white);
		drawable.setLowerLeft(Color.RED);
		drawable.setUpperLeft(Color.GREEN);
		drawable.setUpperRight(Color.BLUE);
		drawable.setLowerRight(Color.WHITE);
		Image image = new Image(drawable);
		root.add(image).expand().fill().pad(100);
	}

	float timer;
	@Override public void render (float delta) {
		super.render(delta);
		timer+=delta;
		root.getColor().a = 0.5f + MathUtils.sin(timer) / 2;
		stage.act(delta);
		stage.draw();
	}

	public static class GradientSpriteDrawable extends SpriteDrawable {
		// clock wise starting at lower left corner
		private Color c1 = new Color();
		private Color c2 = new Color();
		private Color c3 = new Color();
		private Color c4 = new Color();
		public GradientSpriteDrawable (Sprite sprite) {
			super(sprite);
		}

		Color tmpColor = new Color();
		@Override public void draw (Batch batch, float x, float y, float originX, float originY, float width, float height,
			float scaleX, float scaleY, float rotation) {
			Sprite sprite = getSprite();
			sprite.setOrigin(originX, originY);
			sprite.setRotation(rotation);
			sprite.setScale(scaleX, scaleY);
			sprite.setBounds(x, y, width, height);

			Color batchColor = batch.getColor();
			float[] vertices = sprite.getVertices();
			vertices[Batch.C1] = tmpColor.set(c1).mul(batchColor).toFloatBits();
			vertices[Batch.C2] = tmpColor.set(c2).mul(batchColor).toFloatBits();
			vertices[Batch.C3] = tmpColor.set(c3).mul(batchColor).toFloatBits();
			vertices[Batch.C4] = tmpColor.set(c4).mul(batchColor).toFloatBits();

			sprite.draw(batch);
		}

		public void setLowerLeft (Color color) {
			c1.set(color);
		}

		public void setUpperLeft (Color color) {
			c2.set(color);
		}

		public void setUpperRight (Color color) {
			c3.set(color);
		}

		public void setLowerRight (Color color) {
			c4.set(color);
		}

		public Color getLowerLeft () {
			return c1;
		}

		public Color getUpperLeft () {
			return c2;
		}

		public Color getUpperRight () {
			return c3;
		}

		public Color getLowerRight () {
			return c4;
		}
	}
}
