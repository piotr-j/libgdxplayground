package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class ParseTest extends BaseScreen {
    private static final String TAG = ParseTest.class.getSimpleName();

    public ParseTest (GameReset game) {
        super(game);

        for (int i = 0; i < 30; i++) {
            long start = TimeUtils.nanoTime();
            FileHandle fh = Gdx.files.internal("parse.txt");
            if (!fh.exists()) {
                throw new AssertionError("File doesnt exist!");
            }

            // small enough to read entire thing
            ActorsData actorsData = parse(fh.reader());
            Gdx.app.log(TAG, "Done in " + (TimeUtils.nanoTime() - start) / 1000000f + "ms");
//            Gdx.app.log(TAG, "Data = " + actorsData);
        }
    }

    private ActorsData parse (Reader reader) {
        Gdx.app.log(TAG, "Parsing stuff!");
        ActorsData asd = new ActorsData();
        try {
            char[] buffer = new char[512];
            int version = readVersion(reader, buffer);
            if (version == -1) {
                throw new AssertionError("Invalid version!");
            } else {
                Gdx.app.log(TAG, "Version = " + version);
                asd.version = version;
            }
            ActorData actorData = null;
            int line = 0;
            while (true) {
                // TODO is read () slow?
                int count = readLine(reader, buffer);
                if (count == -1) break;
                line++;
                count = trimComments(buffer, count, '#');
                if (count == 0) continue;
                if (isWhitespace(buffer, count)) continue;
                int trimmedCount = trimWhitespace(buffer, count);
//                Gdx.app.log(TAG, "line " + line + " :: " + new String(buffer, 0, trimmedCount));
                if (count - trimmedCount == 0) {
                    // new ActorData
                    actorData = new ActorData();
                    asd.actorData.add(actorData);
                    actorData.name = new String(buffer, 0, trimmedCount);
                } else {
                    if (actorData == null) {
                        Gdx.app.log(TAG, "No actor data, but line is indented! " + new String(buffer, 0, trimmedCount));
                        continue;
                    }
                    int nameCount = trimToChar(buffer, trimmedCount, ':');
                    String name = new String(buffer, 0, nameCount);
                    int valCount = trimFromChar(buffer, trimmedCount, ':');
                    String valStr = new String(buffer, 0, valCount);
                    float value = Float.parseFloat(valStr);
                    if ("x".equals(name)) {
                        actorData.bounds.x = valCount;
                    } else if ("y".equals(name)) {
                        actorData.bounds.y = valCount;
                    } else if ("w".equals(name)) {
                        actorData.bounds.width = valCount;
                    } else if ("h".equals(name)) {
                        actorData.bounds.height = valCount;
                    } else {
                        if (actorData.extras == null) {
                            actorData.extras = new ObjectFloatMap<>();
                        }
                        actorData.extras.put(name, value);
                    }
                }
//                Gdx.app.log(TAG, new String(buffer, 0, count));
//                // TODO make sure we cant overflow
//                buffer[count++] = val;
//                switch (val) {
//                    case '\n': {
//                        Gdx.app.log(TAG, new String(buffer, 0, count-1));
//                        count = 0;
//                    } break;
//                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return asd;
    }

    private int trimToChar (char[] buffer, int count, char c) {
        int ws = 0;
        // find first non whitespace from the start
        for (int i = 0; i < count; i++) {
            if (buffer[i]==c) break;
            ws++;
        }
        return ws;
    }

    private int trimFromChar (char[] buffer, int count, char c) {
        int we = count;
        // find first non whitespace from the end
        for (int i = count-1; i >= 0; i--) {
            if (buffer[i]==c) break;
            we--;
        }
        System.arraycopy(buffer, we, buffer, 0, count-we);
        return count-we;
    }

    private int readVersion (Reader reader, char[] buffer) throws IOException {
        int version = -1;
        while (true) {
            int count = readLine(reader, buffer);
            if (count == -1) break;
            count = trimComments(buffer, count, '#');
            if (count == 0) continue;
            if (isWhitespace(buffer, count)) continue;
            count = trimWhitespace(buffer, count);
            if (count > 0) {
                String val = new String(buffer, 0, count);
                // new String just for the parser to use chars
                try {
                    return Integer.parseInt(val);
                } catch (NumberFormatException ex) {
                    Gdx.app.log(TAG, "Failed for parse int value! " + val);
                    return -1;
                }
            }

        }
        return version;
    }

    private int trimWhitespace (char[] buffer, int count) {
        int ws = 0;
        // find first non whitespace from the start
        for (int i = 0; i < count; i++) {
            if (!isWhitespace(buffer[i])) break;
            ws++;
        }
        int we = count;
        // find first non whitespace from the end
        for (int i = count-1; i >= 0; i--) {
            if (!isWhitespace(buffer[i])) break;
            we--;
        }
        System.arraycopy(buffer, ws, buffer, 0, we - ws);
        return we - ws;
    }

    private boolean isWhitespace (char ch) {
        switch (ch) {
        case ' ': return true;
        case '\t': return true;
        }
        return false;
    }

    private boolean isWhitespace (char[] buffer, int count) {
        for (int i = 0; i < count; i++) {
            char ch = buffer[i];
            if (!isWhitespace(ch)) {
                return false;
            }
        }
        return true;
    }

    private int trimComments (char[] buffer, int count, char commentChar) {
        int comment = count;
        for (int i = 0; i < count; i++) {
            if (buffer[i] == commentChar) {
                comment = i;
                break;
            }
        }
        return comment;
    }

    private int readLine (Reader reader, char[] buffer) throws IOException {
        char[] single = new char[1];
        int count = 0;
        while (true) {
            int read = reader.read(single, 0, 1);
            if (read == -1) {
                return (count > 0)?count:read;
            }
            if (single[0] == '\r' || single[0] == '\n') {
                break;
            }
            buffer[count++] = single[0];
        }
        return count;
    }

    static class ActorsData {
        int version;
        Array<ActorData> actorData = new Array<>();

        @Override public String toString () {
            return "ActorsData{" + "version=" + version + ", actorData=" + actorData + '}';
        }
    }

    static class ActorData {
        String name;
        Rectangle bounds = new Rectangle();
        ObjectFloatMap<String> extras = null;

        @Override public String toString () {
            return "ActorData{" + "name='" + name + '\'' + ", bounds=" + bounds + ", extras=" + extras + '}';
        }
    }

    // allow us to start this test directly
    public static void main (String[] args) {
        LwjglApplicationConfiguration config = PlaygroundGame.config();
        config.width = 720/2;
        config.height = 1280/2;
        PlaygroundGame.start(args, config, ParseTest.class);
    }
}
