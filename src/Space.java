import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Date;

import javax.swing.JFrame;

public class Space extends JFrame
{
	private Space() 
	{ 
		draw = new Draw(this);
	}
	
	public Draw draw;
	
	static private Space space = new Space();
	
	static public Space getSpace() {
		return space;
	}

	/**
	This class is a bit of a mess, but it basically does "everything else" in the game.
	The most interesting function is rotating the bitmap (makeRotationMatrix).

	Other things it handles:
		- Joystick input (p1Move)
		- Bullet firing (p1Shoot)
		- Star drawing (renderStars)
		- Status bar drawing (renderTexts)
		- Decompressing sprites (decompress_bitmap)
		- Handling the menu at the title screen (doMenuAction)
		
	It relies heavily on a SpaceGlobals struct defined in space.h. This is a carry over from the libwiiu
	pong example, but also I believe necesary since global variables don't seem to be able to be set(?)
	**/

	int xMinBoundry = 0;
	int xMaxBoundry = 427;
	int yMinBoundry = 0;
	int yMaxBoundry = 240;

	void blackout()
	{
		draw.fillScreen(0,0,0,0);
	}

	void increaseScore(SpaceGlobals mySpaceGlobals, int inc)
	{
		// count the number of 5000s that fit into the score before adding
		int fiveThousandsBefore = mySpaceGlobals.score / 5000;
		
		// increase the score
		mySpaceGlobals.score += inc;
		
		// count them again
		int fiveThousandsAfter = mySpaceGlobals.score / 5000;
		
		// if it increased, levelup
		if (fiveThousandsAfter > fiveThousandsBefore)
		{
			mySpaceGlobals.level ++;
		}
	}

	void p1Shoot(SpaceGlobals mySpaceGlobals)
	{
		if (mySpaceGlobals.playerExplodeFrame > 1)
			return;
			
//		float sensitivity = 0.1f;
//					
//		float xdif = 0;
//		float ydif = 0;
		
		if (mySpaceGlobals.touched == 1)
//		if ((fabs(mySpaceGlobals.rstick.x) > sensitivity) || (fabs(mySpaceGlobals.rstick.y) > sensitivity))
		{
//			if (fabs(mySpaceGlobals.rstick.x) > sensitivity)
//				xdif = mySpaceGlobals.p1X - (mySpaceGlobals.p1X + (mySpaceGlobals.rstick.x * 18));
//			if (fabs(mySpaceGlobals.rstick.y) > sensitivity)
//				ydif = mySpaceGlobals.p1Y - (mySpaceGlobals.p1Y - (mySpaceGlobals.rstick.y * 18));
			float xdif = mySpaceGlobals.p1X - mySpaceGlobals.touchX + 18;
			float ydif = mySpaceGlobals.p1Y - mySpaceGlobals.touchY + 18;
			mySpaceGlobals.angle = TrigMath.my_atan2(xdif, ydif);
			
//			activateBullet(mySpaceGlobals, mySpaceGlobals.angle - 3.14159265, mySpaceGlobals.p1X, mySpaceGlobals.p1Y);
			// shoot a bullet
			// find an inactive bullet
			float theta = mySpaceGlobals.angle - 3.14159265f;
			int xx;
			for (xx=0; xx<20; xx++)
			{
				if (mySpaceGlobals.bullets[xx].active != 1)
				{
					mySpaceGlobals.bullets[xx].x = (int) (mySpaceGlobals.p1X + 18);
					mySpaceGlobals.bullets[xx].y = (int) (mySpaceGlobals.p1Y + 18);
					mySpaceGlobals.bullets[xx].m_x = 9*TrigMath.my_sin(theta); // 9 is the desired bullet speed 
					mySpaceGlobals.bullets[xx].m_y = 9*TrigMath.my_cos(theta); // we have to solve for the hypotenuese 
					mySpaceGlobals.bullets[xx].active = 1;
					mySpaceGlobals.firstShotFired = true;
					if (mySpaceGlobals.score >= 1000)
						mySpaceGlobals.displayHowToPlay = 0;
					break;
				}
			}
		}
		
		moveBullets(mySpaceGlobals);
	}

	//Updates player1 location
	void p1Move(SpaceGlobals mySpaceGlobals) {
		
		// can't move while exploding
		if (mySpaceGlobals.playerExplodeFrame > 1)
			return;
			
		// Handle analog stick movements
		Vec2D left = mySpaceGlobals.lstick;
		Vec2D right = mySpaceGlobals.rstick;

		// get the differences
		float xdif = left.x + right.x;
		float ydif = left.y + right.y;
		
		// Handle D-pad movements as well
		// max out speed at 1 or -1 in both directions
		xdif = (xdif >  1 || mySpaceGlobals.buttonRight)?  1 : xdif;
		xdif = (xdif < -1 || mySpaceGlobals.buttonLeft)? -1 : xdif;
		ydif = (ydif >  1 || mySpaceGlobals.buttonUp)?  1 : ydif;
		ydif = (ydif < -1 || mySpaceGlobals.buttonDown)? -1 : ydif;
		
		// don't update angle if both are within -.1 < x < .1
		// (this is an expenesive check... 128 bytes compared to just ==0)
		if (xdif < 0.1 && xdif > -0.1 && ydif < 0.1 && ydif > -0.1) return;
		
		// invalid view
		mySpaceGlobals.invalid = 1;
		
		// accept x and y movement from either stick
		mySpaceGlobals.p1X += xdif*5;
		mySpaceGlobals.p1Y -= ydif*5;
			
		// calculate angle to face
		mySpaceGlobals.angle = TrigMath.my_atan2(ydif, xdif) - 3.14159265f/2;
		
		// update score if on a frame divisible by 60 (gain 10 points every second)
		if (mySpaceGlobals.frame % 60 == 0)
		{
			increaseScore(mySpaceGlobals, 10);
			
			// if the score is at least 50 and a shot hasn't been fired yet, display a message about shooting
			if (mySpaceGlobals.score >= 50 && !mySpaceGlobals.firstShotFired)
				mySpaceGlobals.displayHowToPlay = 1;
		}

	};

	void checkPause(SpaceGlobals mySpaceGlobals)
	{
		if (mySpaceGlobals.buttonEnter)
		{
			// switch to the pause state and mark view as invalid
			mySpaceGlobals.state = 3;
			mySpaceGlobals.invalid = 1;
		}
	}

	void handleCollisions(SpaceGlobals mySpaceGlobals)
	{
		int playerLeft = (int) mySpaceGlobals.p1X;
		int playerRight = playerLeft + 36;
		int playerUp = (int) mySpaceGlobals.p1Y;
		int playerDown = playerUp + 36;
		
		// don't let the player go offscreen
		if (playerLeft < xMinBoundry)
			mySpaceGlobals.p1X = xMinBoundry;
		if (playerRight > xMaxBoundry)
			mySpaceGlobals.p1X = xMaxBoundry - 36;
		if (playerUp < yMinBoundry + 20)
			mySpaceGlobals.p1Y = yMinBoundry + 20;
		if (playerDown > yMaxBoundry)
			mySpaceGlobals.p1Y = yMaxBoundry - 36;
		
			// check enemies if they collide with the player or any of the 20 active bullets
			int x;
			for (x=0; x<100; x++)
			{
				if (mySpaceGlobals.enemies[x].position.active == 1)
				{
					// collision checkin from here: http://stackoverflow.com/a/1736741/1137828
					// check player
									
					int sqMe1 = (int) ((mySpaceGlobals.enemies[x].position.x+7)-(mySpaceGlobals.p1X+9));
					int sqMe2 = (int) ((mySpaceGlobals.enemies[x].position.y+7)-(mySpaceGlobals.p1Y+9));
					
					if (sqMe1*sqMe1 + sqMe2*sqMe2 <= (7+9)*(7+9))
					{
						if (mySpaceGlobals.playerExplodeFrame < 1)
						{
							// player was hit
							mySpaceGlobals.playerExplodeFrame = 2;
							initGameState(mySpaceGlobals);						
						}
					}
					
					int y;
					for (y=0; y<20; y++)
					{
						if (mySpaceGlobals.bullets[y].active == 1)
						{
							// check player's bullets
							sqMe1 = ((mySpaceGlobals.enemies[x].position.x+7)-(mySpaceGlobals.bullets[y].x+1));
							sqMe2 = ((mySpaceGlobals.enemies[x].position.y+7)-(mySpaceGlobals.bullets[y].y+1));
							
							if (sqMe1*sqMe1 + sqMe2*sqMe2 <= (7+1)*(7+1))
							{
								// enemy was hit, active = 2 is explode
								increaseScore(mySpaceGlobals, 100); // 100 points for killing enemy
								mySpaceGlobals.enemies[x].position.active = 2;
								
								// bullet is destroyed with enemy
								mySpaceGlobals.bullets[y].active = 0;
								
								break;
							}
						}
					}
				}
			}

	}

	void makeScaleMatrix(int frame, int width, short[][] orig, short[][] targ, int transIndex)
	{

		int x;
		for (x=0; x<width; x++)
		{
			int y;
			for (y=0; y<width; y++)
			{
				targ[x][y] = (short) transIndex;
			}
		}
		int woffset = width/2;
		
		for (x=0; x<width; x++)
		{
			int y=0;
			for (y=0; y<width; y++)
			{
				// rotate the pixel by the angle into a new spot in the rotation matrix
				int newx = (x-woffset)*frame + woffset;
				int newy = (y-woffset)*frame + woffset;
				
				if (orig[newx][newy] == transIndex) continue;
				
				if (newx < 0 || newx >= width) continue;
				if (newy < 0 || newy >= width) continue;
				
				targ[newx][newy] = orig[x][y];
			}
		}
		
	}

	void handleExplosions(SpaceGlobals mySpaceGlobals)
	{
		int x;
		for (x=0; x<100; x++)
		{
			if (mySpaceGlobals.enemies[x].position.active > 1)
			{
				makeScaleMatrix((int)(mySpaceGlobals.enemies[x].position.active/2.0), 23, mySpaceGlobals.enemy, mySpaceGlobals.enemies[x].rotated_sprite, 9);
				mySpaceGlobals.enemies[x].position.active ++;
				
				if (mySpaceGlobals.enemies[x].position.active > 20)
					mySpaceGlobals.enemies[x].position.active = 0;
			}
		}
			
		if (mySpaceGlobals.playerExplodeFrame > 1)
		{
			makeScaleMatrix(mySpaceGlobals.playerExplodeFrame, 36, mySpaceGlobals.orig_ship, mySpaceGlobals.rotated_ship, mySpaceGlobals.transIndex);
			mySpaceGlobals.playerExplodeFrame ++;
			mySpaceGlobals.invalid = 1;
			
			if (mySpaceGlobals.playerExplodeFrame > 20)
			{
				mySpaceGlobals.playerExplodeFrame = 0;
				mySpaceGlobals.lives --;
				if (mySpaceGlobals.lives <= 0)
				{
					// game over!
					mySpaceGlobals.state = 4;
					mySpaceGlobals.invalid = 1;
				}
				else
					mySpaceGlobals.renderResetFlag = true;
			}
		}
	}

	void makeRotationMatrix(float angle, int width, short[][] orig, short[][] targ, int transIndex)
	{
		
		int x;
		for (x=0; x<width; x++)
		{
			int y;
			for (y=0; y<width; y++)
			{
				targ[x][y] = (short) transIndex;
			}
		}
		
		float woffset = width/2.0f;
		
		int ix;
		// go though every pixel in the target bitmap
		for (ix=0; ix<width; ix++)
		{
			int iy;
			for (iy=0; iy<width; iy++)
			{
					
				// rotate the pixel by the angle into a new spot in the rotation matrix
				int oldx = (int)((ix-woffset)*TrigMath.my_cos(angle) + (iy-woffset)*TrigMath.my_sin(angle) + woffset);
				int oldy = (int)((ix-woffset)*TrigMath.my_sin(angle) - (iy-woffset)*TrigMath.my_cos(angle) + woffset);
				
//				if (oldx < 0) oldx += width;
//				if (oldy < 0) oldy += width;
				
				if (orig[oldx][oldy] == transIndex) continue;
				
				if (oldx < 0 || oldx >= width) continue;
				if (oldy < 0 || oldy >= width) continue;
				
				// TODO: crashes with this below line! When trying to assign to target, but only after doing the above math
				targ[ix][iy] = orig[oldx][oldy];
			}
		}
	}

	void renderEnemies(SpaceGlobals mySpaceGlobals)
	{
		// for all active bullets, advance them
		int x=0;
		for (x=0; x<20; x++)
		{
			if (mySpaceGlobals.bullets[x].active == 1)
			{
				
				int z, za;
				for (z=0; z<4; z++)
					for (za=0; za<2; za++)
						draw.drawPixel(mySpaceGlobals.bullets[x].x + z, mySpaceGlobals.bullets[x].y + za, 255, 0, 0);
			}
		}
		
		// for all active enemies, advance them
		for (x=0; x<100; x++) // up to 100 enemies at once
		{
			if (mySpaceGlobals.enemies[x].position.active >= 1)
			{
				draw.drawBitmap(mySpaceGlobals.enemies[x].position.x, mySpaceGlobals.enemies[x].position.y, 23, 23, mySpaceGlobals.enemies[x].rotated_sprite, Images.enemy_palette);
			}
		}
	}

	void render(SpaceGlobals mySpaceGlobals)
	{
		if (mySpaceGlobals.invalid == 1)
		{
			blackout();

			mySpaceGlobals.frame++;

			if (mySpaceGlobals.renderResetFlag)
			{
				renderReset(mySpaceGlobals);
			}

			renderStars(mySpaceGlobals);
			renderEnemies(mySpaceGlobals);
			renderShip(mySpaceGlobals);
			renderTexts(mySpaceGlobals);

			draw.flipBuffers();
			mySpaceGlobals.invalid = 0;
		}
	}

	// see the notes in images.c for more info on how this works
	void decompress_sprite(int arraysize, int width, int height, short[] input, short[][] targ, int i)
	{
		int cx = 0, cy = 0;
		int x;
		int posinrow = 0;
		// go through input array
		for (x=0; x<arraysize; x+=2)
		{
			int count = input[x];
			short value = (short) input[x+1];
			int z;
			
			if (count == -120) // full value rows of last index in palette
			{
				for (z=0; z<value; z++)
				{
					int za;
					for (za=0; za<width; za++)
					{
						targ[cy+z][cx+za] = (short)i;
					}
				}
				
				cy += value;
				continue;
			}
////			
			if (count <= 0) // if it's negative, -count is value, and value is meaningless and advance by one
			{
				value = (short) (-1*count);
				count = 1;
				x--; // subtract one, so next time it goes up by 2, putting us at x+1
			}

			for (z=0; z<count; z++)
			{
				targ[cy][cx] = value;
				cx++;
			}

			posinrow += z;
			if (posinrow >= width)
			{
				posinrow = 0;
				cx = 0;
				cy++;
			}
		}
	}

	void moveBullets(SpaceGlobals mySpaceGlobals)
	{
		// for all active bullets, advance them
		int x=0;
		for (x=0; x<20; x++)
		{
			if (mySpaceGlobals.bullets[x].active == 1)
			{
				mySpaceGlobals.bullets[x].x += mySpaceGlobals.bullets[x].m_x;
				mySpaceGlobals.bullets[x].y += mySpaceGlobals.bullets[x].m_y;
							
				if (mySpaceGlobals.bullets[x].x > xMaxBoundry ||
					mySpaceGlobals.bullets[x].x < xMinBoundry ||
					mySpaceGlobals.bullets[x].y > yMaxBoundry ||
					mySpaceGlobals.bullets[x].y < yMinBoundry + 20)
					mySpaceGlobals.bullets[x].active = 0;
				
				mySpaceGlobals.invalid = 1;
			}
			
		}
		
		for (x=0; x<100; x++)
		{
			if (mySpaceGlobals.enemies[x].position.active == 1)
			{
				mySpaceGlobals.enemies[x].position.x += mySpaceGlobals.enemies[x].position.m_x;
				mySpaceGlobals.enemies[x].position.y += mySpaceGlobals.enemies[x].position.m_y;
							
				if (mySpaceGlobals.enemies[x].position.x > xMaxBoundry ||
					mySpaceGlobals.enemies[x].position.x < xMinBoundry ||
					mySpaceGlobals.enemies[x].position.y > yMaxBoundry ||
					mySpaceGlobals.enemies[x].position.y < yMinBoundry + 20)
					mySpaceGlobals.enemies[x].position.active = 0;
				
				// rotate the enemy slowly
				mySpaceGlobals.enemies[x].angle += 0.02f;
				if (mySpaceGlobals.enemies[x].angle > 6.28318530f)
					mySpaceGlobals.enemies[x].angle = 0.0f;
				
//				int targetAngle = mySpaceGlobals.enemies[x].angle;
							
				// TODO: the below crashes... with angle instead of 0
				makeRotationMatrix(mySpaceGlobals.enemies[x].angle, 23, mySpaceGlobals.enemy, mySpaceGlobals.enemies[x].rotated_sprite, 9);
//				makeScaleMatrix(3, 23, mySpaceGlobals.enemy, mySpaceGlobals.enemies[x].rotated_sprite, 9);

				mySpaceGlobals.invalid = 1;
			}
		}
	}
						
	void renderTexts(SpaceGlobals mySpaceGlobals)
	{
		draw.fillRect(0, 0, xMaxBoundry, 20, 0, 0, 0);

		String score;
		if (mySpaceGlobals.dontKeepTrackOfScore == 1)
			score = "Score: N/A";
		else
			score = "Score: " + mySpaceGlobals.score;
		draw.drawString(0, -1, score);
		
		String level = "Lv " + mySpaceGlobals.level+1;
		draw.drawString(30, -1, level);

		String lives = "Lives: " + mySpaceGlobals.lives;
		draw.drawString(55, -1, lives);
		
		if (mySpaceGlobals.displayHowToPlay == 1)
		{
			String nag = "Touch and hold on the screen to rapid fire!";
			draw.drawString(20, 17, nag);
		}
				
	}

	void renderShip(SpaceGlobals mySpaceGlobals) 
	{
		int posx = (int)mySpaceGlobals.p1X;
		int posy = (int)mySpaceGlobals.p1Y;
		
		if (mySpaceGlobals.playerExplodeFrame < 2)
			makeRotationMatrix(mySpaceGlobals.angle, 36, mySpaceGlobals.orig_ship, mySpaceGlobals.rotated_ship, mySpaceGlobals.transIndex);

		draw.drawBitmap(posx, posy, 36, 36, mySpaceGlobals.rotated_ship, mySpaceGlobals.curPalette);

	}

	void renderStars(SpaceGlobals mySpaceGlobals)
	{
		// don't draw stars if the player is on their last life and died
		if (mySpaceGlobals.lives == 1 && mySpaceGlobals.playerExplodeFrame > 1)
			return;
		
		draw.drawPixels(mySpaceGlobals.stars);
	}

	//Reset the game
	void reset(SpaceGlobals mySpaceGlobals) {
		mySpaceGlobals.button = 0;

		//Set flag to render reset screen;
		mySpaceGlobals.renderResetFlag = true;

	};

	void initGameState(SpaceGlobals mySpaceGlobals)
	{
		// init bullets
		int x;
		for (x=0; x<20; x++)
		{
			mySpaceGlobals.bullets[x].active = 0;
		}
		
		// init x and y pos of player
//		mySpaceGlobals.p1X =  40;
//		mySpaceGlobals.p1Y = 150;
		
		// init enemies
		for (x=0; x<100; x++)
		{
			mySpaceGlobals.enemies[x].position.active = 0;
			mySpaceGlobals.enemies[x].angle = 3.14f;
			makeRotationMatrix(0, 23, mySpaceGlobals.enemy, mySpaceGlobals.enemies[x].rotated_sprite, 9);
		}

	}

	void initStars(SpaceGlobals mySpaceGlobals) 
	{
		// create the stars randomly
		int x;
		for (x=0; x<200; x++)
		{	
			mySpaceGlobals.stars[x].x = (int)(TrigMath.prand(mySpaceGlobals.seed)*xMaxBoundry);
			mySpaceGlobals.stars[x].y = (int)(TrigMath.prand(mySpaceGlobals.seed)*yMaxBoundry);
			int randomNum = (int)(TrigMath.prand(mySpaceGlobals.seed)*4);
			
			// half of the time make them white, 1/4 yellow, 1/4 blue
			mySpaceGlobals.stars[x].r = (randomNum <= 2)? 255 : 0;
			mySpaceGlobals.stars[x].g = (randomNum <= 2)? 255 : 0;
			mySpaceGlobals.stars[x].b = (randomNum != 2)? 255 : 0;
		}
	}

	void displayTitle(SpaceGlobals mySpaceGlobals)
	{
		if (mySpaceGlobals.invalid == 1)
		{
			blackout();
			
			// draw some stars
			renderStars(mySpaceGlobals);

			// display the bitmap in upper center screen
			draw.drawBitmap(107, 30, 200, 100, mySpaceGlobals.title, Images.title_palette);

			String credits = "by vgmoose";

			String musiccredits = "~*cruise*~ by (T-T)b";
			
			String license = "MIT License";
			
			String play = "Start Game";
			String password = "Password";

			//display the menu under it
			draw.drawString(37, 9, credits);
			draw.drawString(25, 12, play);
			draw.drawString(26, 13, password);
			
			draw.drawString(45, 17, musiccredits);
			draw.drawString(-2, 17, license);
			
			drawMenuCursor(mySpaceGlobals);
			
			draw.flipBuffers();
			mySpaceGlobals.invalid = 0;
		}

	}

	void drawMenuCursor(SpaceGlobals mySpaceGlobals)
	{
		// cover up any old cursors (used to be needed before changing to draw everything mode)
//		fillRect(155, 155, 16, 30, 0, 0, 0);
//		fillRect(240, 155, 16, 30, 0, 0, 0);

		// display the cursor on the correct item
		String cursor = ">>            <<";
		draw.drawString(22, 12 + mySpaceGlobals.menuChoice, cursor);
	}

	void doMenuAction(SpaceGlobals mySpaceGlobals)
	{
		// if we've seen the A button not being pressed
		if (!(mySpaceGlobals.buttonA))
		{
			mySpaceGlobals.allowInput = 1;
		}
		
		if (mySpaceGlobals.buttonA && mySpaceGlobals.allowInput == 1)
		{
			// if we're on the title menu
			if (mySpaceGlobals.state == 1)
			{
				if (mySpaceGlobals.menuChoice == 0)
				{
					totallyRefreshState(mySpaceGlobals);
					
					// start game chosen
					mySpaceGlobals.state = 7; // switch to game state
					mySpaceGlobals.renderResetFlag = true; // redraw screen
				}
				else if (mySpaceGlobals.menuChoice == 1)
				{
					// password screen chosen
					mySpaceGlobals.state = 2;
				}
			}
			// password screen
//			else if (mySpaceGlobals.state == 2)
//			{
//				// this is handled by the password menu action function
//			}
			// pause screen 
			else if (mySpaceGlobals.state == 3)
			{
				if (mySpaceGlobals.menuChoice == 0)
				{
					// resume chosen
					mySpaceGlobals.state = 7; // switch to game state
					
				}
				else if (mySpaceGlobals.menuChoice == 1)
				{
					// quit chosen
					totallyRefreshState(mySpaceGlobals);
					mySpaceGlobals.state = 1;
				}
			}
			// game over screen 
			else if (mySpaceGlobals.state == 4)
			{
				totallyRefreshState(mySpaceGlobals);
				
				if (mySpaceGlobals.menuChoice == 0)
				{
					// try again chosen
					
					//player stays on the same level 
					mySpaceGlobals.state = 7; // switch to game state
					
				}
				else if (mySpaceGlobals.menuChoice == 1)
				{
					// quit chosen
					mySpaceGlobals.state = 1;
				}
			}
			
			// reset the choice
			mySpaceGlobals.menuChoice = 0;
			
			// disable menu input after selecting to prevent double selects
			mySpaceGlobals.allowInput = 0;

			// mark view invalid to redraw
			mySpaceGlobals.invalid = 1;
		}
		
		float stickY = mySpaceGlobals.lstick.y + mySpaceGlobals.rstick.y;
		
		if (mySpaceGlobals.buttonDown || stickY < -0.3)
		{
			mySpaceGlobals.menuChoice = 1;
			mySpaceGlobals.invalid = 1;
		}
		
		if (mySpaceGlobals.buttonUp || stickY > 0.3)
		{
			mySpaceGlobals.menuChoice = 0;
			mySpaceGlobals.invalid = 1;
		}
	}

	void displayPause(SpaceGlobals mySpaceGlobals)
	{
		if (mySpaceGlobals.invalid == 1)
		{
			blackout();

			// display the password here
			String resume = "Resume";
			String quit = "Quit";
			
			draw.drawString(27, 12, resume);
			draw.drawString(28, 13, quit);
			
			drawMenuCursor(mySpaceGlobals);
			
			draw.flipBuffers();
			mySpaceGlobals.invalid = 0;
		}
	}

	void doPasswordMenuAction(SpaceGlobals mySpaceGlobals)
	{
		// if we've seen up, down, left, right, and a buttons not being pressed
		if (!(mySpaceGlobals.buttonA	 || 
			  mySpaceGlobals.buttonUp	||
			  mySpaceGlobals.buttonDown  ||
			  mySpaceGlobals.buttonLeft  ||
			  mySpaceGlobals.buttonRight   ))
		{
			mySpaceGlobals.allowInput = 1;
		}
		
		if (mySpaceGlobals.allowInput == 1)
		{
			if (mySpaceGlobals.buttonB)
			{
				// go back to title screen
				mySpaceGlobals.state = 1;
				
				// update the menu choice
				mySpaceGlobals.menuChoice = 0;
				
				// disable menu input after selecting to prevent double selects
				mySpaceGlobals.allowInput = 0;

				// mark view invalid to redraw
				mySpaceGlobals.invalid = 1;
			}
			if (mySpaceGlobals.buttonA)
			{
				// try the password
				tryPassword(mySpaceGlobals);

				// disable menu input after selecting to prevent double selects
				mySpaceGlobals.allowInput = 0;
				
				// update the menu choice
				mySpaceGlobals.menuChoice = 0;

				// mark view invalid to redraw
				mySpaceGlobals.invalid = 1;
			}
			
			float stickY = mySpaceGlobals.lstick.y + mySpaceGlobals.rstick.y;
			float stickX = mySpaceGlobals.lstick.x + mySpaceGlobals.rstick.x;
			boolean down  = (mySpaceGlobals.buttonDown  || stickY < -0.3);
			boolean up	= (mySpaceGlobals.buttonUp	|| stickY >  0.3);
			boolean left  = (mySpaceGlobals.buttonLeft  || stickX < -0.3);
			boolean right = (mySpaceGlobals.buttonRight || stickX >  0.3);
			
			if (up || down)
			{
				int offset = 1, x;
				// keep going up in the 10s place to match current choice
				for (x=0; x<(4 - mySpaceGlobals.menuChoice); x++)
					offset *= 10;
					
				if (up)
					mySpaceGlobals.passwordEntered += offset;
				if (down)
					mySpaceGlobals.passwordEntered -= offset;
					
				mySpaceGlobals.invalid = 1;
				mySpaceGlobals.allowInput = 0;
			}

			if (left || right)
			{
				if (right)
					mySpaceGlobals.menuChoice ++;
				if (left)
					mySpaceGlobals.menuChoice --;;
				
				// bound the menu choices
				if (mySpaceGlobals.menuChoice < 0)
					mySpaceGlobals.menuChoice = 0;
				if (mySpaceGlobals.menuChoice > 4)
					mySpaceGlobals.menuChoice = 4;
				
				mySpaceGlobals.invalid = 1;
				mySpaceGlobals.allowInput = 0;
			}
			
			// bound the password
			if (mySpaceGlobals.passwordEntered < 0)
				mySpaceGlobals.passwordEntered = 0;
			if (mySpaceGlobals.passwordEntered > 99999)
				mySpaceGlobals.passwordEntered = 99999;
		}
	}

	void displayPasswordScreen(SpaceGlobals mySpaceGlobals)
	{
		if (mySpaceGlobals.invalid == 1)
		{
			blackout();
			
//			drawPasswordMenuCursor(mySpaceGlobals);
			String password = "Password:";
			String up_cur = "v";
			int cur_pw = mySpaceGlobals.passwordEntered;
			String down_cur = "^";
			
			draw.drawString(22, 8, password);
			
			draw.drawString(32 + mySpaceGlobals.menuChoice, 7, up_cur);
			draw.drawString(32, 8, ""+cur_pw);
			draw.drawString(32 + mySpaceGlobals.menuChoice, 9, down_cur);
			
			draw.flipBuffers();
			mySpaceGlobals.invalid = 0;
		}
	}

	void addNewEnemies(SpaceGlobals mySpaceGlobals)
	{
		if (mySpaceGlobals.noEnemies == 1 || mySpaceGlobals.playerExplodeFrame > 1)
			return;
		
		// here we make a new enemy with a certain speed based on the level
			
		// get a random position from one of the sides with a random int 0-3
		int side = (int)(TrigMath.prand(mySpaceGlobals.seed)*4);
		
//		// randomly decide to set starting angle right for the player
//		float seekPlayer = prand(&mySpaceGlobals.seed)*2;
		
		float difficulty = mySpaceGlobals.level/100.0f;
		
		float randVal = TrigMath.prand(mySpaceGlobals.seed);
		
		// set the enemy count (max enemies on screen at once) based on level
		int enemyCount = (int) (10 + difficulty*90*randVal);
		
		if (enemyCount > 100) enemyCount = 100;
		
		// set speed randomly within difficulty range
		int speed = (int) (3 + (difficulty)*12*randVal);
		
		int startx, starty;
		
		float theta = TrigMath.prand(mySpaceGlobals.seed)*3.14159265f;
		randVal = TrigMath.prand(mySpaceGlobals.seed);
		
		// horiz size
		if (side < 2)
		{
			startx = (side == 0)? 0 : xMaxBoundry;
			starty = (int) (randVal*yMaxBoundry);
			
			if (startx != 0)
				theta -= 3.14159265;
		}
		else
		{
			starty = (side == 2)? 20 : yMaxBoundry;
			startx = (int) (randVal*xMaxBoundry);
			
			if (starty == 20)
				theta -= 3.14159265/2;
			else
				theta += 3.14159265/2;
		}
		
		// seek directly to the player
		if (mySpaceGlobals.enemiesSeekPlayer == 1)
		{
			float xdif = startx + 11 - (mySpaceGlobals.p1X + 18);
			float ydif = starty + 11 - (mySpaceGlobals.p1Y + 18);
			
			theta = TrigMath.my_atan2(xdif, ydif) - 3.14159265f;
		}
			
		int xx;
		for (xx=0; xx<enemyCount; xx++)
		{
			if (mySpaceGlobals.enemies[xx].position.active == 0)
			{
				mySpaceGlobals.enemies[xx].position.x = startx;
				mySpaceGlobals.enemies[xx].position.y = starty;
				mySpaceGlobals.enemies[xx].position.m_x = speed*TrigMath.my_sin(theta); // speed is the desired enemy speed 
				mySpaceGlobals.enemies[xx].position.m_y = speed*TrigMath.my_cos(theta); // we have to solve for the hypotenuese 
				mySpaceGlobals.enemies[xx].position.active = 1;
				break;
			}
		}
	}

	void totallyRefreshState(SpaceGlobals mySpaceGlobals)
	{
		initGameState(mySpaceGlobals);
		mySpaceGlobals.displayHowToPlay = 0;
		mySpaceGlobals.firstShotFired = false;
		mySpaceGlobals.lives = 3;
		mySpaceGlobals.playerExplodeFrame = 0;
		mySpaceGlobals.score = 0;
		mySpaceGlobals.level = 0;
		mySpaceGlobals.dontKeepTrackOfScore =  0;
		mySpaceGlobals.noEnemies = 0;
		mySpaceGlobals.enemiesSeekPlayer = 0;
	}

	void displayGameOver(SpaceGlobals mySpaceGlobals)
	{
		if (mySpaceGlobals.invalid == 1)
		{
			blackout();
					
			String gameover = "Game Over!";
			draw.drawString(25, 5, gameover);

			// only display score + pw if the player didn't use cheats
			if (mySpaceGlobals.dontKeepTrackOfScore != 1)
			{
				String finalscore = "Score: " +  mySpaceGlobals.score;
				String pass = "Lv " + mySpaceGlobals.level+1 + "Password: "  + mySpaceGlobals.passwordList[mySpaceGlobals.level];

				draw.drawString(23, 7, finalscore);
				draw.drawString(21, 8, pass);
			}
		
			
			String resume = "Try  Again";
			String quit = "Quit";
			
			draw.drawString(25, 12, resume);
			draw.drawString(28, 13, quit);
			
			drawMenuCursor(mySpaceGlobals);
			
			draw.flipBuffers();
			mySpaceGlobals.invalid = 0;
		}
		blackout();
		
		
		
	}

	void tryPassword(SpaceGlobals mySpaceGlobals)
	{
		// Dear Github Viewer, 
		//
		// 		Well, here's where you see the passwords I guess!
		//		With the exception of a few hardcoded ones, the
		//		level passwords are generated and checked against
		//		a seeded random list from program.c
		//	
		// Enjoy!
		
		// Invincibility
		if (mySpaceGlobals.passwordEntered == 55225)
		{
			mySpaceGlobals.playerExplodeFrame = 1;
			mySpaceGlobals.dontKeepTrackOfScore = 1;
			mySpaceGlobals.state = 7;
		}
		
		// 99 Lives
		if (mySpaceGlobals.passwordEntered == 99499)
		{
			mySpaceGlobals.lives = 99;
			mySpaceGlobals.dontKeepTrackOfScore = 1;
			mySpaceGlobals.state = 7;
		}
		
		// No Enemies (loner mode)
		if (mySpaceGlobals.passwordEntered == 82571)
		{
			mySpaceGlobals.noEnemies = 1;
			mySpaceGlobals.dontKeepTrackOfScore = 1;
			mySpaceGlobals.state = 7;
		}
		
		// Play as original spaceship (only if changed)
		if (mySpaceGlobals.passwordEntered == 00000 && mySpaceGlobals.playerChoice != 0)
		{
			mySpaceGlobals.playerChoice = 0;
			decompress_sprite(511, 36, 36, Images.compressed_ship, mySpaceGlobals.orig_ship, 14);
			mySpaceGlobals.curPalette = Images.ship_palette;
			mySpaceGlobals.transIndex = 14;
			mySpaceGlobals.state = 7;
		}
		
		// Play as galaga ship
		if (mySpaceGlobals.passwordEntered == 12345)
		{
			mySpaceGlobals.playerChoice = 3;
			decompress_sprite(452, 36, 36, Images.compressed_ship2, mySpaceGlobals.orig_ship, 5);
			mySpaceGlobals.curPalette = Images.ship2_palette;
			mySpaceGlobals.transIndex = 5;
			mySpaceGlobals.state = 7;
		}
		
		// Play as JWittz
		if (mySpaceGlobals.passwordEntered == 24177)
		{
			mySpaceGlobals.playerChoice = 1;
			decompress_sprite(662, 36, 36, Images.compressed_boss2, mySpaceGlobals.orig_ship, 39);
			mySpaceGlobals.curPalette = Images.boss2_palette;
			mySpaceGlobals.transIndex = 39;
			mySpaceGlobals.state = 7;
		}

		// Play as Etika
		if (mySpaceGlobals.passwordEntered == 37124)
		{
			mySpaceGlobals.playerChoice = 2;
			decompress_sprite(740, 36, 36, Images.compressed_boss, mySpaceGlobals.orig_ship, 39);
			mySpaceGlobals.curPalette = Images.boss_palette;
			mySpaceGlobals.transIndex = 39;
			mySpaceGlobals.state = 7;
		}
		
		// Enemies come right for you (kamikaze mode)
		if (mySpaceGlobals.passwordEntered == 30236)
		{
			mySpaceGlobals.enemiesSeekPlayer = 1;
			mySpaceGlobals.dontKeepTrackOfScore = 1;
			mySpaceGlobals.state = 7;
		}
		
		// start installer for Hykem's IOSU Exploit
		if (mySpaceGlobals.passwordEntered == 41666)
		{
			blackout();
			while(true) { if (1==2) break; };
//			OSFatal("Installing IOSU Exploit... This may take a while.");
		}
		
		// 100 passwords, one for each level
		int x;
		for (x=0; x<100; x++)
		{
			if (mySpaceGlobals.passwordEntered == mySpaceGlobals.passwordList[x])
			{
				mySpaceGlobals.level = x;
				break;
			}
			
			if (x==99) // no password was right
				return;
		}

		// switch to the game state
		mySpaceGlobals.state = 7;
		
		// They are generated 
	}

	void renderReset(SpaceGlobals mySpaceGlobals)
	{
		initGameState(mySpaceGlobals);
		mySpaceGlobals.p1X = 200;
		mySpaceGlobals.p1Y = 100;
		mySpaceGlobals.renderResetFlag = false;
		mySpaceGlobals.invalid = 1;
	}
	
	@Override
	public void paint(Graphics z)
	{
		super.paintComponents(z);
		Graphics2D g = (Graphics2D)z;
		
		g.setPaint(Color.BLACK);
		g.drawLine(0, 0, 200, 200);
		
		long time = System.currentTimeMillis();
				
		for (int x=0; x<draw.screen.length; x++)
			for (int y=0; y<draw.screen[0].length; y++)
			{
				if (draw.screen[x][y] == null) continue;
				
				// i guess there's no draw point so here's drawing a 1x1 line
				g.setPaint(draw.screen[x][y]);
				g.drawLine(x, y, x, y);
			}
		
		System.out.println("Took " + (System.currentTimeMillis()-time) + "ms");
	}
}
