package io.piotrjastrzebski.playground.ecs.fancywalltest.processors;

import com.artemis.*;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.*;

/**
 * Created by PiotrJ on 30/09/15.
 */
@Wire
public class Fancier2 extends EntitySystem {
	protected ComponentMapper<Transform> mTransform;
	protected ComponentMapper<Wall> mWall;
	protected ComponentMapper<Transformed> mTransformed;

	public Fancier2 () {
		super(Aspect.all(Transform.class, Wall.class, Transformed.class));
	}

	IntMap<IntArray> wallToParts = new IntMap<>();

	private int ppu = 4;
	Vector2 tmp = new Vector2();
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

	@Override protected void processSystem () {}
}
