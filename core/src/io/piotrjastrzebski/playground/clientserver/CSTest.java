package io.piotrjastrzebski.playground.clientserver;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.NumberSelector;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import io.piotrjastrzebski.playground.BaseScreen;

/**
 * Very simple client entity prediction test based on
 * http://www.gabrielgambetta.com/fpm_live.html
 * Created by PiotrJ on 21/06/15.
 */
public class CSTest extends BaseScreen {
	boolean prediction;
	boolean reconciliation;
	int clientLag;

	int serverFPS;

	Client client;
	Server server;
	VisLabel ackLabel;

	public CSTest () {
		super();
		client = new Client(this);
		server = new Server();
		server.connect(client);

		VisTable clientTable = new VisTable(true);
		root.add(new VisLabel("Client View"));
		root.row();
		// large pad for client rendering
		root.add(clientTable).padBottom(150);
		NumberSelector lagSelector = new NumberSelector("Lag", 250, 0, 1000, 10);
		lagSelector.addChangeListener(new NumberSelector.NumberSelectorListener() {
			@Override public void changed (int number) {
				client.setLag(number);
			}
		});
		lagSelector.setValue(250);

		clientTable.add(lagSelector);
		final VisCheckBox predictionCB = new VisCheckBox("Prediction");
		clientTable.add(predictionCB);
		final VisCheckBox reconciliationCB = new VisCheckBox("Reconciliation");
		clientTable.add(reconciliationCB);

		predictionCB.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				if (!predictionCB.isChecked()) {
					reconciliationCB.setChecked(false);
					client.setPrediction(false);
				} else {
					client.setPrediction(true);
				}
			}
		});

		reconciliationCB.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				if (reconciliationCB.isChecked()) {
					predictionCB.setChecked(true);
					client.setReconciliation(true);
				} else {
					client.setReconciliation(false);
				}
			}
		});

		clientTable.add(ackLabel = new VisLabel("Not ACKed inputs: 0"));

		root.row();

		root.add(new VisLabel("Server View"));
		root.row();
		VisTable serverTable = new VisTable(true);
		root.add(serverTable);
		NumberSelector serverFPSSelector = new NumberSelector("Server FPS", 60, 5, 120, 5);
		serverFPSSelector.addChangeListener(new NumberSelector.NumberSelectorListener() {
			@Override public void changed (int number) {
				server.setFPS(number);
			}
		});
		serverFPSSelector.setValue(5);
		serverTable.add(serverFPSSelector);
	}

	@Override public void render (float delta) {
		super.render(delta);
		client.update(delta);
		server.update(delta);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);

		if (client.getPlayer() != null) {
			// render client entity
			renderer.setColor(Color.OLIVE);
			renderer.circle(client.getPlayer().getX(), 0, 1.5f, 32);
			renderer.setColor(Color.GREEN);
			renderer.circle(client.getPlayer().getX(), 0, 1.35f, 32);
		}

		if (server.getPlayer() != null) {
			// render server entity
			renderer.setColor(Color.OLIVE);
			renderer.circle(server.getPlayer().getX(), -6.5f, 1.5f, 32);
			renderer.setColor(Color.GREEN);
			renderer.circle(server.getPlayer().getX(), -6.5f, 1.35f, 32);
		}
		renderer.end();
	}

	public void setNotACKed(int amount) {
		ackLabel.setText("Not ACKed inputs: "+amount);
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
			client.left();
		} else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
			client.right();
		}
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
			client.leftUp();
		} else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
			client.rightUp();
		}
		return false;
	}
}
