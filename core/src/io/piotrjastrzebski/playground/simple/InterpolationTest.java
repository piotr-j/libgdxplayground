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
public class InterpolationTest extends BaseScreen {
	VisList<String> list;
	String interpolationNames[], selectedInterpolation;
	float graphSize = 400, steps = graphSize / 2, time = 0, duration = 2.5f;
	Vector2 startPosition = new Vector2(), targetPosition = new Vector2(), position = new Vector2();

	public InterpolationTest (GameReset game) {
		super(game);
		Field[] interpolationFields = ClassReflection.getFields(Interpolation.class);

		// see how many fields are actually interpolations (for safety; other fields may be added with future)
		int interpolationMembers = 0;
		for (int i = 0; i < interpolationFields.length; i++) {
			if (Interpolation.class.isAssignableFrom(interpolationFields[i].getDeclaringClass())) {
				interpolationMembers++;
			}
		}

		// get interpolation names
		interpolationNames = new String[interpolationMembers];
		for (int i = 0; i < interpolationFields.length; i++)
			if (Interpolation.class.isAssignableFrom(interpolationFields[i].getDeclaringClass()))
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

		root.add(scroll).expandX().left().width(100);
		multiplexer.getProcessors().insert(0, new InputAdapter() {
			public boolean scrolled (int amount) {
				if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) return false;
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
		float bottomLeftX = Gdx.graphics.getWidth() / 2 - graphSize / 2, bottomLeftY = Gdx.graphics.getHeight() / 2 - graphSize / 2;

		// only show up to two decimals
		String text = String.valueOf(duration);
		if (text.length() > 4) text = text.substring(0, text.lastIndexOf('.') + 3);
		text = "duration: " + text + " s (ctrl + scroll to change)";
		stage.getBatch().begin();
		list.getStyle().font.draw(stage.getBatch(), text, bottomLeftX + graphSize / 2, bottomLeftY + graphSize
			+ list.getStyle().font.getLineHeight(), 0, Align.center, false);
		stage.getBatch().end();

		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.rect(bottomLeftX, bottomLeftY, graphSize, graphSize); // graph bounds
		float lastX = bottomLeftX, lastY = bottomLeftY;
		for (float step = 0; step <= steps; step++) {
			Interpolation interpolation = getInterpolation(selectedInterpolation);
			float percent = step / steps;
			float x = bottomLeftX + graphSize * percent, y = bottomLeftY + graphSize * interpolation.apply(percent);
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
		renderer.line(bottomLeftX + graphSize * time / duration, bottomLeftY, bottomLeftX + graphSize * time / duration,
			bottomLeftY + graphSize);
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

	/** @return the {@link #position} with the {@link #selectedInterpolation interpolation} applied */
	Vector2 getPosition (float time) {
		position.set(targetPosition);
		position.sub(startPosition);
		position.scl(getInterpolation(selectedInterpolation).apply(time / duration));
		position.add(startPosition);
		return position;
	}

	/** @return the {@link #selectedInterpolation selected} interpolation */
	private Interpolation getInterpolation (String name) {
		try {
			return (Interpolation)Interpolation.class.getField(name).get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static void main (String[] args) {
		PlaygroundGame.start(args, InterpolationTest.class);
	}
}
