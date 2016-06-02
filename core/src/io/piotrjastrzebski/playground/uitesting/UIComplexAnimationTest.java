package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIComplexAnimationTest extends BaseScreen {
	Texture texture;
	Image imageA;
	Image imageB;
	Image imageC;
	Image imageD;

	public UIComplexAnimationTest (GameReset game) {
		super(game);

		texture = new Texture("badlogic.jpg");
		imageA = new Image(texture);
		imageA.setPosition(32, 32);
		root.addActor(imageA);
		imageB = new Image(texture);
		imageB.setPosition(Gdx.graphics.getWidth() - imageB.getWidth() - 32, 32);
		imageB.setColor(Color.GREEN);
		root.addActor(imageB);
		imageC = new Image(texture);
		imageC.setPosition(32, Gdx.graphics.getHeight() - (imageB.getHeight() + 32));
		imageC.setColor(Color.RED);
		root.addActor(imageC);
		imageD = new Image(texture);
		imageD.setPosition(Gdx.graphics.getWidth() - (imageB.getWidth() + 32), Gdx.graphics.getHeight() - imageB.getHeight() - 32);
		imageD.setColor(Color.MAGENTA);
		root.addActor(imageD);

		// how do we do some reasonably complex animation with multiple actors dependant on each other?
		// timings are the most relevant part
		Delay total = new Delay();
		imageA.addAction(Actions.sequence(
			Actions.delay(total.value),
			Actions.moveBy(128, 128, delay(.25f, total)),
			Actions.delay(delay(.25f, total)),
			Actions.moveBy(-128, -128, delay(.25f, total))
		));
		imageB.addAction(Actions.sequence(
			Actions.delay(total.value),
			Actions.moveBy(-128, 128, delay(.25f, total)),
			Actions.delay(delay(.25f, total)),
			Actions.moveBy(128, -128, delay(.25f, total))
		));
		imageC.addAction(Actions.sequence(
			Actions.delay(total.value),
			Actions.moveBy(128, -128, delay(.25f, total)),
			Actions.delay(delay(.25f, total)),
			Actions.moveBy(-128, 128, delay(.25f, total))
		));
		imageD.addAction(Actions.sequence(
			Actions.delay(total.value),
			Actions.moveBy(-128, -128, delay(.25f, total)),
			Actions.delay(delay(.25f, total)),
			Actions.moveBy(128, 128, delay(.25f, total))
		));
	}

	private float delay (float delay, Delay total) {
		total.value += delay;
		return delay;
	}

	private static class Delay {
		public float value;
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

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UIComplexAnimationTest.class);
	}
}
