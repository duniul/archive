package engine;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

/*
 * Keeps track of the players in the game, but is
 * meant to be extended to control the game without
 * being added as a GameObject. Also controls if the
 * game is started or not.
 *
 * ADDED AFTER ASSIGNMENT 1
 */
public class GameHandler implements Inputtable {

    protected boolean isGameStarted;
    protected boolean isGameOver;
    protected ArrayList<GameObject> players;
    protected GameEngine ge;

    public GameHandler(GameEngine ge) {
        this.ge = ge;
        isGameStarted = true;
        isGameOver = false;
        players = new ArrayList<GameObject>();
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean isGameOver) {
        this.isGameOver = isGameOver;
    }

    public ArrayList<GameObject> getPlayers() {
        return players;
    }

    public GameObject getPlayer(int playerNumber) {
        return players.get(playerNumber - 1);
    }

    public void addPlayer(GameObject go) {
        players.add(go);
    }

    // Default ESCAPE operation is to shut down the game.
    @Override
    public void keyInput(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
            if (ge.getPaused()) {
                System.exit(0);
            }
        }
    }

    @Override
    public void keyReleased(int keyCode) {
        // No default operation, should be overridden.
    }
}
