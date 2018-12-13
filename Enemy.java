public class Enemy extends GameChar {
   private final double MAX_SWING_DISP = 500;   //max swing displacement
   
   private double currSwingSpeed = 25;
   
   private boolean swinging = false;
   
   private double swingSpeed = 0;
   
   private double MAX_SWING_SPEED1 = currSwingSpeed * 2;
   private double MAX_SWING_SPEED2 = MAX_SWING_SPEED1 * 2;
   
   public Enemy(int xi, int yi) {
      super(xi, yi);
      setSpeed(getSpeed() / 2);
      weapon[1] = Renderer.getImage(1, 8);
      weapon[2] = Renderer.getImage(1, 9);
      weapon[3] = Renderer.getImage(1, 10);
      weapon[4] = Renderer.getImage(1, 11);
   }
   
   //updating
   public void update() {
      super.update();
      
      if(currSwingSpeed < MAX_SWING_SPEED1 && hp <= maxHp / 2) {
         currSwingSpeed = 2 * currSwingSpeed;
         swinging = false;
      }
      
      if(currSwingSpeed < MAX_SWING_SPEED2 && hp <= maxHp / 4) {
         currSwingSpeed = 2 * currSwingSpeed;
         swinging = false;
      }
      
      if(!isAttacking()) {
         swingSpeed = 0;
         setAimX(Driver.WIDTH / 2);
         swinging = false;
      }
      
      //enemy AI
      if(dist() < getMaxDist()) {
         setTarget(Screen.player);
      }
      
      if(hasTarget()) {
         moveUp(true);
         if(dist() < getWeaponLength() * 5) {
            if(!isAttacking()) {
               //stab
               setAttacking(true);
            }
            else if(!isStabbing()) {
               //swing
               if(!swinging) {
                  swingSpeed = currSwingSpeed;
                  swinging = true;
               }
               if(Math.abs(getAimX() - Driver.WIDTH / 2) > MAX_SWING_DISP) {
                  swingSpeed = -swingSpeed;
               }
               setAimX((int)(getAimX() + swingSpeed * Screen.speed));
            }
         }
         else {
            //stop attacking
            setAttacking(false);
            swinging = false;
            swingSpeed = 0;
            setAimX(Driver.WIDTH / 2);
         }
      }
      else {
         moveUp(false);
      }
   }
}