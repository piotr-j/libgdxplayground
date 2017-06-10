package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIPerspectiveWidgetTest extends BaseScreen {
	Texture badlogic;
	CardWidget cardWidget;
	public UIPerspectiveWidgetTest (GameReset game) {
		super(game);
		clear.set(Color.GRAY);
		badlogic = new Texture("badlogic.jpg");

//		VisWindow window = new VisWindow("Welp");
//		stage.addActor(window);
//		window.centerWindow();
		guiCamera.near = 0;
		guiCamera.far = 1000;
		guiCamera.position.z = 100;
		guiCamera.update();
		cardWidget = new CardWidget(new Image(badlogic));
		root.add(cardWidget);
	}


	float xRotation;
	@Override public void render (float delta) {
		super.render(delta);
		xRotation += delta;
		cardWidget.setXAxisRotation(xRotation);
		stage.act(delta);
		stage.draw();
	}

	public static class CardWidget extends WidgetGroup {

		private Matrix4 transformMatrix = new Matrix4();
		private Matrix4 projectionMatrix = new Matrix4();
		private Image image;
		private float xAxisRotation;


		public CardWidget(final Image image) {
			this.image = image;
			setWidth(image.getWidth());
			setHeight(image.getHeight());
			setOrigin(getWidth() / 2, getHeight() / 2);

			this.addActor(this.image);
		}

		public float getXAxisRotation() {
			return this.xAxisRotation;
		}

		public void setXAxisRotation(float val) {
			this.xAxisRotation = val;
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			transformMatrix.set(batch.getTransformMatrix());
			projectionMatrix.set(batch.getProjectionMatrix());
			batch.end();
			batch.getTransformMatrix().setToTranslation(getX(), getY(), -120);
			batch.getProjectionMatrix().val[Matrix4.M32] = 1f / 1500f;
			batch.getProjectionMatrix().rotate(1, 0, 0, 45);
			batch.begin();
			super.draw(batch, parentAlpha);
			//reset
			batch.setTransformMatrix(transformMatrix);
			batch.setProjectionMatrix(projectionMatrix);
		}
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
	}

	@Override public void dispose () {
		super.dispose();
		badlogic.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UIPerspectiveWidgetTest.class);
	}
}
