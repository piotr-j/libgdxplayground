package io.piotrjastrzebski.playground.clientserverv2;

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
public class CSTestV2 extends BaseScreen {
	Client clientA;
	Client clientB;
	Server server;
	VisLabel ackLabel;

	public CSTestV2() {
		super();
		server = new Server();

		clientA = addClient();
//		clientB = addClient();
		root.add(new VisLabel("Server View"));
		root.row();
		VisTable serverTable = new VisTable(true);
		root.add(serverTable);
		NumberSelector serverFPSSelector = new NumberSelector("Server FPS", 5, 5, 120, 5);
		serverFPSSelector.addChangeListener(new NumberSelector.NumberSelectorListener() {
			@Override
			public void changed(int number) {
				server.setFPS(number);
			}
		});
		serverFPSSelector.setValue(5);
		serverTable.add(serverFPSSelector);
	}

	int clientId = 0;
	private Client addClient() {
		final Client client = new Client(this);
		server.connect(client);

		VisTable clientTable = new VisTable(true);
		root.add(new VisLabel("Client " + (clientId++) + "View"));
		root.row();
		// large pad for client rendering
		root.add(clientTable).padBottom(150);
		root.row();
		NumberSelector lagSelector = new NumberSelector("Lag", 250, 0, 1000, 10);
		lagSelector.addChangeListener(new NumberSelector.NumberSelectorListener() {
			@Override
			public void changed(int number) {
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
		return client;
	}

	@Override public void render (float delta) {
		super.render(delta);
		clientA.update(delta);
//		clientB.update(delta);
		server.update(delta);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);

		if (clientA.getPlayer() != null) {
			for(Entity e : clientA.getEntities().values()) {
				renderer.setColor(Color.GRAY);
				renderer.rect(e.getPositionX(), 3, 1.5f, 1.5f);
			}

			// render client entity
			renderer.setColor(Color.OLIVE);
			renderer.rect(clientA.getPlayer().getPositionX(), 3, 3f, 3f);
			renderer.setColor(Color.GREEN);
			renderer.rect(clientA.getPlayer().getRealPositionX(), 3+1.5f, 3f, 1.5f);
		}

//		if (clientB.getPlayer() != null) {
//			for(Entity e : clientB.getEntities().values()) {
//				renderer.setColor(Color.GRAY);
//				renderer.circle(e.getPositionX(), -3, 1.5f, 32);
//			}
//			// render client entity
//			renderer.setColor(Color.MAROON);
//			renderer.circle(clientB.getPlayer().getPositionX(), -3, 1.5f, 32);
//			renderer.setColor(Color.RED);
//			renderer.circle(clientB.getPlayer().getPositionX(), -3, 1.35f, 32);
//		}

		if (server.getPlayer(0) != null) {
			// render server entity
			renderer.setColor(Color.OLIVE);
			renderer.rect(server.getPlayer(0).getRealPositionX(), 7f, 3f, 3f);
			renderer.setColor(Color.TEAL);
			renderer.rect(server.getPlayer(0).getRealPositionX()+0.2f, 7f+0.2f, 2.6f, 2.6f);
		}

		if (server.getPlayer(1) != null) {
			// render server entity
			renderer.setColor(Color.MAROON);
			renderer.rect(server.getPlayer(1).getRealPositionX(), -9f, 3f, 3f);
			renderer.setColor(Color.RED);
			renderer.rect(server.getPlayer(1).getRealPositionX() + 0.2f, -9f + 0.2f, 2.6f, 2.6f);
		}
		renderer.end();
	}

	public void setNotACKed(int amount) {
		ackLabel.setText("Not ACKed inputs: "+amount);
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.A) {
			clientA.left();
		} else if (keycode == Input.Keys.D) {
			clientA.right();
		}
//		if (keycode == Input.Keys.LEFT) {
//			clientB.left();
//		} else if (keycode == Input.Keys.RIGHT) {
//			clientB.right();
//		}
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		if (keycode == Input.Keys.A) {
			clientA.leftUp();
		} else if (keycode == Input.Keys.D) {
			clientA.rightUp();
		}
//		if (keycode == Input.Keys.LEFT) {
//			clientB.leftUp();
//		} else if (keycode == Input.Keys.RIGHT) {
//			clientB.rightUp();
//		}
		return false;
	}
}
