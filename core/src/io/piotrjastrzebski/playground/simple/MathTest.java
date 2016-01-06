package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

public class MathTest extends BaseScreen {

	public MathTest (GameReset game) {
		super(game);
		Gdx.app.log("" , "neat");
		Matrix m1 = new Matrix();
		Matrix m2 = new Matrix();
		m1.add(m2);
		m1.mul(1f);
		m1.mul(m2);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	}

	protected static class Matrix {
		private int rows = 0;
		private int columns = 0;
		private float[] data = new float[0];

		public float element(int row, int column) {
			return 0;
		}

		public void resize(int rows, int columns) {

		}

		public int rows() {
			return rows;
		}

		public int columns () {
			return columns;
		}

		public Matrix transpose() {

			return this;
		}

		// add matrix to this one
		public Matrix add(Matrix matrix) {
			if (matrix.rows != rows || matrix.columns != columns)
				throw new AssertionError("Invalid dimensions");
			for (int i = 0; i < data.length; i++) {
				data[i] += matrix.data[i];
			}
			return this;
		}
		// multiply this with matrix
		public Matrix mul(float val) {
			for (int i = 0; i < data.length; i++) {
				data[i] *= val;
			}
			return this;
		}

		// multiply this with matrix
		// A x B
		public Matrix mul(Matrix other) {
			/* number of columns must be equal to number of rows,
			   or multiplication doesnt work
			   N = U
			   M,V > 0
					    |   B   |
 					    |  UxV  |

				| A |  |   C   |
				|MxN|  |  MxV  |
				|   |  |       |

			 */
			if (columns != other.rows)
				throw new AssertionError("Invalid dimensions");


			return this;
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, MathTest.class);
	}
}
