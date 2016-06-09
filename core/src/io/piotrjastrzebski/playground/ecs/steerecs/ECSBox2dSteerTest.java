package io.piotrjastrzebski.playground.ecs.steerecs;

import com.artemis.*;
import com.artemis.World;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.steer.*;
import com.badlogic.gdx.ai.steer.behaviors.CollisionAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Steering + ECS
 *
 * We want a bunch of box2d entities wandering around
 *
 * Created by EvilEntity on 28/07/2015.
 */
@SuppressWarnings("Duplicates")
public class ECSBox2dSteerTest extends BaseScreen {
	private final static String TAG = ECSBox2dSteerTest.class.getSimpleName();
	public static final int NULL_ID = -1;

	World world;
	public ECSBox2dSteerTest (GameReset game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();
		config.register("game-cam", gameCamera);
		config.register("gui-cam", guiCamera);
		config.register(renderer);
		config.register(stage);

		config.setSystem(Steerer.class);
		config.setSystem(Physics.class);
		config.setSystem(Renderer.class);
		config.setSystem(SteerRenderer.class);


		world = new World(config);
		Physics physics = world.getSystem(Physics.class);

		for (int i = 0; i < 10; i++) {
			EntityEdit edit = world.createEntity().edit();
			Transform tm = edit.create(Transform.class);
			tm.angle = MathUtils.random(MathUtils.PI2);
			tm.position.set(MathUtils.random(VP_WIDTH-1) - VP_WIDTH/2, MathUtils.random(VP_HEIGHT-1) - VP_HEIGHT/2);
			Box2dBody box2dBody = edit.create(Box2dBody.class);
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyDef.BodyType.DynamicBody;
			bodyDef.position.set(tm.position);
			bodyDef.angle = tm.angle;
			box2dBody.body = physics.box2d.createBody(bodyDef);
			FixtureDef fd = new FixtureDef();
			fd.shape = new CircleShape();
			fd.shape.setRadius(.5f);
			fd.density = 1.0f;
			fd.restitution = .1f;
			fd.friction = .25f;
			box2dBody.body.createFixture(fd);
			fd.shape.dispose();
			ECSSteerable steerable = edit.create(ECSSteerable.class);
			box2dBody.body.setUserData(steerable);
			steerable.location.position.set(tm.position);
			steerable.location.orientation = tm.angle;
			steerable.independentFacing = true;
			steerable.boundingRadius = .5f;

			CollisionAvoidance<Vector2> avoidance = new CollisionAvoidance<>(steerable, null);

			Wander<Vector2> wander = new Wander<>(steerable) //
				// if steerable.independentFacing == false this should be false as well
				.setFaceEnabled(false) // We want to use Face internally (independent facing is on)
				.setAlignTolerance(0.001f) // Used by Face
				.setDecelerationRadius(5f) // Used by Face
				.setTimeToTarget(0.1f) // Used by Face
				.setWanderOffset(4) //
				.setWanderOrientation(steerable.location.orientation) //
				.setWanderRadius(2) //
				.setWanderRate(MathUtils.PI2 * 4);

			Priority priority = new Priority(steerable, 0.0001f);
			priority.add(avoidance);
			priority.add(wander);

			steerable.steering = priority;

			steerable.setZeroLinearSpeedThreshold(.0001f);
			steerable.setMaxLinearSpeed(3);
			steerable.setMaxAngularSpeed(15);
			steerable.setMaxAngularAcceleration(20);
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

	public static class Box2dBody extends Component {
		public Body body;
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
			return ECSBox2dSteerTest.vectorToAngle(vector);
		}

		@Override
		public Vector2 angleToVector (Vector2 outVector, float angle) {
			return ECSBox2dSteerTest.angleToVector(outVector, angle);
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

	public static class Physics extends IteratingSystem {
		@Wire(name = "game-cam") OrthographicCamera camera;
		com.badlogic.gdx.physics.box2d.World box2d;
		Box2DDebugRenderer renderer;
		protected ComponentMapper<Transform> mTransform;
		protected ComponentMapper<Box2dBody> mBox2dBody;
		private boolean debugDraw = true;

		public Physics () {
			super(Aspect.all(Transform.class, Box2dBody.class));
		}

		@Override protected void initialize () {
			box2d = new com.badlogic.gdx.physics.box2d.World(new Vector2(), true);
			renderer = new Box2DDebugRenderer();
		}

		@Override protected void begin () {
			box2d.step(1/60f, 6, 4);
		}

		@Override protected void process (int entityId) {
			Transform transform = mTransform.get(entityId);
			Body body = mBox2dBody.get(entityId).body;
			Vector2 position = body.getPosition();
			transform.position.set(position);
			transform.angle = body.getAngle();

			Vector2 tp = transform.position;
			boolean wrapped = false;
			if (tp.x > VP_WIDTH/2) {
				tp.x -= VP_WIDTH;
				wrapped = true;
			}
			if (tp.x < -VP_WIDTH/2) {
				tp.x += VP_WIDTH;
				wrapped = true;
			}
			if (tp.y > VP_HEIGHT/2) {
				tp.y -= VP_HEIGHT;
				wrapped = true;
			}
			if (tp.y < -VP_HEIGHT/2) {
				tp.y += VP_HEIGHT;
				wrapped = true;
			}
			if (wrapped) {
				body.setTransform(tp, transform.angle);
			}
		}

		@Override protected void end () {
			if (debugDraw) {
				renderer.render(box2d, camera.combined);
			}
		}
	}

	public static class Priority extends PrioritySteering<Vector2> {

		public Priority (Steerable<Vector2> owner) {
			super(owner);
		}

		public Priority (Steerable<Vector2> owner, float epsilon) {
			super(owner, epsilon);
		}

		public Array<SteeringBehavior<Vector2>> getBehaviours () {
			return behaviors;
		}
	}

	public static class Steerer extends IteratingSystem {
		protected ComponentMapper<Transform> mTransform;
		protected ComponentMapper<ECSSteerable> mECSSteerable;
		protected ComponentMapper<Box2dBody> mBox2dBody;

		public Steerer () {
			super(Aspect.all(Transform.class, ECSSteerable.class, Box2dBody.class));
		}
		Vector2 tmp = new Vector2();
		private SteeringAcceleration<Vector2> output = new SteeringAcceleration<>(new Vector2());
		@Wire Physics physics;
		Proximity proximity;

		@Override protected void initialize () {
			proximity = new Proximity(physics, 100f);
		}

		@Override protected void process (int entityId) {
			ECSSteerable steerable = mECSSteerable.get(entityId);
			SteeringBehavior<Vector2> steering = steerable.steering;
			if (steering == null) return;
			proximity.setOwner(steerable);

			if (steering instanceof Priority) {
				Priority ps = (Priority)steering;
				for (SteeringBehavior<Vector2> behavior : ps.getBehaviours()) {
					if (behavior instanceof GroupBehavior) {
						GroupBehavior gp = (GroupBehavior)behavior;
						gp.setProximity(proximity);
					}
				}
			}

			Transform transform = mTransform.get(entityId);
			Box2dBody box2dBody = mBox2dBody.get(entityId);
			Body body = box2dBody.body;

			steerable.location.position.set(transform.position);
			steerable.location.orientation = transform.angle;
			steering.calculateSteering(output);

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

			Vector2 vel = body.getLinearVelocity();
			Vector2 wc = body.getWorldCenter();
			tmp.set(steerable.linearVelocity).sub(vel).scl(body.getMass());
			body.applyLinearImpulse(tmp.x, tmp.y, wc.x, wc.y, true);

			float targetAngle = location.orientation;
			float nextAngle = body.getAngle() + body.getAngularVelocity() / 3.0f;
			float totalRotation = targetAngle - nextAngle;
			while (totalRotation < -MathUtils.PI)
				totalRotation += MathUtils.PI2;
			while (totalRotation > MathUtils.PI)
				totalRotation -= MathUtils.PI2;
			float desiredAngularVelocity = totalRotation * 3;
			float impulse = body.getInertia() * desiredAngularVelocity;
			body.applyAngularImpulse(impulse, true);
		}

		private static class Proximity implements com.badlogic.gdx.ai.steer.Proximity<Vector2>, QueryCallback {
			private final Physics physics;
			private float radius;
			ECSSteerable owner;
			private ProximityCallback<Vector2> behaviorCallback;
			private int neighborCount;

			public Proximity (Physics physics, float radius) {
				this.physics = physics;
				this.radius = radius;
			}

			@Override public Steerable<Vector2> getOwner () {
				return owner;
			}

			@Override public void setOwner (Steerable<Vector2> owner) {
				this.owner = (ECSSteerable)owner;
				radius = this.owner.boundingRadius * 4;
			}

			@Override public int findNeighbors (ProximityCallback<Vector2> callback) {
				this.behaviorCallback = callback;
				neighborCount = 0;
				Vector2 position = owner.location.position;
				physics.box2d.QueryAABB(this, position.x - radius, position.y - radius, position.x + radius, position.y + radius);
				this.behaviorCallback = null;
				return neighborCount;
			}

			@Override public boolean reportFixture (Fixture fixture) {
				ECSSteerable other = (ECSSteerable)fixture.getBody().getUserData();
				if (other == null) return false;
				float range = radius + other.getBoundingRadius();

				// Make sure the current body is within the range.
				// Notice we're working in distance-squared space to avoid square root.
				float distanceSquare = other.getPosition().dst2(owner.getPosition());

				if (distanceSquare <= range * range) {
					if (behaviorCallback.reportNeighbor(other)) neighborCount++;
				}
				return true;
			}
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
			renderSteering(steerable.steering);


			Vector2 lv = steerable.getLinearVelocity();
			shapeRenderer.setColor(Color.GREEN);
			shapeRenderer.line(position.x, position.y, position.x + lv.x, position.y + lv.y);
		}

		private void renderSteering (SteeringBehavior<Vector2> steering) {
			if (steering instanceof Priority) {
				Priority pririty = (Priority)steering;
				for (SteeringBehavior<Vector2> behavior : pririty.getBehaviours()) {
					renderSteering(behavior);
				}
			}
			if (steering instanceof Wander) {
				renderWander((Wander)steering);
			}
			if (steering instanceof Proximity) {
				renderProximity((Proximity)steering);
			}
		}


		private void renderWander(Wander<Vector2> wander) {
			Vector2 itp = wander.getInternalTargetPosition();
			Vector2 center = wander.getWanderCenter();
			float radius = wander.getWanderRadius();
			float offset = wander.getWanderOffset();

			shapeRenderer.setColor(Color.VIOLET);
			shapeRenderer.circle(center.x, center.y, radius, 32);

			shapeRenderer.setColor(Color.RED);
			shapeRenderer.circle(itp.x, itp.y, .2f, 32);
		}

		private void renderProximity (Proximity proximity) {

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
			shapeRenderer.setColor(.75f, 0, .75f, 1);
			Vector2 position = transform.position;
			shapeRenderer.circle(position.x, position.y, .5f, 16);
			tmp.set(0, 1).rotateRad(transform.angle).nor().scl(.5f);
			shapeRenderer.setColor(Color.VIOLET);
			shapeRenderer.rectLine(position.x, position.y, position.x + tmp.x, position.y + tmp.y, .1f);
		}

		@Override protected void end () {
			shapeRenderer.end();
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, ECSBox2dSteerTest.class);
	}
}
