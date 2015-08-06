package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.utils.ArtemisProfiler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by PiotrJ on 05/08/15.
 */
public class SystemProfiler implements ArtemisProfiler, Comparable<SystemProfiler> {
	public static final boolean ENABLED = true;
	public static final int SAMPLES = 200;
	public static boolean SHOW = false;

//	public static final SystemProfiler GAME_LOGIC = new SystemProfiler("Logic");
//	public static final SystemProfiler RENDER = new SystemProfiler("Render");
//	public static final SystemProfiler FRAME = new SystemProfiler("Frame");
// need to clear this at dispose time or something
	private static Array<SystemProfiler> profilers = new Array<>();
	private static ObjectMap<String, SystemProfiler> profilerByName = new ObjectMap<>();

	public static SystemProfiler add(SystemProfiler profiler) {
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

	public static Array<SystemProfiler> get() {
		return profilers;
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
//	public String maxString = "0,00";
//	public String movingString = "0,00";
//	public String localMaxString = "0,00";
	public long total;
	public Color color;

	private BaseSystem system;
	private String name;
	private ProfilerConfig.Type type;

	public SystemProfiler() {}

	public SystemProfiler(String nameOverride) {
		this.name = nameOverride;
	}

	public static void reset() {
		profilers.clear();
	}

	public static void toggleShow() {
		SHOW = !SHOW;
	}

//	public void updateMovingString() {
//		movingString = String.format("%.2f", getAverage() / 1000000f);
//	}

	public long getAverage() {
		return samples == 0 ? 0 : total / Math.min(times.length, samples);
	}

	@Override
	public void start() {
		if (!SHOW) {
			return;
		}
		startTime = System.nanoTime();
	}

	@Override
	public void stop() {
		if (!SHOW) {
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
//			maxString = String.format("%.2f", max / 1000000f);
		}

		if (time > localMax || index == localMaxIndex) {
			localMax = time;
			localMaxIndex = index;
//			localMaxString = String.format("%.2f", localMax / 1000000f);
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
//		if (system instanceof ProfilerConfig) {
//			ProfilerConfig config = (ProfilerConfig)system;
//		}
		if (name == null) {
			name = toString();
		}
		SystemProfiler.add(this);
//		profilers.add(this);
		if (color == null) {
			calculateColor(toString().hashCode(), color = new Color());
		}
	}

	@Override
	public String toString() {
		return name!= null ? name :
			system != null ? system.getClass().getSimpleName():"<dummy>";
	}

	@Override
	public int compareTo(SystemProfiler o) {
		return (int)(o.localMax - localMax);
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
