package javascriptEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import processing.core.PApplet;
import processing.test.PAppChild;

public class Test1 {

  public static void main(String... args) throws Throwable {
    ScriptEngineManager engineManager = new ScriptEngineManager();
    ScriptEngine engine = engineManager.getEngineByName("nashorn");
    engine.eval("function sum(a, b) { return a + b; }");
    //System.out.println(engine.eval("sum(1, 2);"));

    // thought the type or Package stuff would be necessary, but it isnt...
    engine.eval(new FileReader("test.js"));

    //Invocable invocable = (Invocable) engine;
    //System.out.println(invocable.invokeFunction("cool", "5"));

    /*
    PAppChild pap = new PAppChild();
    PApplet.runSketch(new String[] {"processing.test.PAppChild"}, pap);
    engine.getBindings(ScriptContext.ENGINE_SCOPE).put("p",pap);
    pap.getFrame().setLocation(800, 200);
    // check if it works:
    engine.eval("p.stroke(255,240,0);");
    */

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String line = ".";
    //System.out.print(System.getProperty("line.separator"));
    while (!line.equals("end")) {

      line = br.readLine();
      //System.out.println(line);
      if(line.startsWith("#"))
        line = "p."+line.substring(1);
      try {
      Object o =engine.eval(line);
      if(o != null)
      System.out.println(o);
      } catch(ScriptException exc) {
        System.err.println("Careful, you almost broke something: "+
      System.getProperty("line.separator")+exc.getMessage());
      }
    }
    System.exit(0);
  }
}
