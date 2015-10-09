package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskConstraint;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ModelTask<E> implements Pool.Poolable {
	public enum Type {
		BRANCH, DECORATOR, LEAF;
	}
	protected Type type;
	protected Task<E> task;
	// if parent is null, this is a root task
	protected ModelTask<E> parent;
 	protected Array<ModelTask<E>> children = new Array<>();
	protected int minChildCount = 0;
	protected int maxChildCount = Integer.MAX_VALUE;

	protected ModelTask<E> init (ModelTask<E> parent, Task<E> task) {
		this.parent = parent;
		this.task = task;
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

		children.clear();
		for (int i = 0; i < task.getChildCount(); i++) {
			Task<E> child = task.getChild(i);
			children.add(Pools.obtain(ModelTask.class).init(this, child));
		}

		return this;
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
	 * If this task is valid, ie has proper count of children
	 */
	public boolean isValid() {
		return minChildCount <= children.size && children.size <= maxChildCount;
	}

	@Override public void reset () {
		parent = null;
		task = null;
		Pools.freeAll(children, true);
		children.clear();
	}
}
