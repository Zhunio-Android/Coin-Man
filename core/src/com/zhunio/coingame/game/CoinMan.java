package com.zhunio.coingame.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.badlogic.gdx.Gdx.audio;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;

public class CoinMan extends ApplicationAdapter {

    /** Batch used for drawing batches. */
    private SpriteBatch mBatch;

    /** Current man state. */
    private ManState mManState;
    /** Current game state */
    private GameState mGameState;

    /** Background image. */
    private Texture mBackgroundImage;
    /** Bomb image */
    private Texture mBombImage;
    /** Coin image. */
    private Texture mCoinImage;
    /** Dizzy man image. */
    private Texture mDizzyManImage;
    /** Man image. */
    private Texture mManImage;
    /** Running man frame images. */
    private Texture[] mRunningManImages;

    /** Fond used. */
    private BitmapFont mFont;

    /** Sound when colliding a star. */
    private Sound mStarSound;
    /** Sound when colliding a bomb. */
    private Sound mBombSound;
    /** Sound when jumping. */
    private Sound mJumpSound;

    /** Controls how often coins, bombs, adn the man are drawn on the screen */
    private int mTimeCount;
    /** Current score in the game. */
    private int mScoreCount;

    /** Man rectangle for collision detection. */
    private Rectangle mManRectangle;
    /** Bomb rectangle list for collision detection. */
    private List<Rectangle> mBombRectangles;
    /** Coin rectangle list for collision detection. */
    private List<Rectangle> mCoinRectangles;

    /** Man y position. */
    private int mManYPosition;
    /** Man y position. */
    private int mManXPosition;
    /** Bomb x positions on the screen. */
    private List<Integer> mBombXPositions;
    /** Bomb y positions on the screen. */
    private List<Integer> mBombYPositions;
    /** Coins x positions on the screen. */
    private List<Integer> mCoinXPositions;
    /** Coin y positions on the screen */
    private List<Integer> mCoinYPositions;

    /** Controls how fast the man falls. */
    private float mGravity;
    /** Accumulates how much the y position decrements. */
    private float mManYDecrement;

    /** Random generator. */
    private Random mRandom;

    @Override
    public void create() {
        mBatch = new SpriteBatch();

        initStates();
        initImages();
        initFont();
        initSounds();
        initCounts();
        initRectangles();
        initPositions();

        mGravity = .5f;
        mRandom = new Random();
    }

    /** Initializes the man and game state. */
    private void initStates() {
        mManState = new ManState(0);
        mGameState = new GameState(GameState.PAUSED);
    }

    /** Initializes the background, bomb, coin, man, dizzy-man, and running images. */
    private void initImages() {
        mBackgroundImage = new Texture("bg.png");
        mBombImage = new Texture("bomb.png");
        mCoinImage = new Texture("coin.png");
        mDizzyManImage = new Texture("dizzy-1.png");
        mRunningManImages = new Texture[]{
                new Texture("frame-1.png"),
                new Texture("frame-2.png"),
                new Texture("frame-3.png"),
                new Texture("frame-4.png")
        };
        mManImage = mRunningManImages[mManState.getState()];
    }

    /** Initializes the font settings */
    private void initFont() {
        mFont = new BitmapFont();
        mFont.setColor(Color.WHITE);
        mFont.getData().setScale(10);
    }

    /** Initializes the sounds for the start, bomb, and jump. */
    private void initSounds() {
        mStarSound = audio.newSound(files.internal("audio/start.mp3"));
        mBombSound = audio.newSound(files.internal("audio/bomb.mp3"));
        mJumpSound = audio.newSound(files.internal("audio/jump.mp3"));
    }

    /** Initializes the counts for the bomb, coin, time, and score. */
    private void initCounts() {
        mTimeCount = 0;
        mScoreCount = 0;
    }

    /** Initializes the rectangles for the man, bomb, and coin. */
    private void initRectangles() {
        mManRectangle = null;
        mBombRectangles = new ArrayList<Rectangle>();
        mCoinRectangles = new ArrayList<Rectangle>();
    }

    /** Initializes the xy positions of the man, bomb, and coin. */
    private void initPositions() {
        mManYPosition = graphics.getHeight() / 2 - (mManImage.getHeight() / 2);
        mManXPosition = graphics.getWidth() / 2 - (mManImage.getWidth() / 2);
        mBombXPositions = new ArrayList<Integer>();
        mBombYPositions = new ArrayList<Integer>();
        mCoinXPositions = new ArrayList<Integer>();
        mCoinYPositions = new ArrayList<Integer>();
    }

    /** Methods called when re-starting the game. */
    private void reStartGame() {
        initStates();
        initCounts();
        mManYDecrement = 0;
        initRectangles();
        initPositions();
    }

    /**
     * This method is a loop iterating continuously.
     */
    @Override
    public void render() {
        mBatch.begin();
        mBatch.draw(mBackgroundImage, 0, 0, graphics.getWidth(), graphics.getHeight());

        switch (mGameState.getState()) {
            case GameState.LIVE:
                /* BOMBS */
                // Make bomb every 150TH time
                final int ONE_FIFTY = 150;
                if (isNthTime(mTimeCount, ONE_FIFTY))
                    makeCoinOrBomb(mBombXPositions, mBombYPositions, mBombImage);

                /* COINS */
                // Make coins every 50TH time
                final int FIFTY = 50;
                if (isNthTime(mTimeCount, FIFTY))
                    makeCoinOrBomb(mCoinXPositions, mCoinYPositions, mCoinImage);

                /* MAN */
                // switch man state
                final int SIX = 6;
                if (isNthTime(mTimeCount, SIX))
                    mManState.setState((mManState.getState() + 1) % mRunningManImages.length);

                // Create bombs, coins and rectangles, and draw them.
                drawCoinsOrBombs(mBombImage, mBombXPositions, mBombYPositions, mBombRectangles, 8);
                drawCoinsOrBombs(mCoinImage, mCoinXPositions, mCoinYPositions, mCoinRectangles, 4);

                // Select correct mManImage frame
                mManImage = mRunningManImages[mManState.getState()];

                // Make the mManImage jump if touched
                if (input.justTouched()) {
                    mJumpSound.play();
                    mManYDecrement = -20;
                }

                // Calculate by how much the mManImage falls
                mManYDecrement += mGravity;

                // Update mManYPosition
                mManYPosition -= mManYDecrement;

                // Make sure man does not fall off the screen
                if (mManYPosition <= 0)
                    mManYPosition = 0;
                // Make sure man does not jump off the screen
                else if (mManYPosition + mManImage.getHeight() > graphics.getHeight())
                    mManYPosition = graphics.getHeight() - mManImage.getHeight();

                break;
            case GameState.PAUSED:
                if (input.justTouched()) {mGameState.setState(GameState.LIVE);}
                break;
            case GameState.GAME_OVER:

                if (input.justTouched()) {reStartGame();}
                break;
        }

        // Update time count
        mTimeCount++;

        // draw man image based-off game state
        if (mGameState.getState() == GameState.GAME_OVER)
            mBatch.draw(mDizzyManImage, mManXPosition, mManYPosition);
        else
            mBatch.draw(mManImage, mManXPosition, mManYPosition);

        // Create new mManImage rectangle for collisions
        mManRectangle = new Rectangle(mManXPosition, mManYPosition, mManImage.getWidth(), mManImage
                .getHeight());

        // Check coin or bomb collision
        checkCoinOrBombCollision(mManRectangle, mCoinXPositions, mCoinYPositions, mCoinRectangles,
                false);
        checkCoinOrBombCollision(mManRectangle, mBombXPositions, mBombYPositions, mBombRectangles,
                true);

        // draw the font
        mFont.draw(mBatch, String.valueOf(mScoreCount), 100, 200);

        mBatch.end();
    }

    @Override
    public void dispose() {
        mBatch.dispose();
    }

    /**
     * Checks if there is a coin or bomb collision and plays a sound unique to the outcome.
     *
     * @param manRectangle         man rectangle.
     * @param manXPositions        man x positions.
     * @param manYPositions        man y positions.
     * @param coinOrBombRectangles coin or bomb rectangles.
     * @param isBombCollision      true if the collision is on a bomb.
     */
    private void checkCoinOrBombCollision(Rectangle manRectangle,
                                          List<Integer> manXPositions, List<Integer> manYPositions,
                                          List<Rectangle> coinOrBombRectangles,
                                          boolean isBombCollision) {
        for (int i = 0; i < coinOrBombRectangles.size(); i++) {
            if (thereIsCollision(manRectangle, coinOrBombRectangles.get(i))) {
                coinOrBombRectangles.remove(i);
                manXPositions.remove(i);
                manYPositions.remove(i);

                if (isBombCollision) {
                    mBombSound.play();
                    mGameState.setState(GameState.GAME_OVER);
                    mScoreCount--;
                } else {
                    mStarSound.play();
                    mScoreCount++;
                }
            }
        }
    }

    /**
     * Draws images on the screen based-off their xy positions.
     *
     * @param texture    an image.
     * @param xPositions x positions.
     * @param yPositions y positions.
     * @param rectangles rectangles.
     * @param offset     x offset.
     */
    private void drawCoinsOrBombs(Texture texture,
                                  List<Integer> xPositions, List<Integer> yPositions,
                                  List<Rectangle> rectangles, int offset) {
        rectangles.clear();
        for (int i = 0; i < xPositions.size(); i++) {
            int xPosition = xPositions.get(i);
            int yPosition = yPositions.get(i);

            Rectangle rectangle =
                    new Rectangle(xPosition, yPosition, texture.getWidth(), texture.getHeight());

            rectangles.add(rectangle);
            // update x position of the texture
            xPositions.set(i, xPosition - offset);

            mBatch.draw(texture, xPosition, yPosition);
        }
    }

    /**
     * Makes coins or bombs.
     *
     * @param xPositions x positions.
     * @param yPositions y positions.
     */
    private void makeCoinOrBomb(List<Integer> xPositions, List<Integer> yPositions, Texture image) {
        int randomHeight = randInt(0, graphics.getHeight());

        // Don't draw images off the screen
        if (randomHeight + image.getHeight() > graphics.getHeight())
            randomHeight = graphics.getHeight() - image.getHeight();

        yPositions.add(randomHeight);
        xPositions.add(graphics.getWidth());
    }

    /** Returns true if there is a collision. */
    private boolean thereIsCollision(Rectangle r1, Rectangle r2) {
        return Intersector.overlaps(r1, r2);
    }

    /** Returns true if counter is the nth time. */
    private boolean isNthTime(int counter, int n) {
        return counter % n == 0;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    private int randInt(int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return mRandom.nextInt((max - min) + 1) + min;
    }
}
