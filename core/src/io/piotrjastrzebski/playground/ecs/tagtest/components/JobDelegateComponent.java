package io.piotrjastrzebski.playground.ecs.tagtest.components;

import com.artemis.Component;

/**
 * Delegates jobs to actual JobComponent
 * This is required because we cant dynamically subscribe for components in a system
 *
 *
 * Created by PiotrJ on 27/06/15.
 */
public class JobDelegateComponent extends Component {
    private String actualClass;
    // need a way to re add this after serialization
    private transient JobComponent actual;
    // can we save this instead of string?
    private transient Class<? extends JobComponent> aClass;

    public void setActualClass(Class<? extends JobComponent> aClass) {
        this.aClass = aClass;
        actualClass = aClass.getCanonicalName();
    }

    public String getActualClassStr() {
        return actualClass;
    }

    public JobComponent getActual() {
        return actual;
    }

    public void setActual(JobComponent actual) {
        this.actual = actual;
    }

    @Override
    public String toString() {
        return "JobDelegateComponent{" +
                "actualClass='" + actualClass + '\'' +
                '}';
    }
}
