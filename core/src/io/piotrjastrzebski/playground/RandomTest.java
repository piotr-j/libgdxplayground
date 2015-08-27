package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

public class RandomTest extends BaseScreen {
	private final static String TAG = RandomTest.class.getSimpleName();

	public int runs = 10000000;

	private int m1, m2, m3, m4, m5;
	public RandomTest (PlaygroundGame game) {
		super(game);
		Gdx.app.log(TAG, "Running test 1");
		test1();
		Gdx.app.log(TAG, "Running test 2");
		test2();
		Gdx.app.log(TAG, "Running test 3");
		test3();
	}

	private void test1() {
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
		Gdx.app.log(TAG, "m2 " + (m2 / (float)runs) * 100f + "%, expected 6%");
		Gdx.app.log(TAG, "m3 " +(m3/(float)runs)*100f + "%, expected 6%");
		Gdx.app.log(TAG, "m4 " +(m4/(float)runs)*100f + "%, expected 6%");
		Gdx.app.log(TAG, "m5 " + (m5 / (float)runs) * 100f + "%, expected 2%");
	}

	private void test2() {
		int[] prob = {80, 6, 6, 6, 2};
		int[] choices = {0, 0, 0, 0, 0};
		Gdx.app.log(TAG, "Rolling " + runs + " times");
		for (int i = 0; i < runs; i++) {
			int roll = MathUtils.random(1, 100);
			int sum = 0;
			for (int j = 0; j < choices.length; j++) {
				sum += prob[j];
				if (roll <= sum) {
					choices[j]++;
					break;
				}
			}
		}
		Gdx.app.log(TAG, "Results");
		for (int i = 0; i < choices.length; i++) {
			Gdx.app.log(TAG, "m" + i + " " +(choices[i]/(float)runs)*100f + "%, expected "+prob[i]+"%");
		}
	}

	private void test3() {
		float[] prob = {7.5f, 4.5f, 80f, 7.5f, 0.5f};
		int[] choices = {0, 0, 0, 0, 0};
		Gdx.app.log(TAG, "Rolling " + runs + " times");
		for (int run = 0; run < runs; run++) {
			float roll = MathUtils.random(100f);
			float sum = 0;
			for (int choice = 0; choice < choices.length; choice++) {
				sum += prob[choice];
				if (roll <= sum) {
					choices[choice]++;
					break;
				}
			}
		}
		Gdx.app.log(TAG, "Results");
		for (int choice = 0; choice < choices.length; choice++) {
			float percent = (choices[choice]/(float)runs)*100f;
			Gdx.app.log(TAG, "m" + choice + " " + percent + "%, expected "+prob[choice]+"%");
		}
	}
}
