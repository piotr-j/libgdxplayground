package io.piotrjastrzebski.playground.shortcuts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by EvilEntity on 11/09/2015.
 */
public class ShortcutTest extends BaseScreen {
	private static final String TAG = ShortcutTest.class.getSimpleName();
	Shortcuts shortcuts;
	public ShortcutTest (GameReset game) {
		super(game);
		multiplexer.addProcessor(shortcuts = new Shortcuts(this));
	}

	@Shortcuts.Shortcut(Input.Keys.EQUALS)
	public void test01a() {
		Gdx.app.log(TAG, "test01a");
	}

	//	@Shortcuts.Shortcut(Shortcuts.SHIFT | Input.Keys.EQUALS)
	//	@Shortcuts.Shortcut(Input.Keys.EQUALS | Shortcuts.SHIFT)
	@Shortcuts.Shortcut({Input.Keys.PLUS, Input.Keys.Z})
	public void test02() {
		Gdx.app.log(TAG, "test02");
	}
	@Shortcuts.Shortcut(Input.Keys.AT)
	public void test03() {
		Gdx.app.log(TAG, "test03");
	}
	@Shortcuts.Shortcut(Input.Keys.POUND)
	public void test04() {
		Gdx.app.log(TAG, "test04");
	}
	@Shortcuts.Shortcut(Input.Keys.STAR)
	public void test05() {
		Gdx.app.log(TAG, "test05");
	}
	@Shortcuts.Shortcut(Input.Keys.COLON)
	public void test06() {
		Gdx.app.log(TAG, "test06");
	}

	@Shortcuts.Shortcut(Input.Keys.F1)
	public void test1() {
		Gdx.app.log(TAG, "test1");
	}

	@Shortcuts.Shortcut(Input.Keys.F2)
	protected void test2() {
		Gdx.app.log(TAG, "test2");
	}

	@Shortcuts.Shortcut(Input.Keys.F3)
	private void test3() {
		Gdx.app.log(TAG, "test3");
	}

	@Shortcuts.Shortcut(Input.Keys.F4)
	public void test4(String p) {
		Gdx.app.log(TAG, "test4");
	}

	@Shortcuts.Shortcut(Input.Keys.F5)
	protected void test5(int p) {
		Gdx.app.log(TAG, "test5");
	}

	@Shortcuts.Shortcut(Input.Keys.F6)
	private void test6(int p1, int p2) {
		Gdx.app.log(TAG, "test6");
	}


}
