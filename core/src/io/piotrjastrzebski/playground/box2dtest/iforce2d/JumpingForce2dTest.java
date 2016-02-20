package io.piotrjastrzebski.playground.box2dtest.iforce2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 31/07/15.
 */
public class JumpingForce2dTest extends BaseIForce2dTest {
	private static final String TAG = JumpingForce2dTest.class.getSimpleName();
	private final Body body;

	private enum JumpType {DIRECT, FORCE, IMPULSE}
	private JumpType jumpType = JumpType.FORCE;

	public JumpingForce2dTest (GameReset game) {
		super(game);
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1, 1);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1;

		bodyDef.position.set(0, -VP_HEIGHT/2 + 1);
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);

		shape.dispose();

		Gdx.app.log(TAG, "1 - direct");
		Gdx.app.log(TAG, "2 - force");
		Gdx.app.log(TAG, "3 - impulse");
		Gdx.app.log(TAG, "SPACE - jump");
	}

	private float jumpFrames = 6;
	private float jumpDuration = jumpFrames/60f;
	private float jumpTimer;
	@Override public void update (float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			jumpType = JumpType.DIRECT;
			Gdx.app.log(TAG, "type - direct nyi");
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			jumpType = JumpType.FORCE;
			Gdx.app.log(TAG, "type - force");
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
			jumpType = JumpType.IMPULSE;
			Gdx.app.log(TAG, "type - impulse");
		}
		Vector2 wc = body.getWorldCenter();
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			switch (jumpType) {
			case FORCE:
				jumpTimer = jumpDuration;
				break;
			case IMPULSE:
				float impulse = body.getMass() * 10;
				body.applyLinearImpulse(0, impulse, wc.x, wc.y, true);
				break;
			}
		}
		switch (jumpType) {
		case FORCE:
			if (jumpTimer > 0) {
				float force = body.getMass() * 10 / (1/60.0f); //f = mv/t
				//spread this over 6 time steps
				force /= jumpFrames;
				body.applyForce(0, force, wc.x, wc.y, true);
			}
			break;
		}
		jumpTimer -= delta;
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, JumpingForce2dTest.class);
	}
}
