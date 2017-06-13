package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class TokensTest extends BaseScreen {
	private static final String TAG = TokensTest.class.getSimpleName();

	TokenStacks stacksA;
	TokenStacks stacksB;

	public TokensTest (GameReset game) {
		super(game);
		clear.set(0, 0.05f, 0.1f, 1);

		stacksA = new TokenStacks();
		stacksA.bounds.set(-10, -10, 10, 10);
		stacksA.add(1, 3);
		stacksA.add(3, 2);
		stacksA.add(9, 1);

		stacksB = new TokenStacks();
		stacksB.bounds.set(-10, 10, 10, 10);
		stacksB.add(1, 3);
		stacksB.add(3, 2);
		stacksB.add(9, 1);
	}

	private Vector2 v2 = new Vector2();
	@Override public void render (float delta) {
		super.render(delta);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		stacksA.bounds.set(-5, -18, 10, 10);
		stacksB.bounds.set(-5, 6, 10, 10);
		stacksA.draw(renderer);
		stacksB.draw(renderer);

		gameViewport.unproject(v2.set(Gdx.input.getX(), Gdx.input.getY()));
		Token at = tokenAt(v2.x, v2.y);
		TokenStack ts = stackAt(v2.x, v2.y);
		if (at != null) {
			renderer.setColor(Color.CYAN);
			renderer.rect(at.bounds.x + .2f, at.bounds.y + .2f, at.bounds.width - .4f, at.bounds.height - .4f);
			if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
				split(at);
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
				merge(at);
 			}
		}
		if (ts != null){
			if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
				move(ts);
			}
		}
		renderer.end();
	}

	private void split (Token token) {
		// take high level token and split it into lower level tokens
		if (token.value == 1) return;
		TokenStack stack = token.parent;
		int prevValue = 0;
		if (token.value == 3) prevValue = 1;
		if (token.value == 9) prevValue = 3;
		TokenStacks stacks = stack.parent;
		TokenStack prevStack = null;
		for (TokenStack tokenStack : stacks.stacks) {
			if (tokenStack.tokenValue == prevValue) {
				prevStack = tokenStack;
			}
		}
		freeToken(stack.tokens.removeIndex(stack.tokens.size - 1));
		for (int i = 0; i < 3; i++) {
			prevStack.add(getToken(prevValue));
		}
	}

	private void merge (Token token) {
		// take low level tokens and marge them into higher level token
		if (token.value == 9) return;
		TokenStack stack = token.parent;
		// need 3 to merge
		if (stack.tokens.size < 3) return;
		int nextValue = 0;
		if (token.value == 1) nextValue = 3;
		if (token.value == 3) nextValue = 9;
		TokenStacks stacks = stack.parent;
		TokenStack nextStack = null;
		for (TokenStack tokenStack : stacks.stacks) {
			if (tokenStack.tokenValue == nextValue) {
				nextStack = tokenStack;
			}
		}
		for (int i = stack.tokens.size -1, j = 3; i >= 0 && j > 0; i--, j--) {
			freeToken(stack.tokens.removeIndex(i));
		}
		nextStack.add(getToken(nextValue));
	}

	private void move (TokenStack tokenStack) {
		// move token between stacks, automatically splitting/merging if needed/possible

	}

	private Token tokenAt (float x, float y) {
		for (TokenStack stack : stacksA.stacks) {
			for (Token token : stack.tokens) {
				if (token.bounds.contains(x, y)) {
					return token;
				}
			}
		}
		for (TokenStack stack : stacksB.stacks) {
			for (Token token : stack.tokens) {
				if (token.bounds.contains(x, y)) {
					return token;
				}
			}
		}
		return null;
	}

	private TokenStack stackAt (float x, float y) {
		for (TokenStack stack : stacksA.stacks) {
			if (stack.bounds.contains(x, y)) {
				return stack;
			}
		}
		for (TokenStack stack : stacksB.stacks) {
			if (stack.bounds.contains(x, y)) {
				return stack;
			}
		}
		return null;
	}

	// single token with a value
	protected static class Token implements Pool.Poolable {
		Rectangle bounds = new Rectangle();
		final int value;
		TokenStack parent;

		public Token (int value) {
			this.value = value;
			bounds.set(0, 0, 3f, 3f);
		}

		public void draw (ShapeRenderer shapes) {
			switch (value) {
			case 1: {
				shapes.setColor(Color.RED);
			} break;
			case 3: {
				shapes.setColor(Color.GREEN);
			} break;
			case 9: {
				shapes.setColor(Color.BLUE);
			} break;
			}
			shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		@Override public void reset () {
			bounds.setPosition(0, 0);
			parent = null;
		}
	}

	// a set of tokens with same value
	protected static class TokenStack {
		Rectangle bounds = new Rectangle();
		final int tokenValue;
		int totalValue;
		Array<Token> tokens = new Array<>();
		TokenStacks parent;

		public TokenStack (int tokenValue, int tokenCount) {
			this.tokenValue = tokenValue;
			totalValue = tokenValue * tokenCount;
			for (int i = 0; i < tokenCount; i++) {
				Token token = getToken(tokenValue);
				token.parent = this;
				tokens.add(token);
			}
		}

		public int getTotalValue() {
			totalValue = totalValue * tokens.size;
			return totalValue;
		}

		public void draw (ShapeRenderer shapes) {
			for (int i = 0; i < tokens.size; i++) {
				Token token = tokens.get(i);
				token.bounds.setPosition(bounds.x + .2f, bounds.y + i * (token.bounds.height * 1.1f) + .2f);
				bounds.width = token.bounds.width + .4f;
				bounds.height = token.bounds.y - bounds.y + token.bounds.height + .2f;
			}
			shapes.setColor(Color.MAGENTA);
			shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);

			for (int i = 0; i < tokens.size; i++) {
				Token token = tokens.get(i);
				token.draw(shapes);
			}
		}

		public void add (Token token) {
			tokens.add(token);
			token.parent = this;
		}
	}

	// a group of token stacks with different values
	protected static class TokenStacks {
		Rectangle bounds = new Rectangle();
		int totalValue;
		Array<TokenStack> stacks = new Array<>();

		public void add (int stackValue, int tokenCount) {
			totalValue += stackValue * tokenCount;
			TokenStack stack = new TokenStack(stackValue, tokenCount);
			stack.parent = this;
			stacks.add(stack);
		}

		public void draw (ShapeRenderer shapes) {
			for (int i = 0; i < stacks.size; i++) {
				TokenStack stack = stacks.get(i);
				stack.bounds.setPosition(bounds.x + .5f + (stack.bounds.width + .5f)* i, bounds.y + .5f);
				stack.draw(shapes);
			}
		}

		public int getTotalValue() {
			totalValue = 0;
			for (TokenStack stack : stacks) {
				totalValue += stack.getTotalValue();
			}
			return totalValue;
		}
	}

	protected static IntMap<Pool<Token>> tokenPools = new IntMap<>();
	protected static Token getToken (final int tokenValue) {
		Pool<Token> pool = tokenPools.get(tokenValue, null);
		if (pool == null) {
			pool = new Pool<Token>(64) {
				@Override protected Token newObject () {
					return new Token(tokenValue);
				}
			};
			tokenPools.put(tokenValue, pool);
		}
		return pool.obtain();
	}

	private void freeToken (Token token) {
		tokenPools.get(token.value).free(token);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width = 720/2;
		config.height = 1280/2;
		PlaygroundGame.start(args, config, TokensTest.class);
	}
}
