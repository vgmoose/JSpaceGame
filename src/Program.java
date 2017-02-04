import java.awt.Color;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.JPanel;

public class Program {

	enum Button {
		VPAD_BUTTON_LEFT, VPAD_BUTTON_RIGHT, VPAD_BUTTON_UP, VPAD_BUTTON_DOWN
	};

	public static void main(String[] args) {
		Space frame = Space.getSpace();
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(854, 480));
		panel.setBackground(Color.BLACK);
		frame.add(panel);
		frame.pack();
		frame.show();

		/**************************** > Globals < ****************************/
		SpaceGlobals mySpaceGlobals = new SpaceGlobals();
		// Flag for restarting the entire game.
		mySpaceGlobals.restart = 1;

		// initial state is title screen
		mySpaceGlobals.state = 1;
		mySpaceGlobals.titleScreenRefresh = 1;

		// Flags for render states
		mySpaceGlobals.renderResetFlag = false;
		mySpaceGlobals.menuChoice = 0; // 0 is play, 1 is password

		// setup the password list
		int[] pwSeed = { 27 }; // int array of size 1 is acts as an int pointer
		int x;
		for (x = 0; x < 100; x++)
			mySpaceGlobals.passwordList[x] = (int) (TrigMath.prand(pwSeed) * 100000);

		// set the starting time
		int coreinit_handle;
		mySpaceGlobals.seed = new int[] { (int) (new Date()).getTime() };

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
			// VPADRead(0, &vpad_data, 1, &error);
			// TODO: get input

			// Get the status of the gamepad
			// mySpaceGlobals.button = vpad_data.btns_h;
			//
			// mySpaceGlobals.rstick = vpad_data.rstick;
			// mySpaceGlobals.lstick = vpad_data.lstick;
			//
			// mySpaceGlobals.touched = vpad_data.tpdata.touched;
			if (mySpaceGlobals.touched == 1) {
				// mySpaceGlobals.touchX = ((vpad_data.tpdata.x / 9) - 11);
				// mySpaceGlobals.touchY = ((3930 - vpad_data.tpdata.y) / 16);
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
			// //To exit the game
			// if (mySpaceGlobals.button&VPAD_BUTTON_HOME)
			// {
			// break;
			// }

		}

	}
}
