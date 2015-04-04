package processing.test;

import processing.core.PApplet;

public class PAppChild extends PApplet {

  @Override
  public void setup() {
    size(500,500);
    enableMultiKey();
    stroke(255);
  }

 public int s = 1;

  @Override
  public void draw() {
    if(keyPressed(UP) && keyPressed(65))
      println("jo");
   // rect(20,20,100,200);
  //  background(0);
    line((s*frameCount)%width,0,(s*frameCount)%width,height);
  }

  public PApplet pa() {
    return (PApplet) this;
  }


  public void setS(int s) {
    this.s = s;
    println("s"+s);
  }

  public void sw(int sw) {
    strokeWeight(sw);
    System.out.println("sw "+ sw);
  }

 // publi

  public void sw2(int sw) {
    strokeWeight(sw);
    System.out.println("sw "+ sw);
  }

  @Override
  public void keyPressed() {
    println(keyCode);
  }

  public int f(int a) {
    println(s);
    rect(20,20,200,200);
    return a*a;
  }

  public PApplet sup() {
    return (PApplet) this;
  }

  @Override
  public void exit() {
    System.exit(0);
  }

  public static void main(String[] args) {
    //PApplet.main(new String[] {"processing.test.PAppChild"});
    PApplet.runSketch(new String[] {"processing.core.PApplet"}, new PApplet());
  }
}
