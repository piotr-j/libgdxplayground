package io.piotrjastrzebski.playground;

/**
 * Created by EvilEntity on 09/06/2015.
 */
public class Utils {
	/**
	 *
	 * @param value [0, 1]
	 * @param bias [0, 1]
	 */
	public static float bias(float value, float bias){
		return (value / (((1f/bias) - 2f) * (1f - value)) + 1f);
	}

	public static float gain(float value, float gain) {
		if (value < .5f) {
			return bias(value * 2f, gain) / 2f;
		} else {
			return bias(value * 2f - 1f, 1f - gain) / 2f + .5f;
		}
	}
}
