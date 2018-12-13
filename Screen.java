import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class Screen extends JPanel implements MouseMotionListener, MouseListener {
   
   protected static int treesKilled = 0;
   
   protected static double speed = 1.0;
   
   public static final int MAP_WIDTH = (int)(Driver.WIDTH * .15),          //dimensions of mini-map on screen
                           MAP_HEIGHT = (int)(Driver.HEIGHT * .15),
                           MAP_X = (int)(Driver.WIDTH * .8),               //position of mini-map on screen
                           MAP_Y = (int)(Driver.HEIGHT * .05),
                           MAX_WIDTH = Driver.WIDTH * 2,                   //dimensions explorable of world
                           MAX_HEIGHT = Driver.HEIGHT * 2,
                           BLOCK_WIDTH = 100,                               //dimensions of each section for hash table
                           BLOCK_HEIGHT = 100,
                           MAX_WIDTH_BLOCKED = MAX_WIDTH / BLOCK_WIDTH,    //dimensions of world after being blocked off
                           MAX_HEIGHT_BLOCKED = MAX_HEIGHT / BLOCK_HEIGHT,
                           MAX_HASH_CODE = MAX_WIDTH_BLOCKED * (MAX_HEIGHT_BLOCKED);
   
   public static HashTable map = new HashTable();
   
   private static final int ICON_MAP_WIDTH = 8;    //dimensions for an icon on mini-map
   
   private static final long QUICK_DELAY = 15;    //# of frames for counting as a press and release
   
   private static long lastSpawnFrame = 0, deathFrame = 0;
   
   private static long frame = 0;   //current frame that does NOT reset every second
   private static List<Runnable> inputs = new ArrayList<Runnable>();
   private static int fps = 60,
                      groundY = Driver.WIDTH / 2;     //initial y position of horizon
   private static boolean paused = false;
   
   //Strings for naming actions
   private final String UP = "up",
                        DOWN = "down",
                        LEFT = "left",
                        RIGHT = "right",
                        
                        SPACE = "space",
                        
                        UP_RELEASED = "up released",
                        DOWN_RELEASED = "down released",
                        LEFT_RELEASED = "left released",
                        RIGHT_RELEASED = "right released",
                        
                        SPACE_RELEASED = "space released",
                        
                        EXIT = "exit";
   
   public static Player player;
   
   public Screen() {
      super();
      setKeyBindings();
      addMouseListener(this);
      addMouseMotionListener(this);
      
      //********** MAKE MOUSE INVISIBLE **********
      // Transparent 16 x 16 pixel cursor image
      BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
   
      // Create a new blank cursor
      Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
         cursorImg, new Point(0, 0), "blank cursor");
   
      // Set the blank cursor to the JFrame
      setCursor(blankCursor);
      //********** END MAKE MOUSE INVISIBLE **********
   }
   
   //run the actions that the user inputs
   public synchronized void processInputs() {
      for(int i = 0; i < inputs.size(); i++) {
         inputs.get(i).run();
      }
      inputs.clear();
   }
   
   //updates, add to frame count
   public void update() {
      //update all game objects
      for(int i = 0; i < MAX_HASH_CODE; i++) {
         Iterator iterator = map.getIterator(i);
         
         while(iterator.hasNext()) {
            GameObject temp = (GameObject)(iterator.next());
            temp.update();
         }
      }
      
      //initial setup
      if(frame == 0) {
         new Renderer();
         
         map = new HashTable();
         
         player = new Player((int)(Math.random() * MAX_WIDTH), (int)(Math.random() * MAX_HEIGHT));
         
         new Enemy((int)(Math.random() * MAX_WIDTH), (int)(Math.random() * MAX_HEIGHT));
         lastSpawnFrame = 0;
         
         for(int i = 0; i <= MAX_WIDTH; i += 200) {
            for(int j = 0; j <= MAX_HEIGHT; j += 200) {
               new GameObject(i, j);
            }
         }
      }
      else {
         //recompute horizon
         groundY = Driver.HEIGHT / 2 + (int)(getHeight() * Math.sin(player.getZAngle()));
         
         //remake hash table each frame
         HashTable temp = new HashTable();
         
         for(int i = 0; i < MAX_HASH_CODE; i++) {
            Iterator iterator = map.getIterator(i);
         
            while(iterator.hasNext()) {
               temp.add((GameObject)(iterator.next()));
            }
         }
         
         map = temp;
      }
      
      if(frame - 10 * Driver.TARGET_FPS > lastSpawnFrame) {
         new Enemy((int)(Math.random() * MAX_WIDTH), (int)(Math.random() * MAX_HEIGHT));
         lastSpawnFrame = frame;
      }
      
      if(player.isDead()) {
         if(deathFrame == 0) {
            deathFrame = frame;
         }
         else if(frame - 9 * Driver.TARGET_FPS > deathFrame) {
            frame = 0;
            deathFrame = 0;
            treesKilled = 0;
            return;
         }
      }
      
      frame++;
   }
   
   //drawing
   public void paintComponent(Graphics g) {
      
      //after images
      int a = 255;
      if(player != null) {
         double frac = (double)(player.hp) / player.maxHp;
         
         a = (int)(255 * frac);
         
         if(a < 32) {
            a = 32;
         }
         
         speed = frac;
         
         if(speed < .1) {
            speed = .1;
         }
         
         g.setColor(new Color(0, 0, 255, a));
         
         g.fillRect(0, 0, getWidth(), groundY);
      }
      
      if(frame > 0) {
         //draw ground
         g.setColor(new Color(0, 200, 0, a));
         g.fillRect(0, groundY, getWidth(), getHeight() * 2);
         
         //draw all game objects
         {
            Tree tempTree = new Tree();
         
            for(int i = 0; i < MAX_HASH_CODE; i++) {
               Iterator iterator = map.getIterator(i);
            
               while(iterator.hasNext()) {
                  GameObject temp = (GameObject)(iterator.next());
               
                  if(temp != null && temp.isVisible() || temp == player) {
                     tempTree.add(temp);
                  }
               }
            }
         
            Iterator iterator = tempTree.iterator();
            
            while(iterator.hasNext()) {
               ((GameObject)(iterator.next())).draw(g);
            }
         }
      
         /*/draw mini-map
         g.setColor(Color.GRAY);
         g.fillRect(MAP_X, MAP_Y, MAP_WIDTH + ICON_MAP_WIDTH, MAP_HEIGHT + ICON_MAP_WIDTH);*/
         
         //draw all game objects on mini-map
         for(int i = 0; i < MAX_HASH_CODE; i++) {
            Iterator iterator = map.getIterator(i);
         
            while(iterator.hasNext()) {
               GameObject temp = (GameObject)(iterator.next());
               temp.drawMap(g);
            }
         }
         
         //********** FOR DEBUGGING **********
         //draw statistics onto screen
         g.setColor(Color.WHITE);
         g.drawString("FPS: " + fps, (int)(getWidth() * .9), (int)(getHeight() * .95));
         
         //***** GAME OVER *****
         if(player.isDead()) {
            double temp = (frame - deathFrame) / (2.0 * Driver.TARGET_FPS);
            if(temp >= 0 && temp < 1) {
               g.setColor(new Color(255, 0, 0, (int)(256 * temp)));
            }
            else {
               g.setColor(Color.RED);
            }
            g.setFont(new Font("MONOSPACED", Font.BOLD, 50));
            g.drawString("GAME OVER", (int)(getWidth() * .25), (int)(getHeight() * .45));
            
            temp -= 1;
            
            if(temp >= 0 && temp < 1) {
               g.setColor(new Color(255, 0, 0, (int)(256 * temp)));
            }
            else if(temp < 0) {
               g.setColor(new Color(0, 0, 0, 0));
            }
            else {
               g.setColor(Color.RED);
            }
            
            g.setFont(new Font("MONOSPACED", Font.BOLD, 25));
            g.drawString("YOU KILLED " + treesKilled + " TREES", (int)(getWidth() * .27), (int)(getHeight() * .55));
            
            temp -= 1.5;
            if(temp >= 0 && temp < 1) {
               g.setColor(new Color(0, 0, 0,(int)(256 * temp)));
               g.fillRect(0, 0, getWidth(), getHeight());
            }
            else if(temp >= 1) {
               g.setColor(Color.BLACK);
               g.fillRect(0, 0, getWidth(), getHeight());
            }
         }
      }
   }
   
   //rendering graphics
   public void render() {
      repaint();
   }
   
   //setter for fps
   public void setFps(int newFps) {
      fps = newFps;
   }
   
   //getter for frame
   public static long getFrame() {
      return frame;
   }
      
   //add user input to list of actions to be performed
   public static void addInput(Runnable input) {
      inputs.add(input);
   }
   
   //********** KEY BINDINGS **********
   //for user inputs
   private void gimp(String key, String name) {
      getInputMap().put(KeyStroke.getKeyStroke(key), name);
   }
   
   //for user inputs
   private void gamp(String name, AbstractAction action) {
      getActionMap().put(name, action);
   }
   
   //set keys to actions
   private void setKeyBindings() {
      //////////////////////set keys to names/////////////////////////////
      gimp("ESCAPE", EXIT);
      
      gimp("W", UP);
      gimp("UP", UP);
      gimp("S", DOWN);
      gimp("DOWN", DOWN);
      gimp("A", LEFT);
      gimp("LEFT", LEFT);
      gimp("D", RIGHT);
      gimp("RIGHT", RIGHT);
      gimp("SPACE", SPACE);
      
      gimp("released W", UP_RELEASED);
      gimp("released UP", UP_RELEASED);
      gimp("released A", LEFT_RELEASED);
      gimp("released LEFT", LEFT_RELEASED);
      gimp("released D", RIGHT_RELEASED);
      gimp("released RIGHT", RIGHT_RELEASED);
      gimp("released S", DOWN_RELEASED);
      gimp("released DOWN", DOWN_RELEASED);
      gimp("released SPACE", SPACE_RELEASED);
      
      ////////////////////////set names to actions//////////////////////////////////
      gamp(EXIT, 
         new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               System.exit(1);
            }
         }
         );
      
      
      gamp(UP, new MoveAction(0, true));
      gamp(UP_RELEASED, new MoveAction(0, false));
      gamp(RIGHT, new MoveAction(1, true));
      gamp(RIGHT_RELEASED, new MoveAction(1, false));
      gamp(DOWN, new MoveAction(2, true));
      gamp(DOWN_RELEASED, new MoveAction(2, false));
      gamp(LEFT, new MoveAction(3, true));
      gamp(LEFT_RELEASED, new MoveAction(3, false));
      
      gamp(SPACE, new SpaceAction(true));
      gamp(SPACE_RELEASED, new SpaceAction(false));
   }
   
   //moving up, right, down, or left
   private static class MoveAction extends AbstractAction {
      private int dir;              //[0, 1, 2, 3] = [up, right, down, left]  (N, E, S, W)
      private boolean pressed;
      
      private MoveAction(int d, boolean p) {
         dir = d;
         pressed = p;
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
         inputs.add(
            new Runnable() {
               public void run() {
                  switch(dir) {
                     case 0:     //up (N)
                        player.moveUp(pressed);
                        break;
                     case 1:     //right (E)
                        player.moveRight(pressed);
                        break;
                     case 2:     //down (S)
                        player.moveDown(pressed);
                        break;
                     case 3:     //left (W)
                        player.moveLeft(pressed);
                        break;
                  }
               }
               
            });
      }
   }
   
   //pressing space (jump)
   private static class SpaceAction extends AbstractAction {
      private boolean pressed;
      
      private SpaceAction(boolean p) {
         pressed = p;
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
         inputs.add(
            new Runnable() {
               public void run() {
                  if(pressed) {
                     player.jump();
                  }
                  else {
                     
                  }
               }
            });
      }
   }
   //********** END KEY BINDINGS **********
   
   
   //********** MOUSE **********
   @Override
   public void mouseClicked(MouseEvent e) {
      
   }

   @Override
   public void mousePressed(MouseEvent e) {
      if(e.getButton() == MouseEvent.BUTTON1) {
         player.setAttacking(true);
      }
      else if(e.getButton() == MouseEvent.BUTTON3) {
         player.setBlocking(true);
      }
   }

   @Override
   public void mouseReleased(MouseEvent e) {
      player.setAngleSpeed(0);
      if(e.getButton() != MouseEvent.MOUSE_DRAGGED && e.getButton() == MouseEvent.BUTTON2) {
         player.target();
      }
      if(e.getButton() == MouseEvent.BUTTON1) {
         player.setAttacking(false);
      }
      else if(e.getButton() == MouseEvent.BUTTON3) {
         player.setBlocking(false);
      }
   }

   @Override
   public void mouseEntered(MouseEvent e) {
      
   }

   @Override
   public void mouseExited(MouseEvent e) {
      
   }

   @Override
   public synchronized void mouseDragged(MouseEvent e) {
      try {
         if(!player.hasTarget()) {
            //horizontal angle
            player.setAngle(player.getAngle() + ((double)e.getX() - Driver.WIDTH / 2) / 1000);
         
            //vertical angle
            player.setZAngle(player.getZAngle() + (Driver.HEIGHT / 2 - (double)e.getY()) / 3000);
            
            if(Math.abs((Driver.WIDTH / 2 - (double)e.getX()) / 50) > 0.2) {
               //angle speed
               player.setAngleSpeed((Driver.WIDTH / 2 - (double)e.getX()) / 50);
            }
            
            // Move the cursor to the center
            Robot robot = new Robot();
            
            //move mouse back to center
            robot.mouseMove(Driver.getCX(), Driver.getCY());
         }
         else {
            player.setAimX(e.getX());
            player.setAimY(e.getY());
         }
      }
      catch (Exception ex) {
      }
   }

   @Override
   public void mouseMoved(MouseEvent e) {
      mouseDragged(e);
   }
   
   //********** END MOUSE **********
}