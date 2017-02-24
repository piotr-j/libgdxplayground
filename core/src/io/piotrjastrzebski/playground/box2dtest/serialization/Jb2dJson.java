//package io.piotrjastrzebski.playground.box2dtest.serialization;
//
//import com.badlogic.gdx.math.Vector2;
//import com.badlogic.gdx.physics.box2d.*;
//import com.badlogic.gdx.physics.box2d.joints.*;
//import com.badlogic.gdx.utils.*;
//
//import java.io.*;
//import java.lang.StringBuilder;
//import java.nio.charset.Charset;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by EvilEntity on 30/11/2016.
// */
//public class Jb2dJson {
//	public class Jb2dJsonCustomProperties {
//
//		ObjectMap<String, Integer> m_customPropertyMap_int;
//		ObjectMap<String, Double> m_customPropertyMap_float;
//		ObjectMap<String, String> m_customPropertyMap_string;
//		ObjectMap<String, Vector2> m_customPropertyMap_vec2;
//		ObjectMap<String, Boolean> m_customPropertyMap_bool;
//
//		public Jb2dJsonCustomProperties() {
//			m_customPropertyMap_int = new ObjectMap<>();
//			m_customPropertyMap_float = new ObjectMap<>();
//			m_customPropertyMap_string = new ObjectMap<>();
//			m_customPropertyMap_vec2 = new ObjectMap<>();
//			m_customPropertyMap_bool = new ObjectMap<>();
//		}
//	}
//
//	protected boolean m_useHumanReadableFloats;
//
//	protected int m_simulationPositionIterations;
//	protected int m_simulationVelocityIterations;
//	protected float m_simulationFPS;
//
//	protected ObjectMap<Integer, Body> m_indexToBodyMap;
//	protected ObjectMap<Body, Integer> m_bodyToIndexMap;
//	protected ObjectMap<Joint, Integer> m_jointToIndexMap;
//	protected Array<Body> m_bodies;
//	protected Array<Joint> m_joints;
//	protected Array<Jb2dJsonImage> m_images;
//
//	protected ObjectMap<Body, String> m_bodyToNameMap;
//	protected ObjectMap<Fixture, String> m_fixtureToNameMap;
//	protected ObjectMap<Joint, String> m_jointToNameMap;
//	protected ObjectMap<Jb2dJsonImage, String> m_imageToNameMap;
//
//	protected ObjectMap<Body, String> m_bodyToPathMap;
//	protected ObjectMap<Fixture, String> m_fixtureToPathMap;
//	protected ObjectMap<Joint, String> m_jointToPathMap;
//	protected ObjectMap<Jb2dJsonImage, String> m_imageToPathMap;
//
//	// This maps an item (Body, Fixture etc) to a set of custom properties.
//	// Use null for world properties.
//	protected Map<Object, Jb2dJsonCustomProperties> m_customPropertiesMap;
//
//	protected ObjectSet<Body> m_bodiesWithCustomProperties;
//	protected ObjectSet<Fixture> m_fixturesWithCustomProperties;
//	protected ObjectSet<Joint> m_jointsWithCustomProperties;
//	protected ObjectSet<Jb2dJsonImage> m_imagesWithCustomProperties;
//	protected ObjectSet<World> m_worldsWithCustomProperties;
//
//	public Jb2dJson() {
//		this(true);
//	}
//
//	public Jb2dJson(boolean useHumanReadableFloats) {
//
//		if (!useHumanReadableFloats) {
//			// The floatToHex function is not giving the same results
//			// as the original C++ version... not critical so worry about it
//			// later.
//			System.out.println("Non human readable floats are not implemented yet");
//			useHumanReadableFloats = true;
//		}
//
//		m_useHumanReadableFloats = useHumanReadableFloats;
//		m_simulationPositionIterations = 3;
//		m_simulationVelocityIterations = 8;
//		m_simulationFPS = 60;
//
//		m_indexToBodyMap = new ObjectMap<>();
//		m_bodyToIndexMap = new ObjectMap<>();
//		m_jointToIndexMap = new ObjectMap<>();
//		m_bodies = new Array<>();
//		m_joints = new Array<>();
//		m_images = new Array<>();
//
//		m_bodyToNameMap = new ObjectMap<>();
//		m_fixtureToNameMap = new ObjectMap<>();
//		m_jointToNameMap = new ObjectMap<>();
//		m_imageToNameMap = new ObjectMap<>();
//
//		m_bodyToPathMap = new ObjectMap<>();
//		m_fixtureToPathMap = new ObjectMap<>();
//		m_jointToPathMap = new ObjectMap<>();
//		m_imageToPathMap = new ObjectMap<>();
//
//		m_customPropertiesMap = new HashMap<>();
//
//		m_bodiesWithCustomProperties = new ObjectSet<>();
//		m_fixturesWithCustomProperties = new ObjectSet<>();
//		m_jointsWithCustomProperties = new ObjectSet<>();
//		m_imagesWithCustomProperties = new ObjectSet<>();
//		m_worldsWithCustomProperties = new ObjectSet<>();
//	}
//
//	public JsonValue writeToValue(World world) throws SerializationException {
//		if (null == world)
//			return new JsonValue(JsonValue.ValueType.object);
//
//		return b2j(world);
//	}
//
//	public String worldToString(World world, int indentFactor) throws SerializationException {
//		if (null == world)
//			return new String();
//
//		return b2j(world).toString();
//	}
//
//	public boolean writeToFile(World world, String filename, int indentFactor, StringBuilder errorMsg) {
//		if (null == world || null == filename)
//			return false;
//
//		PrintWriter writer;
//		try {
//			writer = new PrintWriter(filename);
//		} catch (FileNotFoundException e) {
//			errorMsg.append("Could not open file " + filename + "for writing");
//			return false;
//		}
//
//		try {
//			writer.println(b2j(world).toString());
//		} catch (SerializationException e) {
//			errorMsg.append("Error writing JSON to file: " + filename);
//		}
//		writer.close();
//
//		return true;
//	}
//
//	public JsonValue b2j(World world) throws SerializationException {
//		JsonValue worldValue = new JsonValue(JsonValue.ValueType.object);
//
//		m_bodyToIndexMap.clear();
//		m_jointToIndexMap.clear();
//
//		vecToJson("gravity", world.getGravity(), worldValue);
//		// no getter :/
////		worldValue.addChild("allowSleep", new JsonValue(world.isAllowSleep()));
//		worldValue.addChild("autoClearForces", new JsonValue(world.getAutoClearForces()));
//		// no getter :/
////		worldValue.addChild("warmStarting", new JsonValue(world.isWarmStarting()));
//		// no getter :/
////		worldValue.addChild("continuousPhysics", new JsonValue(world.isContinuousPhysics()));
//
//		JsonValue customPropertyValue = writeCustomPropertiesToJson(null);
//		if (customPropertyValue.child != null) {
//			worldValue.addChild("customProperties", customPropertyValue);
//		}
//
//		int i = 0;
//		Array<Body> bodies = new Array<>();
//		world.getBodies(bodies);
//		for (Body body : bodies) {
//			m_bodyToIndexMap.put(body, i);
//			worldValue.addChild("body", b2j(body));
//			i++;
//		}
//
//		// need two passes for joints because gear joints reference other joints
//		i = 0;
//		Array<Joint> joints = new Array<>();
//		world.getJoints(joints);
//		for (Joint joint : joints) {
//			if (joint.getType() == JointDef.JointType.GearJoint)
//				continue;
//			m_jointToIndexMap.put(joint, i);
//			worldValue.addChild("joint", b2j(joint));
//			i++;
//		}
//		for (Joint joint : joints) {
//			if (joint.getType() != JointDef.JointType.GearJoint)
//				continue;
//			m_jointToIndexMap.put(joint, i);
//			worldValue.addChild("joint", b2j(joint));
//			i++;
//		}
//
//		// Currently the only efficient way to add images to a Jb2dJson
//		// is by using the R.U.B.E editor. This code has not been tested,
//		// but should work ok.
//		i = 0;
//		for (Jb2dJsonImage image : m_imageToNameMap.keys()) {
//			worldValue.addChild("image", b2j(image));
//		}
//
//		m_bodyToIndexMap.clear();
//		m_jointToIndexMap.clear();
//
//		return worldValue;
//	}
//
//	public void setBodyName(Body body, String name) {
//		m_bodyToNameMap.put(body, name);
//	}
//
//	public void setFixtureName(Fixture fixture, String name) {
//		m_fixtureToNameMap.put(fixture, name);
//	}
//
//	public void setJointName(Joint joint, String name) {
//		m_jointToNameMap.put(joint, name);
//	}
//
//	public void setImageName(Jb2dJsonImage image, String name) {
//		m_imageToNameMap.put(image, name);
//	}
//
//	public void setBodyPath(Body body, String path) {
//		m_bodyToPathMap.put(body, path);
//	}
//
//	public void setFixturePath(Fixture fixture, String path) {
//		m_fixtureToPathMap.put(fixture, path);
//	}
//
//	public void setJointPath(Joint joint, String path) {
//		m_jointToPathMap.put(joint, path);
//	}
//
//	public void setImagePath(Jb2dJsonImage image, String path) {
//		m_imageToPathMap.put(image, path);
//	}
//
//	public JsonValue b2j(Body body) throws SerializationException {
//		JsonValue bodyValue = new JsonValue(JsonValue.ValueType.object);
//
//		String bodyName = getBodyName(body);
//		if (null != bodyName) {
//			bodyValue.addChild("name", new JsonValue(bodyName));
//		}
//
//		switch (body.getType()) {
//		case StaticBody:
//			bodyValue.addChild("type", new JsonValue(0));
//			break;
//		case KinematicBody:
//			bodyValue.addChild("type", new JsonValue(1));
//			break;
//		case DynamicBody:
//			bodyValue.addChild("type", new JsonValue(2));
//			break;
//		}
//
//		vecToJson("position", body.getPosition(), bodyValue);
//		floatToJson("angle", body.getAngle(), bodyValue);
//
//		vecToJson("linearVelocity", body.getLinearVelocity(), bodyValue);
//		floatToJson("angularVelocity", body.getAngularVelocity(), bodyValue);
//
//		if (body.getLinearDamping() != 0)
//			floatToJson("linearDamping", body.getLinearDamping(), bodyValue);
//		if (body.getAngularDamping() != 0)
//			floatToJson("angularDamping", body.getAngularDamping(), bodyValue);
//		if (body.getGravityScale() != 1)
//			floatToJson("gravityScale", body.getGravityScale(), bodyValue);
//
//		if (body.isBullet())
//			bodyValue.addChild("bullet", new JsonValue(true));
//		if (!body.isSleepingAllowed())
//			bodyValue.addChild("allowSleep", new JsonValue(false));
//		if (body.isAwake())
//			bodyValue.addChild("awake", new JsonValue(true));
//		if (!body.isActive())
//			bodyValue.addChild("active", new JsonValue(false));
//		if (body.isFixedRotation())
//			bodyValue.addChild("fixedRotation", new JsonValue(true));
//
//		MassData massData = body.getMassData();
//		if (massData.mass != 0)
//			floatToJson("massData-mass", massData.mass, bodyValue);
//		if (massData.center.x != 0 || massData.center.y != 0)
//			vecToJson("massData-center", massData.center, bodyValue);
//		if (massData.I != 0) {
//			floatToJson("massData-I", massData.I, bodyValue);
//		}
//
//		int i = 0;
//		for (Fixture fixture : body.getFixtureList()) {
//			bodyValue.addChild("fixture", b2j(fixture));
//		}
//
//
//		JsonValue customPropertyValue = writeCustomPropertiesToJson(body);
//		if (customPropertyValue.child != null) {
//			bodyValue.addChild("customProperties", customPropertyValue);
//		}
//
//		return bodyValue;
//	}
//
//	private static Vector2 tmp = new Vector2();
//	public JsonValue b2j(Fixture fixture) throws SerializationException {
//		JsonValue fixtureValue = new JsonValue(JsonValue.ValueType.object);
//
//		String fixtureName = getFixtureName(fixture);
//		if (null != fixtureName)
//			fixtureValue.addChild("name", new JsonValue(fixtureName));
//
//		if (fixture.getRestitution() != 0)
//			floatToJson("restitution", fixture.getRestitution(), fixtureValue);
//		if (fixture.getFriction() != 0)
//			floatToJson("friction", fixture.getFriction(), fixtureValue);
//		if (fixture.getDensity() != 0)
//			floatToJson("density", fixture.getDensity(), fixtureValue);
//		if (fixture.isSensor())
//			fixtureValue.addChild("sensor", new JsonValue(true));
//
//		Filter filter = fixture.getFilterData();
//		if (filter.categoryBits != 0x0001)
//			fixtureValue.addChild("filter-categoryBits", new JsonValue(filter.categoryBits));
//		if (filter.maskBits != -1)
//			fixtureValue.addChild("filter-maskBits", new JsonValue(filter.maskBits));
//		if (filter.groupIndex != 0)
//			fixtureValue.addChild("filter-groupIndex", new JsonValue(filter.groupIndex));
//
//		Shape shape = fixture.getShape();
//		switch (shape.getType()) {
//		case Circle: {
//			CircleShape circle = (CircleShape) shape;
//			JsonValue shapeValue = new JsonValue(JsonValue.ValueType.object);
//			floatToJson("radius", circle.getRadius(), shapeValue);
//			vecToJson("center", circle.getPosition(), shapeValue);
//			fixtureValue.addChild("circle", shapeValue);
//		}
//		break;
//		case Edge: {
//			EdgeShape edge = (EdgeShape) shape;
//			JsonValue shapeValue = new JsonValue(JsonValue.ValueType.object);
//			edge.getVertex1(tmp);
//			vecToJson("vertex1", tmp, shapeValue);
//			edge.getVertex2(tmp);
//			vecToJson("vertex2", tmp, shapeValue);
//			if (edge.hasVertex0())
//				shapeValue.addChild("hasVertex0", new JsonValue(true));
//			if (edge.hasVertex3())
//				shapeValue.addChild("hasVertex3", new JsonValue(true));
//			if (edge.hasVertex0()) {
//				edge.getVertex0(tmp);
//				vecToJson("vertex0", tmp, shapeValue);
//			}
//			if (edge.hasVertex3()) {
//				edge.getVertex3(tmp);
//				vecToJson("vertex3", tmp, shapeValue);
//			}
//			fixtureValue.addChild("edge", shapeValue);
//		}
//		break;
//		case Chain: {
//			ChainShape chain = (ChainShape) shape;
//			JsonValue shapeValue = new JsonValue(JsonValue.ValueType.object);
//			int count = chain.getVertexCount();
//			for (int i = 0; i < count; ++i) {
//				chain.getVertex(i, tmp);
//				vecToJson("vertices", tmp, shapeValue, i);
//			}
//
//			// todo this is for loop, right?
//			if (chain.m_hasPrevVertex)
//				shapeValue.addChild("hasPrevVertex", true);
//			if (chain.m_hasNextVertex)
//				shapeValue.addChild("hasNextVertex", true);
//			if (chain.m_hasPrevVertex)
//				vecToJson("prevVertex", chain.m_prevVertex, shapeValue);
//			if (chain.m_hasNextVertex)
//				vecToJson("nextVertex", chain.m_nextVertex, shapeValue);
//			fixtureValue.addChild("chain", shapeValue);
//		}
//		break;
//		case Polygon: {
//			PolygonShape poly = (PolygonShape) shape;
//			JsonValue shapeValue = new JsonValue(JsonValue.ValueType.object);
//			int vertexCount = poly.getVertexCount();
//			for (int i = 0; i < vertexCount; ++i) {
//				poly.getVertex(i, tmp);
//				vecToJson("vertices", tmp, shapeValue, i);
//			}
//			fixtureValue.addChild("polygon", shapeValue);
//		}
//		break;
//		default:
//			System.out.println("Unknown shape type : " + shape.getType());
//		}
//
//		JsonValue customPropertyValue = writeCustomPropertiesToJson(fixture);
//		if (customPropertyValue.child != null) {
//			fixtureValue.addChild("customProperties", customPropertyValue);
//		}
//
//		return fixtureValue;
//	}
//
//	public JsonValue b2j(Joint joint) throws SerializationException {
//		JsonValue jointValue = new JsonValue(JsonValue.ValueType.object);
//
//		String jointName = getJointName(joint);
//		if (null != jointName)
//			jointValue.addChild("name", new JsonValue(jointName));
//
//		int bodyIndexA = lookupBodyIndex(joint.getBodyA());
//		int bodyIndexB = lookupBodyIndex(joint.getBodyB());
//		jointValue.addChild("bodyA", new JsonValue(bodyIndexA));
//		jointValue.addChild("bodyB", new JsonValue(bodyIndexB));
//		if (joint.getCollideConnected())
//			jointValue.addChild("collideConnected", new JsonValue(true));
//
//		Body bodyA = joint.getBodyA();
//		Body bodyB = joint.getBodyB();
//
//		// why do Joint.getAnchor methods need to take an argOut style
//		// parameter!?
////		Vector2 tmpAnchor = new Vector2();
//
//		switch (joint.getType()) {
//		case RevoluteJoint: {
//			jointValue.addChild("type", new JsonValue("revolute"));
//
//			RevoluteJoint revoluteJoint = (RevoluteJoint) joint;
//			revoluteJoint.getAnchorA();
//			vecToJson("anchorA", bodyA.getLocalPoint(revoluteJoint.getAnchorA()), jointValue);
//			revoluteJoint.getAnchorB();
//			vecToJson("anchorB", bodyB.getLocalPoint(revoluteJoint.getAnchorB()), jointValue);
//			floatToJson("refAngle", bodyB.getAngle() - bodyA.getAngle() - revoluteJoint.getJointAngle(), jointValue);
//			floatToJson("jointSpeed", revoluteJoint.getJointSpeed(), jointValue);
//			jointValue.addChild("enableLimit", new JsonValue(revoluteJoint.isLimitEnabled()));
//			floatToJson("lowerLimit", revoluteJoint.getLowerLimit(), jointValue);
//			floatToJson("upperLimit", revoluteJoint.getUpperLimit(), jointValue);
//			jointValue.addChild("enableMotor", new JsonValue(revoluteJoint.isMotorEnabled()));
//			floatToJson("motorSpeed", revoluteJoint.getMotorSpeed(), jointValue);
//			floatToJson("maxMotorTorque", revoluteJoint.getMaxMotorTorque(), jointValue);
//		}
//		break;
//		case PrismaticJoint: {
//			jointValue.addChild("type", new JsonValue("prismatic"));
//
//			PrismaticJoint prismaticJoint = (PrismaticJoint) joint;
//			vecToJson("anchorA", bodyA.getLocalPoint(prismaticJoint.getAnchorA()), jointValue);
//			vecToJson("anchorB", bodyB.getLocalPoint(prismaticJoint.getAnchorB()), jointValue);
//			vecToJson("localAxisA", prismaticJoint.getLocalAxisA(), jointValue);
//			floatToJson("refAngle", prismaticJoint.getReferenceAngle(), jointValue);
//			jointValue.addChild("enableLimit", new JsonValue(prismaticJoint.isLimitEnabled()));
//			floatToJson("lowerLimit", prismaticJoint.getLowerLimit(), jointValue);
//			floatToJson("upperLimit", prismaticJoint.getUpperLimit(), jointValue);
//			jointValue.addChild("enableMotor", new JsonValue(prismaticJoint.isMotorEnabled()));
//			floatToJson("maxMotorForce", prismaticJoint.getMaxMotorForce(), jointValue);
//			floatToJson("motorSpeed", prismaticJoint.getMotorSpeed(), jointValue);
//		}
//		break;
//		case DistanceJoint: {
//			jointValue.addChild("type", new JsonValue("distance"));
//
//			DistanceJoint distanceJoint = (DistanceJoint) joint;
//			vecToJson("anchorA", bodyA.getLocalPoint(distanceJoint.getAnchorA()), jointValue);
//			vecToJson("anchorB", bodyB.getLocalPoint(distanceJoint.getAnchorB()), jointValue);
//			floatToJson("length", distanceJoint.getLength(), jointValue);
//			floatToJson("frequency", distanceJoint.getFrequency(), jointValue);
//			floatToJson("dampingRatio", distanceJoint.getDampingRatio(), jointValue);
//		}
//		break;
//		case PulleyJoint: {
//			jointValue.addChild("type", new JsonValue("pulley"));
//
//			PulleyJoint pulleyJoint = (PulleyJoint) joint;
//			vecToJson("groundAnchorA", pulleyJoint.getGroundAnchorA(), jointValue);
//			vecToJson("groundAnchorB", pulleyJoint.getGroundAnchorB(), jointValue);
//			vecToJson("anchorA", bodyA.getLocalPoint(pulleyJoint.getAnchorA()), jointValue);
//			floatToJson("lengthA", (pulleyJoint.getGroundAnchorA().sub(pulleyJoint.getAnchorA())).len(), jointValue);
//			vecToJson("anchorB", bodyB.getLocalPoint(pulleyJoint.getAnchorB()), jointValue);
//			floatToJson("lengthB", (pulleyJoint.getGroundAnchorB().sub(pulleyJoint.getAnchorB())).len(), jointValue);
//			floatToJson("ratio", pulleyJoint.getRatio(), jointValue);
//		}
//		break;
//		case MouseJoint: {
//			jointValue.addChild("type", new JsonValue("mouse"));
//
//			MouseJoint mouseJoint = (MouseJoint) joint;
//			vecToJson("target", mouseJoint.getTarget(), jointValue);
//			vecToJson("anchorB", mouseJoint.getAnchorB(), jointValue);
//			floatToJson("maxForce", mouseJoint.getMaxForce(), jointValue);
//			floatToJson("frequency", mouseJoint.getFrequency(), jointValue);
//			floatToJson("dampingRatio", mouseJoint.getDampingRatio(), jointValue);
//		}
//		break;
//		case GearJoint: {
//			jointValue.addChild("type", new JsonValue("gear"));
//
//			GearJoint gearJoint = (GearJoint)joint;
//			int jointIndex1 = lookupJointIndex( gearJoint.getJoint1() );
//			int jointIndex2 = lookupJointIndex( gearJoint.getJoint2() );
//			jointValue.addChild("joint1", new JsonValue(jointIndex1));
//			jointValue.addChild("joint2", new JsonValue(jointIndex2));
//			jointValue.addChild("ratio", new JsonValue(gearJoint.getRatio()));
//
//		}
//		break;
//		case WheelJoint: {
//			jointValue.addChild("type", new JsonValue("wheel"));
//
//			WheelJoint wheelJoint = (WheelJoint)joint;
//			vecToJson("anchorA", bodyA.getLocalPoint(wheelJoint.getAnchorA()), jointValue);
//			vecToJson("anchorB", bodyB.getLocalPoint(wheelJoint.getAnchorB()), jointValue);
//			vecToJson("localAxisA", wheelJoint.getLocalAxisA(), jointValue);
//			jointValue.addChild("enableMotor", new JsonValue(wheelJoint.isMotorEnabled()));
//			floatToJson("motorSpeed", wheelJoint.getMotorSpeed(), jointValue);
//			floatToJson("maxMotorTorque", wheelJoint.getMaxMotorTorque(), jointValue);
//			floatToJson("springFrequency", wheelJoint.getSpringFrequencyHz(), jointValue);
//			floatToJson("springDampingRatio", wheelJoint.getSpringDampingRatio(), jointValue);
//
//		}
//		break;
//		case WeldJoint: {
//			jointValue.addChild("type", new JsonValue("weld"));
//
//			WeldJoint weldJoint = (WeldJoint) joint;
//			vecToJson("anchorA", bodyA.getLocalPoint(weldJoint.getAnchorA()), jointValue);
//			vecToJson("anchorB", bodyB.getLocalPoint(weldJoint.getAnchorB()), jointValue);
//			// no getter?
//			floatToJson("refAngle", weldJoint.getReferenceAngle(), jointValue);
//		}
//		break;
//		case FrictionJoint: {
//			jointValue.addChild("type", new JsonValue("friction"));
//
//			FrictionJoint frictionJoint = (FrictionJoint) joint;
//			vecToJson("anchorA", bodyA.getLocalPoint(frictionJoint.getAnchorA()), jointValue);
//			vecToJson("anchorB", bodyB.getLocalPoint(frictionJoint.getAnchorB()), jointValue);
//			floatToJson("maxForce", frictionJoint.getMaxForce(), jointValue);
//			floatToJson("maxTorque", frictionJoint.getMaxTorque(), jointValue);
//		}
//		break;
//		case RopeJoint: {
//			// Rope joints are apparently not implemented in JBox2D yet, but
//			// when they are, commenting out the following section should work.
//
//			jointValue.addChild("type", new JsonValue("rope");
//
//			RopeJoint ropeJoint = (RopeJoint)joint;
//			vecToJson("anchorA", bodyA.getLocalPoint(ropeJoint.getAnchorA()), jointValue);
//			vecToJson("anchorB", bodyB.getLocalPoint(ropeJoint.getAnchorB()), jointValue);
//			floatToJson("maxLength", ropeJoint.getMaxLength(), jointValue);
//
//		}
//		break;
//		case Unknown:
//		default:
//			System.out.println("Unknown joint type : " + joint.getType());
//		}
//
//		JsonValue customPropertyValue = writeCustomPropertiesToJson(joint);
//		if (customPropertyValue.child != null)
//			jointValue.addChild("customProperties", customPropertyValue);
//
//		return jointValue;
//	}
//
//	JsonValue b2j(Jb2dJsonImage image) throws SerializationException {
//		JsonValue imageValue = new JsonValue(JsonValue.ValueType.object);
//
//		if (null != image.body)
//			imageValue.addChild("body", new JsonValue(lookupBodyIndex(image.body)));
//		else
//			imageValue.addChild("body", new JsonValue(-1));
//
//		if (null != image.name)
//			imageValue.addChild("name", new JsonValue(image.name));
//		if (null != image.file)
//			imageValue.addChild("file", new JsonValue(image.file));
//
//		vecToJson("center", image.center, imageValue);
//		floatToJson("angle", image.angle, imageValue);
//		floatToJson("scale", image.scale, imageValue);
//		floatToJson("aspectScale", image.aspectScale, imageValue);
//		if (image.flip)
//			imageValue.addChild("flip", new JsonValue(true));
//		floatToJson("opacity", image.opacity, imageValue);
//		imageValue.addChild("filter", new JsonValue(image.filter));
//		floatToJson("renderOrder", image.renderOrder, imageValue);
//
//		boolean defaultColorTint = true;
//		for (int i = 0; i < 4; i++) {
//			if ( image.colorTint[i] != 255 ) {
//				defaultColorTint = false;
//				break;
//			}
//		}
//
//		if ( !defaultColorTint ) {
//			JsonValue array = new JsonValue(JsonValue.ValueType.array);
//			imageValue.addChild("colorTint", array);
//			for (int i = 0; i < 4; i++)
//				array.addChild(new JsonValue(image.colorTint[i]));
//		}
//
//		// image->updateCorners();
//		for (int i = 0; i < 4; i++)
//			vecToJson("corners", image.corners[i], imageValue, i);
//
//		// image->updateUVs();
//		for (int i = 0; i < 2 * image.numPoints; i++) {
//			vecToJson("glVertexPointer", image.points[i], imageValue, i);
//			vecToJson("glTexCoordPointer", image.uvCoords[i], imageValue, i);
//		}
//		for (int i = 0; i < image.numIndices; i++)
//			vecToJson("glDrawElements", image.indices[i], imageValue, i);
//
//		JsonValue customPropertyValue = writeCustomPropertiesToJson(image);
//		if (customPropertyValue.child != null)
//			imageValue.addChild("customProperties", customPropertyValue);
//
//		return imageValue;
//	}
//
//	Body lookupBodyFromIndex(int index) {
//		if (m_indexToBodyMap.containsKey(index))
//			return m_indexToBodyMap.get(index);
//		else
//			return null;
//	}
//
//	protected int lookupBodyIndex(Body body) {
//		Integer val = m_bodyToIndexMap.get(body);
//		if (null != val)
//			return val.intValue();
//		else
//			return -1;
//	}
//
//	protected int lookupJointIndex(Joint joint) {
//		Integer val = m_jointToIndexMap.get(joint);
//		if (null != val)
//			return val.intValue();
//		else
//			return -1;
//	}
//
//	public String getBodyName(Body body) {
//		return m_bodyToNameMap.get(body);
//	}
//
//	public String getFixtureName(Fixture fixture) {
//		return m_fixtureToNameMap.get(fixture);
//	}
//
//	public String getJointName(Joint joint) {
//		return m_jointToNameMap.get(joint);
//	}
//
//	public String getImageName(Jb2dJsonImage image) {
//		return m_imageToNameMap.get(image);
//	}
//
//	public String getBodyPath(Body body) {
//		return m_bodyToPathMap.get(body);
//	}
//
//	public String getFixturePath(Fixture fixture) {
//		return m_fixtureToPathMap.get(fixture);
//	}
//
//	public String getJointPath(Joint joint) {
//		return m_jointToPathMap.get(joint);
//	}
//
//	public String getImagePath(Jb2dJsonImage image) {
//		return m_imageToPathMap.get(image);
//	}
//
//	public String floatToHex(float f) {
//		int bits = Float.floatToIntBits(f);
//		return Integer.toHexString(bits);
//	}
//
//	public void floatToJson(String name, float f, JsonValue value) throws SerializationException {
//		// cut down on file space for common values
//		if (f == 0)
//			value.addChild(name, new JsonValue(0));
//		else if (f == 1)
//			value.addChild(name, new JsonValue(1));
//		else {
//			if (m_useHumanReadableFloats)
//				value.addChild(name, new JsonValue(f));
//			else
//				value.addChild(name, new JsonValue(floatToHex(f)));
//		}
//	}
//
//	public void vecToJson(String name, int v, JsonValue value, int index) throws SerializationException {
//		if (index > -1) {
//			JsonValue array = value.get(name);
//			array.addChild(new JsonValue(v));
//		} else
//			value.addChild(name, new JsonValue(v));
//	}
//
//	public void vecToJson(String name, float v, JsonValue value, int index) throws SerializationException {
//		if (index > -1) {
//			if (m_useHumanReadableFloats) {
//				JsonValue array = value.get(name);
//				array.addChild(new JsonValue(v));
//			} else {
//				JsonValue array = value.get(name);
//				if (v == 0)
//					array.addChild(new JsonValue(0));
//				else if (v == 1)
//					array.addChild(new JsonValue(1));
//				else
//					array.addChild(new JsonValue(floatToHex(v)));
//			}
//		} else
//			floatToJson(name, v, value);
//	}
//
//	public void vecToJson(String name, Vector2 vec, JsonValue value) throws SerializationException {
//		vecToJson(name, vec, value, -1);
//	}
//
//	public void vecToJson(String name, Vector2 vec, JsonValue value, int index) throws SerializationException {
//		if (index > -1) {
//			if (m_useHumanReadableFloats) {
//				boolean alreadyHadArray = value.has(name);
//				JsonValue arrayX = alreadyHadArray ? value.get(name).get("x") : new JsonValue(JsonValue.ValueType.array);
//				JsonValue arrayY = alreadyHadArray ? value.get(name).get("y") : new JsonValue(JsonValue.ValueType.array);
//				arrayX.addChild(new JsonValue(vec.x));
//				arrayY.addChild(new JsonValue(vec.y));
//				if (!alreadyHadArray) {
//					JsonValue subValue = new JsonValue(JsonValue.ValueType.object);
//					subValue.addChild("x", arrayX);
//					subValue.addChild("y", arrayY);
//					value.addChild(name, subValue);
//				}
//			} else {
//				boolean alreadyHadArray = value.has(name);
//				JsonValue arrayX = alreadyHadArray ? value.get(name).get("x") : new JsonValue(JsonValue.ValueType.array);
//				JsonValue arrayY = alreadyHadArray ? value.get(name).get("y") : new JsonValue(JsonValue.ValueType.array);
//				if (vec.x == 0)
//					arrayX.addChild(new JsonValue(0));
//				else if (vec.x == 1)
//					arrayX.addChild(new JsonValue(1));
//				else
//					arrayX.addChild(new JsonValue(floatToHex(vec.x)));
//				if (vec.y == 0)
//					arrayY.addChild(new JsonValue(0));
//				else if (vec.y == 1)
//					arrayY.addChild(new JsonValue(1));
//				else
//					arrayY.addChild(new JsonValue(floatToHex(vec.y)));
//				if (!alreadyHadArray) {
//					JsonValue subValue = new JsonValue(JsonValue.ValueType.object);
//					subValue.addChild("x", arrayX);
//					subValue.addChild("y", arrayY);
//					value.addChild(name, subValue);
//				}
//			}
//		} else {
//			if (vec.x == 0 && vec.y == 0)
//				value.addChild(name, new JsonValue(0));// cut down on file space for common values
//			else {
//				JsonValue vecValue = new JsonValue(JsonValue.ValueType.object);
//				floatToJson("x", vec.x, vecValue);
//				floatToJson("y", vec.y, vecValue);
//				value.addChild(name, vecValue);
//			}
//		}
//	}
//
//	public void clear() {
//		m_indexToBodyMap.clear();
//		m_bodyToIndexMap.clear();
//		m_jointToIndexMap.clear();
//		m_bodies.clear();
//		m_joints.clear();
//		m_images.clear();
//
//		m_bodyToNameMap.clear();
//		m_fixtureToNameMap.clear();
//		m_jointToNameMap.clear();
//		m_imageToNameMap.clear();
//
//		m_bodyToPathMap.clear();
//		m_fixtureToPathMap.clear();
//		m_jointToPathMap.clear();
//		m_imageToPathMap.clear();
//	}
//
//	// Pass null for existingWorld to create a new world
//	public World readFromJsonValue(JsonValue worldValue, World existingWorld) throws SerializationException {
//		clear();
//
//		return j2b2World(worldValue, existingWorld);
//	}
//
//	// Pass null for existingWorld to create a new world
//	public World readFromString(String str, StringBuilder errorMsg, World existingWorld) {
//		try {
//			JsonValue worldValue = new JsonValue(str);
//			return j2b2World(worldValue, existingWorld);
//		} catch (SerializationException e) {
//			errorMsg.append("Failed to parse JSON");
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	// Pass null for existingWorld to create a new world
//	public World readFromFile(String filename, StringBuilder errorMsg, World existingWorld) {
//		if (null == filename)
//			return null;
//
//		String str = new String();
//		try {
//			InputStream fis;
//			fis = new FileInputStream(filename);
//			BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
//			String line;
//			while ((line = br.readLine()) != null) {
//				str += line;
//			}
//		} catch (FileNotFoundException e) {
//			errorMsg.append("Could not open file for reading: " + filename);
//			return null;
//		} catch (IOException e) {
//			errorMsg.append("Error reading file: " + filename);
//			return null;
//		}
//
//		try {
//			JsonValue worldValue = new JsonValue(str);
//			return j2b2World(worldValue, existingWorld);
//		} catch (SerializationException e) {
//			errorMsg.append("\nFailed to parse JSON: " + filename);
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	public World j2b2World(JsonValue worldValue, World existingWorld) throws SerializationException {
//		World world = existingWorld;
//		if ( world == null )
//			world = new World(jsonToVec("gravity", worldValue));
//
//		world.setAllowSleep(worldValue.getBoolean("allowSleep"));
//
//		world.setAutoClearForces(worldValue.getBoolean("autoClearForces"));
//		world.setWarmStarting(worldValue.getBoolean("warmStarting"));
//		world.setContinuousPhysics(worldValue.getBoolean("continuousPhysics"));
//		// world.setSubStepping( worldValue.getBoolean("subStepping") );
//
//		readCustomPropertiesFromJson(world, worldValue);
//
//		int i = 0;
//		JsonValue bodyValues = worldValue.optJsonValue("body");
//		if (null != bodyValues) {
//			int numBodyValues = bodyValues.length();
//			for (i = 0; i < numBodyValues; i++) {
//				JsonValue bodyValue = bodyValues.get(i);
//				Body body = j2b2Body(world, bodyValue);
//				readCustomPropertiesFromJson(body, bodyValue);
//				m_bodies.add(body);
//				m_indexToBodyMap.addChild(i, body);
//			}
//		}
//
//		// need two passes for joints because gear joints reference other joints
//		JsonValue jointValues = worldValue.optJsonValue("joint");
//		if (null != jointValues) {
//			int numJointValues = jointValues.length();
//			for (i = 0; i < numJointValues; i++) {
//				JsonValue jointValue = jointValues.get(i);
//				if (!jointValue.optString("type", "").equals("gear")) {
//					Joint joint = j2b2Joint(world, jointValue);
//					readCustomPropertiesFromJson(joint, jointValue);
//					m_joints.add(joint);
//				}
//			}
//			for (i = 0; i < numJointValues; i++) {
//				JsonValue jointValue = jointValues.get(i);
//				if (jointValue.optString("type", "").equals("gear")) {
//					Joint joint = j2b2Joint(world, jointValue);
//					readCustomPropertiesFromJson(joint, jointValue);
//					m_joints.add(joint);
//				}
//			}
//		}
//
//		i = 0;
//		JsonValue imageValues = worldValue.optJsonValue("image");
//		if (null != imageValues) {
//			int numImageValues = imageValues.length();
//			for (i = 0; i < numImageValues; i++) {
//				JsonValue imageValue = imageValues.get(i);
//				Jb2dJsonImage image = j2b2dJsonImage(imageValue);
//				readCustomPropertiesFromJson(image, imageValue);
//				m_images.add(image);
//			}
//		}
//
//		return world;
//	}
//
//	public Body j2b2Body(World world, JsonValue bodyValue) throws SerializationException {
//		BodyDef bodyDef = new BodyDef();
//		switch (bodyValue.getInt("type")) {
//		case 0:
//			bodyDef.type = BodyType.STATIC;
//			break;
//		case 1:
//			bodyDef.type = BodyType.KINEMATIC;
//			break;
//		case 2:
//			bodyDef.type = BodyType.DYNAMIC;
//			break;
//		}
//		bodyDef.position = jsonToVec("position", bodyValue);
//		bodyDef.angle = jsonToFloat("angle", bodyValue);
//		bodyDef.linearVelocity = jsonToVec("linearVelocity", bodyValue);
//		bodyDef.angularVelocity = jsonToFloat("angularVelocity", bodyValue);
//		bodyDef.linearDamping = jsonToFloat("linearDamping", bodyValue, -1, 0);
//		bodyDef.angularDamping = jsonToFloat("angularDamping", bodyValue, -1, 0);
//		bodyDef.gravityScale = jsonToFloat("gravityScale", bodyValue, -1, 1);
//
//		bodyDef.allowSleep = bodyValue.optBoolean("allowSleep", true);
//		bodyDef.awake = bodyValue.optBoolean("awake", false);
//		bodyDef.fixedRotation = bodyValue.optBoolean("fixedRotation");
//		bodyDef.bullet = bodyValue.optBoolean("bullet", false);
//		bodyDef.active = bodyValue.optBoolean("active", true);
//
//		Body body = world.createBody(bodyDef);
//
//		String bodyName = bodyValue.optString("name", "");
//		if ("" != bodyName)
//			setBodyName(body, bodyName);
//
//		String bodyPath = bodyValue.optString("path", "");
//		if ("" != bodyPath)
//			setBodyPath(body, bodyPath);
//
//		int i = 0;
//		JsonValue fixtureValues = bodyValue.optJsonValue("fixture");
//		if (null != fixtureValues) {
//			int numFixtureValues = fixtureValues.length();
//			for (i = 0; i < numFixtureValues; i++) {
//				JsonValue fixtureValue = fixtureValues.get(i);
//				Fixture fixture = j2b2Fixture(body, fixtureValue);
//				readCustomPropertiesFromJson(fixture, fixtureValue);
//			}
//		}
//
//		// may be necessary if user has overridden mass characteristics
//		MassData massData = new MassData();
//		massData.mass = jsonToFloat("massData-mass", bodyValue);
//		massData.center.set(jsonToVec("massData-center", bodyValue));
//		massData.I = jsonToFloat("massData-I", bodyValue);
//		body.setMassData(massData);
//
//		return body;
//	}
//
//	Fixture j2b2Fixture(Body body, JsonValue fixtureValue) throws SerializationException {
//
//		if (null == fixtureValue)
//			return null;
//
//		FixtureDef fixtureDef = new FixtureDef();
//		fixtureDef.restitution = jsonToFloat("restitution", fixtureValue);
//		fixtureDef.friction = jsonToFloat("friction", fixtureValue);
//		fixtureDef.density = jsonToFloat("density", fixtureValue);
//		fixtureDef.isSensor = fixtureValue.optBoolean("sensor", false);
//
//		fixtureDef.filter.categoryBits = fixtureValue.optInt("filter-categoryBits", 0x0001);
//		fixtureDef.filter.maskBits = fixtureValue.optInt("filter-maskBits", 0xffff);
//		fixtureDef.filter.groupIndex = fixtureValue.optInt("filter-groupIndex", 0);
//
//		Fixture fixture = null;
//		if (null != fixtureValue.optJsonValue("circle")) {
//			JsonValue circleValue = fixtureValue.get("circle");
//			CircleShape circleShape = new CircleShape();
//			circleShape.m_radius = jsonToFloat("radius", circleValue);
//			circleShape.m_p.set(jsonToVec("center", circleValue));
//			fixtureDef.shape = circleShape;
//			fixture = body.createFixture(fixtureDef);
//		} else if (null != fixtureValue.optJsonValue("edge")) {
//			JsonValue edgeValue = fixtureValue.get("edge");
//			EdgeShape edgeShape = new EdgeShape();
//			edgeShape.m_vertex1.set(jsonToVec("vertex1", edgeValue));
//			edgeShape.m_vertex2.set(jsonToVec("vertex2", edgeValue));
//			edgeShape.m_hasVertex0 = edgeValue.optBoolean("hasVertex0", false);
//			edgeShape.m_hasVertex3 = edgeValue.optBoolean("hasVertex3", false);
//			if (edgeShape.m_hasVertex0)
//				edgeShape.m_vertex0.set(jsonToVec("vertex0", edgeValue));
//			if (edgeShape.m_hasVertex3)
//				edgeShape.m_vertex3.set(jsonToVec("vertex3", edgeValue));
//			fixtureDef.shape = edgeShape;
//			fixture = body.createFixture(fixtureDef);
//		} else if (null != fixtureValue.optJsonValue("loop")) {// support old
//			// format (r197)
//			JsonValue chainValue = fixtureValue.get("loop");
//			ChainShape chainShape = new ChainShape();
//			int numVertices = chainValue.get("x").length();
//			Vector2 vertices[] = new Vector2[numVertices];
//			for (int i = 0; i < numVertices; i++)
//				vertices[i].set(jsonToVec("vertices", chainValue, i));
//			chainShape.createLoop(vertices, numVertices);
//			fixtureDef.shape = chainShape;
//			fixture = body.createFixture(fixtureDef);
//		} else if (null != fixtureValue.optJsonValue("chain")) {
//			JsonValue chainValue = fixtureValue.get("chain");
//			ChainShape chainShape = new ChainShape();
//			int numVertices = chainValue.get("vertices").get("x").length();
//			Vector2 vertices[] = new Vector2[numVertices];
//			for (int i = 0; i < numVertices; i++)
//				vertices[i] = jsonToVec("vertices", chainValue, i);
//			chainShape.createChain(vertices, numVertices);
//			chainShape.m_hasPrevVertex = chainValue.optBoolean("hasPrevVertex", false);
//			chainShape.m_hasNextVertex = chainValue.optBoolean("hasNextVertex", false);
//			if (chainShape.m_hasPrevVertex)
//				chainShape.m_prevVertex.set(jsonToVec("prevVertex", chainValue));
//			if (chainShape.m_hasNextVertex)
//				chainShape.m_nextVertex.set(jsonToVec("nextVertex", chainValue));
//			fixtureDef.shape = chainShape;
//			fixture = body.createFixture(fixtureDef);
//		} else if (null != fixtureValue.optJsonValue("polygon")) {
//			JsonValue polygonValue = fixtureValue.get("polygon");
//			Vector2 vertices[] = new Vector2[Settings.maxPolygonVertices];
//			int numVertices = polygonValue.get("vertices").get("x").length();
//			if (numVertices > Settings.maxPolygonVertices) {
//				System.out.println("Ignoring polygon fixture with too many vertices.");
//			} else if (numVertices < 2) {
//				System.out.println("Ignoring polygon fixture less than two vertices.");
//			} else if (numVertices == 2) {
//				System.out.println("Creating edge shape instead of polygon with two vertices.");
//				EdgeShape edgeShape = new EdgeShape();
//				edgeShape.m_vertex1.set(jsonToVec("vertices", polygonValue, 0));
//				edgeShape.m_vertex2.set(jsonToVec("vertices", polygonValue, 1));
//				fixtureDef.shape = edgeShape;
//				fixture = body.createFixture(fixtureDef);
//			} else {
//				PolygonShape polygonShape = new PolygonShape();
//				for (int i = 0; i < numVertices; i++)
//					vertices[i] = jsonToVec("vertices", polygonValue, i);
//				polygonShape.set(vertices, numVertices);
//				fixtureDef.shape = polygonShape;
//				fixture = body.createFixture(fixtureDef);
//			}
//		}
//
//		String fixtureName = fixtureValue.optString("name", "");
//		if (fixtureName != "") {
//			setFixtureName(fixture, fixtureName);
//		}
//
//		String fixturePath = fixtureValue.optString("path", "");
//		if (fixturePath != "") {
//			setFixturePath(fixture, fixturePath);
//		}
//
//		return fixture;
//	}
//
//	Joint j2b2Joint(World world, JsonValue jointValue) throws SerializationException {
//		Joint joint = null;
//
//		int bodyIndexA = jointValue.getInt("bodyA");
//		int bodyIndexB = jointValue.getInt("bodyB");
//		if (bodyIndexA >= m_bodies.size() || bodyIndexB >= m_bodies.size())
//			return null;
//
//		// keep these in scope after the if/else below
//		RevoluteJointDef revoluteDef;
//		PrismaticJointDef prismaticDef;
//		DistanceJointDef distanceDef;
//		PulleyJointDef pulleyDef;
//		MouseJointDef mouseDef;
//		GearJointDef gearDef;
//		// WheelJointDef wheelDef;
//		WeldJointDef weldDef;
//		FrictionJointDef frictionDef;
//		RopeJointDef ropeDef;
//
//		// will be used to select one of the above to work with
//		JointDef jointDef = null;
//
//		Vector2 mouseJointTarget = new Vector2(0, 0);
//		String type = jointValue.getString("type", "");
//		if (type.equals("revolute")) {
//			jointDef = revoluteDef = new RevoluteJointDef();
//			revoluteDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
//			revoluteDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
//			revoluteDef.referenceAngle = jsonToFloat("refAngle", jointValue);
//			revoluteDef.enableLimit = jointValue.getBoolean("enableLimit", false);
//			revoluteDef.lowerAngle = jsonToFloat("lowerLimit", jointValue);
//			revoluteDef.upperAngle = jsonToFloat("upperLimit", jointValue);
//			revoluteDef.enableMotor = jointValue.getBoolean("enableMotor", false);
//			revoluteDef.motorSpeed = jsonToFloat("motorSpeed", jointValue);
//			revoluteDef.maxMotorTorque = jsonToFloat("maxMotorTorque", jointValue);
//		} else if (type.equals("prismatic")) {
//			jointDef = prismaticDef = new PrismaticJointDef();
//			prismaticDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
//			prismaticDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
//			if (jointValue.has("localAxisA"))
//				prismaticDef.localAxisA.set(jsonToVec("localAxisA", jointValue));
//			else
//				prismaticDef.localAxisA.set(jsonToVec("localAxis1", jointValue));
//			prismaticDef.referenceAngle = jsonToFloat("refAngle", jointValue);
//			prismaticDef.enableLimit = jointValue.getBoolean("enableLimit");
//			prismaticDef.lowerTranslation = jsonToFloat("lowerLimit", jointValue);
//			prismaticDef.upperTranslation = jsonToFloat("upperLimit", jointValue);
//			prismaticDef.enableMotor = jointValue.getBoolean("enableMotor");
//			prismaticDef.motorSpeed = jsonToFloat("motorSpeed", jointValue);
//			prismaticDef.maxMotorForce = jsonToFloat("maxMotorForce", jointValue);
//		} else if (type.equals("distance")) {
//			jointDef = distanceDef = new DistanceJointDef();
//			distanceDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
//			distanceDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
//			distanceDef.length = jsonToFloat("length", jointValue);
//			distanceDef.frequencyHz = jsonToFloat("frequency", jointValue);
//			distanceDef.dampingRatio = jsonToFloat("dampingRatio", jointValue);
//		} else if (type.equals("pulley")) {
//			jointDef = pulleyDef = new PulleyJointDef();
//			pulleyDef.groundAnchorA.set(jsonToVec("groundAnchorA", jointValue));
//			pulleyDef.groundAnchorB.set(jsonToVec("groundAnchorB", jointValue));
//			pulleyDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
//			pulleyDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
//			pulleyDef.lengthA = jsonToFloat("lengthA", jointValue);
//			pulleyDef.lengthB = jsonToFloat("lengthB", jointValue);
//			pulleyDef.ratio = jsonToFloat("ratio", jointValue);
//		} else if (type.equals("mouse")) {
//			jointDef = mouseDef = new MouseJointDef();
//			mouseJointTarget = jsonToVec("target", jointValue);
//			mouseDef.target.set(jsonToVec("anchorB", jointValue));// alter after creating joint
//			mouseDef.maxForce = jsonToFloat("maxForce", jointValue);
//			mouseDef.frequencyHz = jsonToFloat("frequency", jointValue);
//			mouseDef.dampingRatio = jsonToFloat("dampingRatio", jointValue);
//		}
//		// Gear joints are apparently not implemented in JBox2D yet, but
//		// when they are, commenting out the following section should work.
//		/*
//		 * else if ( type.equals("gear") ) { jointDef = gearDef = new
//		 * GearJointDef(); int jointIndex1 = jointValue.getInt("joint1"); int
//		 * jointIndex2 = jointValue.getInt("joint2"); gearDef.joint1 =
//		 * m_joints.get(jointIndex1); gearDef.joint2 =
//		 * m_joints.get(jointIndex2); gearDef.ratio = jsonToFloat("ratio",
//		 * jointValue); }
//		 */
//		// Wheel joints are apparently not implemented in JBox2D yet, but
//		// when they are, commenting out the following section should work.
//		/*
//		 * else if ( type.equals("wheel") ) { jointDef = wheelDef = new
//		 * WheelJointDef(); wheelDef.localAnchorA.set( jsonToVec("anchorA",
//		 * jointValue) ); wheelDef.localAnchorB.set( jsonToVec("anchorB",
//		 * jointValue) ); wheelDef.localAxisA.set( jsonToVec("localAxisA",
//		 * jointValue) ); wheelDef.enableMotor =
//		 * jointValue.optBoolean("enableMotor",false); wheelDef.motorSpeed =
//		 * jsonToFloat("motorSpeed", jointValue); wheelDef.maxMotorTorque =
//		 * jsonToFloat("maxMotorTorque", jointValue); wheelDef.frequencyHz =
//		 * jsonToFloat("springFrequency", jointValue); wheelDef.dampingRatio =
//		 * jsonToFloat("springDampingRatio", jointValue); }
//		 */
//		// For now, we will make do with a revolute joint.
//		else if (type.equals("wheel")) {
//			jointDef = revoluteDef = new RevoluteJointDef();
//			revoluteDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
//			revoluteDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
//			revoluteDef.enableMotor = jointValue.getBoolean("enableMotor", false);
//			revoluteDef.motorSpeed = jsonToFloat("motorSpeed", jointValue);
//			revoluteDef.maxMotorTorque = jsonToFloat("maxMotorTorque", jointValue);
//		} else if (type.equals("weld")) {
//			jointDef = weldDef = new WeldJointDef();
//			weldDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
//			weldDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
//			weldDef.referenceAngle = 0;
//		} else if (type.equals("friction")) {
//			jointDef = frictionDef = new FrictionJointDef();
//			frictionDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
//			frictionDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
//			frictionDef.maxForce = jsonToFloat("maxForce", jointValue);
//			frictionDef.maxTorque = jsonToFloat("maxTorque", jointValue);
//		} else if ( type.equals("rope") ) {
//			jointDef = ropeDef = new RopeJointDef();
//			ropeDef.localAnchorA.set( jsonToVec("anchorA", jointValue) );
//			ropeDef.localAnchorB.set( jsonToVec("anchorB", jointValue) );
//			ropeDef.maxLength = jsonToFloat("maxLength", jointValue);
//		}
//
//
//		if (null != jointDef) {
//			// set features common to all joints
//			jointDef.bodyA = m_bodies.get(bodyIndexA);
//			jointDef.bodyB = m_bodies.get(bodyIndexB);
//			jointDef.collideConnected = jointValue.getBoolean("collideConnected", false);
//
//			joint = world.createJoint(jointDef);
//
//			if (type.equals("mouse"))
//				((MouseJoint) joint).setTarget(mouseJointTarget);
//
//			String jointName = jointValue.getString("name", "");
//			if (!jointName.equals("")) {
//				setJointName(joint, jointName);
//			}
//
//			String jointPath = jointValue.getString("path", "");
//			if (!jointPath.equals("")) {
//				setJointPath(joint, jointPath);
//			}
//		}
//
//		return joint;
//	}
//
//	Jb2dJsonImage j2b2dJsonImage(JsonValue imageValue) throws SerializationException {
//		Jb2dJsonImage img = new Jb2dJsonImage();
//
//		int bodyIndex = imageValue.getInt("body", -1);
//		if (-1 != bodyIndex)
//			img.body = lookupBodyFromIndex(bodyIndex);
//
//		String imageName = imageValue.getString("name", "");
//		if (!imageName.equals("")) {
//			img.name = imageName;
//			setImageName(img, imageName);
//		}
//
//		String imagePath = imageValue.getString("path", "");
//		if (!imagePath.equals("")) {
//			img.path = imagePath;
//			setImagePath(img, imagePath);
//		}
//
//		String fileName = imageValue.getString("file", "");
//		if (!fileName.equals(""))
//			img.file = fileName;
//
//		img.center = jsonToVec("center", imageValue);
//		img.angle = jsonToFloat("angle", imageValue);
//		img.scale = jsonToFloat("scale", imageValue);
//		img.aspectScale = jsonToFloat("aspectScale", imageValue);
//		img.opacity = jsonToFloat("opacity", imageValue);
//		img.renderOrder = jsonToFloat("renderOrder", imageValue);
//
//		JsonValue colorTintArray = imageValue.get("colorTint");
//		if ( null != colorTintArray ) {
//			for (int i = 0; i < 4; i++) {
//				img.colorTint[i] = colorTintArray.getInt(i);
//			}
//		}
//
//		img.flip = imageValue.getBoolean("flip", false);
//
//		img.filter = imageValue.getInt("filter", 1);
//
//		img.corners = new Vector2[4];
//		for (int i = 0; i < 4; i++)
//			img.corners[i] = jsonToVec("corners", imageValue, i);
//
//		JsonValue vertexPointerArray = imageValue.get("glVertexPointer");
//		JsonValue texCoordArray = imageValue.get("glVertexPointer");
//		if (null != vertexPointerArray && null != texCoordArray && vertexPointerArray.length() == texCoordArray.length()) {
//			int numFloats = vertexPointerArray.length();
//			img.numPoints = numFloats / 2;
//			img.points = new float[numFloats];
//			img.uvCoords = new float[numFloats];
//			for (int i = 0; i < numFloats; i++) {
//				img.points[i] = jsonToFloat("glVertexPointer", imageValue, i);
//				img.uvCoords[i] = jsonToFloat("glTexCoordPointer", imageValue, i);
//			}
//		}
//
//		JsonValue drawElementsArray = imageValue.get("glDrawElements");
//		if (null != drawElementsArray) {
//			img.numIndices = drawElementsArray.length();
//			img.indices = new short[img.numIndices];
//			for (int i = 0; i < img.numIndices; i++)
//				img.indices[i] = (short) drawElementsArray.getInt(i);
//		}
//
//		return img;
//	}
//
//	float jsonToFloat(String name, JsonValue value) {
//		return jsonToFloat(name, value, -1, 0);
//	}
//
//	float jsonToFloat(String name, JsonValue value, int index) {
//		return jsonToFloat(name, value, index, 0);
//	}
//
//	float jsonToFloat(String name, JsonValue value, int index, float defaultValue) {
//		if (!value.has(name))
//			return defaultValue;
//
//		if (index > -1) {
//			JsonValue array = null;
//			try {
//				array = value.get(name);
//			} catch (SerializationException e) {
//			}
//			if (null == array)
//				return defaultValue;
//			Object obj = array.get(index);
//			if (null == obj)
//				return defaultValue;
//				// else if ( value[name].isString() )
//				// return hexToFloat( value[name].asString() );
//			else
//				return ((Number) obj).floatValue();
//		} else {
//			Object obj = value.get(name);
//			if (null == obj)
//				return defaultValue;
//				// else if ( value[name].isString() )
//				// return hexToFloat( value[name].asString() );
//			else
//				return ((Number) obj).floatValue();
//		}
//	}
//
//	Vector2 jsonToVec(String name, JsonValue value) throws SerializationException {
//		return jsonToVec(name, value, -1, new Vector2(0, 0));
//	}
//
//	Vector2 jsonToVec(String name, JsonValue value, int index) throws SerializationException {
//		return jsonToVec(name, value, index, new Vector2(0, 0));
//	}
//
//	Vector2 jsonToVec(String name, JsonValue value, int index, Vector2 defaultValue) throws SerializationException {
//		Vector2 vec = defaultValue;
//
//		if (!value.has(name))
//			return defaultValue;
//
//		if (index > -1) {
//			JsonValue vecValue = value.get(name);
//			JsonValue arrayX = vecValue.get("x");
//			JsonValue arrayY = vecValue.get("y");
//			// if ( arrayX[index].isString() )
//			// vec.x = hexToFloat(value[name]["x"][index].asString());
//			// else
//			vec.x = (float) arrayX.getDouble(index);
//
//			// if ( arrayX[index].isString() )
//			// vec.y = hexToFloat(value[name]["y"][index].asString());
//			// else
//			vec.y = (float) arrayY.getDouble(index);
//		} else {
//			JsonValue vecValue = value.get(name);
//			if (null == vecValue)
//				return defaultValue;
//			else if (!vecValue.has("x")) // should be zero vector
//				vec.set(0, 0);
//			else {
//				vec.x = jsonToFloat("x", vecValue);
//				vec.y = jsonToFloat("y", vecValue);
//			}
//		}
//
//		return vec;
//	}
//
//	public ObjectSet<Body> getBodiesByName(String name) {
//		ObjectSet<Body> keys = new ObjectSet<>();
//		for (ObjectMap.Entry<Body, String> entry : m_bodyToNameMap.entries()) {
//			if (name.equals(entry.value)) {
//				keys.add(entry.key);
//			}
//		}
//
//		return keys;
//	}
//
//	public ObjectSet<Fixture> getFixturesByName(String name) {
//		ObjectSet<Fixture> keys = new ObjectSet<>();
//		for (ObjectMap.Entry<Fixture, String> entry : m_fixtureToNameMap.entries()) {
//			if (name.equals(entry.value)) {
//				keys.add(entry.key);
//			}
//		}
//		return keys;
//	}
//
//	public ObjectSet<Joint> getJointsByName(String name) {
//		ObjectSet<Joint> keys = new ObjectSet<>();
//		for (ObjectMap.Entry<Joint, String> entry : m_jointToNameMap.entries()) {
//			if (name.equals(entry.value)) {
//				keys.add(entry.key);
//			}
//		}
//		return keys;
//	}
//
//	public ObjectSet<Jb2dJsonImage> getImagesByName(String name) {
//		ObjectSet<Jb2dJsonImage> keys = new ObjectSet<>();
//		for (ObjectMap.Entry<Jb2dJsonImage, String> entry : m_imageToNameMap.entries()) {
//			if (name.equals(entry.value)) {
//				keys.add(entry.key);
//			}
//		}
//		return keys;
//	}
//
//	public Body[] getBodiesByPath(String path) {
//		Set<Body> keys = new HashSet<Body>();
//		for (Entry<Body, String> entry : m_bodyToPathMap.entrySet()) {
//			if (path.equals(entry.getValue())) {
//				keys.add(entry.getKey());
//			}
//		}
//		return keys.toArray(new Body[0]);
//	}
//
//	public Fixture[] getFixturesByPath(String path) {
//		Set<Fixture> keys = new HashSet<Fixture>();
//		for (Entry<Fixture, String> entry : m_fixtureToPathMap.entrySet()) {
//			if (path.equals(entry.getValue())) {
//				keys.add(entry.getKey());
//			}
//		}
//		return keys.toArray(new Fixture[0]);
//	}
//
//	public Joint[] getJointsByPath(String path) {
//		Set<Joint> keys = new HashSet<Joint>();
//		for (Entry<Joint, String> entry : m_jointToPathMap.entrySet()) {
//			if (path.equals(entry.getValue())) {
//				keys.add(entry.getKey());
//			}
//		}
//		return keys.toArray(new Joint[0]);
//	}
//
//	public Jb2dJsonImage[] getImagesByPath(String path) {
//		Set<Jb2dJsonImage> keys = new HashSet<Jb2dJsonImage>();
//		for (Entry<Jb2dJsonImage, String> entry : m_imageToPathMap.entrySet()) {
//			if (path.equals(entry.getValue())) {
//				keys.add(entry.getKey());
//			}
//		}
//		return keys.toArray(new Jb2dJsonImage[0]);
//	}
//
//	public Jb2dJsonImage[] getAllImages() {
//		return (Jb2dJsonImage[]) m_images.toArray();
//	}
//
//	public Body getBodyByName(String name) {
//		for (Entry<Body, String> entry : m_bodyToNameMap.entrySet()) {
//			if (name.equals(entry.getValue())) {
//				return entry.getKey();
//			}
//		}
//		return null;
//	}
//
//	public Fixture getFixtureByName(String name) {
//		for (Entry<Fixture, String> entry : m_fixtureToNameMap.entrySet()) {
//			if (name.equals(entry.getValue())) {
//				return entry.getKey();
//			}
//		}
//		return null;
//	}
//
//	public Joint getJointByName(String name) {
//		for (Entry<Joint, String> entry : m_jointToNameMap.entrySet()) {
//			if (name.equals(entry.getValue())) {
//				return entry.getKey();
//			}
//		}
//		return null;
//	}
//
//	public Jb2dJsonImage getImageByName(String name) {
//		for (Entry<Jb2dJsonImage, String> entry : m_imageToNameMap.entrySet()) {
//			if (name.equals(entry.getValue())) {
//				return entry.getKey();
//			}
//		}
//		return null;
//	}
//
//	public Body getBodyByPathAndName(String path, String name) {
//		for (Entry<Body, String> entry : m_bodyToNameMap.entrySet()) {
//			if (name.equals(entry.getValue())) {
//				Body b = entry.getKey();
//				if ( path.equals(getBodyPath(b)) )
//					return b;
//			}
//		}
//		return null;
//	}
//
//	public Fixture getFixtureByPathAndName(String path, String name) {
//		for (Entry<Fixture, String> entry : m_fixtureToNameMap.entrySet()) {
//			if (name.equals(entry.getValue())) {
//				Fixture f = entry.getKey();
//				if ( path.equals(getFixturePath(f)) )
//					return f;
//			}
//		}
//		return null;
//	}
//
//	public Joint getJointByPathAndName(String path, String name) {
//		for (Entry<Joint, String> entry : m_jointToNameMap.entrySet()) {
//			if (name.equals(entry.getValue())) {
//				Joint j = entry.getKey();
//				if ( path.equals(getJointPath(j)) )
//					return j;
//			}
//		}
//		return null;
//	}
//
//	public Jb2dJsonImage getImageByPathAndName(String path, String name) {
//		for (Entry<Jb2dJsonImage, String> entry : m_imageToNameMap.entrySet()) {
//			if (name.equals(entry.getValue())) {
//				Jb2dJsonImage i = entry.getKey();
//				if ( path.equals(getImagePath(i)) )
//					return i;
//			}
//		}
//		return null;
//	}
//
//	// //// custom properties
//
//	public Jb2dJsonCustomProperties getCustomPropertiesForItem(Object item, boolean createIfNotExisting) {
//
//		if (m_customPropertiesMap.containsKey(item))
//			return m_customPropertiesMap.get(item);
//
//		if (!createIfNotExisting)
//			return null;
//
//		Jb2dJsonCustomProperties props = new Jb2dJsonCustomProperties();
//		m_customPropertiesMap.addChild(item, props);
//
//		return props;
//	}
//
//	// setCustomXXX
//
//	protected void setCustomInt(Object item, String propertyName, int val) {
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_int.addChild(propertyName, val);
//	}
//
//	protected void setCustomFloat(Object item, String propertyName, float val) {
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_float.addChild(propertyName, new Double(val));
//	}
//
//	protected void setCustomString(Object item, String propertyName, String val) {
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_string.addChild(propertyName, val);
//	}
//
//	protected void setCustomVector(Object item, String propertyName, Vector2 val) {
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_vec2.addChild(propertyName, val);
//	}
//
//	protected void setCustomBool(Object item, String propertyName, boolean val) {
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_bool.addChild(propertyName, val);
//	}
//
//
//	public void setCustomInt(Body item, String propertyName, int val) {
//		m_bodiesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_int.addChild(propertyName, val);
//	}
//
//	public void setCustomFloat(Body item, String propertyName, float val) {
//		m_bodiesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_float.addChild(propertyName, new Double(val));
//	}
//
//	public void setCustomString(Body item, String propertyName, String val) {
//		m_bodiesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_string.addChild(propertyName, val);
//	}
//
//	public void setCustomVector(Body item, String propertyName, Vector2 val) {
//		m_bodiesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_vec2.addChild(propertyName, val);
//	}
//
//	public void setCustomBool(Body item, String propertyName, boolean val) {
//		m_bodiesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_bool.addChild(propertyName, val);
//	}
//
//
//	public void setCustomInt(Fixture item, String propertyName, int val) {
//		m_fixturesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_int.addChild(propertyName, val);
//	}
//
//	public void setCustomFloat(Fixture item, String propertyName, float val) {
//		m_fixturesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_float.addChild(propertyName, new Double(val));
//	}
//
//	public void setCustomString(Fixture item, String propertyName, String val) {
//		m_fixturesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_string.addChild(propertyName, val);
//	}
//
//	public void setCustomVector(Fixture item, String propertyName, Vector2 val) {
//		m_fixturesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_vec2.addChild(propertyName, val);
//	}
//
//	public void setCustomBool(Fixture item, String propertyName, boolean val) {
//		m_fixturesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_bool.addChild(propertyName, val);
//	}
//
//
//	public void setCustomInt(Joint item, String propertyName, int val) {
//		m_jointsWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_int.addChild(propertyName, val);
//	}
//
//	public void setCustomFloat(Joint item, String propertyName, float val) {
//		m_jointsWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_float.addChild(propertyName, new Double(val));
//	}
//
//	public void setCustomString(Joint item, String propertyName, String val) {
//		m_jointsWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_string.addChild(propertyName, val);
//	}
//
//	public void setCustomVector(Joint item, String propertyName, Vector2 val) {
//		m_jointsWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_vec2.addChild(propertyName, val);
//	}
//
//	public void setCustomBool(Joint item, String propertyName, boolean val) {
//		m_jointsWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_bool.addChild(propertyName, val);
//	}
//
//
//	public void setCustomInt(Jb2dJsonImage item, String propertyName, int val) {
//		m_imagesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_int.addChild(propertyName, val);
//	}
//
//	public void setCustomFloat(Jb2dJsonImage item, String propertyName, float val) {
//		m_imagesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_float.addChild(propertyName, new Double(val));
//	}
//
//	public void setCustomString(Jb2dJsonImage item, String propertyName, String val) {
//		m_imagesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_string.addChild(propertyName, val);
//	}
//
//	public void setCustomVector(Jb2dJsonImage item, String propertyName, Vector2 val) {
//		m_imagesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_vec2.addChild(propertyName, val);
//	}
//
//	public void setCustomBool(Jb2dJsonImage item, String propertyName, boolean val) {
//		m_imagesWithCustomProperties.add(item);
//		getCustomPropertiesForItem(item, true).m_customPropertyMap_bool.addChild(propertyName, val);
//	}
//
//
//
//	// hasCustomXXX
//
//	public boolean hasCustomInt(Object item, String propertyName) {
//		return getCustomPropertiesForItem(item, false) != null &&
//			getCustomPropertiesForItem(item, false).m_customPropertyMap_int.containsKey(propertyName);
//	}
//
//	public boolean hasCustomFloat(Object item, String propertyName) {
//		return getCustomPropertiesForItem(item, false) != null &&
//			getCustomPropertiesForItem(item, false).m_customPropertyMap_float.containsKey(propertyName);
//	}
//
//	public boolean hasCustomString(Object item, String propertyName) {
//		return getCustomPropertiesForItem(item, false) != null &&
//			getCustomPropertiesForItem(item, false).m_customPropertyMap_string.containsKey(propertyName);
//	}
//
//	public boolean hasCustomVector(Object item, String propertyName) {
//		return getCustomPropertiesForItem(item, false) != null &&
//			getCustomPropertiesForItem(item, false).m_customPropertyMap_vec2.containsKey(propertyName);
//	}
//
//	public boolean hasCustomBool(Object item, String propertyName) {
//		return getCustomPropertiesForItem(item, false) != null &&
//			getCustomPropertiesForItem(item, false).m_customPropertyMap_bool.containsKey(propertyName);
//	}
//
//	// getCustomXXX
//
//	public int getCustomInt(Object item, String propertyName, int defaultVal) {
//		Jb2dJsonCustomProperties props = getCustomPropertiesForItem(item, false);
//		if (null == props)
//			return defaultVal;
//		if (props.m_customPropertyMap_int.containsKey(propertyName))
//			return props.m_customPropertyMap_int.get(propertyName);
//		return defaultVal;
//	}
//
//	public float getCustomFloat(Object item, String propertyName, float defaultVal) {
//		Jb2dJsonCustomProperties props = getCustomPropertiesForItem(item, false);
//		if (null == props)
//			return defaultVal;
//		if (props.m_customPropertyMap_float.containsKey(propertyName))
//			return props.m_customPropertyMap_float.get(propertyName).floatValue();
//		return defaultVal;
//	}
//
//	public String getCustomString(Object item, String propertyName, String defaultVal) {
//		Jb2dJsonCustomProperties props = getCustomPropertiesForItem(item, false);
//		if (null == props)
//			return defaultVal;
//		if (props.m_customPropertyMap_string.containsKey(propertyName))
//			return props.m_customPropertyMap_string.get(propertyName);
//		return defaultVal;
//	}
//
//	public Vector2 getCustomVector(Object item, String propertyName, Vector2 defaultVal) {
//		Jb2dJsonCustomProperties props = getCustomPropertiesForItem(item, false);
//		if (null == props)
//			return defaultVal;
//		if (props.m_customPropertyMap_vec2.containsKey(propertyName))
//			return props.m_customPropertyMap_vec2.get(propertyName);
//		return defaultVal;
//	}
//
//	public boolean getCustomBool(Object item, String propertyName, boolean defaultVal) {
//		Jb2dJsonCustomProperties props = getCustomPropertiesForItem(item, false);
//		if (null == props)
//			return defaultVal;
//		if (props.m_customPropertyMap_bool.containsKey(propertyName))
//			return props.m_customPropertyMap_bool.get(propertyName);
//		return defaultVal;
//	}
//
//	// get by custom property value (vector version, body)
//	public int getBodiesByCustomInt(String propertyName, int valueToMatch, Vector<Body> items) {
//		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Body item = iterator.next();
//			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getBodiesByCustomFloat(String propertyName, float valueToMatch, Vector<Body> items) {
//		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Body item = iterator.next();
//			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getBodiesByCustomString(String propertyName, String valueToMatch, Vector<Body> items) {
//		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Body item = iterator.next();
//			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch) )
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getBodiesByCustomVector(String propertyName, Vector2 valueToMatch, Vector<Body> items) {
//		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Body item = iterator.next();
//			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getBodiesByCustomBool(String propertyName, boolean valueToMatch, Vector<Body> items) {
//		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Body item = iterator.next();
//			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	// get by custom property value (single version, body)
//	Body getBodyByCustomInt(String propertyName, int valueToMatch) {
//		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Body item = iterator.next();
//			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Body getBodyByCustomFloat(String propertyName, float valueToMatch) {
//		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Body item = iterator.next();
//			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Body getBodyByCustomString(String propertyName, String valueToMatch) {
//		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Body item = iterator.next();
//			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
//				return item;
//		}
//		return null;
//	}
//
//	Body getBodyByCustomVector(String propertyName, Vector2 valueToMatch) {
//		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Body item = iterator.next();
//			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Body getBodyByCustomBool(String propertyName, boolean valueToMatch) {
//		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Body item = iterator.next();
//			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	// get by custom property value (vector version, Fixture)
//	public int getFixturesByCustomInt(String propertyName, int valueToMatch, Vector<Fixture> items) {
//		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Fixture item = iterator.next();
//			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getFixturesByCustomFloat(String propertyName, float valueToMatch, Vector<Fixture> items) {
//		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Fixture item = iterator.next();
//			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getFixturesByCustomString(String propertyName, String valueToMatch, Vector<Fixture> items) {
//		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Fixture item = iterator.next();
//			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getFixturesByCustomVector(String propertyName, Vector2 valueToMatch, Vector<Fixture> items) {
//		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Fixture item = iterator.next();
//			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getFixturesByCustomBool(String propertyName, boolean valueToMatch, Vector<Fixture> items) {
//		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Fixture item = iterator.next();
//			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	// get by custom property value (single version, Fixture)
//	Fixture getFixtureByCustomInt(String propertyName, int valueToMatch) {
//		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Fixture item = iterator.next();
//			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Fixture getFixtureByCustomFloat(String propertyName, float valueToMatch) {
//		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Fixture item = iterator.next();
//			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Fixture getFixtureByCustomString(String propertyName, String valueToMatch) {
//		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Fixture item = iterator.next();
//			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
//				return item;
//		}
//		return null;
//	}
//
//	Fixture getFixtureByCustomVector(String propertyName, Vector2 valueToMatch) {
//		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Fixture item = iterator.next();
//			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Fixture getFixtureByCustomBool(String propertyName, boolean valueToMatch) {
//		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Fixture item = iterator.next();
//			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	// get by custom property value (vector version, Joint)
//	public int getJointsByCustomInt(String propertyName, int valueToMatch, Vector<Joint> items) {
//		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Joint item = iterator.next();
//			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getJointsByCustomFloat(String propertyName, float valueToMatch, Vector<Joint> items) {
//		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Joint item = iterator.next();
//			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getJointsByCustomString(String propertyName, String valueToMatch, Vector<Joint> items) {
//		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Joint item = iterator.next();
//			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getJointsByCustomVector(String propertyName, Vector2 valueToMatch, Vector<Joint> items) {
//		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Joint item = iterator.next();
//			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getJointsByCustomBool(String propertyName, boolean valueToMatch, Vector<Joint> items) {
//		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Joint item = iterator.next();
//			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	// get by custom property value (single version, Joint)
//	Joint getJointByCustomInt(String propertyName, int valueToMatch) {
//		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Joint item = iterator.next();
//			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Joint getJointByCustomFloat(String propertyName, float valueToMatch) {
//		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Joint item = iterator.next();
//			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Joint getJointByCustomString(String propertyName, String valueToMatch) {
//		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Joint item = iterator.next();
//			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
//				return item;
//		}
//		return null;
//	}
//
//	Joint getJointByCustomVector(String propertyName, Vector2 valueToMatch) {
//		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Joint item = iterator.next();
//			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Joint getJointByCustomBool(String propertyName, boolean valueToMatch) {
//		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Joint item = iterator.next();
//			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	// get by custom property value (vector version, Image)
//	public int getImagesByCustomInt(String propertyName, int valueToMatch, Vector<Jb2dJsonImage> items) {
//		Iterator<Jb2dJsonImage> iterator = m_imagesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Jb2dJsonImage item = iterator.next();
//			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getImagesByCustomFloat(String propertyName, float valueToMatch, Vector<Jb2dJsonImage> items) {
//		Iterator<Jb2dJsonImage> iterator = m_imagesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Jb2dJsonImage item = iterator.next();
//			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getImagesByCustomString(String propertyName, String valueToMatch, Vector<Jb2dJsonImage> items) {
//		Iterator<Jb2dJsonImage> iterator = m_imagesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Jb2dJsonImage item = iterator.next();
//			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getImagesByCustomVector(String propertyName, Vector2 valueToMatch, Vector<Jb2dJsonImage> items) {
//		Iterator<Jb2dJsonImage> iterator = m_imagesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Jb2dJsonImage item = iterator.next();
//			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	public int getImagesByCustomBool(String propertyName, boolean valueToMatch, Vector<Jb2dJsonImage> items) {
//		Iterator<Jb2dJsonImage> iterator = m_imagesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Jb2dJsonImage item = iterator.next();
//			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
//				items.add(item);
//		}
//		return items.size();
//	}
//
//	// get by custom property value (single version, Image)
//	Jb2dJsonImage getImageByCustomInt(String propertyName, int valueToMatch) {
//		Iterator<Jb2dJsonImage> iterator = m_imagesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Jb2dJsonImage item = iterator.next();
//			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Jb2dJsonImage getImageByCustomFloat(String propertyName, float valueToMatch) {
//		Iterator<Jb2dJsonImage> iterator = m_imagesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Jb2dJsonImage item = iterator.next();
//			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Jb2dJsonImage getImageByCustomString(String propertyName, String valueToMatch) {
//		Iterator<Jb2dJsonImage> iterator = m_imagesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Jb2dJsonImage item = iterator.next();
//			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
//				return item;
//		}
//		return null;
//	}
//
//	Jb2dJsonImage getImageByCustomVector(String propertyName, Vector2 valueToMatch) {
//		Iterator<Jb2dJsonImage> iterator = m_imagesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Jb2dJsonImage item = iterator.next();
//			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	Jb2dJsonImage getImageByCustomBool(String propertyName, boolean valueToMatch) {
//		Iterator<Jb2dJsonImage> iterator = m_imagesWithCustomProperties.iterator();
//		while (iterator.hasNext()) {
//			Jb2dJsonImage item = iterator.next();
//			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
//				return item;
//		}
//		return null;
//	}
//
//	protected JsonValue writeCustomPropertiesToJson(Object item) throws SerializationException {
//		JsonValue customPropertiesValue = new JsonValue(JsonValue.ValueType.array);
//
//		Jb2dJsonCustomProperties props = getCustomPropertiesForItem(item, false);
//		if (null == props)
//			return customPropertiesValue;
//
//		int i = 0;
//
//		{
//			Iterator<Entry<String, Integer>> it = props.m_customPropertyMap_int.entrySet().iterator();
//			while (it.hasNext()) {
//				Entry<String, Integer> pair = (Entry<String, Integer>) it.next();
//				JsonValue propValue = new JsonValue();
//				propValue.addChild("name", pair.getKey());
//				propValue.addChild("int", pair.getValue());
//				customPropertiesValue.addChild(i++, propValue);
//			}
//		}
//		{
//			Iterator<Entry<String, Double>> it = props.m_customPropertyMap_float.entrySet().iterator();
//			while (it.hasNext()) {
//				Entry<String, Double> pair = (Entry<String, Double>) it.next();
//				JsonValue propValue = new JsonValue();
//				propValue.addChild("name", pair.getKey());
//				propValue.addChild("float", pair.getValue());
//				customPropertiesValue.addChild(i++, propValue);
//			}
//		}
//		{
//			Iterator<Entry<String, String>> it = props.m_customPropertyMap_string.entrySet().iterator();
//			while (it.hasNext()) {
//				Entry<String, String> pair = (Entry<String, String>) it.next();
//				JsonValue propValue = new JsonValue();
//				propValue.addChild("name", pair.getKey());
//				propValue.addChild("string", pair.getValue());
//				customPropertiesValue.addChild(i++, propValue);
//			}
//		}
//		{
//			Iterator<Entry<String, Vector2>> it = props.m_customPropertyMap_vec2.entrySet().iterator();
//			while (it.hasNext()) {
//				Entry<String, Vector2> pair = (Entry<String, Vector2>) it.next();
//				JsonValue propValue = new JsonValue();
//				propValue.addChild("name", pair.getKey());
//				vecToJson("vec2", pair.getValue(), propValue);
//				customPropertiesValue.addChild(i++, propValue);
//			}
//		}
//		{
//			Iterator<Entry<String, Boolean>> it = props.m_customPropertyMap_bool.entrySet().iterator();
//			while (it.hasNext()) {
//				Entry<String, Boolean> pair = (Entry<String, Boolean>) it.next();
//				JsonValue propValue = new JsonValue();
//				propValue.addChild("name", pair.getKey());
//				propValue.addChild("bool", pair.getValue());
//				customPropertiesValue.addChild(i++, propValue);
//			}
//		}
//
//		return customPropertiesValue;
//	}
//
//	protected void readCustomPropertiesFromJson(Body item, JsonValue value) throws SerializationException {
//		if (null == item)
//			return;
//
//		if (!value.has("customProperties"))
//			return;
//
//		JsonValue propValues = value.get("customProperties");
//		if (null != propValues) {
//			for (JsonValue propValue : propValues) {
//				String propertyName = propValue.getString("name");
//				if (propValue.has("int"))
//					setCustomInt(item, propertyName, propValue.getInt("int"));
//				if (propValue.has("float"))
//					setCustomFloat(item, propertyName, (float) propValue.getDouble("float"));
//				if (propValue.has("string"))
//					setCustomString(item, propertyName, propValue.getString("string"));
//				if (propValue.has("vec2"))
//					setCustomVector(item, propertyName, this.jsonToVec("vec2", propValue));
//				if (propValue.has("bool"))
//					setCustomBool(item, propertyName, propValue.getBoolean("bool"));
//			}
//		}
//	}
//
//
//	protected void readCustomPropertiesFromJson(Fixture item, JsonValue value) throws SerializationException {
//		if (null == item)
//			return;
//
//		if (!value.has("customProperties"))
//			return;
//
//		JsonValue propValues = value.get("customProperties");
//		if (null != propValues) {
//			for (JsonValue propValue : propValues) {
//				String propertyName = propValue.getString("name");
//				if (propValue.has("int"))
//					setCustomInt(item, propertyName, propValue.getInt("int"));
//				if (propValue.has("float"))
//					setCustomFloat(item, propertyName, (float) propValue.getDouble("float"));
//				if (propValue.has("string"))
//					setCustomString(item, propertyName, propValue.getString("string"));
//				if (propValue.has("vec2"))
//					setCustomVector(item, propertyName, this.jsonToVec("vec2", propValue));
//				if (propValue.has("bool"))
//					setCustomBool(item, propertyName, propValue.getBoolean("bool"));
//			}
//		}
//	}
//
//	protected void readCustomPropertiesFromJson(Joint item, JsonValue value) throws SerializationException {
//		if (null == item)
//			return;
//
//		if (!value.has("customProperties"))
//			return;
//
//		JsonValue propValues = value.get("customProperties");
//		if (null != propValues) {
//			for (JsonValue propValue : propValues) {
//				String propertyName = propValue.getString("name");
//				if (propValue.has("int"))
//					setCustomInt(item, propertyName, propValue.getInt("int"));
//				if (propValue.has("float"))
//					setCustomFloat(item, propertyName, (float) propValue.getDouble("float"));
//				if (propValue.has("string"))
//					setCustomString(item, propertyName, propValue.getString("string"));
//				if (propValue.has("vec2"))
//					setCustomVector(item, propertyName, this.jsonToVec("vec2", propValue));
//				if (propValue.has("bool"))
//					setCustomBool(item, propertyName, propValue.getBoolean("bool"));
//			}
//		}
//	}
//
//	protected void readCustomPropertiesFromJson(Jb2dJsonImage item, JsonValue value) throws SerializationException {
//		if (null == item)
//			return;
//
//		if (!value.has("customProperties"))
//			return;
//
//		JsonValue propValues = value.get("customProperties");
//		if (null != propValues) {
//			for (JsonValue propValue : propValues) {
//				String propertyName = propValue.getString("name");
//				if (propValue.has("int"))
//					setCustomInt(item, propertyName, propValue.getInt("int"));
//				if (propValue.has("float"))
//					setCustomFloat(item, propertyName, (float) propValue.getDouble("float"));
//				if (propValue.has("string"))
//					setCustomString(item, propertyName, propValue.getString("string"));
//				if (propValue.has("vec2"))
//					setCustomVector(item, propertyName, this.jsonToVec("vec2", propValue));
//				if (propValue.has("bool"))
//					setCustomBool(item, propertyName, propValue.getBoolean("bool"));
//			}
//		}
//	}
//
//	protected void readCustomPropertiesFromJson(World item, JsonValue value) throws SerializationException {
//		if (null == item)
//			return;
//
//		if (!value.has("customProperties"))
//			return;
//
//		JsonValue propValues = value.get("customProperties");
//		if (null != propValues) {
//			for (JsonValue propValue : propValues) {
//				String propertyName = propValue.getString("name");
//				if (propValue.has("int"))
//					setCustomInt(item, propertyName, propValue.getInt("int"));
//				if (propValue.has("float"))
//					setCustomFloat(item, propertyName, (float) propValue.getDouble("float"));
//				if (propValue.has("string"))
//					setCustomString(item, propertyName, propValue.getString("string"));
//				if (propValue.has("vec2"))
//					setCustomVector(item, propertyName, this.jsonToVec("vec2", propValue));
//				if (propValue.has("bool"))
//					setCustomBool(item, propertyName, propValue.getBoolean("bool"));
//			}
//		}
//	}
//
//}
