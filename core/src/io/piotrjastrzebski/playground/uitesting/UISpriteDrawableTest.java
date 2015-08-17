package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISpriteDrawableTest extends BaseScreen {
	public UISpriteDrawableTest (PlaygroundGame game) {
		super(game);

		Sprite white = new Sprite(new Texture("white.png"));
		GradientSpriteDrawable drawable = new GradientSpriteDrawable(white);
		drawable.setC1(Color.RED);
		drawable.setC2(Color.GREEN);
		drawable.setC3(Color.BLUE);
		drawable.setC4(Color.WHITE);
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

		public void setC1 (Color c1) {
			this.c1 = c1;
		}

		public void setC2 (Color c2) {
			this.c2 = c2;
		}

		public void setC3 (Color c3) {
			this.c3 = c3;
		}

		public void setC4 (Color c4) {
			this.c4 = c4;
		}

		public Color getC1 () {
			return c1;
		}

		public Color getC2 () {
			return c2;
		}

		public Color getC3 () {
			return c3;
		}

		public Color getC4 () {
			return c4;
		}
	}
}
