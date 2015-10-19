package io.piotrjastrzebski.playground.bttests.btedittest2.model;

import com.badlogic.gdx.ai.btree.Task;

/**
 * Created by PiotrJ on 15/10/15.
 */
public interface BTModelListener<E> {
	void statusChanged(BTTask<E> task, Task.Status from, Task.Status to);
	void validityChanged(BTTask<E> task, boolean isValid);
}
