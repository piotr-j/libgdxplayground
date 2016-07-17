package io.piotrjastrzebski.playground.poly2tri;

import io.piotrjastrzebski.playground.poly2tri.Shapes.Point;

import java.util.Collection;

/**
 * Why is this garbage even a thing? all this does is hold another garbage objects and delegates few things
 * Created by PiotrJ on 02/06/16.
 */
public class CDT {
	SweepContext sweepContext;
	Sweep sweep;
	public CDT (Collection<Point> polyline) {
		sweepContext = new SweepContext(polyline);
		sweep = new Sweep();
	}

	public void addHole (Collection<Point> polyline) {
		sweepContext.addHole(polyline);
	}

	public void addPoint (Point point) {
		sweepContext.addPoint(point);
	}

	public void triangulate () {
		sweep.triangulate(sweepContext);
	}

	public Collection<Shapes.Triangle> getTriangles () {
		return sweepContext.getTriangles();
	}

	public Collection<Shapes.Triangle> getMap () {
		return sweepContext.getMap();
	}
}
