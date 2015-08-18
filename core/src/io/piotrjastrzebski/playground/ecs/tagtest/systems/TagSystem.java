package io.piotrjastrzebski.playground.ecs.tagtest.systems;

import com.artemis.*;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import io.piotrjastrzebski.playground.ecs.tagtest.components.TagComponent;

/**
 * Created by PiotrJ on 25/06/15.
 */
@Wire public class TagSystem extends EntitySystem {
    ComponentMapper<TagComponent> tagM;
    ObjectMap<String, Class> tagToClass = new ObjectMap<>();
    ObjectMap<String, IntArray> tagToEntities = new ObjectMap<>();

    public TagSystem() {
        super(Aspect.all(TagComponent.class));
    }


    @Override
    protected void inserted(int e) {
        TagComponent tagComponent = tagM.get(e);
        for (String tag:tagComponent.tags) {
            Class aClass = tagToClass.get(tag, null);
            if (aClass == null) continue;
            world.getEntity(e).edit().create(aClass);

            IntArray bag = tagToEntities.get(tag, null);
            if (bag == null) {
                bag = new IntArray();
                tagToEntities.put(tag, bag);
            }
            bag.add(e);
        }
    }

    @Override
    protected void removed(int e) {
        TagComponent tagComponent = tagM.get(e);
        for (String tag:tagComponent.tags) {
            IntArray bag = tagToEntities.get(tag, null);
            if (bag != null) {
                bag.removeValue(e);
            }
        }
    }

    @Override
    protected void processSystem () {

    }

    public <T extends Component> void add(String tag, Class<T> aClass) {
        if (tagToClass.get(tag, null) != null) {
            Gdx.app.log("", "Overriding tag " + tag);
        }
        tagToClass.put(tag, aClass);
    }

    public void remove(String tag, Entity e) {
        TagComponent tagComponent = tagM.get(e);
        if (tagComponent == null) {
            Gdx.app.log("", "You can only remove tgs from tagged entities");
            return;
        }
        tagComponent.remove(tag);
        // remove the component itself if none left
        // TODO do we really want this? or is empty component fine?
        if (tagComponent.tags.size == 0) {
            e.edit().remove(TagComponent.class);
        }
        Class aClass = tagToClass.get(tag, null);
        if (aClass != null) {
            // we know this only can be a component
            e.edit().remove(aClass);
        }
        IntArray bag = tagToEntities.get(tag, null);
        if (bag != null) {
            bag.removeValue(e.getId());
        }
    }

    public void get(String tag, IntArray fill) {
        IntArray ids = tagToEntities.get(tag, null);
        if (ids != null) {
            fill.addAll(ids);
        }
    }

    // get by id is preferable
    public void get(String tag, Array<Entity> fill) {
        IntArray ids = tagToEntities.get(tag, null);
        if (ids != null) {
            for (int i = 0; i < ids.size; i++) {
                fill.add(world.getEntity(ids.get(i)));
            }
        }
    }
}
