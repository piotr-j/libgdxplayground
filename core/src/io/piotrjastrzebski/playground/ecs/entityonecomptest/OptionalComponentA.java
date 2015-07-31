package io.piotrjastrzebski.playground.ecs.entityonecomptest;

import com.artemis.Component;

/**
 * Created by PiotrJ on 10/07/15.
 */
public class OptionalComponentA extends Component {
    public String data = "some optional data A";

    @Override
    public String toString() {
        return "OptionalComponentA{}";
    }
}
