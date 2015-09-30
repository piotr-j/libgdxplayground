package io.piotrjastrzebski.playground.ecs.fancywalltest.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import io.piotrjastrzebski.playground.ecs.ECSTestBase;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.*;

/**
 * Created by PiotrJ on 30/09/15.
 */
@Wire
public class Fancier extends EntityProcessingSystem {
	protected ComponentMapper<Transform> mTransform;
	protected ComponentMapper<Wall> mWall;
	protected ComponentMapper<Tint> mTint;
	protected ComponentMapper<Transformed> mTransformed;
	protected ComponentMapper<Transformer> mTransformer;

	@Wire
	ViewBoundsUpdater vbu;
	private Rectangle vb = new Rectangle();

	public Fancier () {
		super(Aspect.all(Transform.class, Wall.class, Tint.class));
	}

	IntMap<IntArray> wallToParts = new IntMap<>();

	private int ppu = 4;
	@Override protected void inserted (int entityId) {
		// create fancy things
		Transform wt = mTransform.get(entityId);
		Wall w = mWall.get(entityId);
		IntArray parts = wallToParts.get(entityId, null);
		if (parts == null) {
			parts = new IntArray();
			wallToParts.put(entityId, parts);
		}
		float height = w.height;
		int points = (int)(height * ppu);
		float dst = height / points;
		float off = 0;
		for (int i = 0; i < points; i++) {
			tmp.set(wt.pos).interpolate(w.off, off/height, Interpolation.linear);
			create(tmp.x, tmp.y, wt.angle, parts);
			off += dst;
		}
	}

	float posSpr = .2f;
	float posSrcMult = 5f;
	float angleSpr = 15f;
	private void create (float x, float y, float angle, IntArray parts) {
		Entity entity = world.createEntity();
		EntityEdit edit = entity.edit();

		/*
		float sin = MathUtils.sinDeg(pTrans.angle);
		float cos = MathUtils.cosDeg(pTrans.angle);
		tTrans.pos.set(
			pPos.x + cos * off.x - sin * off.y,
			pPos.y + sin * off.x + cos * off.y
		);
		*/

		float w = MathUtils.random(0.5f, 1.5f);
		float dstAngle = angle + MathUtils.random(-angleSpr, angleSpr);
		float dstX = x + MathUtils.random(-posSpr, posSpr) - w/2;
		float dstY = y + MathUtils.random(-posSpr, posSpr) - 0.025f;
		float srcAngle = angle + MathUtils.random(-angleSpr, angleSpr);
		float srcX = x + MathUtils.random(-posSpr * posSrcMult, posSpr * posSrcMult) - w/2;
		float srcY = y + MathUtils.random(-posSpr * posSrcMult, posSpr * posSrcMult) - 0.025f;


		Transform t = edit.create(Transform.class).set(srcX, srcY, srcAngle);

		Transformer transformer = edit.create(Transformer.class);
		transformer.setSrs(srcX, srcY, srcAngle);
		transformer.setDst(dstX, dstY, dstAngle);
		transformer.duration = MathUtils.random(.5f, 1f);

		edit.create(Bounds.class).set(w, 0.05f);
		edit.create(Tint.class).color.set(Color.CYAN);
		edit.create(Filled.class);
		edit.create(Transformed.class);
		parts.add(entity.getId());
	}

	@Override protected void removed (int entityId) {
		IntArray parts = wallToParts.get(entityId, null);
		if (parts == null) return;
		for (int i = 0; i < parts.size; i++) {
			world.deleteEntity(parts.get(i));
		}
		parts.clear();

	}

	public static final float VB_SCALE = .25f;
	@Override protected void begin () {
		vbu.getBounds(VB_SCALE, vb);
	}

	Vector2 tmp = new Vector2();
	@Override protected void process (Entity e) {
		Transform src = mTransform.get(e);
		Wall wall = mWall.get(e);
		Tint tint = mTint.get(e);
		tmp.set(1, 0).setAngle(src.angle).scl(wall.height).add(src.pos);
		if (overlaps(src.pos, tmp, vb)) {
			if (!mTransformed.has(e)) {
				tint.color.set(Color.RED);
			}
			IntArray parts = wallToParts.get(e.getId(), null);
			if (parts == null) return;
			for (int i = 0; i < parts.size; i++) {
				int id = parts.get(i);
				Transformer t = mTransformer.get(id);
				if (t.reverse) {
					t.timer = 0;
					t.reverse = false;
				}
			}
		} else {
			tint.color.set(Color.GREEN);
			e.edit().remove(Transformed.class);
			IntArray parts = wallToParts.get(e.getId(), null);
			if (parts == null) return;
			for (int i = 0; i < parts.size; i++) {
				int id = parts.get(i);
				Transformer t = mTransformer.get(id);
				if (!t.reverse) {
					t.timer = 0;
					t.reverse = true;
				}
			}
		}
	}

	private static boolean overlaps(Vector2 a, Vector2 b, Rectangle r) {
		if (r.contains(a) || r.contains(b)) {
			return true;
		}
		float maxX = r.x + r.width;
		float maxY = r.y + r.height;
		// fully outside
		if ((a.x < r.x && b.x < r.x)
			|| (a.y < r.y && b.y < r.y)
			|| (a.x > maxX && b.x > maxX)
			|| (a.y > maxY && b.y > maxY))
			return false;

		// slope
		float m = (b.y - a.y) / (b.x - a.x);

		// check for intersections
		float y = m * (r.x - a.x) + a.y;
		if (y > r.y && y < maxY) return true;

		y = m * (maxX - a.x) + a.y;
		if (y > r.y && y < maxY) return true;

		float x = (r.y - a.y) / m + a.x;
		if (x > r.x && x < maxX) return true;

		x = (maxY - a.y) / m + a.x;
		if (x > r.x && x < maxX) return true;

		return false;
	}
}
