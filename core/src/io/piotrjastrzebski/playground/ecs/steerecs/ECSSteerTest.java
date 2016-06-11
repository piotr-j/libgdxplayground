package io.piotrjastrzebski.playground.ecs.steerecs;

import com.artemis.*;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Steering + ECS
 *
 * We want a bunch of entities wandering around
 *
 * Created by EvilEntity on 28/07/2015.
 */
@SuppressWarnings("Duplicates")
public class ECSSteerTest extends BaseScreen {
	private final static String TAG = ECSSteerTest.class.getSimpleName();
	public static final int NULL_ID = -1;

	World world;
	public ECSSteerTest (GameReset game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();
		config.register("game-cam", gameCamera);
		config.register("gui-cam", guiCamera);
		config.register(renderer);
		config.register(stage);

		config.setSystem(Steerer.class);
		config.setSystem(Renderer.class);
		config.setSystem(SteerRenderer.class);


		world = new World(config);

		for (int i = 0; i < 10; i++) {
			EntityEdit edit = world.createEntity().edit();
			Transform tm = edit.create(Transform.class);
			tm.angle = MathUtils.random(MathUtils.PI2);
			tm.position.set(MathUtils.random(VP_WIDTH-1) - VP_WIDTH/2, MathUtils.random(VP_HEIGHT-1) - VP_HEIGHT/2);

			ECSSteerable steerable = edit.create(ECSSteerable.class);
			steerable.location.position.set(tm.position);
			steerable.location.orientation = tm.angle;
			steerable.independentFacing = true;
			steerable.boundingRadius = .5f;
			steerable.steering = new Wander<>(steerable) //
				// if steerable.independentFacing == false this should be false as well
				.setFaceEnabled(true) // We want to use Face internally (independent facing is on)
				.setAlignTolerance(0.001f) // Used by Face
				.setDecelerationRadius(5f) // Used by Face
				.setTimeToTarget(0.1f) // Used by Face
				.setWanderOffset(4) //
				.setWanderOrientation(steerable.location.orientation) //
				.setWanderRadius(2) //
				.setWanderRate(MathUtils.PI2 * 4);

			steerable.setZeroLinearSpeedThreshold(.0001f);
			steerable.setMaxLinearSpeed(3);
			steerable.setMaxAngularSpeed(5);
			steerable.setMaxAngularAcceleration(10);
			steerable.setMaxLinearAcceleration(2);
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		GdxAI.getTimepiece().update(delta);
		world.delta = Math.min(delta, 1f/15f);
		world.process();
	}

	public static class Transform extends Component {
		public Vector2 position = new Vector2();
		public float angle;
	}

	public static class ECSSteerable extends Component implements Steerable<Vector2> {
		public SteeringBehavior<Vector2> steering;

		public Vector2 linearVelocity = new Vector2();
		public float angularVelocity;
		public float boundingRadius;
		public boolean tagged;

		public ECSLocation location = new ECSLocation();
		public boolean independentFacing;

		public float zeroLinearSpeedThreshold = 0;
		public float maxLinearSpeed = 0;
		public float maxLinearAcceleration;
		public float maxAngularSpeed;
		public float maxAngularAcceleration;

		// Steerable
		@Override public Vector2 getLinearVelocity () {
			return linearVelocity;
		}

		@Override public float getAngularVelocity () {
			return angularVelocity;
		}

		@Override public float getBoundingRadius () {
			return boundingRadius;
		}

		@Override public boolean isTagged () {
			return tagged;
		}

		@Override public void setTagged (boolean tagged) {
			this.tagged = tagged;
		}

		// Location
		@Override public Vector2 getPosition () {
			return location.position;
		}

		@Override public float getOrientation () {
			return location.orientation;
		}

		@Override public void setOrientation (float orientation) {
			location.orientation = orientation;
		}

		@Override public float vectorToAngle (Vector2 vector) {
			return location.vectorToAngle(vector);
		}

		@Override public Vector2 angleToVector (Vector2 outVector, float angle) {
			return location.angleToVector(outVector, angle);
		}

		@Override public Location<Vector2> newLocation () {
			return location.newLocation();
		}

		// Limiter
		@Override public float getZeroLinearSpeedThreshold () {
			return zeroLinearSpeedThreshold;
		}

		@Override public void setZeroLinearSpeedThreshold (float value) {
			zeroLinearSpeedThreshold = value;
		}

		@Override public float getMaxLinearSpeed () {
			return maxLinearSpeed;
		}

		@Override public void setMaxLinearSpeed (float maxLinearSpeed) {
			this.maxLinearSpeed = maxLinearSpeed;
		}

		@Override public float getMaxLinearAcceleration () {
			return maxLinearAcceleration;
		}

		@Override public void setMaxLinearAcceleration (float maxLinearAcceleration) {
			this.maxLinearAcceleration = maxLinearAcceleration;
		}

		@Override public float getMaxAngularSpeed () {
			return maxAngularSpeed;
		}

		@Override public void setMaxAngularSpeed (float maxAngularSpeed) {
			this.maxAngularSpeed = maxAngularSpeed;
		}

		@Override public float getMaxAngularAcceleration () {
			return maxAngularAcceleration;
		}

		@Override public void setMaxAngularAcceleration (float maxAngularAcceleration) {
			this.maxAngularAcceleration = maxAngularAcceleration;
		}
	}

	/**
	 * Is there a point in delegating to this?
	 */
	public static class ECSLocation implements Location<Vector2> {
		public Vector2 position = new Vector2();
		// in radians
		public float orientation;

		@Override
		public Vector2 getPosition () {
			return position;
		}

		@Override
		public float getOrientation () {
			return orientation;
		}

		@Override
		public void setOrientation (float orientation) {
			this.orientation = orientation;
		}

		@Override
		public Location<Vector2> newLocation () {
			return new ECSLocation();
		}

		@Override
		public float vectorToAngle (Vector2 vector) {
			return ECSSteerTest.vectorToAngle(vector);
		}

		@Override
		public Vector2 angleToVector (Vector2 outVector, float angle) {
			return ECSSteerTest.angleToVector(outVector, angle);
		}
	}

	public static float vectorToAngle (Vector2 vector) {
		return (float)Math.atan2(-vector.x, vector.y);
	}

	public static Vector2 angleToVector (Vector2 outVector, float angle) {
		outVector.x = -(float)Math.sin(angle);
		outVector.y = (float)Math.cos(angle);
		return outVector;
	}

	public static class Steerer extends IteratingSystem {
		protected ComponentMapper<Transform> mTransform;
		protected ComponentMapper<ECSSteerable> mECSSteerable;

		public Steerer () {
			super(Aspect.all(Transform.class, ECSSteerable.class));
		}

		private SteeringAcceleration<Vector2> output = new SteeringAcceleration<>(new Vector2());
		@Override protected void process (int entityId) {
			Transform transform = mTransform.get(entityId);

			ECSSteerable steerable = mECSSteerable.get(entityId);
			if (steerable.steering == null) return;

			steerable.steering.calculateSteering(output);

			float delta = world.delta;
			ECSLocation location = steerable.location;
			location.position.mulAdd(steerable.linearVelocity, delta);
			steerable.linearVelocity.mulAdd(output.linear, delta).limit(steerable.getMaxLinearSpeed());

			if (steerable.independentFacing) {
				location.orientation += steerable.angularVelocity * delta;
				steerable.angularVelocity += output.angular * delta;
			} else {
				if (!steerable.linearVelocity.isZero(steerable.getZeroLinearSpeedThreshold())) {
					float newOrientation = vectorToAngle(steerable.linearVelocity);
					// this is superfluous if independentFacing is always true
					steerable.angularVelocity = (newOrientation - location.orientation) * delta;
					location.orientation = newOrientation;
				}
			}
//			System.out.println(transform.angle + " " + location.orientation);
			transform.position.set(location.position);
			transform.angle = location.orientation;

			Vector2 tp = transform.position;
			if (tp.x > VP_WIDTH/2) {
				tp.x -= VP_WIDTH;
			}
			if (tp.x < -VP_WIDTH/2) {
				tp.x += VP_WIDTH;
			}
			if (tp.y > VP_HEIGHT/2) {
				tp.y -= VP_HEIGHT;
			}
			if (tp.y < -VP_HEIGHT/2) {
				tp.y += VP_HEIGHT;
			}
			location.position.set(tp);
		}
	}
	public static class SteerRenderer extends IteratingSystem {
		@Wire(name = "game-cam") OrthographicCamera camera;
		@Wire ShapeRenderer shapeRenderer;
		ComponentMapper<Transform> mTransform;
		ComponentMapper<ECSSteerable> mECSSteerable;

		public SteerRenderer () {
			super(Aspect.all(Transform.class, ECSSteerable.class));
		}

		@Override protected void begin () {
			shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		}

		@Override protected void process (int entityId) {
			ECSSteerable steerable = mECSSteerable.get(entityId);
			if (steerable.steering == null) return;
			Vector2 position = steerable.location.position;
			if (steerable.steering instanceof Wander) {
				Wander<Vector2> wander = (Wander<Vector2>)steerable.steering;

				Vector2 itp = wander.getInternalTargetPosition();
				Vector2 center = wander.getWanderCenter();
				float radius = wander.getWanderRadius();
				float offset = wander.getWanderOffset();

				shapeRenderer.setColor(Color.VIOLET);
				shapeRenderer.circle(center.x, center.y, radius, 32);

				shapeRenderer.setColor(Color.RED);
				shapeRenderer.circle(itp.x, itp.y, .2f, 32);
			}

			Vector2 lv = steerable.getLinearVelocity();
			shapeRenderer.setColor(Color.GREEN);
			shapeRenderer.line(position.x, position.y, position.x + lv.x, position.y + lv.y);
		}

		@Override protected void end () {
			shapeRenderer.end();
		}
	}

	public static class Renderer extends IteratingSystem {
		@Wire(name = "game-cam") OrthographicCamera camera;
		@Wire ShapeRenderer shapeRenderer;
		ComponentMapper<Transform> mTransform;

		public Renderer () {
			super(Aspect.all(Transform.class));
		}

		@Override protected void begin () {
			shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		}

		Vector2 tmp = new Vector2();
		@Override protected void process (int entityId) {
			Transform transform = mTransform.get(entityId);
			shapeRenderer.setColor(Color.CYAN);
			Vector2 position = transform.position;
			shapeRenderer.circle(position.x, position.y, .5f, 16);
			tmp.set(0, 1).rotateRad(transform.angle).nor().scl(.5f);
			shapeRenderer.setColor(Color.MAGENTA);
			shapeRenderer.rectLine(position.x, position.y, position.x + tmp.x, position.y + tmp.y, .1f);
		}

		@Override protected void end () {
			shapeRenderer.end();
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, ECSSteerTest.class);
	}
}
