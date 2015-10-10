package io.piotrjastrzebski.playground.bttests.btreeserializationtest;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;

/**
 * Created by EvilEntity on 13/07/2015.
 */
public class MyBTree<E> extends BehaviorTree<E> {
	public MyBTree () {
	}

	public MyBTree (Task<E> rootTask, E object) {
		super(rootTask, object);
	}

//	public Task<E> getRunningTask() {
//		return runningTask;
//	}
}
