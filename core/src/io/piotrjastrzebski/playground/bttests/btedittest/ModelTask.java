package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskConstraint;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ModelTask<E> implements Pool.Poolable, Iterable<ModelTask<E>> {
	public enum Type {
		BRANCH, DECORATOR, LEAF
	}

	protected Type type;
	protected Task<E> task;
	// NOTE: if parent is null, this is a root task
	protected ModelTask<E> parent;
	protected Array<ModelTask<E>> children = new Array<>();
	protected int minChildCount = 0;
	protected int maxChildCount = Integer.MAX_VALUE;
	protected Array<StatusListener> listeners = new Array<>();
	protected Pool<ModelTask<E>> pool;
	protected ModelTree<E> tree;

	public ModelTask (Pool<ModelTask<E>> pool, ModelTree<E> tree) {
		this.pool = pool;
		this.tree = tree;
	}

	protected ModelTask<E> init (ModelTask<E> parent, Task<E> task) {
		this.parent = parent;
		this.task = task;
		tree.map(task, this);
		if (task instanceof BranchTask) {
			type = Type.BRANCH;
		} else if (task instanceof Decorator) {
			type = Type.DECORATOR;
		} else if (task instanceof LeafTask) {
			type = Type.LEAF;
		}

		Annotation annotation = ClassReflection.getAnnotation(task.getClass(), TaskConstraint.class);
		if (annotation != null) {
			TaskConstraint tc = annotation.getAnnotation(TaskConstraint.class);
			minChildCount = tc.minChildren();
			maxChildCount = tc.maxChildren();
		}

		// TODO get fields with @TaskAttribute

		children.clear();
		for (int i = 0; i < task.getChildCount(); i++) {
			Task<E> child = task.getChild(i);
			children.add(pool.obtain().init(this, child));
		}

		return this;
	}

	public void update (Task.Status previousStatus) {
		for (StatusListener listener : listeners) {
			listener.statusChanged(previousStatus, task.getStatus());
		}
	}

	public Task.Status getStatus () {
		if (task == null) return null;
		return task.getStatus();
	}

	/**
	 * Possible min count of children for this task
	 */
	public int getMinChildrenCount () {
		return minChildCount;
	}

	/**
	 * Possible max count of children for this task
	 */
	public int getMaxChildrenCount () {
		return maxChildCount;
	}

	/**
	 * If this task is valid, ie has proper count of children and all of its children
	 */
	public boolean isValid() {
		boolean valid = minChildCount <= children.size && children.size <= maxChildCount;
		if (!valid) return false;

		for (ModelTask<E> child : children) {
			if (!child.isValid()) return false;
		}
		return true;
	}

	@Override public void reset () {
		parent = null;
		task = null;
		pool.freeAll(children);
		children.clear();
	}

	public boolean isRoot () {
		return parent == null;
	}

	@Override public String toString () {
		return "ModelTask{" +
			"type=" + type +
			", task=" + task.getClass().getSimpleName() +
			", children=" + children.size +
			'}';
	}

	public String getName () {
		return task.getClass().getSimpleName();
	}

	@Override public Iterator<ModelTask<E>> iterator () {
		return children.iterator();
	}

	@Override public void forEach (Consumer<? super ModelTask<E>> action) {
		children.forEach(action);
	}

	@Override public Spliterator<ModelTask<E>> spliterator () {
		return children.spliterator();
	}

	public void addListener(StatusListener listener) {
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
		}
	}

	public void removeListener(StatusListener listener) {
		listeners.removeValue(listener, true);
	}

	public interface StatusListener {
		void statusChanged(Task.Status from, Task.Status to);
	}
}
