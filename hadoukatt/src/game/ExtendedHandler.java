package game;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import engine.AudioEngine;
import engine.GameEngine;
import engine.GameHandler;
import engine.GameObject;
import engine.Inputtable;

/*
 * This extended version of the GameHandler keeps track of who wins
 * the round and who dies.
 */
public class ExtendedHandler extends GameHandler implements Inputtable {

    private PlayerObject roundWinner;
    private HashMap<PlayerObject, Integer> playerDeaths;
    private HashMap<String, String> soundPaths;

    // Needs to be attached to the game engine aswell by using
    // setGameHandler() in the GameEngine class.
    public ExtendedHandler(GameEngine ge) {
        super(ge);
        playerDeaths = new HashMap<PlayerObject, Integer>();
        soundPaths = new HashMap<String, String>();
    }

    // Kills a player and adds to the death counter, which is
    // used by the HUD to display the points.
    public void registerDeath(PlayerObject player) {
        player.getStats().setIsAlive(false);

        // If the game isn't over, save the death and end the round, then
        // mark the game as over. This condition have to be met in order to
        // prevent players from getting points after someone already has died.
        if (!isGameOver) {
            playerDeaths.put(player, playerDeaths.get(player) + 1);
            if (player.equals(players.get(0))) {
                roundWinner = (PlayerObject) players.get(1);
                if (soundPaths.containsKey("p2win")) {
                    AudioEngine.playSound(ge.getLoader().getSound(soundPaths.get("p2win")));
                }
            } else {
                roundWinner = (PlayerObject) players.get(0);
                if (soundPaths.containsKey("p1win")) {
                    AudioEngine.playSound(ge.getLoader().getSound(soundPaths.get("p1win")));
                }
            }
            isGameOver = true;
        }
    }

    @Override
    public void addPlayer(GameObject go) {
        if (go instanceof PlayerObject) {
            PlayerObject player = (PlayerObject) go;
            players.add(go);
            playerDeaths.put(player, 0);
        }
    }

    @Override
    public void keyInput(int keyCode) {
        // ESCAPE pauses the game, clicking it again exits
        // the game while clicking ENTER resumes it.
        if (keyCode == KeyEvent.VK_ESCAPE) {
            if (ge.getPaused()) {
                System.exit(0);
            } else {
                ge.setPaused(true);
                AudioEngine.pauseMusic();
                if (soundPaths.containsKey("pause")) {
                    AudioEngine.playSound(ge.getLoader().getSound(soundPaths.get("pause")));
                }
            }
        }

        // Clicking ENTER when the game is finished
        // starts a new round.
        if (keyCode == KeyEvent.VK_ENTER) {
            if (ge.getPaused()) {
                ge.setPaused(false);
                if (soundPaths.containsKey("pause")) {
                    AudioEngine.playSound(ge.getLoader().getSound(soundPaths.get("pause")));
                }
                AudioEngine.resumeMusic();
            } else if (isGameOver) {
                resetRound();
            }
        }
    }

    @Override
    public void keyReleased(int keyCode) {
        // keyReleased() has to be overridden, but doesn't do anything here.
    }


    // Resets all player objects to their original state.
    public void resetRound() {
        for (int i = 0; i < players.size(); i++) {
            ((PlayerObject) players.get(i)).resetPlayer();
        }
        isGameOver = false;
        if (soundPaths.containsKey("announcer")) {
            AudioEngine.playSound(ge.getLoader().getSound(soundPaths.get("announcer")));
        }
    }

    public int getPlayerDeaths(PlayerObject player) {
        return playerDeaths.get(player);
    }

    public PlayerObject getRoundWinner() {
        return roundWinner;
    }

    public void addSoundFilePath(String id, String soundPath) {
        soundPaths.put(id, soundPath);
    }

    public void addSoundFilePaths(HashMap<String, String> soundPaths) {
        this.soundPaths.putAll(soundPaths);
    }

}
