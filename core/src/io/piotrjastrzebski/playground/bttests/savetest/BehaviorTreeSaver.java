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
		Array<Class<? extends Task>> classes = new Array<>();
		findClasses(tree.getChild(0), classes);
		classes.sort(new Comparator<Class<? extends Task>>() {
			@Override public int compare (Class<? extends Task> o1, Class<? extends Task> o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});

		String save = "# Alias definitions\n";

		for (Class<? extends Task> aClass : classes) {
			save += "import " + toAlias(aClass.getSimpleName()) + ":\"" + aClass.getCanonicalName()+"\"\n";
		}

		save += "\n";
		save +="root\n";
		save += writeTask(tree.getChild(0), 1);

		savePath.writeString(save, false);
	}

	private static String writeTask (Task task, int depth) {
		String save = "";
		for (int i = 0; i < depth; i++) {
			save += "  ";
		}
		save += toAlias(task.getClass().getSimpleName());
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
			return name + ":\"" + distToString((Distribution)o) + "\"";
		}
		return name + ":" + value;
	}

	private static String distToString(Distribution dist) {
		if (dist instanceof ConstantIntegerDistribution) {
			return "constant," + ((ConstantIntegerDistribution)dist).getValue();
		}
		if (dist instanceof ConstantLongDistribution) {
			return "constant," + ((ConstantLongDistribution)dist).getValue();
		}
		if (dist instanceof ConstantFloatDistribution) {
			return "constant," + ((ConstantFloatDistribution)dist).getValue();
		}
		if (dist instanceof ConstantDoubleDistribution) {
			return "constant," + ((ConstantDoubleDistribution)dist).getValue();
		}
		if (dist instanceof GaussianFloatDistribution) {
			GaussianFloatDistribution gfd = (GaussianFloatDistribution)dist;
			return "gaussian," + gfd.getMean() + "," + gfd.getStandardDeviation();
		}
		if (dist instanceof GaussianDoubleDistribution) {
			GaussianDoubleDistribution gdd = (GaussianDoubleDistribution)dist;
			return "gaussian," + gdd.getMean() + ","+ gdd.getStandardDeviation();
		}
		if (dist instanceof TriangularIntegerDistribution) {
			TriangularIntegerDistribution tid = (TriangularIntegerDistribution)dist;
			return "triangular," + tid.getLow() + "," + tid.getHigh() + "," + tid.getMode();
		}
		if (dist instanceof TriangularLongDistribution) {
			TriangularLongDistribution tld = (TriangularLongDistribution)dist;
			return "triangular," + tld.getLow() + "," + tld.getHigh() + "," + tld.getMode();
		}
		if (dist instanceof TriangularFloatDistribution) {
			TriangularFloatDistribution tfd = (TriangularFloatDistribution)dist;
			return "triangular," + tfd.getLow() + "," + tfd.getHigh() + "," + tfd.getMode();
		}
		if (dist instanceof TriangularDoubleDistribution) {
			TriangularDoubleDistribution tdd = (TriangularDoubleDistribution)dist;
			return "triangular," + tdd.getLow() + "," + tdd.getHigh() + "," + tdd.getMode();
		}
		if (dist instanceof UniformIntegerDistribution) {
			UniformIntegerDistribution uid = (UniformIntegerDistribution)dist;
			return "uniform," + uid.getLow() + "," + uid.getHigh();
		}
		if (dist instanceof UniformLongDistribution) {
			UniformLongDistribution uld = (UniformLongDistribution)dist;
			return "uniform," + uld.getLow() + "," + uld.getHigh();
		}
		if (dist instanceof UniformFloatDistribution) {
			UniformFloatDistribution ufd = (UniformFloatDistribution)dist;
			return "uniform," + ufd.getLow() + "," + ufd.getHigh();
		}
		if (dist instanceof UniformDoubleDistribution) {
			UniformDoubleDistribution udd = (UniformDoubleDistribution)dist;
			return "uniform," + udd.getLow() + "," + udd.getHigh();
		}
		throw new IllegalArgumentException("Unknown distribution type " + dist);
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

	public static String toAlias (String name) {
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		if (name.length() == 0) throw new IllegalArgumentException("Name cannot be 0 length");
		return Character.toLowerCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
	}
}
