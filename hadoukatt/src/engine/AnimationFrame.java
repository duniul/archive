package engine;

import java.awt.image.BufferedImage;

/*
 * This class is used by the Animation class for its frames.
 * It keeps the image used in the animation together with the display duration.
 *
 * It's based on the example provided in an answer here:
 * http://gamedev.stackexchange.com/questions/53705/how-can-i-make-a-sprite-sheet-based-animation-system
 *
 * ADDED AFTER ASSIGNMENT 1
 */
public class AnimationFrame {

    private BufferedImage frame;
    private int duration;

    public AnimationFrame(BufferedImage frame, int duration) {
        this.frame = frame;
        this.duration = duration;
    }

    public BufferedImage getFrame() {
        return frame;
    }

    public void setFrame(BufferedImage frame) {
        this.frame = frame;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

}
