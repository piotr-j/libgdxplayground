package io.piotrjastrzebski.playground.poly2tri;

import static io.piotrjastrzebski.playground.poly2tri.Shapes.*;

/**
 * Created by PiotrJ on 02/06/16.
 */
public class Utils {
	// C99 removes M_PI from math.h
	public static final double PI_3div4 = 3 * Math.PI / 4;
	public static final double PI_div2 = 1.57079632679489661923;
	public static final double EPSILON = 1e-12;

	public enum Orientation {CW, CCW, COLLINEAR}

	/**
	 * Formula to calculate signed area<br>
	 * Positive if CCW<br>
	 * Negative if CW<br>
	 * 0 if collinear<br>
	 * <pre>
	 * A[P1,P2,P3]  =  (x1*y2 - y1*x2) + (x2*y3 - y2*x3) + (x3*y1 - y3*x1)
	 *              =  (x1-x3)*(y2-y3) - (y1-y3)*(x2-x3)
	 * </pre>
	 */
	public static Orientation orientation2d (Point pa, Point pb, Point pc) {
		double detLeft = (pa.x - pc.x) * (pb.y - pc.y);
		double detRight = (pa.y - pc.y) * (pb.x - pc.x);
		double val = detLeft - detRight;
		if (val > -EPSILON && val < EPSILON) {
			return Orientation.COLLINEAR;
		} else if (val > 0) {
			return Orientation.CCW;
		}
		return Orientation.CW;
	}

	public static boolean inScanArea (Point pa, Point pb, Point pc, Point pd) {
		return (pa.x - pb.x) * (pd.y - pb.y) - (pd.x - pb.x) * (pa.y - pb.y) < -EPSILON
			&& (pa.x - pc.x) * (pd.y - pc.y) - (pd.x - pc.x) * (pa.y - pc.y) > EPSILON;
	}
}
