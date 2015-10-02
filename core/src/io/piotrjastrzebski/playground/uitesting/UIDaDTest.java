package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * We want to figure out a way to drag and drop stuff from one tree to another, to proper places
 * Also change level of a node in a tree and order at same depth
 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/DragAndDropTest.java
 * Created by PiotrJ on 20/06/15.
 */
public class UIDaDTest extends BaseScreen {

	public UIDaDTest (GameReset game) {
		super(game);

	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}
}
