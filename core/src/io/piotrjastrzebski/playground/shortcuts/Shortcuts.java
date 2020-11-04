package io.piotrjastrzebski.playground.shortcuts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simple annotation based shortcut {@link InputProcessor}
 *
 * Created by EvilEntity on 11/09/2015.
 */
public class Shortcuts implements InputProcessor {
	private static final String TAG = Shortcuts.class.getSimpleName();
	public static final int CTRL = 1 << 10;
	public static final int ALT = 1 << 11;
	public static final int SHIFT = 1 << 12;

	private final IntMap<Method> shortcuts = new IntMap<>();
	private final IntMap<Object> owners = new IntMap<>();
	private int modifiers;

	public Shortcuts () {
		register(this);
	}

	public Shortcuts (Object obj) {
		this();
		register(obj);
	}

	public void register(Object obj) {
		Method[] methods = ClassReflection.getDeclaredMethods(obj.getClass());
		for (Method method : methods) {
			Annotation annotation = method.getDeclaredAnnotation(Shortcut.class);
			if (annotation == null) continue;

			method.setAccessible(true);
			if (method.getParameterTypes().length > 0) {
				Gdx.app.error(TAG, "Only methods without parameters are supported! " + method.getName());
				continue;
			}
			Shortcut sc = annotation.getAnnotation(Shortcut.class);
			int[] values = sc.value();
			for (int value : values) {
				int kc = translateKeyCode(value);
				if (kc <= 0) continue;
				shortcuts.put(kc, method);
				owners.put(kc, obj);
			}
		}
	}

	@Override
	public final boolean keyDown(int keycode) {
		switch (keycode) {
		case Input.Keys.CONTROL_LEFT:
		case Input.Keys.CONTROL_RIGHT:
			modifiers |= CTRL;
			break;
		case Input.Keys.ALT_LEFT:
		case Input.Keys.ALT_RIGHT:
			modifiers |= ALT;
			break;
		case Input.Keys.SHIFT_LEFT:
		case Input.Keys.SHIFT_RIGHT:
			modifiers |= SHIFT;
			break;
		}
		return modKeyDown(modifiers | keycode);
	}

	private boolean modKeyDown (int modKeyCode) {
		Method method = shortcuts.get(modKeyCode);
		if (method == null) return false;

		try {
			Object owner = owners.get(modKeyCode);
			if (method.getReturnType() == boolean.class) {
				return (Boolean) method.invoke(owner);
			} else {
				method.invoke(owner);
				return true;
			}
		} catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public final boolean keyUp(int keycode) {
		switch (keycode) {
		case Input.Keys.CONTROL_LEFT:
		case Input.Keys.CONTROL_RIGHT:
			modifiers ^= CTRL;
			break;
		case Input.Keys.ALT_LEFT:
		case Input.Keys.ALT_RIGHT:
			modifiers ^= ALT;
			break;
		case Input.Keys.SHIFT_LEFT:
		case Input.Keys.SHIFT_RIGHT:
			modifiers ^= SHIFT;
			break;
		}
		return false;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Shortcut {
		int[] value();
	}

	public static int translateKeyCode(int keyCode) {
		// TODO handle other combinations
		switch (keyCode) {
		case Input.Keys.PLUS:
			return SHIFT | Input.Keys.EQUALS;
		case Input.Keys.AT:
			return SHIFT | Input.Keys.NUM_2;
		case Input.Keys.POUND:
			return SHIFT | Input.Keys.NUM_3;
		case Input.Keys.STAR:
			return SHIFT | Input.Keys.NUM_8;
		case Input.Keys.COLON:
			return SHIFT | Input.Keys.SEMICOLON;
		}
		return keyCode;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		return false;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		return false;
	}

	@Override public boolean scrolled (float amountX, float amountY) {
		return false;
	}
}
