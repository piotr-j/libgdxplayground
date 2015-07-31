package io.piotrjastrzebski.playground.ecs.tagtest.components;

/**
 * Created by PiotrJ on 27/06/15.
 */
public class BuildJobComponent extends JobComponent {
    public BuildJobComponent() {
        tag = "build";
    }

    @Override
    public String toString() {
        return "BuildJobComponent{tag="+tag+"}";
    }
}
