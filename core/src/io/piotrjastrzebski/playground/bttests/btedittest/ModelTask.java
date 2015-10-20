package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskConstraint;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ModelTask<E> implements Pool.Poolable, Iterable<ModelTask<E>> {
	private static final String TAG = ModelTask.class.getSimpleName();

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
	protected Pool<ModelTask<E>> pool;
	protected ModelTree<E> tree;
	private boolean isValid;

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
		validate();
		return this;
	}

	boolean dirty;
	/**
	 * Add task as child
	 */
	public void add (ModelTask<E> modelTask) {
		// TODO how to we handle this?
		// TODO we cant actually add to task if its decorator or leaf, because we will crash...
		// TODO should we add only if the tree validates properly? sounds reasonable
//		task.addChild(modelTask.getTask());
		dirty = true;
		children.add(modelTask);
	}

	/**
	 * Insert task as child at given position
	 */
	public void insert (int at, ModelTask<E> modelTask) {
		dirty = true;
		children.insert(at, modelTask);
	}

	/**
	 * Remove this task from tree
	 */
	public void remove () {
		// TODO same as in insert/add but we can probably set the child in parent to null/remove
		// TODO then tree wont be valid and wont run
	}

	public Task<E> getTask () {
		return task;
	}

	public void remove (ModelTask<E> toRemove) {
		Iterator<ModelTask<E>> it = children.iterator();
		while (it.hasNext()) {
			ModelTask<E> next = it.next();
			if (next == toRemove) {
				removeChild(task, next.task);
				it.remove();
				pool.free(next);
				break;
			} else {
				next.remove(toRemove);
			}
		}
		validate();
	}

	private void removeChild(Task<E> parent, Task<E> child) {
		try {
			if (child.getStatus() == Task.Status.RUNNING) {
				child.cancel();
			}
			// hacky, gotta wait for removeTask
			if (parent instanceof BranchTask) {
				Field field = ClassReflection.getDeclaredField(BranchTask.class, "children");
				field.setAccessible(true);
				Array<Task> children = (Array<Task>)field.get(parent);
				children.removeValue(child, true);
			} else if (parent instanceof Decorator) {
				Field field = ClassReflection.getDeclaredField(Decorator.class, "child");
				field.setAccessible(true);
				field.set(parent, null);
			} else {
				Gdx.app.error(TAG, "Invalid parent type " + parent);
			}
		} catch (ReflectionException e) {
			Gdx.app.error(TAG, "Failed to remove child from " + parent, e);
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
		return isValid;
	}

	public boolean validate () {
		boolean valid = minChildCount <= children.size && children.size <= maxChildCount;
		if (!valid) {
			setValid(false);
			return false;
		}

		for (ModelTask<E> child : children) {
			if (!child.isValid()) {
				setValid(false);
				return false;
			}
		}
		setValid(true);
		return true;
	}

	private void setValid (boolean valid) {
		if (isValid == valid) return;
		isValid = valid;
		tree.validChanged(this, valid);
	}

	@Override public void reset () {
		parent = null;
		task = null;
		isValid = false;
		pool.freeAll(children);
		children.clear();
	}

	public boolean isRoot () {
		return parent == null;
	}

	@Override public String toString () {
		return "ModelTask{" +
			"type=" + type +
			", task=" + (task==null?"<no-task>":task.getClass().getSimpleName()) +
			", children=" + children.size +
			'}';
	}

	/**
	 * @return text used for display
	 */
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

	public ModelTask<E> getParent () {
		return parent;
	}
}
