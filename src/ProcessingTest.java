import processing.core.PApplet;

public class ProcessingTest extends PApplet {
    // Run this project as Java application and this
    // method will launch the sketch
    public static void main(String[] args) {
        String[] a = {"MAIN"};
        PApplet.runSketch( a, new ProcessingTest());
    }

    // The rest is what you would expect in the sketch

    int x, y;

    public void settings(){
        size(100, 100);
    }

    public void setup() {
        // Other setuo code here
    }

    public void mouseMoved(){
        x = mouseX;
        y = mouseY;
    }

    public void draw() {
        background(0);
        ellipse(x, y, 10, 10);
    }
}
