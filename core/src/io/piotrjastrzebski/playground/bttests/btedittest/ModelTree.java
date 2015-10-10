package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ModelTree<E> implements Pool.Poolable, BehaviorTree.Listener<E> {
	private static final String TAG = ModelTree.class.getSimpleName();
	protected BehaviorTree<E> bt;
	protected ModelTask<E> root;
	protected Pool<ModelTask<E>> pool;

	public ModelTree () {
		pool = new Pool<ModelTask<E>>() {
			@Override protected ModelTask<E> newObject () {
				// we want a pool so we can get children
				return new ModelTask<>(this);
			}
		};
	}

	public void init (BehaviorTree<E> bt) {
		if (this.bt != null) reset();
		this.bt = bt;
		root = pool.obtain();
		root.init(null, bt.getChild(0));
		bt.addListener(this);
	}

	@Override public void reset () {
		if (bt != null) bt.removeListener(this);
		bt = null;
		if (root != null) pool.free(root);
	}

	@Override public void statusUpdated (Task<E> task, Task.Status previousStatus) {
		if (task.getStatus() != previousStatus) {
			Gdx.app.log(TAG, " task " + task + " updated from " + task.getStatus() + " to " + previousStatus);
		}
	}

	@Override public void childAdded (Task<E> task, int index) {

	}
}
