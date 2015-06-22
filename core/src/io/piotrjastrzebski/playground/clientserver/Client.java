package io.piotrjastrzebski.playground.clientserver;

import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * Created by PiotrJ on 21/06/15.
 */
public class Client {

	// lag in ms
	private int lag;
	private boolean prediction;
	private boolean reconciliation;

	private Entity player;
	boolean left;
	boolean right;

	LagNetwork<Array<EntityState>> network;

	int playerId;

	long inputSeq = 0;
	private Server server;
	CSTest csTest;
	public Client (CSTest csTest) {
		this.csTest = csTest;
		network = new LagNetwork<>();
	}

	public void update (float delta) {
		processServerMSG();

		if (player == null) return;

		processInputs(delta);

	}

	private void processServerMSG () {
		while (true) {
			LagNetwork<Array<EntityState>>.Message<Array<EntityState>> gameState = network.receive();
			if (gameState == null) {
				csTest.setNotACKed(pendingInputs.size);
				return;
			}

			for (int i = 0; i < gameState.data.size; i++) {
				EntityState state = gameState.data.get(i);
				// TODO this is a bit dumb, playerId is set by the server directly
				if (state.id == playerId) {
					if (player == null) {
						player = new Entity(0);
						player.id = state.id;
					}

					player.x = state.x;

					if (reconciliation) {
						Iterator<GameInput> pendingIter = pendingInputs.iterator();
						while (pendingIter.hasNext()) {
							GameInput gameInput = pendingIter.next();
							if (gameInput.seqId <= state.lastSeqId) {
								pendingIter.remove();
							} else {
								player.applyInput(gameInput);
							}
						}
					} else {
						pendingInputs.clear();
					}
				} else {
					// TODO other entities
				}
			}

			network.free(gameState);
		}
	}

	Array<GameInput> pendingInputs = new Array<>();

	private void processInputs (float dt) {
		// TODO pool
		GameInput input = new GameInput();
		if (left) {
			input.duration = -dt;
		} else if (right) {
			input.duration = dt;
		} else {
			return;
		}

		input.seqId = inputSeq++;
		input.id = player.id;

		server.network.send(lag, input);

		if (prediction && player != null) {
			player.applyInput(input);
		}

		pendingInputs.add(input);
	}

	public void setLag (int lag) {
		this.lag = lag;
	}

	public void setPrediction (boolean prediction) {
		this.prediction = prediction;
	}

	public void setReconciliation (boolean reconciliation) {
		this.reconciliation = reconciliation;
	}

	public Entity getPlayer () {
		return player;
	}

	public void left() {
		left = true;
	}

	public void right() {
		right = true;
	}

	public void setServer (Server server) {
		this.server = server;
	}

	public int getLag () {
		return lag;
	}

	public void leftUp () {
		left = false;
	}

	public void rightUp () {
		right = false;
	}
}
