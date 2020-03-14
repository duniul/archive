package engine;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/*
 * Uses JavaFX, which requires Java SE 7, to play audio.
 * The MediaPlayer can handle a multitude of file formats by default, including .wav, .mp3 and .m4a.
 *
 * Uses 2 separate MediaPlayer instances for sound and music to be able to
 * for instance be able to implement location-based sound in future versions.
 *
 * CHANGES SINCE ASSIGNMENT 1:
 * - added ability to loop played music
 * - added queueMusic() & queueSound() methods
 */
public class AudioEngine {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 6835347574692801172L;
    static MediaPlayer soundPlayer;
    static MediaPlayer musicPlayer;
    static boolean soundIsPlaying;
    static boolean musicIsPlaying;

    private AudioEngine() {
    }

    // Plays a sound from a file.
    public static void playSound(Media soundFile) {
        soundPlayer = new MediaPlayer(soundFile);
        soundPlayer.play();
    }

    // Queues up a sound file to play after the current one has finished.
    public static void queueSound(Media musicFile, boolean shouldLoop) {
        if (soundIsPlaying) {
            soundPlayer.setOnEndOfMedia(new Runnable() {
                public void run() {
                    playMusic(musicFile, shouldLoop);
                }
            });
        }
    }

    // Plays a music file.
    public static void playMusic(Media musicFile, boolean shouldLoop) {
        musicPlayer = new MediaPlayer(musicFile);

        if (shouldLoop) {
            musicPlayer.setOnEndOfMedia(new Runnable() {
                public void run() {
                    musicPlayer.seek(Duration.ZERO);
                }
            });

        } else {
            musicPlayer.setOnEndOfMedia(new Runnable() {
                public void run() {
                    musicIsPlaying = false;
                }
            });
        }

        musicPlayer.play();
        musicIsPlaying = true;
    }

    // Queues up a music file to play after the current one has finished.
    public static void queueMusic(Media musicFile, boolean shouldLoop) {
        if (musicIsPlaying) {
            musicPlayer.setOnEndOfMedia(new Runnable() {
                public void run() {
                    playMusic(musicFile, shouldLoop);
                }
            });
        }
    }

    // Pauses the currently playing music track.
    public static void pauseMusic() {
        musicPlayer.pause();
    }

    // Resumes the paused music.
    public static void resumeMusic() {
        musicPlayer.play();
    }
}