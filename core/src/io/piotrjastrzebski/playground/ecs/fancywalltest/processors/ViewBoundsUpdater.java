package io.piotrjastrzebski.playground.ecs.fancywalltest.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.ecs.ECSTestBase;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Bounds;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Tint;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Transform;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.ViewBounds;

/**
 * Created by PiotrJ on 30/09/15.
 */
@Wire
public class ViewBoundsUpdater extends EntityProcessingSystem {
	protected ComponentMapper<ViewBounds> mViewBounds;
	protected ComponentMapper<Transform> mTransform;
	protected ComponentMapper<Bounds> mBounds;

	@Wire(name = ECSTestBase.WIRE_GAME_VP)
	ExtendViewport vp;

	@Wire(name = ECSTestBase.WIRE_GAME_CAM)
	OrthographicCamera cam;

	public ViewBoundsUpdater() {
		super(Aspect.all(ViewBounds.class, Transform.class, Bounds.class));
	}

	@Override protected void initialize () {
		EntityEdit edit = world.createEntity().edit();
		edit.create(ViewBounds.class);
		edit.create(Transform.class);
		edit.create(Bounds.class);
		edit.create(Tint.class).color.set(Color.GOLD);
	}

	@Override protected void process (Entity e) {
		ViewBounds vb = mViewBounds.get(e);
		Transform t = mTransform.get(e);
		getBounds(Fancier.VB_SCALE, vb.rect);
		Bounds b = mBounds.get(e);
		t.pos.set(vb.rect.x, vb.rect.y);
		b.rect.setSize(vb.rect.width, vb.rect.height);
	}

	public Rectangle getBounds(float gutter, Rectangle out) {
		float w = vp.getWorldWidth();
		float h = vp.getWorldHeight();
		float x = cam.position.x - w / 2;
		float y = cam.position.y - h / 2;
		float wg = w * gutter * 1.75f;
		float hg = h * gutter;
		out.set(x + wg, y + hg, w - wg * 2, h - hg * 2);
		return out;
	}
}
