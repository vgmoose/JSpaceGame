import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Draw {

    // this matrix emulates the pixel grid on the wii u gamepad
    BufferedImage screen = new BufferedImage(427, 240, BufferedImage.TYPE_INT_RGB);
    Space space;

    public Draw(Space space) {
        this.space = space;
    }

    void flipBuffers() {
        space.repaint();
    }

    /**
     * This is the main function that does the grunt work of drawing to both screens. It takes in the
     * Services structure that is constructed in program.c, which contains the pointer to the function
     * that is responsible for putting a pixel on the screen. By doing it this way, the OSScreenPutPixelEx function pointer is only
     * looked up once, at the program initialization, which makes successive calls to this pixel caller quicker.
     **/
    void putAPixel(int x, int y, int r, int g, int b) {
        if (x < 0 || y < 0 || x >= 427 || y >= 240) return;

        int num = (r << 16) | (g << 8) | (b);
        screen.setRGB(x, y, num);
    }

    void drawString(int x, int y, String string) {
        x += 2;
        Graphics2D g2d = screen.createGraphics();
        g2d.drawImage(screen, 0, 0, null);
        g2d.setPaint(Color.white);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(string, (int) (6.25 * x), fm.getHeight() * y);
        g2d.dispose();
    }

    void drawStringTv(int x, int y, String string) {
    }

    void fillScreen(int r, int g, int b, int a) {
        for (int x = 0; x < 427; x++)
            for (int y = 0; y < 240; y++)
                putAPixel(x, y, r, g, b);
    }

    // draw black rect all at once
    void fillRect(int ox, int oy, int width, int height, int r, int g, int b) {

        int rx;
        for (rx = 0; rx < width; rx++) {
            int ry;
            for (ry = 0; ry < height; ry++) {
                int x = ox + rx;
                int y = oy + ry;

                // do actual pixel drawing logic
                putAPixel(x, y, r, g, b);
            }
        }
    }

    /**
     * This function draws a "bitmap" in a very particular fashion: it takes as input the matrix of chars to draw.
     * In this matrix, each char represents the index to look it up in the palette variable which is also passed.
     * Alpha isn't used here, and instead allows the "magic color" of 0x272727 to be "skipped" when drawing.
     * By looking up the color in the palette, the bitmap can be smaller. Before compression was implemented, this was
     * more important. A potential speedup may be to integrate the three pixel colors into a matrix prior to this function.
     **/
    void drawBitmap(int ox, int oy, int width, int height, short[][] inp, short[][] pal) {
        int rx;
        for (rx = 0; rx < width; rx++) {
            int ry;
            for (ry = 0; ry < height; ry++) {
                short[] color = pal[inp[ry][rx]];
                short r = color[2];
                short g = color[1];
                short b = color[0];

//				// transparent pixels
                if (r == 0x27 && g == 0x27 && b == 0x27) {
                    continue;
                }

                int x = ox + rx;
                int y = oy + ry;

                // do actual pixel drawing logic
                putAPixel(x, y, r, g, b);
            }
        }

    }

    /**
     * This is primarly used for drawing the stars, and takes in a pixel map. It is similar to bitmap, but now
     * it takes the whole pixel map as well as which portion of it to actually draw. At the beginning, all of the stars
     * are drawn, but whenever the ship moves, only the stars underneath the ship need to be redrawn.
     **/
    void drawPixels(Pixel[] pixels) {
        int rx;
        for (rx = 0; rx < 200; rx++) {
            int x = pixels[rx].x;
            int y = pixels[rx].y;

            putAPixel(x, y, pixels[rx].r, pixels[rx].g, pixels[rx].b);
        }

    }

    void drawPixel(int x, int y, int r, int g, int b) {
        putAPixel(x, y, r, g, b);

    }
}
