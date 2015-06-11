package io.piotrjastrzebski.playground;

/**
 * Created by EvilEntity on 09/06/2015.
 */
public class Utils {
	/**
	 *
	 * @param alpha [0, 1]
	 * @param bias [0, 1]
	 */
	public static float bias(float alpha, float bias){
		return (alpha / (((1f/bias) - 2f) * (1f - alpha)) + 1f);
	}

	public static float gain(float alpha, float gain) {
		if (alpha < .5f) {
			return bias(alpha * 2f, gain) / 2f;
		} else {
			return bias(alpha * 2f - 1f, 1f - gain) / 2f + .5f;
		}
	}
}
