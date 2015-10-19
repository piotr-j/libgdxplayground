package io.piotrjastrzebski.playground.bttests.btedittest2.model;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import io.piotrjastrzebski.playground.bttests.dog.*;
import io.piotrjastrzebski.playground.bttests.simplishedittest.BTModel;
import io.piotrjastrzebski.playground.bttests.simplishedittest.BTTask;
import io.piotrjastrzebski.playground.bttests.simplishedittest.TaskType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by PiotrJ on 15/10/15.
 */
public class BTModelTest {
	BTModel<Dog> model;
	BehaviorTree<Dog> tree;
	BTTask<Dog> root;

	@Before public void setUp () throws Exception {
		tree = new BehaviorTree<>(createDogBehavior());
		tree.setObject(new Dog("Dog A"));
		model = new BTModel<>();
		model.init(tree);
		root = model.getRootNode();
	}

	@After public void tearDown () throws Exception {

	}

	@Test public void testInit () {
		assertTrue(model.validate());
		BTTask<Dog> root = model.getRootNode();
		Task<Dog> task = root.getChild(0).getTask();
		assertEquals(Task.Status.FRESH, task.getStatus());
		model.step();
		assertNotEquals(Task.Status.FRESH, task.getStatus());
	}

	@Test public void checkAdd () {
		boolean valid = model.checkAdd(root, BarkTask.class);
		assertTrue(valid);

		BTTask<Dog> seqBark = root.getChild(1).getChild(0);
		valid = model.checkAdd(seqBark, BarkTask.class);
		assertFalse(valid);
	}

	@Test public void addValid () {
		// add stuff, verify its there and tree is valid
		CareTask careTask = new CareTask();

		BTTask<Dog> parallel = root.getChild(0);
		model.add(parallel, careTask);

		assertEquals(careTask, parallel.getChild(2).getTask());
		assertTrue(model.isValid());
	}

	@Test public void addInvalid () {
		// add invalid stuff, verify that tree is invalid
		CareTask careTask = new CareTask();

		BTTask<Dog> fail = root.getChild(0).getChild(1);
		model.add(fail, careTask);
		assertEquals(2, fail.getChildCount());
		assertFalse(model.isValid());
	}

	@Test public void addInvalidCantAdd () {
		// add invalid stuff, verify that tree is invalid
		CareTask careTask = new CareTask();

		BTTask<Dog> care = root.getChild(0).getChild(0);
		model.add(care, careTask);
		assertEquals(0, care.getChildCount());
		assertTrue(model.isValid());
	}

	@Test public void insertValidFirst () {
		// insert stuff, verify its there
		CareTask careTask = new CareTask();

		BTTask<Dog> seq = root.getChild(1);
		model.insert(seq, careTask, 0);
		assertEquals(3, seq.getChildCount());
		assertEquals(careTask, seq.getChild(0).getTask());
	}

	@Test public void insertValidMid () {
		// insert stuff, verify its there
		CareTask careTask = new CareTask();

		BTTask<Dog> seq = root.getChild(1);
		model.insert(seq, careTask, 1);
		assertEquals(3, seq.getChildCount());
		assertEquals(careTask, seq.getChild(1).getTask());
	}

	@Test public void insertValidLast () {
		// insert stuff, verify its there
		CareTask careTask = new CareTask();

		BTTask<Dog> seq = root.getChild(1);
		model.insert(seq, careTask, 2);
		assertEquals(3, seq.getChildCount());
		assertEquals(careTask, seq.getChild(2).getTask());
	}

	@Test public void insertInvalidFirst () {
		// insert invalid stuff, verify its not there
		CareTask careTask = new CareTask();

		BTTask<Dog> fail = root.getChild(0).getChild(1);
		assertEquals(TaskType.DECORATOR, fail.getType());

		model.insert(fail, careTask, 0);
		assertEquals(2, fail.getChildCount());
		assertFalse(model.isValid());
	}

	@Test public void insertInvalidLast () {
		// insert invalid stuff, verify its not there
		CareTask careTask = new CareTask();

		BTTask<Dog> fail = root.getChild(0).getChild(1);
		assertEquals(TaskType.DECORATOR, fail.getType());

		model.insert(fail, careTask, 1);
		assertEquals(2, fail.getChildCount());
		assertFalse(model.isValid());
	}

	@Test public void removeRoot () {
		// remove stuff, verify its not there
		model.remove(root);
		assertFalse(model.isValid());
		assertNull(model.getRootNode());
	}

	@Test public void removeExistingValid () {
		// remove stuff, verify its not there
		BTTask<Dog> fail = root.getChild(0).getChild(1);
		BTTask<Dog> removed = model.remove(fail);
		assertNotNull(removed);
		assertTrue(model.isValid());
		assertEquals(1, root.getChild(0).getChildCount());
	}

	@Test public void removeExistingInvalid () {
		// remove stuff, verify its not there
		BTTask<Dog> fail = root.getChild(0).getChild(1);
		BTTask<Dog> rest = fail.getChild(0);
		BTTask<Dog> removed = model.remove(rest);
		assertNotNull(removed);
		assertFalse(model.isValid());
		assertEquals(0, fail.getChildCount());
	}

	@Test public void removeNotExisting () {
		// remove stuff, verify its not there
		BTTask<Dog> remove = model.remove(new BarkTask());
		assertNull(remove);
		assertTrue(model.isValid());
	}

	private static Task<Dog> createDogBehavior () {
		/* this is eq tree to one made in code below
		selector
		  parallel
			 care
			 always
				rest
		  sequence
			 bark
			 walk
		 */

		Selector<Dog> selector = new Selector<>();

		Parallel<Dog> parallel = new Parallel<>();
		selector.addChild(parallel);

		CareTask care = new CareTask();
		parallel.addChild(care);
		parallel.addChild(new AlwaysFail<>(new RestTask()));

		Sequence<Dog> sequence = new Sequence<>();
		selector.addChild(sequence);

		sequence.addChild(new BarkTask());
		sequence.addChild(new WalkTask());

		return selector;
	}
}
