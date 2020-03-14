package engine;

import java.awt.image.BufferedImage;

/*
 * This is a static class that can be help when working with sprites and spritesheets.
 *
 * ADDED AFTER ASSIGNMENT 1
 */
public class SpriteTool {

    // Loads sprites from a spritesheet, trims them and then saves them.
    public static BufferedImage[] getSpritesFromSpritesheet(BufferedImage spritesheet, int spriteWidth,
                    int spriteHeight, int rows, int columns, int amountOfSprites) {
        BufferedImage[] sprites = new BufferedImage[amountOfSprites];

        // Divides the spreadsheet into checkered subsections, each containing one sprite.
        // Loops through the sections and saves them as a separate sprite.
        int spritesDone = 0;
        spriteLoop: for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                sprites[(i * columns) + j] = spritesheet.getSubimage(j * spriteWidth, i * spriteHeight, spriteWidth,
                                spriteHeight);
                spritesDone++;
                if (spritesDone == amountOfSprites) {
                    break spriteLoop;
                }
            }
        }

        return sprites;
    }

    /*
     * Loops through the pixels of a spritses until it finds the outermost colored pixel for each
     * side. Uses the left X value and top Y value to find the top left corner of the sprite
     * rectangle, and the right X value and bottom Y value to find the bottom right corner.
     * Everything outside of the rectangle is removed.
     *
     * Based on the solution found here:
     * http://stackoverflow.com/questions/3224561/crop-image-to-smallest-size-by-removing-
     * transparent-pixels-in-java/23999123#23999123
     *
     * It checks transparency using BufferedImage's getRGB instead of the solution in the link,
     * even though it's less efficient, since I couldn't get that solution to work.
     */
    public static BufferedImage trimSprite(BufferedImage sprite) {
        int[][] spritePixels = new int[sprite.getHeight()][sprite.getWidth()];
        int x0 = 0;
        int y0 = 0;
        int x1 = 0;
        int y1 = 0;

        // Saves all pixels ARGB values.
        for (int row = 0; row < sprite.getHeight(); row++) {
            for (int col = 0; col < sprite.getWidth(); col++) {
                spritePixels[row][col] = sprite.getRGB(row, col);
            }
        }

        // Left loop
        leftLoop: for (int row = 0; row < sprite.getWidth(); row++) {
            for (int column = 0; column < sprite.getHeight(); column++) {
                if (((spritePixels[row][column] & 0xff000000) >> 24) != 0x00) {
                    x0 = row;
                    break leftLoop;
                }
            }
        }

        // Top loop
        topLoop: for (int column = 0; column < sprite.getHeight(); column++) {
            for (int row = 0; row < sprite.getWidth(); row++) {
                if (((spritePixels[row][column] & 0xff000000) >> 24) != 0x00) {
                    y0 = column;
                    break topLoop;
                }
            }
        }

        // Right loop
        rightLoop: for (int row = sprite.getWidth() - 1; row >= 0; row--) {
            for (int column = 0; column < sprite.getHeight(); column++) {
                if (((spritePixels[row][column] & 0xff000000) >> 24) != 0x00) {
                    x1 = row + 1;
                    break rightLoop;
                }
            }
        }

        // Bottom loop
        bottomLoop: for (int column = sprite.getHeight() - 1; column >= 0; column--) {
            for (int row = 0; row < sprite.getWidth(); row++) {
                if (((spritePixels[row][column] & 0xff000000) >> 24) != 0x00) {
                    y1 = column + 1;
                    break bottomLoop;
                }
            }
        }

        return sprite.getSubimage(x0, y0, x1 - x0, y1 - y0);
    }

    // Performs the trimSprites() operation on an array of sprites.
    public static BufferedImage[] trimSprites(BufferedImage[] sprites) {

        for (int i = 0; i < sprites.length; i++) {
            sprites[i] = trimSprite(sprites[i]);
        }

        return sprites;
    }

}
