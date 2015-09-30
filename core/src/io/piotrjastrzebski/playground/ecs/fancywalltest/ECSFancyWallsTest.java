package io.piotrjastrzebski.playground.ecs.fancywalltest;

import com.artemis.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.ecs.ECSTestBase;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Tint;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Transform;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Wall;
import io.piotrjastrzebski.playground.ecs.fancywalltest.processors.*;

/**
 * Created by PiotrJ on 30/09/15.
 */
public class ECSFancyWallsTest extends ECSTestBase {

	public ECSFancyWallsTest (GameReset game) {
		super(game);
	}

	@Override protected void preInit (WorldConfiguration config) {
		config.setSystem(new CamMover());
		config.setSystem(new ViewBoundsUpdater());
		config.setSystem(new Interpolator());
		config.setSystem(new Fancier());
//		config.setSystem(new Fancier2());
		config.setSystem(new DebugFillRenderer());
		config.setSystem(new DebugRenderer());
		config.setSystem(new DebugWallRenderer());
	}
	Vector2 tmp;
	@Override protected void postInit () {
//		Entity entity = world.createEntity();
//		EntityEdit edit = entity.edit();
//		edit.create(Transform.class).set(-4f, -4f, 0);
//		edit.create(Bounds.class).set(8, 8);
//		edit.create(Tint.class).color.set(Color.RED);

		tmp = new Vector2();

		wall(-1.5f, -4f, 0f, 3f);
		wall(-1.5f, 4f, 0f, 3f);
		wall(-4f, -1.5f, 90f, 3f);
		wall(4f, -1.5f, 90f, 3f);

		float sqrt = (float)Math.sqrt(2.5*2.5*2);
		wall(-4f, 1.5f, 45f, sqrt);
		wall(1.5f, 4f, -45f, sqrt);
		wall(1.5f, -4f, 45f, sqrt);
		wall(-4f, -1.5f, -45f, sqrt);

	}

	private void wall (float x, float y, float angle, float height) {
		Entity entity = world.createEntity();
		EntityEdit edit = entity.edit();
		edit.create(Transform.class).set(x, y, angle);
		edit.create(Tint.class).color.set(Color.GREEN);
		Wall wall = edit.create(Wall.class);
		wall.height = height;
		tmp.set(1, 0).setAngle(angle).scl(height).add(x, y);
		wall.off.set(tmp.x, tmp.y);
	}
}
