package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.TimeUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
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
                    setText(sdf.format(client1.serverTime) + " :: " + client1.timeDiff);
                }
            };
            root.add(client1STime).expandX().left().row();

            root.add(new VisLabel("Client 1 CT:")).expandX().right().pad(10);
            client1CTime = new VisLabel("Client 1: ") {
                @Override public void act (float delta) {
                    super.act(delta);
                    setText(sdf.format(client1.localTime) + " :: " +(client1.localTime - client1.serverTime));
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
                    setText(sdf.format(client2.localTime) + " :: " +(client2.localTime - client2.serverTime));
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
        float lagMinMS = 2750;
        float lagMaxMS = 3250;
        ScheduledExecutorService service;
        private long serverTime;
        Array<Client> clients = new Array<>();

        public Server () {
            service = Executors.newSingleThreadScheduledExecutor();
        }

        public void tick () {
            for (final Client client : clients) {
                final TimeMessage message = new TimeMessage(serverTime);
                // lag + random spike
                long spike = MathUtils.random(1f) <= .05f ? 2 : 1;
                final long lag = (long)(MathUtils.random(lagMinMS, lagMaxMS)) * spike;
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
        private long timeDiff = -1;
        private SkipMean mean = new SkipMean(10, 1, 3);

        public Client () {
            localTime = TimeUtils.millis();
        }

        public void update (float delta) {
            // we probably dont want the time to go back
            long newTime = TimeUtils.millis() - timeDiff;
            if (newTime > localTime) {
                localTime = newTime;
            }
        }

        public void message (TimeMessage message) {
            // ignore old messages, tho cant really happen with tcp
            if (message.ts < serverTime) {
                return;
            }
            serverTime = message.ts;
            long timeDiff = TimeUtils.millis() - serverTime;
            mean.addValue(timeDiff);
            if (!mean.hasEnoughData()) {
                this.timeDiff = timeDiff;
            } else {
                this.timeDiff = (long)mean.getMean();
            }
        }
    }

    private static class TimeMessage {
        private long ts;

        public TimeMessage (long ts) {
            this.ts = ts;
        }
    }

    private static class SkipMean {
        float values[];
        int addedValues = 0;
        int lastValue;
        float mean = 0;
        int skipLow = 0;
        int skipHigh = 0;
        boolean dirty = true;
        FloatArray tmpValues;

        public SkipMean (int windowSize, int skipLow, int skipHigh) {
            values = new float[windowSize];
            if (windowSize < skipLow + skipHigh + 1) {
                throw new AssertionError("Window size too small for given skip counts");
            }
            this.skipLow = skipLow;
            this.skipHigh = skipHigh;
            tmpValues = new FloatArray(windowSize);
            tmpValues.size = windowSize;
        }

        public boolean hasEnoughData () {
            return addedValues >= values.length;
        }

        public void clear () {
            addedValues = 0;
            lastValue = 0;
            for (int i = 0; i < values.length; i++) {
                values[i] = 0;
            }
            dirty = true;
        }

        public void addValue (float value) {
            if (addedValues < values.length) {
                addedValues++;
            }
            values[lastValue++] = value;
            if (lastValue > values.length - 1) {
                lastValue = 0;
            }
            dirty = true;
        }

        public float getMean () {
            if (hasEnoughData()) {
                if (dirty) {
                    for (int i = 0; i < values.length; i++) {
                        tmpValues.set(i, values[i]);
                    }
                    tmpValues.sort();
                    float[] items = tmpValues.items;
                    float mean = 0;
                    int count = items.length - (skipLow + skipHigh);
                    for (int i = skipLow; i < count; i++)
                        mean += values[i];

                    this.mean = mean / count;
                    dirty = false;
                }
                return this.mean;
            } else {
                return 0;
            }
        }
    }


    // allow us to start this test directly
    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
        config.setWindowedMode(1280/2, 720/2);
        PlaygroundGame.start(args, config, UIClockSyncTest.class);
    }
}
