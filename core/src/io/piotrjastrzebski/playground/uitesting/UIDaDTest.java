package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * We want to figure out a way to drag and drop stuff from one tree to another, to proper places
 * Also change level of a node in a tree and order at same depth
 * Largish actors in nodes
 *
 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/DragAndDropTest.java
 * Created by PiotrJ on 20/06/15.
 */
public class UIDaDTest extends BaseScreen {
	VisWindow window;
	VisTree tree;

	VisTextButton rebuild;

	public UIDaDTest (GameReset game) {
		super(game);

		window = new VisWindow("BTE");
		stage.addActor(window);
		window.setSize(500, 500);
		window.centerWindow();

		rebuild = new VisTextButton("Rebuild");
		rebuild.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				rebuild();
			}
		});
		window.add(rebuild).row();

		tree = new VisTree();
		window.add(tree);
		rebuild();
	}

	private void rebuild() {
		tree.clearChildren();
		Tree.Node node = new Tree.Node(new VisLabel("welp A!"));
		node.add(new Tree.Node(new VisLabel("welp 1!")));
		node.add(new Tree.Node(new VisLabel("welp 2!")));
		tree.add(node);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}
}
