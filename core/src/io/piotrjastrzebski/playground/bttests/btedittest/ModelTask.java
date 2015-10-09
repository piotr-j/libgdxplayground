package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ModelTask<E> implements Pool.Poolable {
	public static final int ANY = -1;
	public static final int GREATER_THAN_0 = -2;

	public enum Type {
		BRANCH(GREATER_THAN_0), DECORATOR(1), LEAF(0);
		private int children;
		Type (int children) {
			this.children = children;
		}

		public boolean isValid(int count) {
			if (children == -1) return true;
			if (children == -2) return count > 0;
			return children == count;
		}
	}

	protected Type type;
	protected Task<E> task;
	// if parent is null, this is a root task
	protected ModelTask<E> parent;
 	protected Array<ModelTask<E>> children = new Array<>();

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
		children.clear();
		for (int i = 0; i < task.getChildCount(); i++) {
			Task<E> child = task.getChild(i);
			children.add(Pools.obtain(ModelTask.class).init(this, child));
		}

		return this;
	}

	/**
	 * Possible count of children for this task, -1 for "infinite"
	 */
	public int getChildrenCount () {
		return type.children;
	}

	/**
	 * If this task is valid, ie has proper count of children
	 */
	public boolean isValid() {
		return type.isValid(children.size);
	}

	@Override public void reset () {
		parent = null;
		task = null;
		Pools.freeAll(children, true);
		children.clear();
	}
}
