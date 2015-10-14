package io.piotrjastrzebski.playground.bttests.simplishedittest;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by EvilEntity on 14/10/2015.
 */
public class BTModel<E> implements Pool.Poolable {

	private BehaviorTree<E> bt;
	private boolean valid;
	private BTTask<E> root;

	public BTModel () {

	}

	public void init(BehaviorTree<E> bt) {
		if (this.bt != null) reset();
		this.bt = bt;
		// TODO pool all the things
		root = new BTTask<>();
		root.init(bt.getChild(0));
		valid = root.isValid;
	}

	public void step () {
		if (!isValid())
			return;
		bt.step();
	}

	public boolean canAdd () {

		return false;
	}

	public void add () {
		if (!canAdd()) return;

	}

	public boolean canMove () {

		return false;
	}

	public void move () {
		if (!canMove()) return;

	}

	public void remove () {
		// we can always remove stuff

	}

	public boolean validate () {
		if (root == null) {
			return valid = false;
		}
		return valid = root.validate();
	}

	public boolean isValid () {
		return valid;
	}

	@Override public void reset () {
		if (root != null) root.reset();
		root = null;
		valid = false;
	}

	@Override public String toString () {
		return "BTModel{" +
			"bt=" + bt +
			", valid=" + valid +
			'}';
	}

	public static class BTTask<E> implements Pool.Poolable {
		private Task<E> task;
		private TaskType type;
		private Array<BTTask<E>> children;
		private boolean isValid;

		public BTTask () {
			children = new Array<>();
		}

		public void init (Task<E> task) {
			if (this.task != null) reset();
			this.task = task;
			type = TaskType.valueFor(task);
			for (int i = 0; i < task.getChildCount(); i++) {
				addChild(task.getChild(i));
			}
			validate();
		}

		@Override public void reset () {
			for (BTTask<E> child : children) {
				child.reset();
			}
			children.clear();
		}

		public int getChildCount() {
			return children.size;
		}

		public int insertChild(int index, Task<E> task) {
			BTTask<E> child = new BTTask<>();
			child.init(task);
			return insertChild(index, child);
		}

		public int insertChild(int index, BTTask<E> child) {
			children.insert(index, child);
			return children.size - 1;
		}

		public int addChild(Task<E> task) {
			BTTask<E> child = new BTTask<>();
			child.init(task);
			return addChild(child);
		}

		public int addChild(BTTask<E> child) {
			children.add(child);
			return children.size - 1;
		}

		public BTTask<E> getChild(int i) {
			return children.get(i);
		}

		public BTTask<E> removeChild (int i) {
			return children.removeIndex(i);
		}

		public BTTask<E> removeChild (BTTask<E> child) {
			children.removeValue(child, true);
			return child;
		}

		public boolean validate () {
			// check if we have correct amount of children
			boolean valid = type.isValid(children.size);
			for (BTTask<E> child : children) {
				if (!child.validate()) {
					valid = false;
				}
			}
			setValid(valid);
			return isValid;
		}

		public void setValid(boolean newValid) {
			if (isValid != newValid) {
				// notify that valid status changed

			}
			isValid = newValid;
		}

		@Override public String toString () {
			return "BTTask{" +
				"task=" + (task!=null?task.getClass().getSimpleName():"null") +
				", type=" + type +
				", valid=" + isValid +
				", children=" + getChildCount() +
				'}';
		}
	}
}
