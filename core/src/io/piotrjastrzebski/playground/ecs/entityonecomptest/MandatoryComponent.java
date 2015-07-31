package io.piotrjastrzebski.playground.ecs.entityonecomptest;

import com.artemis.Component;

/**
 * Created by PiotrJ on 10/07/15.
 */
public class MandatoryComponent extends Component {
    public String data = "some mendatory data";

    @Override
    public String toString() {
        return "MandatoryComponent{}";
    }
}
