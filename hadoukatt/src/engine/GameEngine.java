package engine;

import javafx.embed.swing.JFXPanel;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

/*
 * This class contains the game loop, as well as some methods to allow
 * for easier access of values and structures stored in other classes.
 *
 * CHANGES SINCE ASSIGNMENT 1:
 * - GameHandler handles player management instead of GameEngine.
 * - added the ability to pause the game.
 */
public class GameEngine extends Canvas implements Runnable {

    private static final long serialVersionUID = -5103206432348131407L;
    // These variables are used to control the delta time of the game loop.
    // Uses Variable timestep. Partially based on the implementation described in the link below:
    // http://www.java-gaming.org/index.php?PHPSESSID=bia0f1lfk6oth7asdgtnrsfou5&/topic,24220.0
    private final long ONE_SECOND = 1000000000;
    private final long NANO_TO_MILI = 1000000;
    private long optimalTime;

    // This value is used to determine how many laps have passed in the current second
    private int lapsInCurrentSec = 0;

    // Treats the HUD as separate from the rest of the game objects for greater control
    private GameHUD hud;

    // Initiates JFXPanel - needed for AudioEngine to work.
    static JFXPanel fxPanel = new JFXPanel();

    // Contains all the GameObjects currently loaded within the game
    private GameObjectContainer objectContainer = new GameObjectContainer();

    // The class that will handle input. Will pass the info along to input components.
    private Input input;

    // GameHandler that will be used by the engine.
    private GameHandler gameHandler = null;

    // Creates the game window. Suppresses unused warning since
    // the window only needs to be initialized to be of use.
    @SuppressWarnings("unused")
    private GameWindow win;
    private Thread primeThread;
    boolean running = false;

    // Instance of loader used to load resources.
    private Loader loader = Loader.getInstance();

    // Boolean that controls if the game is paused or not (not the thread).
    private boolean pauseState;

    // The constructor creates a new game window and initializes objects and listeners.
    public GameEngine(int width, int height, String title, int targetFps) {
        win = new GameWindow(width, height, title, this);
        optimalTime = ONE_SECOND / targetFps;
        input = new Input(this, objectContainer);
        gameHandler = new GameHandler(this);
        this.addKeyListener(input);
    }

    // Initiates the thread in which the engine will execute
    public synchronized void start() {
        primeThread = new Thread(this);
        primeThread.start();
        running = true;
    }

    // Stops the thread
    public synchronized void stop() {
        try {
            primeThread.join(0);
            running = false;
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    // Pauses the game by freezing every object.
    public void setPaused(boolean pauseState) {
        this.pauseState = pauseState;
    }

    // Resumes a paused game.
    public boolean getPaused() {
        return pauseState;
    }

    // Main loop of the game engine
    public void run() {
        this.requestFocus();
        long lastFpsTime = 0;
        long lastLoopTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;
            double delta = updateLength / ((double) optimalTime);
            lastFpsTime += updateLength;
            if (lastFpsTime >= ONE_SECOND) {
                lastFpsTime = 0;
                lapsInCurrentSec = 0;
            }

            // Delta needs to be know by all game objects so that they
            // can act independent of FPS drop. Only tick objects if the
            // game isn't paused.
            if (!pauseState) {
                objectContainer.tickAllObjects(delta);
            }

            // If the implementation has added a HUD - update this separately.
            if (hud != null) {
                hud.tick();
            }

            // Render all objects, including HUD.
            render();

            // If the loop was faster than the FPS demands, sleep to keep time synchronized.
            if ((lastLoopTime - System.nanoTime() + optimalTime) / NANO_TO_MILI > 0) {
                try {
                    Thread.sleep((lastLoopTime - System.nanoTime() + optimalTime) / NANO_TO_MILI);
                } catch (InterruptedException e) {
                    System.err.println("Thread.sleep interrupted.");
                    e.printStackTrace();
                    running = false;
                }
            }
            lapsInCurrentSec++;
        }
        stop();
    }

    // Renders all objects and creates a BufferStrategy if one is not yet present.
    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics2D g = (Graphics2D) bs.getDrawGraphics();

        objectContainer.renderAll(g);

        if (hud != null) {
            hud.render(g);
        }

        g.dispose();
        bs.show();
    }

    public int getLapsInCurrentSec() {
        return lapsInCurrentSec;
    }

    // Adds an object which implements the HUD interface.
    public void addHud(GameHUD hud) {
        this.hud = hud;
    }

    // Adds a GameObject to the engine
    public void addGameObject(GameObject go) {
        objectContainer.addObject(go);
    }

    /*
     * Special add method used for player object - useful so that the game programmer more easily
     * can access the player object after adding it
     */
    public void addPlayerObject(GameObject go) {
        if (gameHandler.getPlayers().size() <= 2) {
            gameHandler.addPlayer(go);
            objectContainer.addObject(go);
        } else {
            System.out.println("Max amount of players have been added.");
        }
    }

    public void removeObject(GameObject go) {
        if (gameHandler.getPlayers().contains(go)) {
            gameHandler.getPlayers().remove(go);
        }
        objectContainer.removeObject(go);
    }

    /*
     * A method that can bypass the need to access the physics engine directly Present only for
     * convenience in the game implementation.
     */
    public void setGravity(int gravityForce) {
        objectContainer.setGravity(gravityForce);
    }

    public Input getInputInstance() {
        return input;
    }

    public Loader getLoader() {
        return loader;
    }

    public GameObjectContainer getGameObjectContainer() {
        return objectContainer;
    }

    public PhysicsEngine getPhysicsEngine() {
        return objectContainer.getPhysicsEng();
    }

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public void setGameHandler(GameHandler gh) {
        this.gameHandler = gh;
    }

}
