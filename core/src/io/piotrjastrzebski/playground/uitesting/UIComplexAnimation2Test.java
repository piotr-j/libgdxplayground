package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIComplexAnimation2Test extends BaseScreen {
	Texture texture;
	Image imageA;
	Image imageB;
	Image imageC;
	Image imageD;

	public UIComplexAnimation2Test (GameReset game) {
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
		SequenceAction sequenceA = Actions.sequence();
		sequenceA.addAction(Actions.moveBy(128, 128, delay(.25f, total)));
		sequenceA.addAction(Actions.delay(.25f + delay(.25f, total)));
		sequenceA.addAction(Actions.moveBy(-128, -128, .25f));

		SequenceAction sequenceB = Actions.sequence();
		sequenceB.addAction(Actions.delay(total.value));
		sequenceB.addAction(Actions.moveBy(-128, 128, delay(.25f, total)));
		sequenceB.addAction(Actions.delay(.25f + delay(.25f, total)));
		sequenceB.addAction(Actions.moveBy(128, -128, .25f));

		SequenceAction sequenceC = Actions.sequence();
		sequenceC.addAction(Actions.delay(total.value));
		sequenceC.addAction(Actions.moveBy(128, -128, delay(.25f, total)));
		sequenceC.addAction(Actions.delay(.25f + delay(.25f, total)));
		sequenceC.addAction(Actions.moveBy(-128, 128, .25f));

		SequenceAction sequenceD = Actions.sequence();
		sequenceD.addAction(Actions.delay(total.value));
		sequenceD.addAction(Actions.moveBy(-128, -128, delay(.25f, total)));
		sequenceD.addAction(Actions.delay(.25f + delay(.25f, total)));
		sequenceD.addAction(Actions.moveBy(128, 128, .25f));


		imageA.addAction(sequenceA);
		imageB.addAction(sequenceB);
		imageC.addAction(sequenceC);
		imageD.addAction(sequenceD);

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
		PlaygroundGame.start(args, UIComplexAnimation2Test.class);
	}
}
