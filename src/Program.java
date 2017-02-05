import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

public class Program {

    static int WIDTH = 427;
    static int HEIGHT = 240;

    enum Button {
        VPAD_BUTTON_LEFT, VPAD_BUTTON_RIGHT, VPAD_BUTTON_UP, VPAD_BUTTON_DOWN
    }

    ;

    public static void main(String[] args) {
        Space frame = Space.getSpace();
        JFrame panel = new JFrame();
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.setBackground(Color.BLACK);
        panel.add(frame);
        panel.pack();
        panel.addKeyListener(frame);
        panel.addMouseListener(frame);
        panel.show();

        /**************************** > Globals < ****************************/
        SpaceGlobals mySpaceGlobals = new SpaceGlobals();
        frame.mySpaceGlobals = mySpaceGlobals;

        // Flag for restarting the entire game.
        mySpaceGlobals.restart = 1;

        // initial state is title screen
        mySpaceGlobals.state = 1;
        mySpaceGlobals.titleScreenRefresh = 1;

        // Flags for render states
        mySpaceGlobals.renderResetFlag = false;
        mySpaceGlobals.menuChoice = 0; // 0 is play, 1 is password

        // setup the password list
        long[] pwSeed = {27}; // int array of size 1 is acts as an int pointer
        int x;
        for (x = 0; x < 100; x++)
            mySpaceGlobals.passwordList[x] = (int) (TrigMath.prand(pwSeed) * 100000);

        // set the starting time
        int coreinit_handle;
        mySpaceGlobals.seed = new long[]{(long) (new Date()).getTime()};

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    // Open an audio input stream.
                    URL url = Program.class.getClassLoader().getResource("spacegame.wav");
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                    // Get a sound clip resource.
                    Clip clip = AudioSystem.getClip();
                    // Open audio clip and load samples from the audio input stream.
                    clip.open(audioIn);
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                    clip.start();
                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });
        t.start();


        /****************************
         * > VPAD Loop <
         ****************************/
        int error;
        // VPADData vpad_data;

        // decompress compressed things into their arrays, final argument is the
        // transparent color in their palette
        frame.decompress_sprite(3061, 200, 100, Images.compressed_title, mySpaceGlobals.title, 39);
        frame.decompress_sprite(511, 36, 36, Images.compressed_ship, mySpaceGlobals.orig_ship, 14);
        frame.decompress_sprite(206, 23, 23, Images.compressed_enemy, mySpaceGlobals.enemy, 9);

        // setup palette and transparent index
        mySpaceGlobals.curPalette = Images.ship_palette;
        mySpaceGlobals.transIndex = 14;

        mySpaceGlobals.passwordEntered = 0;

        // initialize starfield for this game
        frame.initStars(mySpaceGlobals);

        mySpaceGlobals.invalid = 1;

        boolean exitApplication = false;

        while (!exitApplication) {

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (mySpaceGlobals.restart == 1) {
                frame.reset(mySpaceGlobals);
                mySpaceGlobals.restart = 0;
            }

            if (mySpaceGlobals.state == 1) // title screen
            {
                frame.displayTitle(mySpaceGlobals);
                frame.doMenuAction(mySpaceGlobals);
            } else if (mySpaceGlobals.state == 2) // password screen
            {
                frame.displayPasswordScreen(mySpaceGlobals);
                frame.doPasswordMenuAction(mySpaceGlobals);
            } else if (mySpaceGlobals.state == 3) // pause screen
            {
                frame.displayPause(mySpaceGlobals);
                frame.doMenuAction(mySpaceGlobals);
            } else if (mySpaceGlobals.state == 4) // game over screen
            {
                frame.displayGameOver(mySpaceGlobals);
                frame.doMenuAction(mySpaceGlobals);
            } else // game play
            {
                // Update location of player1 and 2 paddles
                frame.p1Move(mySpaceGlobals);

                // perform any shooting
                frame.p1Shoot(mySpaceGlobals);

                // handle any collisions
                frame.handleCollisions(mySpaceGlobals);

                // do explosions
                frame.handleExplosions(mySpaceGlobals);

                // if we're out of lives, break
                if (mySpaceGlobals.lives <= 0 && mySpaceGlobals.state == 4)
                    continue;

                // add any new enemies
                frame.addNewEnemies(mySpaceGlobals);

                // Render the scene
                frame.render(mySpaceGlobals);

                // check for pausing
                frame.checkPause(mySpaceGlobals);
            }
//			// //To exit the game
//			 if (mySpaceGlobals.buttonEscape)
//			 {
//				 break;
//			 }

//				System.out.println(mySpaceGlobals.buttonDown);


        }

    }
}
