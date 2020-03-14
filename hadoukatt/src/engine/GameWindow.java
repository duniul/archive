package engine;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JFrame;

//Acts as a simple Window class.

public class GameWindow extends Canvas {

	private static final long serialVersionUID = -7975505597069675943L;

	JFrame frame;
	private int width;
	private int height;

	public GameWindow(int w, int h, String title, GameEngine ge){
		this.width = w;
		this.height = h;
		frame = new JFrame(title);
		frame.setPreferredSize(new Dimension(w, h));
		frame.setMaximumSize(new Dimension(w, h));
		frame.setMinimumSize(new Dimension(w, h));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
		frame.add(ge);
		frame.setVisible(true);
	}

	public int getHeight(){
		return height;
	}

	public int getWidth(){
		return width;
	}

	public void setFullscreen(boolean makeFullscreen) {
	    if(makeFullscreen) {
	        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
	        frame.setUndecorated(true);
	    } else {
	        frame.setExtendedState(Frame.NORMAL);
            frame.setUndecorated(false);
	    }
	}
}
