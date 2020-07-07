package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class LongPressTest extends BaseScreen {
	private static final String TAG = LongPressTest.class.getSimpleName();

	public LongPressTest (GameReset game) {
		super(game);
		GestureDetector gd = new GestureDetector(new GestureDetector.GestureAdapter(){
			long tdt;
			@Override public boolean touchDown (float x, float y, int pointer, int button) {
				tdt = TimeUtils.millis();
				return super.touchDown(x, y, pointer, button);
			}

			@Override public boolean longPress (float x, float y) {
				long dt = TimeUtils.millis() - tdt;
				Gdx.app.log("", "Long press after " + (dt /1000f) + "s");
				return super.longPress(x, y);
			}
		});
		gd.setLongPressSeconds(2);
		Gdx.input.setInputProcessor(gd);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, LongPressTest.class);
	}
}
