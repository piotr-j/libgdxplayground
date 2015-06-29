package io.piotrjastrzebski.playground.tagtest.components;

import com.artemis.Component;
import com.badlogic.gdx.utils.Array;

/**
 * Created by PiotrJ on 25/06/15.
 */
public class TagComponent extends Component {
    public Array<String> tags = new Array<>();

    public TagComponent add(String tag) {
        if (!tags.contains(tag, false)) {
            tags.add(tag);
        }
        return this;
    }

    public TagComponent addAll(String... tags) {
        for(String tag:tags) {
            add(tag);
        }
        return this;
    }

    public boolean remove(String tag) {
        if (tags.removeValue(tag, false)) {
            // TODO need to handle removal somehow, we want this?
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "TagComponent{" +
                "tags=" + tags.toString() +
                '}';
    }
}
