package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ModelTree<E> extends ModelTask implements Pool.Poolable {
	protected BehaviorTree<E> bt;

	public void init (BehaviorTree<E> bt) {
		this.bt = bt;
		init(null, bt.getChild(0));
	}

	@Override public void reset () {
		super.reset();
		this.bt = null;
	}
}
