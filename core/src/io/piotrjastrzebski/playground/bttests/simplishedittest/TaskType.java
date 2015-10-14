package io.piotrjastrzebski.playground.bttests.simplishedittest;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.Task;

/**
 *
 * Created by EvilEntity on 14/10/2015.
 */
public enum TaskType {
	BRANCH("Branch", 1, Integer.MAX_VALUE),
	DECORATOR("Decorator", 1, 1),
	LEAF("Leaf", 0, 0);

	private String name;
	private int minChildren;
	private int maxChildren;

	TaskType (String name, int min, int max) {
		this.name = name;
		minChildren = min;
		maxChildren = max;
	}

	/**
	 * Check if this count is a valid for this {@link TaskType}
	 * @param count count to check
	 * @return if count is within min and max
	 */
	public boolean isValid (int count) {
		return minChildren <= count && count <= maxChildren;
	}

	/**
	 * Get TaskType for given Task
	 */
	public static TaskType valueFor (Task task) {
		if (task instanceof Decorator) {
			return TaskType.DECORATOR;
		}
		if (task instanceof BranchTask) {
			return TaskType.BRANCH;
		}
		return TaskType.LEAF;
	}

	@Override public String toString () {
		return name + "{" +
			minChildren +
			// do we pretend that oo is infinity?
			", " + (maxChildren == Integer.MAX_VALUE ? "oo" : maxChildren) +
			'}';
	}
}
