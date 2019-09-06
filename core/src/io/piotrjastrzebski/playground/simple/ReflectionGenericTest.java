package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class ReflectionGenericTest extends BaseScreen {
	public ReflectionGenericTest (GameReset game) {
		super(game);

		TextureRegion[] regions = new TextureRegion[] {new TextureRegion()};
		TextureRegion[] regions2 = new TextureRegion[] {new TextureRegion()};

		Array<TextureRegion> regionArray = new Array<>();
		regionArray.addAll(regions);

		//		Animation<TextureRegion> animation = new Animation<>(.1f, regions);
		Animation<TextureRegion> animation = new Animation<>(.1f, regionArray);
		try {
			Method setKeyFrames = ClassReflection.getDeclaredMethod(Animation.class, "setKeyFrames", Object[].class);
			setKeyFrames.setAccessible(true);
			setKeyFrames.invoke(animation, (Object)regions2);
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
		if (regions2 != animation.getKeyFrames()) {
			throw new AssertionError("regions2 != animation.getKeyFrames()");
		}
		Gdx.app.exit();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, ReflectionGenericTest.class);
	}
}
