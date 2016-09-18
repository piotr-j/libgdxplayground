package io.piotrjastrzebski.playground.bttests.savetest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.utils.random.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.Comparator;

/**
 * Utility class for serialization of {@link BehaviorTree}s in a format readable by {@link com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser}
 *
 * Created by PiotrJ on 21/10/15.
 */
public class BehaviorTreeSaver {
	/**
	 * Save the tree in parsable format
	 * @param tree behaviour tree to save
	 * @param path external file path to save to, can't be a folder
	 */
	public static void save (BehaviorTree tree, String path) {
		FileHandle savePath = Gdx.files.external(path);
		if (savePath.isDirectory()) {
			Gdx.app.error("BehaviorTreeSaver", "save path cannot be a directory!");
			return;
		}
		savePath.writeString(serialize(tree), false);
	}

	/**
	 * Serialize the tree to parser readable format
	 * @param tree tree to serialize
	 * @return serialized tree
	 */
	public static String serialize(BehaviorTree tree) {
		Array<Class<? extends Task>> classes = new Array<>();
		findClasses(tree.getChild(0), classes);
		classes.sort(new Comparator<Class<? extends Task>>() {
			@Override public int compare (Class<? extends Task> o1, Class<? extends Task> o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});

		String save = "# Alias definitions\n";

		for (Class<? extends Task> aClass : classes) {
			save += "import " + toAlias(aClass) + ":\"" + aClass.getCanonicalName()+"\"\n";
		}

		save += "\n";
		save +="root\n";
		save += writeTask(tree.getChild(0), 1);
		return save;
	}

	private static String writeTask (Task task, int depth) {
		String save = "";
		for (int i = 0; i < depth; i++) {
			save += "  ";
		}
		save += toAlias(task.getClass());
		save += getTaskAttributes(task);
		save += "\n";
		for (int i = 0; i < task.getChildCount(); i++) {
			save += writeTask(task.getChild(i), depth + 1);
		}
		return save;
	}

	private static String getTaskAttributes (Task task) {
		String attrs = "";
		Class<?> aClass = task.getClass();
		Field[] fields = ClassReflection.getFields(aClass);
		for (Field f : fields) {
			Annotation a = f.getDeclaredAnnotation(TaskAttribute.class);
			if (a == null)
				continue;
			TaskAttribute annotation = a.getAnnotation(TaskAttribute.class);
			attrs += " " + getFieldString(task, annotation, f);
		}
		return attrs;
	}

	private static String getFieldString (Task task, TaskAttribute ann, Field field) {
		// prefer name from annotation if there is one
		String name = ann.name();
		if (name == null || name.length() == 0) {
			name = field.getName();
		}
		String value;
		Object o;
		try {
			field.setAccessible(true);
			o = field.get(task);
			value = String.valueOf(o);
		} catch (ReflectionException e) {
			Gdx.app.error("", "Failed to get field", e);
			return "";
		}
		if (field.getType().isEnum() || field.getType() == String.class) {
			return name + ":\"" + value + "\"";
		}
		if (Distribution.class.isAssignableFrom(field.getType())) {
			return name + ":\"" + toParseableString((Distribution)o) + "\"";
		}
		return name + ":" + value;
	}

	/**
	 * Attempts to create a parseable string for given distribution
	 * @param distribution distribution to create parsable string for
	 * @return string that can be parsed by distribution classes
	 */
	public static String toParseableString (Distribution distribution) {
		if (distribution == null) throw new IllegalArgumentException("Distribution cannot be null");
		if (distribution instanceof ConstantIntegerDistribution) {
			return "constant," + ((ConstantIntegerDistribution)distribution).getValue();
		}
		if (distribution instanceof ConstantLongDistribution) {
			return "constant," + ((ConstantLongDistribution)distribution).getValue();
		}
		if (distribution instanceof ConstantFloatDistribution) {
			return "constant," + ((ConstantFloatDistribution)distribution).getValue();
		}
		if (distribution instanceof ConstantDoubleDistribution) {
			return "constant," + ((ConstantDoubleDistribution)distribution).getValue();
		}
		if (distribution instanceof GaussianFloatDistribution) {
			GaussianFloatDistribution gfd = (GaussianFloatDistribution)distribution;
			return "gaussian," + gfd.getMean() + "," + gfd.getStandardDeviation();
		}
		if (distribution instanceof GaussianDoubleDistribution) {
			GaussianDoubleDistribution gdd = (GaussianDoubleDistribution)distribution;
			return "gaussian," + gdd.getMean() + ","+ gdd.getStandardDeviation();
		}
		if (distribution instanceof TriangularIntegerDistribution) {
			TriangularIntegerDistribution tid = (TriangularIntegerDistribution)distribution;
			return "triangular," + tid.getLow() + "," + tid.getHigh() + "," + tid.getMode();
		}
		if (distribution instanceof TriangularLongDistribution) {
			TriangularLongDistribution tld = (TriangularLongDistribution)distribution;
			return "triangular," + tld.getLow() + "," + tld.getHigh() + "," + tld.getMode();
		}
		if (distribution instanceof TriangularFloatDistribution) {
			TriangularFloatDistribution tfd = (TriangularFloatDistribution)distribution;
			return "triangular," + tfd.getLow() + "," + tfd.getHigh() + "," + tfd.getMode();
		}
		if (distribution instanceof TriangularDoubleDistribution) {
			TriangularDoubleDistribution tdd = (TriangularDoubleDistribution)distribution;
			return "triangular," + tdd.getLow() + "," + tdd.getHigh() + "," + tdd.getMode();
		}
		if (distribution instanceof UniformIntegerDistribution) {
			UniformIntegerDistribution uid = (UniformIntegerDistribution)distribution;
			return "uniform," + uid.getLow() + "," + uid.getHigh();
		}
		if (distribution instanceof UniformLongDistribution) {
			UniformLongDistribution uld = (UniformLongDistribution)distribution;
			return "uniform," + uld.getLow() + "," + uld.getHigh();
		}
		if (distribution instanceof UniformFloatDistribution) {
			UniformFloatDistribution ufd = (UniformFloatDistribution)distribution;
			return "uniform," + ufd.getLow() + "," + ufd.getHigh();
		}
		if (distribution instanceof UniformDoubleDistribution) {
			UniformDoubleDistribution udd = (UniformDoubleDistribution)distribution;
			return "uniform," + udd.getLow() + "," + udd.getHigh();
		}
		throw new IllegalArgumentException("Unknown distribution type " + distribution);
	}

	private static void findClasses (Task task, Array<Class<? extends Task>> classes) {
		Class<? extends Task> aClass = task.getClass();
		String cName = aClass.getCanonicalName();
		// ignore task classes from gdx-ai, as they are already accessible by the parser
		if (!cName.startsWith("com.badlogic.gdx.ai.btree.") && !classes.contains(aClass, true)) {
			classes.add(aClass);
		}
		for (int i = 0; i < task.getChildCount(); i++) {
			findClasses(task.getChild(i), classes);
		}
	}

	/**
	 * Create a update alias name
	 * @param aClass class of task
	 * @return update alias for the class
	 */
	public static String toAlias (Class<? extends Task> aClass) {
		if (aClass == null) throw new IllegalArgumentException("Class cannot be null");
		String name = aClass.getSimpleName();
		return Character.toLowerCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
	}
}
