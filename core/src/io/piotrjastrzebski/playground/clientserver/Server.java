package io.piotrjastrzebski.playground.clientserver;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

/**
 * Created by PiotrJ on 21/06/15.
 */
public class Server {
	int id;
	IntMap<Client> clients = new IntMap<>();
	IntMap<Entity> entities = new IntMap<>();
	IntMap<GameInput> lastInputs = new IntMap<>();

	LagNetwork<GameInput> network = new LagNetwork<>();

	public Server () {
	}

	public void connect(Client client) {
		client.setServer(this);
		client.playerId = id;
		clients.put(id, client);

		Entity entity = new Entity(0);
		entities.put(id, entity);

		entity.id = client.playerId;

		id++;
	}

	float accum;
	public void update (float delta) {
		accum+=delta;
		if (accum >= tickTime) {
			tick();
			accum -= tickTime;
		}
	}

	public void tick() {
		processInputs();
		sendWorldState();
	}

	private void processInputs () {
		while (true) {
			LagNetwork<GameInput>.Message<GameInput> message = network.receive();
			if (message == null) {
				return;
			}

			if (validateInput(message.data)) {
				int id = message.entityId;
				entities.get(id).applyInput(message.data);

				lastInputs.put(id, message.data);
			}

			network.free(message);
		}
	}

	Array<EntityState> worldState = new Array<>();
	private void sendWorldState () {
		worldState.clear();
		// gather world state
		// todo cull so we sent only required data
		for (int i = 0; i < entities.size; i++) {
			Entity entity = entities.get(i);
			// todo pool etc
			EntityState state = new EntityState();
			state.id = entity.id;
			state.x = entity.x;
			GameInput input = lastInputs.get(i);
			if (input != null) {
				state.lastSeqId = input.seqId;
			}
			worldState.add(state);
		}

		// send data to clients
		for(Client client : clients.values()) {
			client.network.send(client.getLag(), worldState);
		}
	}

	private boolean validateInput(GameInput input) {
		return input != null && Math.abs(input.duration) > 0.01f;
	}

	float tickTime;
	public void setFPS (int fps) {
		tickTime = 1.0f/fps;
	}

	public Entity getPlayer () {
		if (entities.size > 0) {
			return entities.get(0);
		}
		return null;
	}
}
