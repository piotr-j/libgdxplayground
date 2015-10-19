package io.piotrjastrzebski.playground.bttests.btedittest2.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * Maps Task class to archetype, used by model to get new instances of tasks to add them to the tree
 * @param <E> type of the blackboard object in the task, same as in the model
 */
public class TaskLibrary<E> {
	private final static String TAG = io.piotrjastrzebski.playground.bttests.simplishedittest.TaskLibrary.class.getSimpleName();

	private ObjectMap<Class<? extends Task>, Task<E>> classToInstance;

	protected TaskLibrary () {
		classToInstance = new ObjectMap<>();
	}

	/**
	 * @param aClass type of {@link Task} to add, will be instantiated via reflection
	 */
	public void add (Class<? extends Task> aClass) {
		if (aClass == null)
			throw new IllegalArgumentException("Task class  cannot be null!");
		try {
			Task<E> task = ClassReflection.newInstance(aClass);
			classToInstance.put(aClass, task);
		} catch (ReflectionException e) {
			Gdx.app.error(TAG, "Failed to create task of type " + aClass , e);
		}
	}

	/**
	 * @param task instance of {@link Task} to add
	 */
	public void add (Task<E> task) {
		if (task == null)
			throw new IllegalArgumentException("Task cannot be null!");
		classToInstance.put(task.getClass(), task);
	}

	/**
	 * @param aClass type of {@link Task}
	 * @return cloned task, via {@link Task#cloneTask()} or null
	 */
	public Task<E> get (Class<? extends Task> aClass) {
		if (aClass == null)
			throw new IllegalArgumentException("Task class cannot be null!");
		Task<E> arch = classToInstance.get(aClass, null);
		if (arch != null)
			return arch.cloneTask();
		return null;
	}

	/**
	 * @param aClass type of {@link Task}
	 * @return actual instance of the {@link Task}
	 */
	public Task<E> getArchetype (Class<? extends Task> aClass) {
		if (aClass == null)
			throw new IllegalArgumentException("Task class cannot be null!");
		return classToInstance.get(aClass, null);
	}

	/**
	 * @param aClass type of {@link Task}
	 * @return if this type has been added
	 */
	public boolean has (Class<? extends Task> aClass) {
		if (aClass == null)
			throw new IllegalArgumentException("Task class cannot be null!");
		return classToInstance.containsKey(aClass);
	}

	/**
	 * @param aClass type of {@link Task} to remove
	 * @return removed {@link Task} or null
	 */
	public Task<E> remove (Class<? extends Task> aClass) {
		if (aClass == null)
			throw new IllegalArgumentException("Task class cannot be null!");
		return classToInstance.remove(aClass);
	}

	/**
	 * Remove all added {@link Task}s
	 */
	public void clear () {
		classToInstance.clear();
	}
}
