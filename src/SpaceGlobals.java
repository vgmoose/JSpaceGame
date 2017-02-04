
public class SpaceGlobals {

	public SpaceGlobals() {
		for (int x=0; x<bullets.length; x++)
			bullets[x] = new Bullet();
		for (int x=0; x<enemies.length; x++)
			enemies[x] = new Enemy();
		for (int x=0; x<stars.length; x++)
			stars[x] = new Pixel();
				
	}
int restart;
	
	//Gameplay boundry
	int frame;
	
	long[] seed;
	
	short[][] rotated_ship = new short[36][36];
	short[][] orig_ship = new short[36][36];
	short[][] curPalette = new short[3][1];
	short[][] enemy = new short[23][23];
	short[][] title = new short[100][200];
	
	int[] passwordList = new int[100];
	int playerExplodeFrame;

	//Globals for player1 location and movement dx/dy
	float p1X;
	float p1Y;
	float angle;
	
	int touched;
	int touchX;
	int touchY;
	int titleScreenRefresh;

	//Game engine globals
	int button;
	boolean buttonUp = false;
	boolean buttonDown = false;
	boolean buttonLeft = false;
	boolean buttonRight = false;
	boolean buttonEnter = false;
	boolean buttonA = false;
	boolean buttonB = false;
	boolean buttonEscape = false;
	
	Vec2D lstick = new Vec2D();
	Vec2D rstick = new Vec2D();
	
	// only 20 bullets can be onscreen at a time
	Bullet[] bullets = new Bullet[20];
	
	// the locations of the 200 random stars
	Pixel[] stars = new Pixel[200];
	
	// the location of enemies
	Enemy[] enemies = new Enemy[100];

	boolean renderResetFlag;
	int invalid;
	int transIndex;
		
	// bonuses
	int playerChoice;
	int dontKeepTrackOfScore;
	int noEnemies;
	int enemiesSeekPlayer;
	
	int state; // 1 is title screen, 2 is gameplay, 3 is password, 4 is about
	int lives;
	int score;
	int level;
	
	int menuChoice;
	int passwordEntered;
	int allowInput;
	
	int displayHowToPlay;
	boolean firstShotFired;

}
