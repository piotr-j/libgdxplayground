package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.utils.ArtemisProfiler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * {@link ArtemisProfiler} implementation, {@link SystemProfiler#dispose()} should be called to clean static references as needed
 *
 * Created by PiotrJ on 05/08/15.
 */
public class SystemProfiler implements ArtemisProfiler {
	/**
	 * Global flag if all annotated profilers are enabled
	 */
	public static final boolean ENABLED = true;
	public static final int SAMPLES = 200;
	public static boolean RUNNING = false;

	private static Array<SystemProfiler> profilers = new Array<>();
	private static ObjectMap<String, SystemProfiler> profilerByName = new ObjectMap<>();
	private boolean added;

	public static SystemProfiler add(SystemProfiler profiler) {
		if (profiler.added) return profiler;
		profiler.added = true;
		profilers.add(profiler);
		profilerByName.put(profiler.getName(), profiler);
		return profiler;
	}

	public static int sixe () {
		return profilers.size;
	}

	public static SystemProfiler get (String name) {
		return profilerByName.get(name, null);
	}

	public static SystemProfiler get (int id) {
		return profilers.get(id);
	}

	/**
	 * Get profiler for given system
	 * @return profiler pr null
	 */
	public static SystemProfiler getFor (BaseSystem system) {
		Object[] items = profilers.items;
		for (int i = 0; i < profilers.size; i++) {
			SystemProfiler profiler = (SystemProfiler)items[i];
			if (profiler.system == system) {
				return profiler;
			}
		}
		return null;
	}

	/**
	 * get or create system for given system
	 * @return profiler pr null
	 */
	public static SystemProfiler getFor (BaseSystem system, World world) {
		Object[] items = profilers.items;
		for (int i = 0; i < profilers.size; i++) {
			SystemProfiler profiler = (SystemProfiler)items[i];
			if (profiler.system == system) {
				return profiler;
			}
		}
		return createFor(system, world);
	}

	public static SystemProfiler create (String name) {
		return SystemProfiler.add(new SystemProfiler(name));
	}

	public static SystemProfiler createFor (BaseSystem system, World world) {
		return SystemProfiler.add(new SystemProfiler(system, world));
	}

	public static Array<SystemProfiler> get() {
		return profilers;
	}

	/**
	 * Pause all profilers
	 */
	public static void pause() {
		RUNNING = false;
	}

	/**
	 * Resume all profilers
	 */
	public static void resume() {
		RUNNING = true;
	}

	/**
	 * Must be disposed
	 */
	public static void dispose() {
		profilers.clear();
		profilerByName.clear();
	}

	private long startTime;
	public long[] times = new long[SAMPLES];
	public int index;
	public long max;
	public long localMax;
	public long localMaxIndex;
	public int samples;
	int maxCounter;

	public long total;
	private Color color;
	private BaseSystem system;
	private String name;

	public SystemProfiler() {}

	public SystemProfiler(String name) {
		this.name = name;
	}

	public SystemProfiler (BaseSystem system, World world) {
		initialize(system, world);
	}

	boolean drawGraph = true;

	public boolean getDrawGraph () {
		return drawGraph;
	}

	public void setDrawGraph (boolean drawGraph) {
		this.drawGraph = drawGraph;
	}

	public long getAverage() {
		return samples == 0 ? 0 : total / Math.min(times.length, samples);
	}

	@Override
	public void start() {
		if (!RUNNING) {
			return;
		}
		startTime = System.nanoTime();
	}

	@Override
	public void stop() {
		if (!RUNNING) {
			return;
		}
		long time = System.nanoTime() - startTime;
		sample(time);
	}

	public float getMax () {
		return max / 1000000f;
	}

	public float getLocalMax () {
		return localMax / 1000000f;
	}

	public float getMovingAvg () {
		return getAverage() / 1000000f;
	}

	public long getCurrentSample() {
		return times[Math.abs(index - 1)];
	}

	public void sample(long time) {
		maxCounter++;
		if (time > max || maxCounter > 2000) {
			max = time;
			maxCounter = 0;
		}

		if (time > localMax || index == localMaxIndex) {
			localMax = time;
			localMaxIndex = index;
		}
		add(time);
	}

	public void add(long time) {
		total -= times[index];
		samples++;
		times[index] = time;
		total += time;
		if (++index == times.length){
			index = 0;
		}
	}

	@Override
	public void initialize(BaseSystem baseSystem, World world) {
		system = baseSystem;
		if (name == null) {
			name = toString();
		}
		if (color == null) {
			calculateColor(toString().hashCode(), color = new Color());
		}
		SystemProfiler.add(this);
	}

	@Override
	public String toString() {
		return name!= null ? name :
			system != null ? system.getClass().getSimpleName():"<dummy>";
	}

	public static Color calculateColor(int hash, Color color) {
		float hue = (hash % 333) / 333f;
		float saturation = ((hash % 271) / 271f) * 0.2f + 0.8f;
		float brightness = ((hash % 577) / 577f)*0.1f + 0.9f;

		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float) Math.floor(hue)) * 6.0f;
			float f = h - (float) java.lang.Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
			case 0:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (t * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 1:
				r = (int) (q * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 2:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (t * 255.0f + 0.5f);
				break;
			case 3:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (q * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 4:
				r = (int) (t * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 5:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (q * 255.0f + 0.5f);
				break;
			}
		}

		return color.set(r / 255f, g / 255f, b / 255f, 1);
	}

	public String getName () {
		return name;
	}

	public Color getColor () {
		return color;
	}

	public void setColor (float r, float g, float b, float a) {
		if (color == null) {
			color = new Color();
		}
		color.set(r, g, b, a);
	}
}
