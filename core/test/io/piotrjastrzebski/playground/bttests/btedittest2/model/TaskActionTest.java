package io.piotrjastrzebski.playground.bttests.btedittest2.model;

import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import io.piotrjastrzebski.playground.bttests.dog.BarkTask;
import io.piotrjastrzebski.playground.bttests.dog.CareTask;
import io.piotrjastrzebski.playground.bttests.dog.Dog;
import io.piotrjastrzebski.playground.bttests.dog.WalkTask;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by PiotrJ on 15/10/15.
 */
public class TaskActionTest {
	TaskAction.Add add;
	TaskAction.Insert insert;
	TaskAction.Remove remove;
	Selector<Dog> selector;
	BarkTask bark;
	WalkTask walk;
	CareTask care;
	AlwaysFail<Dog> alwaysFail;

	@Before public void setUp () throws Exception {
		selector = new Selector<>();
		alwaysFail = new AlwaysFail<>();
		bark = new BarkTask();
		walk = new WalkTask();
		care = new CareTask();

		add = new TaskAction.Add();
		insert = new TaskAction.Insert();
		remove = new TaskAction.Remove();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullTask () throws Exception {
		add.init(selector, null);
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullTarget () throws Exception {
		add.init(null, bark);
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullBoth () throws Exception {
		add.init(null, null);
		fail();
	}

	@Test public void testAddBranchEmpty () throws Exception {
		boolean added = add.init(selector, bark).execute();
		assertTrue(added);
		// checking both feels redundant most of the time, fix?
		assertEquals(1, selector.getChildCount());
		assertEquals(bark, selector.getChild(0));
	}

	@Test public void testAddBranchNotEmpty () throws Exception {
		selector.addChild(walk);

		boolean added = add.init(selector, bark).execute();
		assertTrue(added);
		assertEquals(2, selector.getChildCount());
		assertEquals(bark, selector.getChild(1));
	}

	@Test public void testAddBranchNotEmptyDupe () throws Exception {
		selector.addChild(walk);

		boolean added = add.init(selector, walk).execute();
		assertFalse(added);
		assertEquals(1, selector.getChildCount());
		assertEquals(walk, selector.getChild(0));
	}

	@Test public void testAddDecoratorEmpty () throws Exception {
		// adding to empty decorator adds the child
		assertEquals(0, alwaysFail.getChildCount());

		boolean added = add.init(alwaysFail, bark).execute();
		assertTrue(added);
		assertEquals(1, alwaysFail.getChildCount());
		assertEquals(bark, alwaysFail.getChild(0));
	}

	@Test public void testAddDecoratorNotEmpty () throws Exception {
		// adding to not empty decorator replaces the child
		alwaysFail.addChild(walk);
		assertEquals(1, alwaysFail.getChildCount());
		assertEquals(walk, alwaysFail.getChild(0));

		boolean added = add.init(alwaysFail, bark).execute();
		assertTrue(added);
		assertEquals(1, alwaysFail.getChildCount());
		assertEquals(bark, alwaysFail.getChild(0));
	}

	@Test public void testAddLeaf () throws Exception {
		// cant add anything to the leaf
		boolean added = add.init(bark, walk).execute();
		assertFalse(added);
	}

	@Test public void testInsertBranchFirst () throws Exception {
		selector.addChild(walk);
		assertEquals(1, selector.getChildCount());
		// inserts at pos 0
		boolean execute = insert.init(selector, bark).execute();
		assertTrue(execute);
		assertEquals(2, selector.getChildCount());
		assertEquals(bark, selector.getChild(0));
	}

	@Test public void testInsertBranchMid () throws Exception {
		selector.addChild(walk);
		selector.addChild(care);
		assertEquals(2, selector.getChildCount());

		boolean execute = insert.init(selector, bark, 1).execute();
		assertTrue(execute);
		assertEquals(bark, selector.getChild(1));
		assertEquals(3, selector.getChildCount());
	}

	@Test public void testInsertBranchDupe () throws Exception {
		selector.addChild(walk);
		selector.addChild(care);
		assertEquals(2, selector.getChildCount());

		boolean execute = insert.init(selector, walk, 1).execute();
		assertFalse(execute);
		assertEquals(walk, selector.getChild(0));
		assertEquals(care, selector.getChild(1));
		assertEquals(2, selector.getChildCount());
	}

	@Test public void testInsertBranchLast () throws Exception {
		selector.addChild(walk);
		assertEquals(1, selector.getChildCount());

		boolean execute = insert.init(selector, bark, 1).execute();
		assertTrue(execute);
		assertEquals(bark, selector.getChild(1));
		assertEquals(2, selector.getChildCount());
	}

	@Test public void testInsertBranchEmpty () throws Exception {
		assertEquals(0, selector.getChildCount());
		// can insert to empty at pos 0
		boolean execute = insert.init(selector, bark, 0).execute();
		assertTrue(execute);
		assertEquals(1, selector.getChildCount());
		assertEquals(bark, selector.getChild(0));
	}

	@Test public void testInsertBranchOOB () throws Exception {
		selector.addChild(walk);
		selector.addChild(care);
		assertEquals(2, selector.getChildCount());


		boolean execute = insert.init(selector, bark, 3).execute();
		assertFalse(execute);
		assertEquals(2, selector.getChildCount());
		assertEquals(walk, selector.getChild(0));
		assertEquals(care, selector.getChild(1));

	}

	@Test public void testInsertDecoratorEmpty () throws Exception {
		boolean execute = insert.init(alwaysFail, bark, 0).execute();
		assertTrue(execute);
		assertEquals(bark, alwaysFail.getChild(0));
	}

	@Test public void testInsertDecoratorEmptyDefault () throws Exception {
		boolean execute = insert.init(alwaysFail, bark, 0).execute();
		assertTrue(execute);
		assertEquals(bark, alwaysFail.getChild(0));
	}

	@Test public void testInsertDecoratorEmptyNotFirst () throws Exception {
		boolean execute = insert.init(alwaysFail, bark, 1).execute();
		assertFalse(execute);
		assertEquals(0, alwaysFail.getChildCount());
	}

	@Test public void testInsertDecoratorNotEmptyNotFirst () throws Exception {
		alwaysFail.addChild(care);
		boolean execute = insert.init(alwaysFail, bark, 1).execute();
		assertFalse(execute);
		assertEquals(care, alwaysFail.getChild(0));
	}


	@Test public void testInsertDecoratorNotEmpty () throws Exception {
		alwaysFail.addChild(care);
		boolean execute = insert.init(alwaysFail, bark, 0).execute();
		assertFalse(execute);
		assertEquals(care, alwaysFail.getChild(0));
	}

	@Test public void testInsertLeaf () throws Exception {
		// cant insert into leaf
		boolean execute = insert.init(care, bark, 0).execute();
		assertFalse(execute);
	}

	@Test public void testRemoveBranchExisting () throws Exception {
		selector.addChild(care);
		selector.addChild(walk);

		boolean execute = remove.init(selector, care).execute();
		assertTrue(execute);
		assertEquals(walk, selector.getChild(0));
		assertEquals(1, selector.getChildCount());
	}

	@Test public void testRemoveBranchNotExisting () throws Exception {
		selector.addChild(care);
		selector.addChild(walk);

		boolean execute = remove.init(selector, bark).execute();
		assertFalse(execute);
		assertEquals(care, selector.getChild(0));
		assertEquals(walk, selector.getChild(1));
		assertEquals(2, selector.getChildCount());
	}

	@Test public void testRemoveBranchEmpty () throws Exception {
		boolean execute = remove.init(selector, bark).execute();
		assertFalse(execute);
		assertEquals(0, selector.getChildCount());
	}

	@Test public void testRemoveDecoratorEmpty () throws Exception {
		boolean execute = remove.init(alwaysFail, bark).execute();
		assertFalse(execute);
		assertEquals(0, selector.getChildCount());
	}

	@Test public void testRemoveDecoratorNotEmpty () throws Exception {
		alwaysFail.addChild(bark);
		boolean execute = remove.init(alwaysFail, bark).execute();
		assertTrue(execute);
		assertEquals(0, selector.getChildCount());
	}

	@Test public void testRemoveDecoratorNotSame () throws Exception {
		alwaysFail.addChild(care);
		boolean execute = remove.init(alwaysFail, bark).execute();
		assertFalse(execute);
		assertEquals(1, alwaysFail.getChildCount());
	}

	@Test public void testRemoveLeaf () throws Exception {
		// nothing to remove from leaf
		boolean execute = remove.init(care, bark).execute();
		assertFalse(execute);
	}
}
