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
	private boolean start, done, mousePressed;
    private int obstacles[];      // saving all obstacles including the ground and the real obstacles as rectangles
    private Random rand;          // just a randomizer for obstacles
    private int obstaclesPassed;  // keeping track of which obstacle we should start with from the obstacles[] array
    private int n;                // the total number of obstacles at this level
    private int flyAt;            // position of the hero when the hero is flying up (the user holds the mouse)
    
    private Timer timer;          // this timer is used to repaint the panel so that it will look like the ground is moving 
	
	public void start() {
		// Create new obstacles only if the user passed the current set of obstacles
		if (obstaclesPassed >= n)
			createNewObstacles();
		
		obstaclesPassed = 0;
		flyAt = 0;
		mousePressed = false;
		start = true;
		done = false;
		
		shapes = new java.util.ArrayList<Shape>(); // saving all shapes that we need to draw
		
		scoreLabel.setText("Score: 0");
    	// setting the timer for repaint of the game in ms, it defines the speed of the game in this case
    	timer = new Timer(50, this); 
    	timer.start();
	}

    public GPanelForGame(Color color) {
        setBackground(color);
        start = mousePressed = false;
        done = true;
        obstaclesPassed = 0;
    }
    
    private void createNewObstacles() {
    	rand = new Random();
    	n = 300;
    	obstacles = new int[n];
    	// Fill up the obstacles array with random obstacles
    	for (int i = 20; i < n;) { // starting with 20 because the first 20 spots should not have any obstacles
    		int obstacle = rand.nextInt(100) + 50; // the height of the obstacle
    		int freeSpace = rand.nextInt(30);      // the distance between real obstacles
    		
    		obstacles[i] = obstacle;
    		for (int j = i + 1; j < freeSpace + i + 1 && j < n; j++) {
    			obstacles[j] = 0; // fill up free space with zeros
    		}
    		// set the current index in the array to the number, passed the latest obstacle
    		i = (freeSpace + i + 1 < n) ? (freeSpace + i + 1) : n; 
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
        if (done)
        	timer.stop();
    }

    public void paintComponent (Graphics g) {
    	if (!start) return;
    	
        // Redraw the whole app, it will clear up all the previous drawings
        super.paintComponent(g);
        
    	int obstacleNum = 60; // number of obstacles on the screen at the same time
    	int heroWidth = 30;   // the width of the hero
    	int heroHeight = 65;  // the height of the hero
    	shapes.clear(); // clear the shapes and create new ones
    	int w = this.getWidth();
    	int obstacleInitX = 0;
    	int obstacleInitY = this.getHeight() - 50;
    	int obstacleWidth = w / obstacleNum;
    	
    	// Add ground and obstacles to the shapes
    	for (int i = obstaclesPassed; i < obstaclesPassed + obstacleNum && i < n; i++) {
    		// add ground
    		shapes.add(new Rectangle2D.Double(obstacleInitX, obstacleInitY, obstacleWidth, 10)); // x, y, width, height
    		
    		// add obstacle
    		if (obstacles[i] != 0 && i % 2 == 0) { // put to the bottom
    			shapes.add(new Rectangle2D.Double(obstacleInitX, obstacleInitY - obstacles[i], obstacleWidth, obstacles[i]));
    		}
    		else if (obstacles[i] != 0) {          // put to the top
    			shapes.add(new Rectangle2D.Double(obstacleInitX, 0, obstacleWidth, obstacles[i]));
    		}
    		
    		obstacleInitX += obstacleWidth;
    	}
    	obstaclesPassed++;
    	if (obstaclesPassed >= n) {
    		// you won!
    		start = false;
    		done = true;
    	}
        
        g.setColor(Color.blue);
        // Add all shapes to the panel on the screen
        for (Shape s : shapes){
        	((Graphics2D)g).fill(s);
        }
        
        // Draw the hero, check for collision with the obstacles, and calculate the score
        processHero(g, obstacleInitY, heroWidth, heroHeight, shapes);
    }
    
    private void processHero(Graphics g, int obstacleInitY, int heroWidth, int heroHeight, java.util.List<Shape> shapes) {
    	int flyAtDelta = 25;   // how fast you want the hero to fly up while the mouse is pressed
    	g.setColor(Color.RED); // set the color for the hero
    	// Draw the hero as an ellipse, remember to keep track of flyAt
    	Ellipse2D hero = new Ellipse2D.Double(0, obstacleInitY - heroHeight - flyAt, heroWidth, heroHeight);
        ((Graphics2D)g).fill(hero); // x, y, width, height
        
        // Update how high the hero will fly up or down while the user presses the mouse
        if (mousePressed && obstacleInitY - heroHeight - flyAt > 10) { // can't go higher than the ceiling of this game which is 10 px
        	flyAt += flyAtDelta;
        }
        else if (!mousePressed && flyAt - flyAtDelta >= 0) {
        	flyAt -= flyAtDelta;
        }
        
        // Check for the hero colliding with the obstacles
        for (Shape s : shapes) {
        	if (hero.intersects((Rectangle2D)s)) {
        		// well, game over
        		done = true;
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

}
