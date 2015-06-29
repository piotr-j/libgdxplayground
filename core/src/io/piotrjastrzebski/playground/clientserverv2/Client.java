package io.piotrjastrzebski.playground.clientserverv2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

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
	private IntMap<Entity> entities = new IntMap<>();
	boolean left;
	boolean right;

	LagNetwork<Array<EntityState>> network;

	int id;

	long inputSeq = 0;
	private Server server;
	CSTestV2 csTest;
	public Client (CSTestV2 csTest) {
		this.csTest = csTest;
		network = new LagNetwork<>();
	}


	private final static int MAX_STEPS = 3;
	float fixed;

	public void update (float delta) {
		processServerMSG();

		if (player == null) return;

		processInputs(delta);

		fixed += delta;
		int steps = 0;
		while (Server.TICK_RATE < fixed && MAX_STEPS > steps){
			fixed -= Server.TICK_RATE;
			steps++;
			for (Entity entity:entities.values()) {
				entity.fixedUpdate();
			}
			Gdx.app.log("", "tick");
		}
		float alpha = fixed/Server.TICK_RATE;


		for (Entity entity:entities.values()) {
			entity.update(delta, alpha);
		}
	}

	private void processServerMSG () {
		while (true) {
			LagNetwork<Array<EntityState>>.Message<Array<EntityState>> gameState = network.receive();
			if (gameState == null) {
				csTest.setNotACKed(pendingInputs.size);
				return;
			}

			for (EntityState state : gameState.data) {

				// TODO this is a bit dumb, playerId is set by the server directly
				if (state.id == id) {
					if (player == null) {
						player = new Entity(0);
						player.id = state.id;
						entities.put(player.id, player);
					}

					player.positionX = state.x;
					player.velocity = state.velocity;
//					player.accel = state.accel;

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
//					Entity entity = entities.get(state.id);
//					if (entity == null) {
//						entity = new Entity(state.x);
//						entities.put(state.id, entity);
//					}
//					entity.positionX = state.x;
//					entity.velocity = state.velocity;
//					entity.accel = state.accel;
				}
			}

			network.free(gameState);
		}
	}

	Array<GameInput> pendingInputs = new Array<>();

	private void processInputs (float dt) {
		// TODO pool
		GameInput input = new GameInput();
		input.dt = dt;
		if (left) {
			input.accel = -Entity.MAX_ACCEL;
		} else if (right) {
			input.accel = Entity.MAX_ACCEL;
		} else {
			return;
		}

		input.seqId = inputSeq++;
		input.id = player.id;

//		Gdx.app.log("", ""+ lag);
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

	@Override
	public String toString() {
		return "Client{" +
				"id=" + id +
				'}';
	}

	public IntMap<Entity> getEntities() {
		return entities;
	}
}
