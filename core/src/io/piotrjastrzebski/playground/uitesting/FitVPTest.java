package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class FitVPTest extends BaseScreen {
	OrthographicCamera fitCamera;
	FitViewport fitViewport;
	public FitVPTest (PlaygroundGame game) {
		super(game);
		fitCamera = new OrthographicCamera();
		fitViewport = new FitViewport(1000, 1000, fitCamera);
	}
	Vector3 cursor = new Vector3();
	@Override public void render (float delta) {
		super.render(delta);
		cursor.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		fitViewport.unproject(cursor);

		renderer.setProjectionMatrix(fitCamera.combined);
		renderer.setColor(Color.RED);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.circle(cursor.x, cursor.y, 25);
		renderer.end();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		fitViewport.update(width, height, true);
	}
}
