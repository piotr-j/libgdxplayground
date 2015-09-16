package io.piotrjastrzebski.playground.gdxai.policyparalleltest;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.utils.Array;

/** A {@code Parallel} is a special branch task that starts or resumes all children every single time, parallel task will succeed
 * if all the children succeed, fail if one of the children fail. Does not wait for other children to finish.
 * The typical use case: make the game entity react on event while
 * sleeping or wandering.
 *
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 *
 * @author implicit-invocation
 * @author davebaol */
public class PolicyParallel<E> extends BranchTask<E> {

	private boolean[] runningTasks;
	private boolean success;
	private boolean noRunningTasks;
	private int currentChildIndex;

	@TaskAttribute
	public boolean sequencePolicy;

	/** Creates a parallel task with no children */
	public PolicyParallel () {
		this(new Array<Task<E>>());
	}

	/** Creates a parallel task with the given children
	 * @param tasks the children */
	public PolicyParallel (Task<E>... tasks) {
		this(new Array<Task<E>>(tasks));
	}

	/** Creates a parallel task with the given children
	 * @param tasks the children */
	public PolicyParallel (Array<Task<E>> tasks) {
		super(tasks);
		this.success = true;
		this.noRunningTasks = true;
	}

	@Override
	public void start () {
		if (runningTasks == null)
			runningTasks = new boolean[children.size];
		else {
			for (int i = 0; i < runningTasks.length; i++)
				runningTasks[i] = false;
		}
		success = true;
	}

	@Override
	public void run () {
		noRunningTasks = true;
		for (currentChildIndex = 0; currentChildIndex < children.size; currentChildIndex++) {
			Task<E> child = children.get(currentChildIndex);
			if (runningTasks[currentChildIndex]) {
				child.run();
			} else {
				child.setControl(this);
				child.start();
				child.run();
			}
		}
	}

	@Override
	public void childRunning (Task<E> task, Task<E> reporter) {
		runningTasks[currentChildIndex] = true;
		noRunningTasks = false;
		control.childRunning(this, this);
	}

	@Override
	public void childSuccess (Task<E> runningTask) {
		runningTasks[currentChildIndex] = false;
		success = success && true;
		if (noRunningTasks && currentChildIndex == children.size - 1) {
			if (success) {
				success();
			} else {
				fail();
			}
		}
	}

	@Override
	public void childFail (Task<E> runningTask) {
		runningTasks[currentChildIndex] = false;
		success = false;
		if (noRunningTasks && currentChildIndex == children.size - 1) {
			fail();
		}
	}

	@Override
	public void reset () {
		super.reset();
		if (runningTasks != null) {
			for (int i = 0; i < runningTasks.length; i++)
				runningTasks[i] = false;
		}
		success = true;
	}

	@Override protected Task<E> copyTo (Task<E> task) {
		PolicyParallel<E> parallel = (PolicyParallel<E>)task;
		parallel.sequencePolicy = sequencePolicy;
		return super.copyTo(task);
	}
}

