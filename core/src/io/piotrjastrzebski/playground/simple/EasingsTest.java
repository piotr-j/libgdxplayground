package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class EasingsTest extends BaseScreen {
	VisList<String> list;
	String interpolationNames[], selectedInterpolation;
	float graphWidth = 800;
	float graphHeight = 400;
	float steps = graphWidth / 2;
	float time = 0;
	float duration = 2.5f;
	Vector2 startPosition = new Vector2(), targetPosition = new Vector2(), position = new Vector2();

	public EasingsTest (GameReset game) {
		super(game);
		Field[] interpolationFields = ClassReflection.getFields(Easings.class);

		// see how many fields are actually interpolations (for safety; other fields may be added with future)
		int interpolationMembers = 0;
		for (int i = 0; i < interpolationFields.length; i++) {
			if (Interpolation.class.isAssignableFrom(interpolationFields[i].getType())) {
				interpolationMembers++;
			}
		}

		// get interpolation names
		interpolationNames = new String[interpolationMembers];
		for (int i = 0; i < interpolationFields.length; i++)
			if (Interpolation.class.isAssignableFrom(interpolationFields[i].getType()))
				interpolationNames[i] = interpolationFields[i].getName();
		selectedInterpolation = interpolationNames[0];

		list = new VisList<>();
		list.setItems(interpolationNames);
		list.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				selectedInterpolation = list.getSelected();
				time = 0;
				resetPositions();
			}
		});

		VisScrollPane scroll = new VisScrollPane(list);
		scroll.setFadeScrollBars(false);
		scroll.setScrollingDisabled(true, false);

		root.add(scroll).expandX().left().width(300);
		multiplexer.getProcessors().insert(0, new InputAdapter() {
			public boolean scrolled (int amount) {
				if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
					return false;
				duration -= amount / 15f;
				duration = MathUtils.clamp(duration, 0, Float.POSITIVE_INFINITY);
				return true;
			}
		});
	}

	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (!Float.isNaN(time)) // if "walking" was interrupted by this touch down event
			startPosition.set(getPosition(time)); // set startPosition to the current position
		targetPosition.set(stage.screenToStageCoordinates(targetPosition.set(screenX, screenY)));
		time = 0;
		return true;
	}

	@Override public void render (float delta) {
		super.render(delta);

		Gdx.gl.glClearColor(.3f, .3f, .3f, 1);
		float bottomLeftX = Gdx.graphics.getWidth() / 2 - graphWidth / 2, bottomLeftY =
			Gdx.graphics.getHeight() / 2 - graphHeight / 2;

		// only show up to two decimals
		String text = String.valueOf(duration);
		if (text.length() > 4)
			text = text.substring(0, text.lastIndexOf('.') + 3);
		text = "duration: " + text + " s (ctrl + scroll to change)";
		stage.getBatch().begin();
		list.getStyle().font.draw(stage.getBatch(), text, bottomLeftX + graphWidth / 2,
			bottomLeftY + graphHeight + list.getStyle().font.getLineHeight(), 0, Align.center, false);
		stage.getBatch().end();

		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.rect(bottomLeftX, bottomLeftY, graphWidth, graphHeight); // graph bounds
		float lastX = bottomLeftX, lastY = bottomLeftY;
		for (float step = 0; step <= steps; step++) {
			Interpolation interpolation = getInterpolation(selectedInterpolation);
			float percent = step / steps;
			float x = bottomLeftX + graphWidth * percent, y = bottomLeftY + graphHeight * interpolation.apply(percent);
			renderer.line(lastX, lastY, x, y);
			lastX = x;
			lastY = y;
		}
		time += Gdx.graphics.getDeltaTime();
		if (time > duration) {
			time = Float.NaN; // stop "walking"
			startPosition.set(targetPosition); // set startPosition to targetPosition for next click
		}
		// draw time marker
		renderer.line(bottomLeftX + graphWidth * time / duration, bottomLeftY, bottomLeftX + graphWidth * time / duration,
			bottomLeftY + graphHeight);
		// draw path
		renderer.setColor(Color.GRAY);
		renderer.line(startPosition, targetPosition);
		renderer.setColor(Color.WHITE);
		renderer.end();

		// draw the position
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		if (!Float.isNaN(time)) // don't mess up position if time is NaN
			getPosition(time);
		renderer.circle(position.x, position.y, 7);
		renderer.end();

		stage.act(delta);
		stage.draw();
	}

	void resetPositions () {
		startPosition.set(stage.getWidth() - stage.getWidth() / 5f, stage.getHeight() - stage.getHeight() / 5f);
		targetPosition.set(startPosition.x, stage.getHeight() / 5f);
	}

	/**
	 * @return the {@link #position} with the {@link #selectedInterpolation interpolation} applied
	 */
	Vector2 getPosition (float time) {
		position.set(targetPosition);
		position.sub(startPosition);
		position.scl(getInterpolation(selectedInterpolation).apply(time / duration));
		position.add(startPosition);
		return position;
	}

	/**
	 * @return the {@link #selectedInterpolation selected} interpolation
	 */
	private Interpolation getInterpolation (String name) {
		try {
			return (Interpolation)Easings.class.getField(name).get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Libgdx {@link Interpolation}'s conforming to http://easings.net/ naming conventions
	 * Based on http://gsgd.co.uk/sandbox/jquery/easing/jquery.easing.1.3.js
	 */
	public static class Easings {
		static public final Interpolation easeInSine = new Interpolation() {
			@Override public float apply (float a) {
				return -MathUtils.cos(a * MathUtils.PI * .5f) + 1.0f;
			}
		};

		static public final Interpolation easeOutSine = new Interpolation() {
			@Override public float apply (float a) {
				return MathUtils.sin(a * MathUtils.PI * .5f);
			}
		};

		static public final Interpolation easeInOutSine = new Interpolation() {
			@Override public float apply (float a) {
				return -.5f * (MathUtils.cos(a * MathUtils.PI) - 1);
			}
		};

		static public final Interpolation easeInQuad = new Interpolation() {
			@Override public float apply (float a) {
				return a * a;
			}
		};

		static public final Interpolation easeOutQuad = new Interpolation() {
			@Override public float apply (float a) {
				return -a * (a - 2);
			}
		};

		static public final Interpolation easeInOutQuad = new Interpolation() {
			@Override public float apply (float a) {
				if ((a /= .5f) < 1)
					return .5f * a * a;
				return -.5f * ((a - 1) * (a - 3) - 1);
			}
		};

		static public final Interpolation easeInCubic = new Interpolation() {
			@Override public float apply (float a) {
				return a * a * a;
			}
		};

		static public final Interpolation easeOutCubic = new Interpolation() {
			@Override public float apply (float a) {
				return (a -= 1) * a * a + 1;
			}
		};

		static public final Interpolation easeInOutCubic = new Interpolation() {
			@Override public float apply (float a) {
				if ((a /= .5f) < 1)
					return .5f * a * a * a;
				return .5f * ((a -= 2) * a * a + 2);
			}
		};

		static public final Interpolation easeInQuart = new Interpolation() {
			@Override public float apply (float a) {
				return a * a * a * a;
			}
		};

		static public final Interpolation easeOutQuart = new Interpolation() {
			@Override public float apply (float a) {
				return -((a -= 1) * a * a * a - 1);
			}
		};

		static public final Interpolation easeInOutQuart = new Interpolation() {
			@Override public float apply (float a) {
				if ((a /= .5f) < 1)
					return .5f * a * a * a * a;
				return -.5f * ((a -= 2) * a * a * a - 2);
			}
		};

		static public final Interpolation easeInQuint = new Interpolation() {
			@Override public float apply (float a) {
				return a * a * a * a * a;
			}
		};

		static public final Interpolation easeOutQuint = new Interpolation() {
			@Override public float apply (float a) {
				return ((a = a - 1) * a * a * a * a + 1);
			}
		};

		static public final Interpolation easeInOutQuint = new Interpolation() {
			@Override public float apply (float a) {
				if ((a /= .5f) < 1)
					return .5f * a * a * a * a * a;
				return .5f * ((a -= 2) * a * a * a * a + 2);
			}
		};

		static public final Interpolation easeInExpo = new Interpolation() {
			@Override public float apply (float a) {
				return (a == 0) ? 0 : (float)Math.pow(2, 10 * (a - 1));
			}
		};

		static public final Interpolation easeOutExpo = new Interpolation() {
			@Override public float apply (float a) {
				return (a == 1.0f) ? 1.0f : (float)((-Math.pow(2, -10 * a) + 1));
			}
		};

		static public final Interpolation easeInOutExpo = new Interpolation() {
			@Override public float apply (float a) {
				if (a == 0)
					return 0;
				if (a == 1.0f)
					return 1.0f;
				if ((a /= .5f) < 1)
					return (float)(.5f * Math.pow(2, 10 * (a - 1)));
				return (float)(.5f * (-Math.pow(2, -10 * (a - 1)) + 2));
			}
		};

		static public final Interpolation easeInCirc = new Interpolation() {
			@Override public float apply (float a) {
				return (float)(-(Math.sqrt(1 - a*a) - 1));
			}
		};

		static public final Interpolation easeOutCirc = new Interpolation() {
			@Override public float apply (float a) {
				return (float)Math.sqrt(1 - (a = a - 1) * a);
			}
		};

		static public final Interpolation easeInOutCirc = new Interpolation() {
			// t: current time = alpha
			// b: begInnIng value = 0
			// c: change In value = 1
			// d: duration = 1
			@Override public float apply (float a) {
				if ((a /= .5f) < 1)
					return (float)(-.5f * (Math.sqrt(1 - a * a) - 1));
				return (float)(.5f * (Math.sqrt(1 - (a -= 2) * a) + 1));
			}
		};

		static public final Interpolation easeInBack = new Interpolation() {
			@Override public float apply (float a) {
				float s = 1.70158f;
				return a * a * ((s + 1) * a - s);
			}
		};

		static public final Interpolation easeOutBack = new Interpolation() {
			@Override public float apply (float a) {
				float s = 1.70158f;
				return ((a -= 1) * a * ((s + 1) * a + s) + 1);
			}
		};
		static public final Interpolation easeInOutBack = new Interpolation() {
			@Override public float apply (float a) {
				float s = 1.70158f;
				if ((a /= .5f) < 1)
					return .5f * (a * a * (((s *= (1.525)) + 1) * a - s));
				return .5f * ((a -= 2) * a * (((s *= (1.525)) + 1) * a + s) + 2);
			}
		};

		static public final Interpolation easeInElastic = new Interpolation() {
			@Override public float apply (float a) {
				if (a == 0)
					return 0;
				if (a == 1)
					return 1;
				float p = .3f;
				float s = p / MathUtils.PI2 * 1.57079f;
				return (float)-Math.pow(2, 10 * (a -= 1)) * MathUtils.sin((a - s) * MathUtils.PI2 / p);
			}
		};

		static public final Interpolation easeOutElastic = new Interpolation() {
			@Override public float apply (float a) {
				if (a == 0)
					return 0;
				if (a == 1)
					return 1;
				float p = .3f;
				float s = p / MathUtils.PI2 * 1.57079f;
				return (float)(Math.pow(2, -10 * a) * MathUtils.sin((a - s) * MathUtils.PI2 / p) + 1);
			}
		};

		static public final Interpolation easeInOutElastic = new Interpolation() {
			@Override public float apply (float t) {
				if (t == 0)
					return 0;
				if ((t /= .5f) == 2)
					return 1;
				float p = .45f;
				float s = p / MathUtils.PI2 * 1.57079f;
				if (t < 1) {
					return (float)(-.5f * (Math.pow(2, 10 * (t -= 1)) * MathUtils.sin((t - s) * MathUtils.PI2 / p)));
				}
				return (float)(Math.pow(2, -10 * (t -= 1)) * MathUtils.sin((t - s) * MathUtils.PI2 / p) * .5f + 1);
			}
		};

		static public final Interpolation easeInBounce = new Interpolation() {
			@Override public float apply (float a) {
				return 1.0f - easeOutBounce.apply(1.0f - a);
			}
		};

		static public final Interpolation easeOutBounce = new Interpolation() {
			@Override public float apply (float a) {
				if ((a) < (1 / 2.75f)) {
					return (7.5625f * a * a);
				} else if (a < (2 / 2.75f)) {
					return (7.5625f * (a -= (1.5f / 2.75f)) * a + .75f);
				} else if (a < (2.5 / 2.75)) {
					return (7.5625f * (a -= (2.25f / 2.75f)) * a + .9375f);
				} else {
					return (7.5625f * (a -= (2.625f / 2.75f)) * a + .984375f);
				}
			}
		};

		static public final Interpolation easeInOutBounce = new Interpolation() {
			@Override public float apply (float a) {
				if (a < .5f)
					return easeInBounce.apply(a * 2) * .5f;
				return easeOutBounce.apply(a * 2 - 1) * .5f + .5f;
			}
		};
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, EasingsTest.class);
	}
}
