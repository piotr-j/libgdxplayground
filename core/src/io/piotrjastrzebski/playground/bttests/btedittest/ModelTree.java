package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.Array;
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
	private Array<TaskListener<E>> listeners = new Array<>();

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
		if (bt == null)
			return;
		step++;

//		if (isValid()) {
//			bt.step();
//		}
	}

	@Override public void reset () {
		Gdx.app.log(TAG, "reset()");
		if (bt != null) bt.removeListener(this);
		bt = null;
		if (root != null) pool.free(root);
	}

	protected ObjectMap<Task, ModelTask<E>> taskToModel = new ObjectMap<>();
	protected void map (Task<E> task, ModelTask<E> modelTask) {
		taskToModel.put(task, modelTask);
	}

	@Override public void statusUpdated (Task<E> task, Task.Status previousStatus) {
	ModelTask<E> modelTask = taskToModel.get(task, null);
		if (modelTask != null) {
			for (TaskListener<E> listener : listeners) {
				listener.statusChanged(modelTask, previousStatus, task.getStatus());
			}
		} else {
			Gdx.app.log(TAG, "Task mapping not found for " + task);
		}
	}

	protected void validChanged (ModelTask<E> task, boolean valid) {
		for (TaskListener<E> listener : listeners) {
			listener.validChanged(task, valid);
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

	public interface TaskListener<E> {
		void statusChanged(ModelTask<E> task, Task.Status from, Task.Status to);
		void validChanged(ModelTask<E> task, boolean valid);
	}

	public void addListener(TaskListener<E> listener) {
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
		}
	}

	public void removeListener(TaskListener<E> listener) {
		listeners.removeValue(listener, true);
	}
}
