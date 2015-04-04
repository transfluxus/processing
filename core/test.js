function cool(i) {
	return i*i;
}

load("nashorn:mozilla_compat.js");
importPackage(java.util);

/*
var stdout = java.lang.System.out;
stdout.println("Hello");
*/


/*
this can be replaced with... see below ( maybe better?)
Java.type("processing.core.PApplet");
Java.type("processing.core.PConstants");
Java.type("processing.core.PGraphics");
Java.type("processing.core.PGraphicsJava2D");
Java.type("processing.core.PVector");
*/
	
//Packages.processing

// JavaImporter constructor accepts one or more Java Package objects
//var imports = new JavaImporter(java.util, java.io);

//  doesnt work like this:
// p5.stroke(200,0,5);
