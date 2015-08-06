package io.piotrjastrzebski.playground.ecs.profiler;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by PiotrJ on 05/08/15.
 */
public interface ProfilerConfig {
	enum Type {DEBUG, LOGIC, RENDER, }
	Type getType();
	float getRefreshRate ();
	void setColor(Color color);
	String getName();
}
