package io.piotrjastrzebski.playground.ecs.assettest;

import com.artemis.*;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.g2d.Sprite;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 11/08/15.
 */
public class ECSAssetTest extends BaseScreen {
	World world;
	public ECSAssetTest (PlaygroundGame game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();

		config.setSystem(new SpriteRenderableInitSystem());
		config.setSystem(new SpriteRenderer());

		world = new World(config);
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.process();
	}

	public static class SpriteRenderableInitSystem extends EntityProcessingSystem {
		protected ComponentMapper<SpriteRenderableDef> mSpriteRenderableDef;
		public SpriteRenderableInitSystem () {
			super(Aspect.all(SpriteRenderableDef.class));
			setPassive(true);
		}

		@Override protected void inserted (Entity e) {
			SpriteRenderableDef spriteRenderableDef = mSpriteRenderableDef.get(e);
			SpriteRenderable renderable = e.edit().create(SpriteRenderable.class);
			// get sprite from some where
			//renderable.sprite =
		}

		@Override protected void process (Entity e) {
			// passive we dont care
		}
	}

	public static class SpriteRenderer extends EntityProcessingSystem {
		protected ComponentMapper<SpriteRenderable> mSpriteRenderable;
		public SpriteRenderer () {
			super(Aspect.all(SpriteRenderable.class));
		}

		@Override protected void process (Entity e) {
			SpriteRenderable spriteRenderable = mSpriteRenderable.get(e);
			// render stuff
		}
	}
}
