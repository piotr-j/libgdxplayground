package io.piotrjastrzebski.playground.ecs.aitest;

import com.artemis.Component;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import io.piotrjastrzebski.playground.ecs.aitest.dog.Dog;

/**
 * Created by PiotrJ on 04/08/15.
 */
public class AI extends Component {
	public transient BehaviorTree<Dog> bTree;
	public String bTreeStr;

	@Override public String toString () {
		return "AI{" +
			"bTreeStr='" + bTreeStr + '\'' +
			'}';
	}
}
