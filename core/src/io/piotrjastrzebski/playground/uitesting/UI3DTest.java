package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UI3DTest extends BaseScreen {
	public UI3DTest (GameReset game) {
		super(game);
		Texture texture = new Texture("badlogic.jpg");
		PerspectiveGroup group = new PerspectiveGroup();
		Image image = new Image(texture);
		image.setColor(Color.GREEN);
		group.addActor(image);
		group.addActor(new VisLabel("GROUP 1"));
		PerspectiveGroup2 group2 = new PerspectiveGroup2();
		image = new Image(texture);
		image.setColor(Color.RED);
		group2.addActor(image);
		group.addActor(new VisLabel("GROUP 2"));

		guiCamera.near = .1f;
		guiCamera.far = 1000f;
		guiCamera.update();
		stage.addActor(group);
		group.setPosition(Gdx.graphics.getWidth()/2 - 256 - 64, Gdx.graphics.getHeight()/2);
		stage.addActor(group2);
		group2.setPosition(Gdx.graphics.getWidth()/2 + 64, Gdx.graphics.getHeight()/2);
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		guiCamera.position.z = 500;
		guiCamera.update();
	}

	/**
	 * Allows to render Group in perspective.
	 *
	 * Requires those OrthographicCamera settings:
	 * camera.near = -1000;
	 * camera.far = 1000;
	 * @author Lukasz Zmudziak, @lukz_dev on 2016-06-06.
	 */
	public static class PerspectiveGroup extends Group {

		private Matrix4 transformMatrix = new Matrix4();
		private Matrix4 projectionMatrix = new Matrix4();

		private Affine2 worldTransform = new Affine2();

		private float xAxisRotation = 0;
		private float perspective = 1f / 1500f;

		@Override public void act (float delta) {
			super.act(delta);
			xAxisRotation += delta * 45;
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			Matrix4 bpm = batch.getProjectionMatrix();
			Matrix4 btm = batch.getTransformMatrix();
			transformMatrix.set(btm);
			projectionMatrix.set(bpm);
			batch.end();

			float originX = getOriginX();
			float originY = getOriginY();
			float rotation = getRotation();
			float scaleX = getScaleX();
			float scaleY = getScaleY();

			float scaledOriginX = originX * scaleX;
			float scaledOriginY = originY * scaleY;

			worldTransform.setToTrnRotScl(scaledOriginX, scaledOriginY, rotation, scaleX, scaleY);
			if (originX != 0 || originY != 0) worldTransform.translate(-originX, -originY);

			btm.set(worldTransform);

			bpm.translate(getX() + (originX - scaledOriginX), getY() + (originY - scaledOriginY), 0);
			bpm.val[Matrix4.M32] = perspective;
			bpm.rotate(1, 0, 0, xAxisRotation);

			batch.begin();

			drawChildren(batch, parentAlpha);

			//reset
			batch.setTransformMatrix(transformMatrix);
			batch.setProjectionMatrix(projectionMatrix);
		}

		public float getPerspective() {
			return perspective;
		}

		public void setPerspective(float perspective) {
			this.perspective = perspective;
		}

		public float getXAxisRotation() {
			return this.xAxisRotation;
		}

		public void setXAxisRotation(float val) {
			this.xAxisRotation = val;
		}
	}

	public static class PerspectiveGroup2 extends Group {

		private Matrix4 transformMatrix = new Matrix4();
		private Matrix4 projectionMatrix = new Matrix4();

		private Affine2 worldTransform = new Affine2();

		private float xAxisRotation = 0;
		private float yAxisRotation = 0;
		private float perspective = 1f / 1500f;

		@Override public void act (float delta) {
			super.act(delta);
			xAxisRotation += delta * 45;
//			yAxisRotation += delta * 45;
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			projectionMatrix.set(batch.getProjectionMatrix());

			batch.getProjectionMatrix().val[Matrix4.M32] = perspective;
			batch.getProjectionMatrix().rotate(1, 0, 0, xAxisRotation);
			batch.getProjectionMatrix().rotate(0, 1, 0, yAxisRotation);

			super.draw(batch, parentAlpha);

			//reset
			batch.setProjectionMatrix(projectionMatrix);
		}

		public float getPerspective() {
			return perspective;
		}

		public void setPerspective(float perspective) {
			this.perspective = perspective;
		}

		public float getXAxisRotation() {
			return this.xAxisRotation;
		}

		public void setXAxisRotation(float val) {
			this.xAxisRotation = val;
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UI3DTest.class);
	}
}
