package engine;

//One of the interfaces that subclasses of GameObject can implement

public interface Inputtable {
	
	public void keyInput(int keyCode);
	
	public void keyReleased(int keyCode);
}
