package io.piotrjastrzebski.playground.ecs.tagtest.components;

import com.artemis.Component;
import com.artemis.Entity;

import java.util.UUID;

/**
 * Base class for various jobs
 *
 * Created by PiotrJ on 27/06/15.
 */
public abstract class JobComponent extends Component {
    // tag for this job
    public String tag;

    // if some entity is working on this
    public boolean claimed;

    // TODO other stuff


    public void claim(Entity worker) {
        // NOTE support for multiple would be nice at some point
        claimed = true;
    }
}
