package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIClockSyncTest extends BaseScreen {
    protected Server server;
    protected VisLabel serverTime;
    protected Client client1;
    protected VisLabel client1STime;
    protected VisLabel client1CTime;
    protected Client client2;
    protected VisLabel client2STime;
    protected VisLabel client2CTime;

    public UIClockSyncTest (GameReset game) {
        super(game);
        server = new Server();
        client1 = new Client();
        server.clients.add(client1);
        client2 = new Client();
        server.clients.add(client2);

        final SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss:SSS");
        root.add(new VisLabel("Server:")).expandX().right().pad(10);
        serverTime = new VisLabel("Server: ") {
            @Override public void act (float delta) {
                super.act(delta);
                setText(sdf.format(server.serverTime));
            }
        };
        root.add(serverTime).expandX().left().row();
        root.add().pad(25).row();
        {
            root.add(new VisLabel("Client 1 ST:")).expandX().right().pad(10);
            client1STime = new VisLabel("Client 1: ") {
                @Override public void act (float delta) {
                    super.act(delta);
                    setText(sdf.format(client1.serverTime) + " :: " + client2.timeDiff);
                }
            };
            root.add(client1STime).expandX().left().row();

            root.add(new VisLabel("Client 1 CT:")).expandX().right().pad(10);
            client1CTime = new VisLabel("Client 1: ") {
                @Override public void act (float delta) {
                    super.act(delta);
                    setText(sdf.format(client1.localTime));
                }
            };
            root.add(client1CTime).expandX().left().row();

            root.add(new VisLabel("Real Time:")).expandX().right().pad(10);
            VisLabel realTime = new VisLabel("Real: ") {
                @Override public void act (float delta) {
                    super.act(delta);
                    setText(sdf.format(TimeUtils.millis()));
                }
            };
            root.add(realTime).expandX().left().row();
        }
        root.add().pad(25).row();
        {
            root.add(new VisLabel("Client 2 ST:")).expandX().right().pad(10);
            client2STime = new VisLabel("Client 2: ") {
                @Override public void act (float delta) {
                    super.act(delta);
                    setText(sdf.format(client2.serverTime) + " :: " + client2.timeDiff);
                }
            };
            root.add(client2STime).expandX().left().row();

            root.add(new VisLabel("Client 2 CT:")).expandX().right().pad(10);
            client2CTime = new VisLabel("Client 2: ") {
                @Override public void act (float delta) {
                    super.act(delta);
                    setText(sdf.format(client2.localTime));
                }
            };
            root.add(client2CTime).expandX().left().row();

            root.add(new VisLabel("Real Time:")).expandX().right().pad(10);
            VisLabel realTime = new VisLabel("Real: ") {
                @Override public void act (float delta) {
                    super.act(delta);
                    setText(sdf.format(TimeUtils.millis()));
                }
            };
            root.add(realTime).expandX().left().row();
        }
    }

    private float tickTimer;

    @Override public void render (float delta) {
        super.render(delta);
        tickTimer += delta;
        if (tickTimer > .5f) {
            tickTimer -= .5f;
            server.tick();
        }
        server.update(delta);
        client1.update(delta);
        client2.update(delta);

        stage.act(delta);
        stage.draw();
    }

    private static class Server {
        float lagMinMS = 750;
        float lagMaxMS = 1250;
        ScheduledExecutorService service;
        private long serverTime;
        Array<Client> clients = new Array<>();

        public Server () {
            service = Executors.newSingleThreadScheduledExecutor();
        }

        public void tick () {
            for (final Client client : clients) {
                final TimeMessage message = new TimeMessage(serverTime);
                final long lag = (long)MathUtils.randomTriangular(lagMinMS, lagMaxMS);
                service.schedule(new Runnable() {
                    @Override public void run () {
                        client.message(message);
                    }
                }, lag, TimeUnit.MILLISECONDS);
            }
        }

        public void update (float delta) {
            serverTime = TimeUtils.millis();
        }
    }

    private static class Client {
        private long serverTime;
        private long localTime;
        private long timeDiff;
        private WindowedMean mean = new WindowedMean(10);

        public Client () {

        }

        public void update (float delta) {
            localTime = TimeUtils.millis() - timeDiff;
        }

        public void message (TimeMessage message) {
            serverTime = message.ts;
            long timeDiff = TimeUtils.millis() - serverTime;

            mean.addValue(timeDiff);
            float meanDiff = mean.getMean();
            if (meanDiff > 0) {
                this.timeDiff = (long)meanDiff;
            }
        }
    }

    private static class TimeMessage {
        private long ts;

        public TimeMessage (long ts) {
            this.ts = ts;
        }
    }

    // allow us to start this test directly
    public static void main (String[] args) {
        LwjglApplicationConfiguration config = PlaygroundGame.config();
        config.width *= .5f;
        config.height *= .5f;
        PlaygroundGame.start(args, config, UIClockSyncTest.class);
    }
}
