package io.piotrjastrzebski.playground.bttests.savetest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.*;
import com.badlogic.gdx.ai.btree.leaf.Failure;
import com.badlogic.gdx.ai.btree.leaf.Success;
import com.badlogic.gdx.ai.btree.leaf.Wait;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.ai.utils.random.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.bttests.dog.Dog;

import java.io.Reader;
import java.util.Comparator;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class BTSaveTest extends BaseScreen {

	private BehaviorTree<Dog> tree;

	public BTSaveTest (GameReset game) {
		super(game);
		tree = null;
		Reader reader = null;
		try {
			reader = Gdx.files.internal("btree/dog.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE) {
				protected BehaviorTree<Dog> createBehaviorTree (Task<Dog> root, Dog object) {
					if (debug > BehaviorTreeParser.DEBUG_LOW) printTree(root, 0);
					return new BehaviorTree<>(root, object);
				}
			};
			tree = parser.parse(reader, null);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
		tree.setObject(new Dog("Welp"));
	}

	private void save (BehaviorTree tree, String path) {
		FileHandle savePath = Gdx.files.external(path);
		if (savePath.isDirectory()) {
			Gdx.app.error("", "save path cannot be a directory!");
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
		save += writeTask(tree.getChild(0), 2);

		savePath.writeString(save, false);
	}

	private static String writeTask (Task task, int inset) {
		// this is horrible :c
		String save = "";
		for (int i = 0; i < inset; i++) {
			save += " ";
		}
		// TODO task attributes
		save += toAlias(task.getClass().getSimpleName());
		save += getTaskAttributes(task);
		save += "\n";
		for (int i = 0; i < task.getChildCount(); i++) {
			save += writeTask(task.getChild(i), inset + 2);
		}
		return save;
	}

	private static String getTaskAttributes (Task task) {
		String atts = "";
		Class<?> aClass = task.getClass();
		Field[] fields = ClassReflection.getFields(aClass);
		for (Field f : fields) {
			Annotation a = f.getDeclaredAnnotation(TaskAttribute.class);
			if (a == null)
				continue;
			TaskAttribute annotation = a.getAnnotation(TaskAttribute.class);
			atts += " " + getFieldString(task, annotation, f);
		}
		return atts;
	}

	private static String getFieldString (Task task, TaskAttribute ann, Field field) {
		// prefer name from annotation if there is one
		String name = ann.name();
		if (name == null || name.length() == 0) {
			name = field.getName();
		}
		String value = "<failed to get value of field>";
		Object o = null;
		try {
			o = field.get(task);
			value = String.valueOf(o);
		} catch (ReflectionException e) {
			Gdx.app.error("", "Failed to get field", e);
		}
		if (field.getType().isEnum()) {
			return name + ":\"" + value + "\"";
		}
		if (field.getType() == String.class) {
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

	private static String toAlias (String name) {
		return Character.toLowerCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
	}

	private static Array<Class<? extends Task>> defClasses = new Array<Class<? extends Task>>(new Class[]{Selector.class, Sequence.class,
		Selector.class, Parallel.class, AlwaysFail.class, AlwaysSucceed.class, Include.class, Invert.class, Random.class,
		Repeat.class, SemaphoreGuard.class, UntilFail.class, UntilSuccess.class, Wait.class, Success.class, Failure.class});
	private static void findClasses (Task task, Array<Class<? extends Task>> classes) {
		Class<? extends Task> aClass = task.getClass();
		if (!defClasses.contains(aClass, true) && !classes.contains(aClass, true)) {
			classes.add(aClass);
		}
		for (int i = 0; i < task.getChildCount(); i++) {
			findClasses(task.getChild(i), classes);
		}
	}

	private void load () {
		Reader reader = null;
		try {
			reader = Gdx.files.external("save.tree").reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE) {
				protected BehaviorTree<Dog> createBehaviorTree (Task<Dog> root, Dog object) {
					if (debug > BehaviorTreeParser.DEBUG_LOW) printTree(root, 0);
					return new BehaviorTree<>(root, object);
				}
			};
			BehaviorTree tree = parser.parse(reader, null);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.F5:
			Gdx.app.log("", "save");
			save(tree, "save.tree");
			break;
		case Input.Keys.F9:
			Gdx.app.log("", "load");
			load();
			break;
		}
		return super.keyDown(keycode);
	}
}
