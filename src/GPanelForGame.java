import javax.swing.*;
import BreezySwing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

import javax.swing.Timer;

/**
 * This panel draws our avoid-obstacles game.
 * Implements ActionListened for using Timer. Timer is used to repaint the panel to simulate the moving ground.
 * Extends GBPanel from the BreezySwing framework.
 * 
 * @author Vitaly Ford
 *
 */
public class GPanelForGame extends GBPanel implements ActionListener {
	
	// Add a label for scoring
	private JLabel scoreLabel = addLabel("Score: 0", 1,1,1,1);
	
	private java.util.List<Shape> shapes;
	private boolean start, done, mousePressed, cheatMode, moveHeroLeft, moveHeroRight;
    private int obstacles[];      // saving all obstacles including the ground and the real obstacles as rectangles
    private Random rand;          // just a randomizer for obstacles
    private int obstaclesPassed;  // keeping track of which obstacle we should start with from the obstacles[] array
    private int n;                // total number of obstacles at this level
    private int heroWidth;        // width of the hero
    private int heroHeight;       // height of the hero
    private int flyAt;            // position of the hero when the hero is flying up (the user holds the mouse)
    private int heroXLoc;         // position of the hero on X axis
    private int moveX;            // defines how much per key-press the hero can move left/right
    
    private Timer timer;          // this timer is used to repaint the panel so that it will look like the ground is moving 
	
	public void start() {
		// Create new obstacles only if the user passed the current set of obstacles
		if (obstaclesPassed >= n)
			createNewObstacles();
		
		obstaclesPassed = 0;
		flyAt           = 0;
		heroXLoc        = 0;
		moveX           = 20;
		mousePressed    = false;
		start           = true;
		done            = false;
		
		shapes = new java.util.ArrayList<Shape>(); // saving all shapes that we need to draw
		
		scoreLabel.setText("Score: 0");
    	
		// Set the timer for repainting the game in ms, it defines the speed of the game in this case
    	timer = new Timer(50, this); 
    	timer.start();
    	
    	// Set focus on the panel -> the panel will be forced to be focused on
    	// Focusing on the panel will allow for reading keyboard presses
    	this.setFocusable(true);
    	this.requestFocusInWindow();
	}

    public GPanelForGame(Color color) {
    	// Initializing all variables in this constructor
        setBackground(color);
        start = mousePressed = cheatMode = false;
        done  = true;
        obstaclesPassed = 0;
        
        heroWidth  = 32;
        heroHeight = 64;
    }
    
    private void createNewObstacles() {
    	rand = new Random();
    	n = 3000; // number of obstacles including the ground and real ones
    	obstacles = new int[n];
    	
    	// If switchTopBottom is true, then freeSpace distance must be even, otherwise odd
    	// It will allow for alternating the obstacles at the top and bottom
    	boolean switchTopBottom = true;
    	
    	// Fill up the obstacles array with random obstacles
    	for (int i = 20; i < n;) { // starting with 20 because the first 20 spots should not have any obstacles
    		int obstacle = rand.nextInt(100) + 50; // the height of the obstacle
    		int freeSpace = rand.nextInt(25);      // the distance between real obstacles
    		if (switchTopBottom && freeSpace % 2 != 0)
    			freeSpace++; // make the freeSpace distance even
    		else if (!switchTopBottom && freeSpace % 2 == 0)
    			freeSpace++; // make the freeSpace distance odd
    		
    		obstacles[i] = obstacle;
    		for (int j = i + 1; j < freeSpace + i + 1 && j < n; j++) {
    			obstacles[j] = 0; // fill up free space with zeros
    			
    			// Break for an even distance; it creates a little bit more complicated levels, IMHO
    			if (switchTopBottom) break; 
    		}
    		// Set the current index in the array to the number, passed the latest obstacle
    		i = (freeSpace + i + 1 < n) ? (freeSpace + i + 1) : n; 
    		
    		switchTopBottom = !switchTopBottom; // alternates obstacles from top, bottom, top, and so on
    	}
    }
    
    // This method is called when the timer is activated
    public void actionPerformed(ActionEvent ev) {
    	// Repaint the whole panel when the timer is kicked and the user pressed Start button
    	if (ev.getSource() == timer && start) {
    		repaint();
    		
    		// Update the score
            scoreLabel.setText("Score: " + Integer.toString(obstaclesPassed * 20));
    	}
    	
    	// If the user won or lost, then we gotta show that as well
        if (obstaclesPassed >= n && done)
        	scoreLabel.setText(scoreLabel.getText() + " You won, tap yourself on the shoulder!");
        else if (obstaclesPassed > 0 && done)
        	scoreLabel.setText(scoreLabel.getText() + " You lost ¯\\_(ツ)_/¯");
        
        // If the user won or lost, we stop the timer
        if (done) {
        	obstaclesPassed = 0;
        	timer.stop();
        }
    }

    public void paintComponent (Graphics g) {
    	if (!start) return; // if we did not start yet, don't even try to draw
    	
        // Redraw the whole app, it will clear up all the previous drawings
        super.paintComponent(g);
        
    	int obstacleNum = 60; // number of obstacles on the screen at the same time
    	shapes.clear();       // clear the shapes
    	int w             = this.getWidth(); // width of the this panel
    	int obstacleInitX = 0;
    	int obstacleInitY = this.getHeight() - 50;
    	int obstacleWidth = w / obstacleNum;
    	
    	// Add ground and obstacles to the shapes
    	for (int i = obstaclesPassed; i < obstaclesPassed + obstacleNum && i < n; i++) {
    		// add ground
    		shapes.add(new Rectangle2D.Double(obstacleInitX, obstacleInitY, obstacleWidth, 10)); // x, y, width, height
    		
    		// add obstacle
    		if (obstacles[i] != 0 && i % 2 == 0) { // put to the bottom the even-indexed obstacle
    			shapes.add(new Rectangle2D.Double(obstacleInitX, obstacleInitY - obstacles[i], obstacleWidth, obstacles[i]));
    		}
    		else if (obstacles[i] != 0) {          // put to the top
    			shapes.add(new Rectangle2D.Double(obstacleInitX, 0, obstacleWidth, obstacles[i]));
    		}
    		
    		obstacleInitX += obstacleWidth;
    	}
    	obstaclesPassed++;
    	// If we passed all obstacles, the end of the level!
    	if (obstaclesPassed >= n) {
    		// you won!
    		start = false;
    		done = true;
    	}
        
    	// Set color before drawing all obstacles
        g.setColor(Color.blue);
        // Add all shapes (obstacles) to the panel on the screen
        for (Shape s : shapes) {
        	((Graphics2D)g).fill(s);
        }
        
        // Draw the hero and check for collision with the obstacles
        processHero(g, obstacleInitY, heroWidth, heroHeight, shapes);
    }
    
    private void processHero(Graphics g, int obstacleInitY, int heroWidth, int heroHeight, java.util.List<Shape> shapes) {
    	int flyAtDelta = 25;   // how fast you want the hero to fly up while the mouse is pressed
    	g.setColor(Color.RED); // set the color for the hero
    	// Draw the hero as an ellipse, remember to keep track of flyAt
    	Ellipse2D hero;
    	if (mousePressed) // for fun: it changes the form of the hero from vertical to horizontal when the mouse is pressed
    		hero = new Ellipse2D.Double(heroXLoc, obstacleInitY - heroHeight - flyAt, heroHeight, heroWidth);
    	else
    		hero = new Ellipse2D.Double(heroXLoc, obstacleInitY - heroHeight - flyAt, heroWidth, heroHeight);
        ((Graphics2D)g).fill(hero); // x, y, width, height
        
        // Update how high the hero will fly up or down while the user presses the mouse
        if (mousePressed && obstacleInitY - heroHeight - flyAt - flyAtDelta > 0) { // can't go higher than the ceiling of this game which is 10 px
        	flyAt += flyAtDelta;
        }
        else if (mousePressed) { // in this case, the hero is almost at the ceiling, so let it be at the ceiling
        	flyAt += obstacleInitY - heroHeight - flyAt;
        }
        else if (!mousePressed && flyAt - flyAtDelta >= 0) {
        	flyAt -= flyAtDelta;
        }
        else if (!mousePressed) { // in this case, the hero is almost on the ground, so let it be on the ground
        	flyAt = 0;
        }
        
        // Move hero to the left if the user pressed left key
        if (moveHeroLeft) {
        	if (heroXLoc - moveX >= 0) {
				heroXLoc -= moveX;
			}
			else {
				heroXLoc = 0;
			}
        }
        
        // Move hero to the right if the user pressed right key
        if (moveHeroRight) {
	        if (heroXLoc + moveX + heroWidth <= this.getWidth()) {
				heroXLoc += moveX;
			}
			else {
				heroXLoc = this.getWidth() - heroWidth;
			}
        }
        
        // Check for the hero colliding with the obstacles
        for (Shape s : shapes) {
        	if (hero.intersects((Rectangle2D)s) && !cheatMode) {
        		// Well, game over, time to get back to reality
        		done  = true;
        		start = false;
        		break;
        	}
        }
    }
    
    public void mousePressed(int x, int y) {
    	mousePressed = true; // flyAt will start increasing
    }
    
    public void mouseReleased(int x, int y) {
    	mousePressed = false; // flyAt will start decreasing
    }
    
    // Process keyboard presses
    protected void processComponentKeyEvent(KeyEvent e) {
    	// Check what event happened on the keyboard and then check what key was pressed
    	switch(e.getID()) {
    	case KeyEvent.KEY_PRESSED:
    		if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) {
    			mousePressed = true; // Space or key-up will replace the mouse presses
    		}
    		else if (e.getKeyCode() == KeyEvent.VK_BACK_QUOTE) {
    			cheatMode = true;    // I had to make this one ;-)
    		}
    		else if (e.getKeyCode() == KeyEvent.VK_Q) { // increase the size of the hero
    			if (heroWidth * 2 <= this.getWidth() && heroHeight * 2 <= this.getHeight()) {
	    			heroHeight *= 2;     // Fuuuuuuuun ;-)
	    			heroWidth  *= 2;
    			}
    		}
    		else if (e.getKeyCode() == KeyEvent.VK_W) {
    			if (heroWidth > 2 && heroHeight > 2) { // decrease the size but make sure it cannot reach zero
    				heroHeight /= 2;     // Continuing fuuuuuuun ;-)
    				heroWidth  /= 2;
    			}
    		}
    		else if (e.getKeyCode() == KeyEvent.VK_LEFT) { // move the hero to the left
    			moveHeroLeft = true;
    		}
    		else if (e.getKeyCode() == KeyEvent.VK_RIGHT) { // move the hero to the right
    			moveHeroRight = true;
    		}
    		else if (e.getKeyCode() == KeyEvent.VK_S) {
    		    if (timer.getDelay() < 400)
    		        timer.setDelay(timer.getDelay() * 2);
    		}
            else if (e.getKeyCode() == KeyEvent.VK_A) {
                if (timer.getDelay() > 10)
                    timer.setDelay(timer.getDelay() / 2);
            }
    		break;
    	case KeyEvent.KEY_RELEASED:
    		if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) {
    			mousePressed = false; // Enter will replace the mouse presses
    		}
    		else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
    			moveHeroLeft = false;
    		}
    		else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
    			moveHeroRight = false;
    		}
    		else if (e.getKeyCode() == KeyEvent.VK_S) {
    			timer.setDelay(50);
    		}
            else if (e.getKeyCode() == KeyEvent.VK_A) {
                timer.setDelay(50);
            }
    		break;
    	}
    	// Make sure that all other elements are aware of this key press
    	super.processComponentKeyEvent(e);
    }

}

