function cool(i) {
	return i*i;
}

//load("nashorn:mozilla_compat.js");

//importPackage(java.util);

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
// 	
Packages.processing.core
//Packages.sun.java2d

 var PVector = Java.type("processing.core.PVector");
	var v1 = new PVector();
/* this works, but doesnt update the screen (background,...)
var PApplet = Java.type("processing.core.PApplet");
var p5 = new PApplet();
var srtingArray = Java.type("java.lang.String[]");
var p5InitS = new srtingArray(1);
p5InitS[0] = "processing.core.PApplet";
PApplet.runSketch(p5InitS, p5);
p5.size(500,500);
print("cool");
p5.background(0);
p5.loop();
p5.background(0);
*/
	/*
 * what is the class path? needed for runSketch 
 var MyP5 = Java.extend(PApplet, {
    draw: function() {
        print("Run in separate thread");
    }
});
print(MyP5);
for(var a in MyP5) {
	print(a);
}
*/
var MP5 = Java.type("processing.test.PAppChild");
/*  
var MP5G = Java.type("processing.core.PGraphics");
var MP5G2D = Java.type("processing.core.PGraphicsJava2D");
var MP5G2D2X = Java.type("processing.core.PGraphicsJava2D2X");
*/
	print("hello");
var mp5 = new MP5();
var srtingArray = Java.type("java.lang.String[]");
var PApplet = Java.type("processing.core.PApplet");
var p5InitS2 = new srtingArray(1);
p5InitS2[0] = "processing.test.PAppChild";
PApplet.runSketch(p5InitS2, mp5);
// schade!:
mp5.coolStuff = function(x){return x*x};

mp5.f1(1); // public
//mp5.f2(2); // protected
// mp5.f3(3); // private

// JavaImporter constructor accepts one or more Java Package objects
//var imports = new JavaImporter(java.util, java.io);

//  doesnt work like this:
// p5.stroke(200,0,5);


var MP52 = Java.extend(PApplet, { 
    setup: function() { 
        size(500,500);
    } ,

    draw: function() {
    	background(0);
        line((frameCount)%width,0,(frameCount)%width,height);
    }

}); 

print(MP52);
var mp52 = new MP52();
PApplet.runSketch(p5InitS2, mp52);
