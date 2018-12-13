import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class GameObject implements Comparable {
   public static final double GRAVITY = .25;   //acceleration due to gravity
   
   public static final double JUMP_SPEED = 5;
   
   public BufferedImage image;
   
   private int x, y, z = 0;
   
   protected double xRem = 0, yRem = 0, zRem = 0;
   
   private int width = 50, height = 100;
   
   private double speed = 5, 
                  zSpeed = 0;
   
   private double angle = 0,     //angle in radians, 0 is up [0, 2pi)
                  zAngle = 0;    //angle in radians for looking up and down, 0 is straight, positive is up [-pi, pi]
   
   private double maxView = Math.PI / 3,
                  maxZView = Math.PI / 2;
   
   private int maxDist = 1000;
   
   private int screenX, screenY, screenWidth, screenHeight;
   
   public GameObject(int xi, int yi) {
      x = xi;
      y = yi;
      image = Renderer.getImage(1, 0);
      
      Screen.map.add(this);
   }
   
   //drawing on first person view
   public void draw(Graphics g) {
      if(isVisible()) {
         double playerAngle = Screen.player.getAngle();
         int d = Screen.player.getD();
         int playerZ = Screen.player.getZ();
         
         double dist = dist();
         int dx = x - Screen.player.getX();
         int dy = y - Screen.player.getY();
         
         double dHorizontal = dx * Math.cos(playerAngle) + dy * Math.sin(playerAngle);
         double dz = Math.sqrt(dist * dist - dHorizontal * dHorizontal);
         
         screenWidth = (int)(width * d / dz);
         screenHeight = (int)(height * d / dz);
         
         screenX = (int)(dHorizontal * d / dz + Driver.WIDTH / 2.0 - screenWidth / 2.0);
         screenY = (int)(Driver.HEIGHT / 2.0 * (1 + Math.pow(.955, dz) + 2 * Math.sin(Screen.player.getZAngle())) - screenHeight / 2.0 + playerZ * 250 / dist - z * 250 / dist);
         
         g.setColor(Color.RED);
         
         g.drawImage(image, screenX, screenY, screenWidth, screenHeight, null);
      }
   }
   
   //drawing on mini-map
   public void drawMap(Graphics g) {/*
      if(isVisible()) {
         int mapX = (int)(Screen.MAP_X + (double)getX() / Screen.MAX_WIDTH * Screen.MAP_WIDTH);
         int mapY = (int)(Screen.MAP_Y + (double)getY() / Screen.MAX_HEIGHT * Screen.MAP_HEIGHT);
      
         g.setColor(Color.GREEN);
      
         g.fillRect(mapX, mapY, (int)((double)width / Screen.MAX_WIDTH * Screen.MAP_WIDTH), (int)((double)width / Screen.MAX_HEIGHT * Screen.MAP_HEIGHT));
      }*/
   }
   
   public void update() {
      updatePos();
      
      checkEdgeCollisions();
   }
   
   public void updatePos() {
      //gravity
      zRem += zSpeed * Screen.speed;
      z += zRem;
      zRem %= 1;
      if(z <= 0) {
         z = 0;
         zRem = 0;
         zSpeed = 0;
      }
      else {
         zSpeed -= GRAVITY * Screen.speed;
      }
   }
   
   //getter for x position
   public int getX() {
      return x;
   }
   
   //getter for y position
   public int getY() {
      return y;
   }
   
   //getter for z position
   public int getZ() {
      return z;
   }
   
   //getter for width
   public int getWidth() {
      return width;
   }
   
   //getter for height
   public int getHeight() {
      return height;
   }
   
   //getter for screen width
   public int getScreenWidth() {
      return screenWidth;
   }
   
   //getter for screen height
   public int getScreenHeight() {
      return screenHeight;
   }
   
   //getter for screen x position
   public int getScreenX() {
      return screenX;
   }
   
   //getter for screen y position
   public int getScreenY() {
      return screenY;
   }
   
   //setter for width
   public void setWidth(int w) {
      width = w;
   }
   
   //setter for height
   public void setHeight(int h) {
      height = h;
   }
   
   //setter for screen width
   public void setScreenWidth(int w) {
      screenWidth = w;
   }
   
   //setter for screen height
   public void setScreenHeight(int h) {
      screenHeight = h;
   }
   
   //setter for x position
   public void setX(int newX) {
      x = newX;
   }
   
   //setter for y position
   public void setY(int newY) {
      y = newY;
   }
   
   //setter for screen x position
   public void setScreenX(int newX) {
      screenX = newX;
   }
   
   //setter for screen y position
   public void setScreenY(int newY) {
      screenY = newY;
   }
   
   //getter for angle
   public double getAngle() {
      return angle;
   }
   
   //setter for angle
   public void setAngle(double newAngle) {
      angle = (newAngle + Math.PI * 2) % (Math.PI * 2);
   }
   
   //getter for zAngle
   public double getZAngle() {
      return zAngle;
   }
   
   //setter for zAngle
   public void setZAngle(double newAngle) {
      zAngle = newAngle;
      
      if(zAngle > maxZView) {
         zAngle = maxZView;
      }
      else if(zAngle < -maxZView) {
         zAngle = -maxZView;
      }
   }
   
   //getter for speed
   public double getSpeed() {
      return speed;
   }
   
   //setter for speed
   public void setSpeed(double newSpeed) {
      speed = newSpeed;
   }
   
   //getter for zSpeed
   public double getZSpeed() {
      return zSpeed;
   }
   
   //setter for zSpeed
   public void setZSpeed(double newZSpeed) {
      zSpeed = newZSpeed;
   }
   
   //check if this object should be drawn
   public boolean isVisible() {
      if(this instanceof Player) {
         return false;
      }
      
      double dx = x - Screen.player.getX();
      double dy = y - Screen.player.getY();
      
      double dangle = 0;
      if(dx != 0) {
         dangle = Math.atan(dy / dx) + Math.PI / 2;
      }
      else {
         dangle = 0;
         if(dy < 0) {
            dangle = Math.PI;
         }
      }
      
      if(dx <= 0) {
         dangle += Math.PI;
         dangle %= Math.PI * 2;
      }
      
      dangle = (dangle + 2 * Math.PI) % (2 * Math.PI);
      
      return dist() < maxDist && 
             Math.abs(angleDiff(Screen.player.getAngle(), dangle)) < maxView;
   }
   
   protected double getRelativeAngle() {
      if(this instanceof Player) {
         return angle - Math.PI;
      }
      double dx = x - Screen.player.getX();
      double dy = y - Screen.player.getY();
      
      double dangle = 0;
      if(dx != 0) {
         dangle = Math.atan(dy / dx) + Math.PI / 2;
      }
      else {
         dangle = 0;
         if(dy < 0) {
            dangle = Math.PI;
         }
      }
      
      if(dx <= 0) {
         dangle += Math.PI;
         dangle %= Math.PI * 2;
      }
      
      dangle = (dangle + 2 * Math.PI) % (2 * Math.PI);
      
      return dangle;
   }
   
   //check if this object can be seen by gc
   public boolean isVisible(GameChar gc) {
      if(this == gc) {
         return false;
      }
      
      double dx = x - gc.getX();
      double dy = y - gc.getY();
      
      double dangle = 0;
      if(dx != 0) {
         dangle = Math.atan(dy / dx) + Math.PI / 2;
      }
      else {
         dangle = 0;
         if(dy < 0) {
            dangle = Math.PI;
         }
      }
      
      if(dx <= 0) {
         dangle += Math.PI;
         dangle %= Math.PI * 2;
      }
      
      dangle = (dangle + 2 * Math.PI) % (2 * Math.PI);
      
      return dist(gc) < maxDist && 
             Math.abs(angleDiff(gc.getAngle(), dangle)) < maxView;
   }
   
   
   //distance formula helper method
   private static double dist(int x1, int x2, int y1, int y2) {
      return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
   }
   
   //returns distance to gc
   public double dist(GameChar gc) {
      return dist(x, gc.getX(), y, gc.getY());
   }
   
   //returns distance to player
   public double dist() {
      return dist(Screen.player);
   }
   
   //helper method for view angle
   //post: returns the difference between two angles, keeping result (-pi, pi]
   protected static double angleDiff(double a, double b) {
      double diff = a - b;
      if(diff <= -Math.PI) {
         diff += Math.PI * 2;
      }
      else if(diff > Math.PI) {
         diff -= Math.PI * 2;
      }
      return diff;
   }
   
   //Hash code is based on current x and y position
   @Override
   public int hashCode() {
      return (x / Screen.BLOCK_WIDTH) + Screen.MAX_WIDTH_BLOCKED * (y / Screen.BLOCK_HEIGHT);
   }
   
   //compares by simple hash code
   //return a negative integer if this object is less than the object o
   //return zero if this object is equal to the object o
   //return a positive integer if this object is greater than the object o
   @Override
   public int compareTo(Object o) {
      if(o instanceof GameObject) {
         GameObject go = (GameObject)o;
         double myDist = dist();
         double otherDist = go.dist();
         return (int)(myDist - otherDist);
      }
      
      return 0;
   }
   
   //checking collision with edge of world
   private void checkEdgeCollisions() {
      if(getX() < 0) {
         setX(0);
      }
      else if(getX() >= Screen.MAX_WIDTH) {
         setX(Screen.MAX_WIDTH - 1);
      }
      if(getY() < 0) {
         setY(0);
      }
      else if(getY() >= Screen.MAX_HEIGHT) {
         setY(Screen.MAX_HEIGHT - 1);
      }
   }
   
   public int getMaxDist() {
      return maxDist;
   }
}