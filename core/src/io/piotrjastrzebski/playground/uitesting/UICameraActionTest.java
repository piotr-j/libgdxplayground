package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UICameraActionTest extends BaseScreen {
	public UICameraActionTest (GameReset game) {
		super(game);

		// some crap so we can see it it works
		VisWindow window = new VisWindow("Window!");
		window.setResizable(true);
		window.setMovable(true);
		window.defaults().pad(5);
		window.add(new VisLabel("Label1!"));
		window.add().expandX().fillX();
		window.add(new VisLabel("Label2!"));
		window.row();
		VisLabel longLabel = new VisLabel("ELLIPSIS!ELLIPSIS!ELLIPSIS!ELLIPSIS!ELLIPSIS!ELLIPSIS!ELLIPSIS!");
		longLabel.setWrap(true);
		longLabel.setEllipsis(true);
		window.add(longLabel).expandX().fillX().colspan(3);
		window.row();
		VisLabel longLabel2 = new VisLabel("WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!");
		longLabel2.setWrap(true);
		window.add(longLabel2).expandX().fillX().colspan(3);
		window.row();
		window.add().colspan(3).expand().fill();
		window.row();
		window.add(new VisLabel("Label3!"));
		window.add().expandX().fillX();
		window.add(new VisLabel("Label4!"));
		window.pack();

		stage.addActor(window);
		window.centerWindow();
		OrthographicCamera stageCamera = (OrthographicCamera)stage.getCamera();
		stage.addAction(Actions.sequence(Actions.delay(2),
			new CameraZoomBy(stageCamera, -.25f, 1f, Interpolation.linear),
			new CameraMoveBy(stageCamera, 0f, 100f, 2f, Interpolation.linear),
			new CameraMoveBy(stageCamera, 0f, -100f, 2f, Interpolation.linear),
			new CameraZoomBy(stageCamera, .25f, 1f, Interpolation.linear)
		));
	}

	public static class CameraMoveBy extends TemporalAction {
		protected OrthographicCamera camera;
		protected float startX, startY;
		protected float endX, endY;

		public CameraMoveBy (OrthographicCamera camera, float amountX, float amountY) {
			super(1f, Interpolation.linear);
			this.camera = camera;
			this.endX = amountX;
			this.endY = amountY;
		}

		public CameraMoveBy (OrthographicCamera camera, float amountX, float amountY, float duration) {
			super(duration, Interpolation.linear);
			this.camera = camera;
			this.endX = amountX;
			this.endY = amountY;
		}

		public CameraMoveBy (OrthographicCamera camera, float amountX, float amountY, float duration, Interpolation interpolation) {
			super(duration, interpolation);
			this.camera = camera;
			this.endX = amountX;
			this.endY = amountY;
		}

		@Override protected void begin () {
			startX = camera.position.x;
			startY = camera.position.y;
			endX += startX;
			endY += startY;
		}

		@Override protected void update (float percent) {
			System.out.println(percent);
			camera.position.x = startX + (endX - startX) * percent;
			camera.position.y = startY + (endY - startY) * percent;
			camera.update();
		}
	}

	public static class CameraZoomBy extends TemporalAction {
		protected OrthographicCamera camera;
		protected float startZoom;
		protected float endZoom;

		public CameraZoomBy (OrthographicCamera camera, float zoomBy, float duration, Interpolation interpolation) {
			super(duration, interpolation);
			this.camera = camera;
			endZoom = zoomBy;
		}

		@Override protected void begin () {
			startZoom = camera.zoom;
			endZoom += startZoom;
		}

		@Override protected void update (float percent) {
			camera.zoom = startZoom + (endZoom - startZoom) * percent;
			camera.update();
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
		PlaygroundGame.start(args, UICameraActionTest.class);
	}
}
