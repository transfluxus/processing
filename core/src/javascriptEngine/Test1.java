package javascriptEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import processing.core.PApplet;
import processing.test.PAppChild;

public class Test1 {

  public static void main(String... args) throws Throwable {
    ScriptEngineManager engineManager = new ScriptEngineManager();
    ScriptEngine engine = engineManager.getEngineByName("nashorn");
    engine.eval("function sum(a, b) { return a + b; }");
    System.out.println(engine.eval("sum(1, 2);"));

    engine.eval(new FileReader("test.js"));
    Invocable invocable = (Invocable) engine;
    System.out.println(invocable.invokeFunction("cool", "5"));

    PAppChild pap = new PAppChild();
    PApplet.runSketch(new String[] {"processing.test.PAppChild"}, pap);
    engine.getBindings(ScriptContext.ENGINE_SCOPE).put("m", (PApplet)pap);
//    engine.getBindings(ScriptContext.ENGINE_SCOPE).put("mp", (PApplet)pap.super);
    pap.getFrame().setLocation(800, 200);

//  PApplet pap = new PApplet();
//  PApplet.runSketch(new String[] {"processing.core.PApplet"}, pap);
//  engine.getBindings(ScriptContext.ENGINE_SCOPE).put("manager", pap);

    //final ScriptEngine s = new ScriptEngineManager().getEngineByExtension("js");
    //s.eval("manager.test(); manager.test2();");

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String line = ".";
    //System.out.print(System.getProperty("line.separator"));
    while (!line.equals("end")) {
      line = br.readLine();
      //System.out.println(line);
      if(line.startsWith("#"))
        line = "m."+line.substring(1);
      Object o =engine.eval(line);
      //if(o != null)
     // System.out.println(o);
    }
    System.exit(0);
  }
}
