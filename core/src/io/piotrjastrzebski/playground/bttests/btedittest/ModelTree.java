package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.ObjectMap;
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
				return new ModelTask<>(this, ModelTree.this);
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

	int step;
	public void step () {
		if (bt == null) return;
		step++;
//		bt.step();
	}

	@Override public void reset () {
		Gdx.app.log(TAG, "reset()");
		if (bt != null) bt.removeListener(this);
		bt = null;
		if (root != null) pool.free(root);
	}

	protected ObjectMap<Task, ModelTask> taskToModel = new ObjectMap<>();
	protected void map (Task<E> task, ModelTask<E> modelTask) {
		taskToModel.put(task, modelTask);
	}

	@Override public void statusUpdated (Task<E> task, Task.Status previousStatus) {
//		if (task.getStatus() == previousStatus) return;
		String name = task.getClass().getSimpleName();
//		Gdx.app.log(TAG, " task " + name + " updated from " + task.getStatus() + " to " + previousStatus);
		ModelTask modelTask = taskToModel.get(task, null);
		if (modelTask != null) {
			modelTask.statusUpdated(previousStatus);
		} else {
			Gdx.app.log(TAG, "Task mapping not found for " + task);
		}
	}

	@Override public void childAdded (Task<E> task, int index) {

	}

	public boolean isValid () {
		return root.isValid();
	}

	public ModelTask<E> getRoot () {
		return root;
	}

	public void remove (ModelTask<E> toRemove) {
		if (root == toRemove) {
			reset();
			return;
		}
		root.remove(toRemove);
		bt.reset();
	}
}
