package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.utils.Align.*;
import static com.badlogic.gdx.utils.Align.bottom;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UITableScaleAlignPositionTest extends BaseScreen {
	public UITableScaleAlignPositionTest (GameReset game) {
		super(game);
		rebuild();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		guiViewport.setUnitsPerPixel(.5f);
		rebuild();
	}

	void rebuild () {
		PLog.log("rebuild");
		root.clear();
		float cx = guiCamera.viewportWidth / 2;
		float cy = guiCamera.viewportHeight / 2;
		Label.LabelStyle style = new Label.LabelStyle(skin.get("default", Label.LabelStyle.class));
		Drawable drawable = skin.getDrawable("default-rect");
		style.background = drawable;
		float duration = 2f;
		{
			float scale = .75f;
			Table table = new Table();
			table.add(new Label("hello 0.75 ", style));
			table.setScale(scale);
			table.setTransform(true);
			table.pack();
			table.setOrigin(center);
			setPosition(table, cx, cy + 100, center);
			table.debugAll();
			table.addAction(Actions.forever(Actions.sequence(
				Actions.scaleTo(1f, 1f, duration/2),
				Actions.scaleTo(scale, scale, duration/2)
			)));

			root.addActor(table);
		}
		{
			float scale = 1f;
			Table table = new Table();
			table.add(new Label("hello 1.0 ", style));
			table.setScale(1);
			table.setTransform(true);
			table.pack();
			table.setOrigin(center);
			setPosition(table, cx, cy, left);
			table.debugAll();
			table.addAction(Actions.forever(Actions.sequence(
				Actions.scaleTo(2f, 2f, duration/2),
				Actions.scaleTo(scale, scale, duration/2)
			)));

			root.addActor(table);
		}
		{
			float scale = 1.25f;
			Table table = new Table();
			table.add(new Label("hello 1.25 ", style));
			table.setScale(scale);
			table.setTransform(true);
			table.pack();
			table.setOrigin(center);
			setPosition(table, cx, cy - 100, center);
			table.debugAll();
			table.addAction(Actions.forever(Actions.sequence(
				Actions.scaleTo(1f, 1f, duration/2),
				Actions.scaleTo(scale, scale, duration/2)
			)));

			root.addActor(table);
		}
		clear.set(Color.GRAY);
	}

	private void setPosition (Actor actor, float x, float y, int alignment ) {
		float width = actor.getWidth() * actor.getScaleX();
		if ((alignment & right) != 0)
			x -= width;
		else if ((alignment & left) == 0) //
			x -= width / 2;

		float height = actor.getHeight() * actor.getScaleY();
		if ((alignment & top) != 0)
			y -= height;
		else if ((alignment & bottom) == 0) //
			y -= height / 2;

		actor.setPosition(x, y);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();

		renderer.setProjectionMatrix(guiCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.MAGENTA);
		float cx = guiCamera.viewportWidth / 2;
		float cy = guiCamera.viewportHeight / 2;
		x(renderer, cx, cy + 100, 16);
		x(renderer, cx, cy, 16);
		x(renderer, cx, cy - 100, 16);
		renderer.end();

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			rebuild();
		}
	}

	private void x (ShapeRenderer renderer, float x, float y, float size) {
		renderer.line(x - size/2, y, x + size/2, y);
		renderer.line(x, y - size/2, x, y + size/2);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UITableScaleAlignPositionTest.class);
	}
}
