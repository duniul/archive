package game;

/*
 * This class sets the default controls used for the players.
 */
public class PlayerControls {

    private int keyUp;
    private int keyDown;
    private int keyLeft;
    private int keyRight;
    private int keyShoot;
    private int keyBlock;

    public PlayerControls() {

    }

    public PlayerControls(int keyUp, int keyDown, int keyLeft, int keyRight, int keyShoot, int keyBlock) {
        this.keyUp = keyUp;
        this.keyDown = keyDown;
        this.keyLeft = keyLeft;
        this.keyRight = keyRight;
        this.keyShoot = keyShoot;
        this.keyBlock = keyBlock;

    }

    // Getters and setters
    public int[] getControls() {
        int keyArr[] = { keyUp, keyDown, keyLeft, keyRight, keyShoot, keyBlock };
        return keyArr;
    }

    public void setControls(int keyUp, int keyDown, int keyLeft, int keyRight, int keyUse, int keyBlock) {
        this.keyUp = keyUp;
        this.keyDown = keyDown;
        this.keyLeft = keyLeft;
        this.keyRight = keyRight;
        this.keyShoot = keyUse;
        this.keyBlock = keyBlock;
    }

    public int getKeyUp() {
        return keyUp;
    }

    public void setKeyUp(int keyUp) {
        this.keyUp = keyUp;
    }

    public int getKeyDown() {
        return keyDown;
    }

    public void setKeyDown(int keyDown) {
        this.keyDown = keyDown;
    }

    public int getKeyLeft() {
        return keyLeft;
    }

    public void setKeyLeft(int keyLeft) {
        this.keyLeft = keyLeft;
    }

    public int getKeyRight() {
        return keyRight;
    }

    public void setKeyRight(int keyRight) {
        this.keyRight = keyRight;
    }

    public int getKeyShoot() {
        return keyShoot;
    }

    public void setKeyShoot(int keyShoot) {
        this.keyShoot = keyShoot;
    }

    public int getKeyBlock() {
        return keyBlock;
    }

    public void setKeyBlock(int keyBlock) {
        this.keyBlock = keyBlock;
    }
}
