package io.piotrjastrzebski.playground.bttests.simplishedittest;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * Created by PiotrJ on 15/10/15.
 */
abstract class TaskAction implements Pool.Poolable {
	protected static Logger logger;

	protected Task task;
	protected Task target;

	public static void setLogger(Logger logger) {
		TaskAction.logger = logger;
	}

	private static void log(String tag, String msg) {
		if (logger != null) logger.log(tag, msg);
	}

	private static void error(String tag, String msg) {
		if (logger != null) logger.error(tag, msg);
	}

	private static void error(String tag, String msg, Exception e) {
		if (logger != null) logger.error(tag, msg, e);
	}

	public TaskAction init (Task target, Task task) {
		if (target == null)
			throw new IllegalArgumentException("Target cannot be null");
		if (task == null)
			throw new IllegalArgumentException("Task cannot be null");

		this.task = task;
		this.target = target;

		return this;
	}

	@Override public void reset () {
		task = null;
		target = null;
	}

	abstract boolean execute ();

	public static class Add extends TaskAction {
		@Override boolean execute () {
			try {
				// TODO fix logs so they work in tests
				log("ADD", task + " to " + target);
				// we need to check it task is in target before we add, as that will happen on init
				if (target instanceof BranchTask) {
					Field field = ClassReflection.getDeclaredField(BranchTask.class, "children");
					field.setAccessible(true);
					Array children = (Array)field.get(target);
					if (!children.contains(task, true)) {
						target.addChild(task);
						return true;
					}
				} else if (target instanceof Decorator) {
					Field field = ClassReflection.getDeclaredField(Decorator.class, "child");
					field.setAccessible(true);
					Task old = (Task)field.get(target);
					if (old == null) {
						field.set(target, task);
					} else if (old != task) {
						log("ADD", "Replace " + old + " with " + task);
						field.set(target, task);
					}
					return true;
				} else {
					error("ADD", "cannot add " + task + " to " + target + " as its a leaf");
				}
			} catch (ReflectionException e) {
				error("REMOVE", "ReflectionException error", e);
			}
			return false;
		}
	}

	public static class Insert extends TaskAction {
		protected int at;

		public TaskAction init (Task target, Task task, int at) {
			super.init(target, task);
			if (at < 0)
				throw new IllegalArgumentException("at cannot be < 0, is " + at);
			this.at = at;
			return this;
		}

		@Override boolean execute () {
			try {
				log("INSERT", task + " to " + target + " at " + at);
				// we need to check it task is in target before we add, as that will happen on init
				if (target instanceof BranchTask) {
					Field field = ClassReflection.getDeclaredField(BranchTask.class, "children");
					field.setAccessible(true);
					Array children = (Array)field.get(target);
					// disallow if out of bounds,  allow to insert if empty
					if (at > children.size && at > 0) {
						error("INSERT", "cannot insert " + task + " to " + target + " at " + at + " as its out of range");
						return false;
					}
					if (!children.contains(task, true)) {
						// if insert at end, add as we cant use array.insert
						if (children.size == at) {
							children.add(task);
						} else {
							children.insert(at, task);
						}
					} else {
						error("INSERT", "cannot insert " + task + " to " + target + " at " + at + ", target already contains task");
						return false;
					}
					return true;
				} else if (target instanceof Decorator) {
					// can insert if decorator is empty
					Field field = ClassReflection.getDeclaredField(Decorator.class, "child");
					field.setAccessible(true);
					Object old = field.get(target);
					if (old == null && at == 0) {
						field.set(target, task);
						return true;
					} else {
						error("INSERT", "cannot insert " + task + " to " + target + " as its a decorator");
					}
				} else {
					error("INSERT", "cannot insert " + task + " to " + target + " as its a leaf");
				}
			} catch (ReflectionException e) {
				error("REMOVE", "ReflectionException error", e);
			}
			return false;
		}

		@Override public void reset () {
			super.reset();
			at = 0;
		}
	}

	public static class Remove extends TaskAction {
		@Override boolean execute () {
			if (task.getStatus() == Task.Status.RUNNING) {
				task.cancel();
			}
			// remove from bt
			try {
				log("REMOVE", task + " from " + target);
				// we need to check it task is in target before we add, as that will happen on init
				if (target instanceof BranchTask) {
					Field field = ClassReflection.getDeclaredField(BranchTask.class, "children");
					field.setAccessible(true);
					Array children = (Array)field.get(target);
					return children.removeValue(task, true);
				} else if (target instanceof Decorator) {
					error("REMOVE", "cannot add " + task + " to " + target + " as its a leaf");
					Field field = ClassReflection.getDeclaredField(Decorator.class, "child");
					field.setAccessible(true);
					Object old = field.get(target);
					if (old == task || old == null) {
						field.set(target, null);
					} else {
						return false;
					}
					return old != null;
				} else {
					error("REMOVE", "cannot add " + task + " to " + target + " as its a leaf");
				}
			} catch (ReflectionException e) {
				error("REMOVE", "ReflectionException error", e);
			}
			return false;
		}
	}

	public static TaskAction add (Task task, Task target) {
		return new Add().init(task, target);
	}

	public static TaskAction insert (Task task, Task target, int at) {
		return new Insert().init(task, target, at);
	}

	public static TaskAction remove (Task task, Task target) {
		return new Remove().init(task, target);
	}
}
