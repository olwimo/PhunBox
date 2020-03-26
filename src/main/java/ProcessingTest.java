import org.openni.*;
import processing.core.PApplet;
import processing.core.PImage;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

public class ProcessingTest extends PApplet {
    // Run this project as Java application and this
    // method will launch the sketch
    public static void main(String[] args) {
        String[] a = {"MAIN"};

        PApplet.runSketch( a, new ProcessingTest());
    }

    Device device = null;
    PImage image;
    static final float near = 600;
    static final float far = 900;

    @Override
    public void settings()
    {
        size(640, 480);
    }

    @Override
    public void setup() {
        String uri;

        OpenNI.initialize();

        List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
        if (devicesInfo.isEmpty()) {
            System.out.println("No Kinect device");
            exit();
            return;
        }
        uri = devicesInfo.get(0).getUri();

        colorMode(HSB, 1f);

        image = createImage(width, height, HSB);
        image.loadPixels();
        for (int i = 0, length = image.pixels.length; i < length; i++) image.pixels[i] = color(0);
        image.updatePixels();

        Device device = Device.open(uri);

        // SensorInfo sensorInfo = device.getSensorInfo(SensorType.DEPTH);

        VideoStream videoStream = VideoStream.create(device, SensorType.DEPTH);

        List<VideoMode> supportedModes = videoStream.getSensorInfo().getSupportedVideoModes();
        VideoMode mode = supportedModes.get(0);
        for (VideoMode videoMode : supportedModes) {
            System.out.println("VideoMode: " + videoMode.getResolutionX() + "x" + videoMode.getResolutionY());
            if (videoMode.getResolutionX() >= mode.getResolutionX() &&
                    videoMode.getResolutionY() >= mode.getResolutionY())
                mode = videoMode;
        }

        videoStream.setVideoMode(mode);
        videoStream.start();

        videoStream.addNewFrameListener(new VideoStream.NewFrameListener() {
            long lastTime = System.currentTimeMillis();

            @Override
            public void onFrameReady(VideoStream stream) {
                VideoFrameRef frame = stream.readFrame();

                // PixelFormat pixelFormat = frame.getVideoMode().getPixelFormat();
                // System.out.println(pixelFormat.name());

                int frameWidth = frame.getWidth();
                int frameHeight = frame.getHeight();

                // System.out.println("Width: " + frameWidth + " | Height: " + frameHeight);

                ShortBuffer buffer = frame.getData().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                buffer.rewind();

                image.loadPixels();
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int z = buffer.get((int)Math.floor(((double)x / (double)width) * frameWidth) +
                                (int)Math.floor(((double)y / (double)height) * frameHeight) * frameWidth);
                        if (z < 0) z = 65536 + z;
                        //Point3D<Float> point = CoordinateConverter.convertDepthToWorld(videoStream, x, y, z);

                        if (z >= near && z < far)
                            image.pixels[x + y * width] = color((((float)z - near) / (far - near) ), .5f, .5f);
                        else
                            image.pixels[x + y * width] = color(0);
                    }
                }
                image.updatePixels();

                frame.release();
                // System.out.println("Frame time since last: " + (-lastTime + (lastTime = System.currentTimeMillis())));
            }
        });
    }

    @Override
    public void exitActual() {
        System.out.println("Shutting down...");

        if (device != null) device.close();
        OpenNI.shutdown();

        super.exitActual();
    }

    @Override
    public void draw() {
        image(image, 0, 0);
    }
}
