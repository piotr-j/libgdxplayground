package io.piotrjastrzebski.playground.ecs.tagtest.systems;

import com.artemis.Entity;

/**
 * Created by PiotrJ on 27/06/15.
 */
public interface JobHandler {
    void finishJob(Entity entity);
}
