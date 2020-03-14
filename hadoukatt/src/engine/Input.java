package engine;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/*
 * Handles key input from the player.
 * This class passes information about key presses along to the game objects.
 */

public class Input extends KeyAdapter{

    private GameEngine ge;
	private GameObjectContainer objectContainer;

	public Input(GameEngine ge, GameObjectContainer goc){
	    this.ge = ge;
		objectContainer = goc;
	}

	//When a key is pressed - pass key info along to objects implementing Inputtable
	@Override
	public void keyPressed(KeyEvent e){
		int key = e.getKeyCode();
		// The game handlers keyInputs are allowed even when the game is paused.
		ge.getGameHandler().keyInput(key);
		if(!ge.getPaused()) {
		      for(GameObject go : objectContainer.getObjects()){
		            if(go instanceof Inputtable){
		                Inputtable ki = (Inputtable)go;
		                ki.keyInput(key);
		            }
		        }
		}
	}

	//When a key is released - pass key info along to objects implementing Inputtable
	@Override
	public void keyReleased(KeyEvent e){
		int key = e.getKeyCode();
		for(GameObject go : objectContainer.getObjects()){
			if(go instanceof Inputtable){
				Inputtable ki = (Inputtable)go;
				ki.keyReleased(key);
			}
		}
	}
}
