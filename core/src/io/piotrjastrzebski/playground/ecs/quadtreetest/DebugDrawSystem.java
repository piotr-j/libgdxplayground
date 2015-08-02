package io.piotrjastrzebski.playground.ecs.quadtreetest;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

@Wire
public class DebugDrawSystem extends EntityProcessingSystem {
	private ComponentMapper<Position> mPosition;
	private ComponentMapper<Size> mSize;
	private ComponentMapper<Selected> mSelected;
	private ComponentMapper<InQuad> mInQuad;
	@Wire OrthographicCamera camera;
	@Wire ShapeRenderer renderer;
	QTSystem qtSystem;
	public DebugDrawSystem () {
		super(Aspect.all(Position.class, Size.class));
	}

	@Override protected void begin () {
		renderer.setProjectionMatrix(camera.combined);
		renderer.setColor(Color.GREEN);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		if (drawQuadTree) {
			drawQT(renderer, qtSystem.getQuadTree(), false);
		}
		if (drawQuadTreeTouched) {
			drawQT(renderer, qtSystem.getQuadTree(), true);
		}
	}

	public boolean drawQuadTree = true;
	public boolean drawQuadTreeTouched = true;
	private void drawQT(ShapeRenderer renderer, QTSystem.QuadTree quadTree, boolean touched) {
		QTSystem.QuadTree[] nodes = quadTree.getNodes();
		if (nodes != null && nodes[0] != null) {
			for (QTSystem.QuadTree tree : nodes) {
				drawQT(renderer, tree, touched);
			}
		}
		QTSystem.Bounds bounds = quadTree.getBounds();
		if (touched && quadTree.isTouched()) {
			renderer.setColor(Color.CYAN);
			renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
		}
		if (!touched && !quadTree.isTouched()) {
			renderer.setColor(Color.BLUE);
			renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}

	public boolean drawSelected = true;
	public boolean drawInQuad = true;
	public boolean drawRest = true;
	@Override protected void process (Entity e) {
		Position position = mPosition.get(e);
		Size size = mSize.get(e);
		if (mSelected.has(e) && drawSelected) {
			renderer.setColor(Color.GREEN);
			e.edit().remove(Selected.class);
			renderer.rect(position.x, position.y, size.width, size.height);
		} else if (mInQuad.has(e) && drawInQuad) {
			renderer.setColor(Color.OLIVE);
			e.edit().remove(InQuad.class);
			renderer.rect(position.x, position.y, size.width, size.height);
		} else if (drawRest) {
			if (position.dirty) {
				renderer.setColor(Color.RED);
			} else {
				renderer.setColor(Color.MAROON);
			}
			renderer.rect(position.x, position.y, size.width, size.height);
		}
	}

	@Override protected void end () {
		renderer.end();

	}
}
