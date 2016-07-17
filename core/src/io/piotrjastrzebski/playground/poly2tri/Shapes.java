package io.piotrjastrzebski.playground.poly2tri;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by PiotrJ on 02/06/16.
 */
public class Shapes {
	public static class Point {
		public double x;
		public double y;
		public Collection<Edge> edges = new ArrayList<>();

		public Point () {
			this(0, 0);
		}

		public Point (double y, double x) {
			this.y = y;
			this.x = x;
		}

		public Point (Point copy) {
			this.y = copy.y;
			this.x = copy.x;
		}

		public Point add(Point point) {
			x += point.x;
			y += point.y;
			return this;
		}

		public Point sub(Point point) {
			x -= point.x;
			y -= point.y;
			return this;
		}

		public Point mul(double scalar) {
			x *= scalar;
			y *= scalar;
			return this;
		}

		public double length() {
			return Math.sqrt(x * x + y * y);
		}

		public double normalize() {
			double length = length();
			x /= length;
			y /= length;
			return length;
		}

		@Override public String toString () {
			return "Point{" +
				"x=" + x +
				", y=" + y +
				'}';
		}
	}

	public static class Edge {
		public Point p;
		public Point q;

		public Edge (Point p1, Point p2) {
			if (p1.y > p2.y) {
				q = p1;
				p = p2;
			} else if (p1.y == p2.y) {
				if (p1.x > p2.x) {
					q = p1;
					p = p2;
				} else if (p1.x == p2.x) {
					throw new AssertionError("Duplicate points");
				}
			}
			q.edges.add(this);
		}

		@Override public String toString () {
			return "Edge{" +
				"p=" + p +
				", q=" + q +
				'}';
		}
	}

	public static class Triangle {
		private Point[] points = new Point[3];
		private Triangle[] neighbors = new Triangle[3];
		private boolean constrainedEdges[] = new boolean[3];
		private boolean delaunayEdges[] = new boolean[3];
		private boolean interior;

		public Triangle (Point p1, Point p2, Point p3) {
			points[0] = p1;
			points[1] = p2;
			points[2] = p3;
		}
	}
}
