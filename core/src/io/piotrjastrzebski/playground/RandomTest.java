package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

public class RandomTest extends BaseScreen {
	private final static String TAG = RandomTest.class.getSimpleName();

	public int runs = 1000000;

	private int m1, m2, m3, m4, m5;
	public RandomTest (PlaygroundGame game) {
		super(game);
		Gdx.app.log(TAG, "Rolling " + runs + " times");
		for (int i = 0; i < runs; i++) {
			int randomer = MathUtils.random(99);
			if(randomer < 80) m1++; //80 % probability
			else if(randomer < 86) m2++; //6 % probability
			else if(randomer < 92) m3++; //6 % probability
			else if(randomer < 98) m4++; //6 % probability
			else m5++; //2 % probability
		}
		Gdx.app.log(TAG, "Results");
		Gdx.app.log(TAG, "m1 " +(m1/(float)runs)*100f + "%, expected 80%");
		Gdx.app.log(TAG, "m2 " +(m2/(float)runs)*100f + "%, expected 6%");
		Gdx.app.log(TAG, "m3 " +(m3/(float)runs)*100f + "%, expected 6%");
		Gdx.app.log(TAG, "m4 " +(m4/(float)runs)*100f + "%, expected 6%");
		Gdx.app.log(TAG, "m5 " +(m5/(float)runs)*100f + "%, expected 2%");
	}
}
