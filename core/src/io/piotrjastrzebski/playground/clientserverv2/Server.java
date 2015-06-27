package io.piotrjastrzebski.playground.clientserverv2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

/**
 * Created by PiotrJ on 21/06/15.
 */
public class Server {
	public static float TICK_RATE;
	int id;
	IntMap<Client> clients = new IntMap<>();
	IntMap<Entity> entities = new IntMap<>();
	IntMap<GameInput> lastInputs = new IntMap<>();

	LagNetwork<GameInput> network = new LagNetwork<>();

	public Server () {
	}

	public void connect(Client client) {
		// TODO some clever scheme for ids so we dont run out after a while
		client.setServer(this);
		client.id = id;
		clients.put(id, client);

		Entity entity = new Entity(0);
		entities.put(id, entity);

		entity.id = client.id;
		Gdx.app.log("", "Connected, id: "+client);
		id++;
	}

	float accum;
	public void update (float delta) {
		accum+=delta;
		if (accum >= Server.TICK_RATE) {
			tick();
			accum -= Server.TICK_RATE;
		}
	}

	public void tick() {
		processInputs();
		sendWorldState();
	}

	private void processInputs () {
		for (Entity entity:entities.values()) {
			// TODO proper server delta
			entity.clearForces();
		}
		while (true) {
			LagNetwork<GameInput>.Message<GameInput> message = network.receive();
			if (message == null) {
				break;
			}

			if (validateInput(message.data)) {
				int id = message.data.id;
				entities.get(id).applyInput(message.data);

				lastInputs.put(id, message.data);
			}

			network.free(message);
		}
		// simulate entities
		for (Entity entity:entities.values()) {
			entity.fixedUpdate();
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
			state.x = entity.positionX;
			state.velocity = entity.velocity;
			state.accel = entity.accel;

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
		return input != null && Math.abs(input.accel) <= Entity.MAX_ACCEL;
	}

	public void setFPS (int fps) {
		Server.TICK_RATE = 1.0f/fps;
	}

	public io.piotrjastrzebski.playground.clientserverv2.Entity getPlayer(int i) {
		if (i < entities.size && i >= 0) {
			return entities.get(i);
		}
		return null;
	}
}
