package processing.test;

import processing.core.PApplet;

public class PAppChild extends PApplet {

  @Override
  public void setup() {
    size(500,500);
    enableMultiKey();
    stroke(255);
    frame.setVisible(false);
    frame.setLocation(displayWidth-width, 100);
 frame.setVisible(true);
  }

 public int s = 1;

 public boolean bg = false;

 public boolean isBg() {
 return bg;
}



public void setBg(boolean bg) {
 this.bg = bg;
}

  @Override
  public void draw() {
    if(bg)
      background(0);
    if(keyPressed(UP) && keyPressed(65))
      println("jo");
   // rect(20,20,100,200);
  //  background(0);
    line((s*frameCount)%width,0,(s*frameCount)%width,height);
  }



  public void setS(int s) {
    this.s = s;
    println("s"+s);
  }

  public void sw(int sw) {
    strokeWeight(sw);
    System.out.println("sw "+ sw);
  }

 // access private doesnt work yet
  // static supermethods neither.
  private void sw2(int sw) {
    strokeWeight(sw);
    System.out.println("sw "+ sw);
  }

  @Override
  public void keyPressed() {
    println(keyCode);
  }

  public int f1(int a) {
    println(s);
    rect(20,20,200,200);
    return a*a;
  }

  protected int f2(int a) {
    println(s);
    rect(20,20,200,200);
    return a*a;
  }

  private int f3(int a) {
    println(s);
    rect(20,20,200,200);
    return a*a;
  }
/*
  public boolean bg() {
    background(0);
    return true;
  }
*/
  @Override
  public void exit() {
    System.exit(0);
  }

  public static void main(String[] args) {
    //PApplet.main(new String[] {"processing.test.PAppChild"});
    PApplet.runSketch(new String[] {"processing.core.PApplet"}, new PApplet());
  }
}
