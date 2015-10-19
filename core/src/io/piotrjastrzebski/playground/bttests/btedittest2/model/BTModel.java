package io.piotrjastrzebski.playground.bttests.btedittest2.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Model that represents {@link BehaviorTree} so it can be edited
 *
 * Stuff we want to do:
 * modify the tree
 *  - remove any node
 *  - move any node to a another valid node, ie not inside own children
 *  - add new node at specified place
 * validate the tree, node child count must be valid before the tree is running
 * if model is valid, update the underlying {@link BehaviorTree}
 * notify observers that node status changed, valid, position in tree, removed etc
 * edit params of {@link Task}s
 *
 * Created by EvilEntity on 14/10/2015.
 */
public class BTModel<E> implements Pool.Poolable, BTTaskPool<E>, BehaviorTree.Listener<E>, BTTask.ValidChangeListener<E> {
	private final static String TAG = BTModel.class.getSimpleName();

	private TaskLibrary<E> taskLibrary;
	private BehaviorTree<E> bt;
	private boolean valid;
	private boolean dirty;
	private BTTask<E> root;

	public BTModel () {
		taskLibrary = new TaskLibrary<>();
	}

	public void init(BehaviorTree<E> bt) {
		if (this.bt != null) reset();
		this.bt = bt;
		// TODO pool all the things
		root = obtain();
		root.init(bt.getChild(0));
		valid = root.isValid();
		if (valid) root.executePending();
		bt.addListener(this);
	}

	public void step () {
		if (!isValid()) {
			log(TAG, "invalid");
			return;
		}
		if (dirty) {
			log(TAG, "dirty, reset bt");
			dirty = false;
			bt.reset();
		}
		bt.step();
	}

	public boolean checkAdd(BTTask<E> target, Task<E> task) {
		return checkAdd(target, task.getClass());
	}

	public boolean checkAdd(BTTask<E> target, Class<? extends Task> task) {
		// we allow adding if target is not a Leaf
		return !TaskType.LEAF.equals(target.getType());
	}

	public BTTask<E> add (BTTask<E> target, Class<? extends Task> task) {
		Task<E> eTask = taskLibrary.get(task);
		if (eTask == null) {
			error(TAG, task + " is not a registered task type, register it via TaskLibrary#add()}");
			return null;
		}
		return add(target, eTask);
	}


	public BTTask<E> add (BTTask<E> target, Task<E> task) {
		if (!checkAdd(target, task)) {
			error(TAG, task + " is not a valid add target to " + target);
			return null;
		}
		BTTask<E> node = obtain();
		node.init(task);
		target.addChild(task);

		validate();
		dirty = true;
		return node;
 	}

	public boolean checkInsert(BTTask<E> target, Task<E> task, int at) {
		return checkInsert(target, task.getClass(), at);
	}

	public boolean checkInsert(BTTask<E> target, Class<? extends Task> task, int at) {
		// we allow inserting if target is not a Leaf
		return !TaskType.LEAF.equals(target.getType());
	}

	public BTTask<E> insert (BTTask<E> target, Class<? extends Task> task, int at) {
		Task<E> eTask = taskLibrary.get(task);
		if (eTask == null) {
			error(TAG, task + " is not a registered task type, register it via TaskLibrary#add()}");
			return null;
		}
		return insert(target, eTask, at);
	}

	public BTTask<E> insert (BTTask<E> target, Task<E> task, int at) {
		if (!checkInsert(target, task, at)) {
			error(TAG, task + " is not a valid insert target to " + target + " at " + at);
			return null;
		}
		BTTask<E> node = obtain();
		node.init(task);
		target.insertChild(at, task);

		validate();
		dirty = true;
		return node;
	}

	public BTTask<E> remove (Task<E> target) {
		BTTask<E> task = findBTTask(target);
		if (task == null) {
			error(TAG, target + " not in the mode!");
			return null;
		}
		return remove(task);
	}

	public BTTask<E> remove (BTTask<E> target) {
		BTTask<E> parent = target.getParent();
		// root has null parent
		if (parent == null) {
			if (target == root) {
				reset();
			} else {
				error(TAG, "Target is not part of the model!");
			}
			return target;
		}
		parent.removeChild(target);
		validate();
		dirty = true;
		return target;
	}

	private BTTask<E> findBTTask (Task<E> target) {
		return root.find(target);
	}

	@Override
	public BTTask<E> obtain () {
		BTTask<E> ebtTask = new BTTask<>(this);
		ebtTask.setChangeListener(this);
		return ebtTask;
	}

	@Override
	public void free (BTTask<E> task) {
		task.reset();
	}

	public boolean validate () {
		if (root == null) {
			return valid = false;
		}
		valid = root.validate();
		if (valid) {
			// execute pending, modify wrapped behaviour tree
			root.executePending();
		}
		return valid;
	}

	public boolean isValid () {
		return valid;
	}

	@Override public void reset () {
		if (root != null) root.reset();
		root = null;
		valid = false;
		if (bt != null) {
			bt.removeListener(this);
			bt = null;
		}
	}

	@Override public String toString () {
		return "BTModel{" +
			"bt=" + bt +
			", valid=" + valid +
			'}';
	}

	public TaskLibrary<E> getTaskLibrary () {
		return taskLibrary;
	}

	public BTTask<E> getRootNode () {
		return root;
	}

	Array<BTModelListener<E>> listeners = new Array<>();
	public void addListener(BTModelListener<E> listener) {
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
		}
	}

	public void removeListener(BTModelListener<E> listener) {
		listeners.removeValue(listener, true);
	}


	private void error (String tag, String msg) {
		Gdx.app.error(tag, msg);
	}

	private void log (String tag, String msg) {
		Gdx.app.log(tag, msg);
	}

	@Override public void statusUpdated (Task<E> task, Task.Status previousStatus) {
		BTTask<E> btTask = findBTTask(task);
		if (btTask == null) {
			error(TAG, "ModelTask not found for " + task);
			return;
		}
		for (BTModelListener<E> listener : listeners) {
			listener.statusChanged(btTask, previousStatus, btTask.getStatus());
		}

	}

	@Override public void childAdded (Task<E> task, int index) {

	}

	@Override public void validChanged (BTTask<E> task, boolean isValid) {
		for (BTModelListener<E> listener : listeners) {
			listener.validityChanged(task, isValid);
		}
	}
}
