package engine;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/*
 * This class represents an animation used by an object. It keeps track
 * of the display time for individual frames and can loop or end
 * depending on what the user wants.
 *
 * It's based on the example provided in an answer here:
 * http://gamedev.stackexchange.com/questions/53705/how-can-i-make-a-sprite-sheet-based-animation-system
 *
 * ADDED AFTER ASSIGNMENT 1
 */
public class Animation {

    private int frameCount; // Used to count how many frames have passed, increased every tick.
    private int frameDuration; // Decides for how long a frame should be shown.
    private int currentFrame;
    private int totalFrames;
    private String id;
    private boolean isLooping;
    private boolean hasFixedHeight;
    private boolean animationDone = false;

    private boolean stopped;

    private ArrayList<AnimationFrame> frames = new ArrayList<AnimationFrame>();

    public Animation(BufferedImage[] frames, String id, int frameDuration, boolean hasFixedHeight, boolean isLooping) {
        if (frameDuration <= 0) {
            this.frameDuration = 1;
        } else {
            this.frameDuration = frameDuration;
        }

        for (int i = 0; i < frames.length; i++) {
            addFrame(frames[i], frameDuration);
        }

        // Initializes variables.
        this.id = id;
        this.isLooping = isLooping;
        this.hasFixedHeight = hasFixedHeight;
        this.stopped = true;
        frameCount = 0;
        this.frameDuration = frameDuration;
        currentFrame = 0;
        totalFrames = this.frames.size();
    }

    // Start the animation
    public void start() {
        if (!stopped) {
            return;
        }

        if (frames.size() == 0) {
            return;
        }

        stopped = false;
    }

    // Stop the animation
    public void stop() {
        if (frames.size() == 0) {
            return;
        }

        stopped = true;
    }

    // Restart the animation
    public void restart() {
        if (frames.size() == 0) {
            return;
        }

        stopped = false;
        animationDone = false;
        currentFrame = 0;
    }

    // Reset the animation
    public void reset() {
        this.stopped = true;
        this.frameCount = 0;
        this.currentFrame = 0;
        this.animationDone = false;
    }

    // Adds a new frame to the animation.
    private void addFrame(BufferedImage frame, int duration) {
        if (duration <= 0) {
            duration = 1;
        }

        frames.add(new AnimationFrame(frame, duration));
        currentFrame = 0;
    }

    // Updates the animation, meant to be called each tick.
    public void update() {
        // Doesn't update if the animation is stopped or is done and not looping.
        if (!stopped && (isLooping || (!isLooping && !animationDone))) {
            frameCount++;

            // If the frame has been displayed for long enough, switch to the next one.
            // Otherwise, do nothing.
            if (frameCount > frameDuration) {
                frameCount = 0;
                currentFrame++;

                // If the animation has reached its end, mark it as done.
                // If the animation is looping, restart it.
                if (currentFrame > totalFrames - 1) {
                    if (!isLooping) {
                        currentFrame = totalFrames - 1;
                        animationDone = true;
                    } else {
                        currentFrame = 0;
                    }
                } else if (currentFrame < 0) {
                    if (!isLooping) {
                        animationDone = true;
                    }

                    currentFrame = totalFrames - 1;
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public ArrayList<AnimationFrame> getFrames() {
        return frames;
    }

    public BufferedImage getFirstFrame() {
        return frames.get(0).getFrame();
    }

    public BufferedImage getCurrentFrame() {
        return frames.get(currentFrame).getFrame();
    }

    // If the animation uses a fixed height, return that value.
    // Otherwise return the frame height.
    public int getCurrentFrameHeight() {
        if (hasFixedHeight) {
            return frames.get(0).getFrame().getHeight();
        } else {
            return frames.get(currentFrame).getFrame().getHeight();
        }
    }

    public boolean getIsAnimationDone() {
        return animationDone;
    }
}