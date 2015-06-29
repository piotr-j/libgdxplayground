package io.piotrjastrzebski.playground.tagtest.components;

/**
 * Created by PiotrJ on 27/06/15.
 */
public class MineJobComponent extends JobComponent {
    public MineJobComponent() {
        tag = "mine";
    }

    @Override
    public String toString() {
        return "MineJobComponent{tag="+tag+"}";
    }
}
