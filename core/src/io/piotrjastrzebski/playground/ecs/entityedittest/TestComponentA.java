package io.piotrjastrzebski.playground.ecs.entityedittest;

import com.artemis.Component;

/**
 * Created by EvilEntity on 09/07/2015.
 */
public class TestComponentA extends Component {
	public String data;

	@Override public String toString () {
		return "TestComponentA{"+data+"}";
	}
}
