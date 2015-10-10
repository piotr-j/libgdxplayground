package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Pool;
import com.kotcrab.vis.ui.widget.VisTree;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ViewTree<E> extends VisTree implements Pool.Poolable {
	private static final String TAG = ViewTree.class.getSimpleName();
	protected Pool<ViewTask<E>> pool;
	protected ModelTree<E> model;

	public ViewTree () {
		pool = new Pool<ViewTask<E>>() {
			@Override protected ViewTask<E> newObject () {
				return new ViewTask<>(this);
			}
		};
		// single selection makes thing simpler for edit
		getSelection().setMultiple(false);
		addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				ViewTree.this.changed((ViewTask<E>)getSelection().getLastSelected());
			}
		});
	}

	public void update (float delta) {
		if (viewRoot == null) return;
		viewRoot.update(delta);
	}

	private void changed (ViewTask<E> selection) {
		if (selection != null) {
			Gdx.app.log(TAG, "Selected " + selection);
		} else {
			Gdx.app.log(TAG, "Deselected ");
		}
	}

	ViewTask<E> viewRoot;
	public void init (ModelTree<E> model) {
		if (this.model != null) reset();
		this.model = model;
		ModelTask<E> root = model.getRoot();
		add(viewRoot = pool.obtain().init(root));
		for (ModelTask<E> task : root) {
			addViewTask(viewRoot, task);
		}
		expandAll();
	}

	protected void addViewTask (ViewTask<E> parent, ModelTask<E> task) {
		ViewTask<E> node = pool.obtain().init(task);
		parent.add(node);
		for (ModelTask<E> child : task) {
			addViewTask(node, child);
		}
	}

	@Override public void reset () {

	}
}
