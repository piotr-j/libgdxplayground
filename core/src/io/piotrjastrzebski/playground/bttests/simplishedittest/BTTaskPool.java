package io.piotrjastrzebski.playground.bttests.simplishedittest;

/**
 * Created by PiotrJ on 15/10/15.
 */
public interface BTTaskPool<E> {
	BTTask<E> obtain ();

	void free (BTTask<E> task);
}
