package io.piotrjastrzebski.playground.simple;

import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class ActionTest extends BaseScreen {
	private static final String TAG = ActionTest.class.getSimpleName();

	Func2<Integer, Float, Boolean> func;
	Action2<Integer, Float> action;

	public ActionTest (GameReset game) {
		super(game);
		clear.set(0, 0.05f, 0.1f, 1);

		// 3 cheers for auto boxing...
		func = new Func2<Integer, Float, Boolean>(){
			@Override public Boolean call (Integer p1, Float p2) {
				return false;
			}
		};
		boolean result = func.call(1, 2f);
		action = new Action2<Integer, Float>(){
			@Override public void call (Integer p1, Float p2) {
				// do stuff
			}
		};
		action.call(1, 2f);
	}

	protected abstract static class Func0<R> {
		public abstract R call();
	}

	protected abstract static class Func1<T1, R> {
		public abstract R call(T1 p1);
	}

	protected abstract static class Func2<T1, T2, R> {
		public abstract R call(T1 p1, T2 p2);
	}

	protected abstract static class Func3<T1, T2, T3, R> {
		public abstract R call(T1 p1, T2 p2, T3 p3);
	}

	protected abstract static class Action0 {
		public abstract void call();
	}

	protected abstract static class Action1<T1> {
		public abstract void call(T1 p1);
	}

	protected abstract static class Action2<T1, T2> {
		public abstract void call(T1 p1, T2 p2);
	}

	protected abstract static class Action3<T1, T2, T3> {
		public abstract void call(T1 p1, T2 p2, T3 p3);
	}

	@Override public void render (float delta) {
		super.render(delta);
		enableBlending();

	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, ActionTest.class);
	}
}
