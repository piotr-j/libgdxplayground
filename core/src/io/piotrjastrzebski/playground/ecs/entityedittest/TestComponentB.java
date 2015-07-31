package io.piotrjastrzebski.playground.ecs.entityedittest;

import com.artemis.Component;

/**
 * Created by EvilEntity on 09/07/2015.
 */
public class TestComponentB extends Component {
	public String data;

	@Override public String toString () {
		return "TestComponentB{"+data+"}";
	}
}
