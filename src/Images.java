
public class Images {
    /**
     * This file contains compressed bitmaps that are loaded, decompresed, and used in game.
     * The file format is very hacky. There is most likely a faster way to do this with gx2,
     * and they could be even more compressed if they were stored at the bit level rather than the byte level.
     * The compression format is as follows (does not apply to palettes, they are uncompressed):
     * - Palette element sare in {B, G, R} format, as this is what is in windows style bitmap hex
     * - When uncompressed, bitmaps are stored as matrices of indices for the corresponding pallette.
     * - For example, bitmap = {{0, 1, 0},
     * {1, 2, 1},
     * {0, 1, 0}}
     * palette = {{0x00, 0x00, 0x00}, {0xFF, 0x00, 0x00}, {0x00, 0x00, 0xFF}}
     * This would result in a 3x3 image, with:
     * - a black background (palette[0]) (four pixels in each corner)
     * - a blue plus sign (palette[1]) (four pixels up, down, left, right)
     * - a red dot in the center (palette[2]) (middle pixel)
     * - For compression:
     * - The first number is a count and the second number is the color index number:
     * {44, 3, 52, 1} // this would draw forty-four palette[3] colors, followed by fifty-two palette[1] colors
     * - If a negative number is encountered, that pixel is just drawn (this is to compress {1, 3} cases, this would instead just be {-3}
     * - If a -120 is encountered, a very special case happens: It expects the number after it to be the number of rows to paint with the LAST color in the palette (typically transparent). This is to quickly compress transparent rows that may be used for rotations or logos.
     * { -120, 5 } // this would put 5 rows of the transparent color (whichever is 0x272727 in the palette, should be at the end)
     * <p>
     * I know this explanation is a bit messy, but you're really going to have to think about how you want this implementation
     * to go down in order to squeeze as many images into your size-restrained binary as possible.
     * I have two helper python scripts that I've been using to encode and compress the bitmaps:
     * - https://gist.github.com/vgmoose/1a6810aacc46c28344ab
     * - https://gist.github.com/vgmoose/0f80a9b6192096f7aac6
     **/

    // use short[][]s instead of unsigned/signed bytes due to primitive limitations
    // uses more memory, but loading images like this in Java is aleady ridiculous, so that's fine

    static short[][] title_palette = {{0x00, 0x00, 0x00}, {0xFF, 0xFF, 0xFF}, {0xF8, 0xF5, 0xF5}, {0xF6, 0xF5, 0xF5}, {0x9A, 0x68, 0x5F}, {0xBF, 0xA7, 0xA3}, {0xD5, 0xCA, 0xC8}, {0xF1, 0xEB, 0xEA}, {0xE6, 0xE0, 0xDF}, {0xA5, 0x86, 0x80}, {0xC9, 0xBA, 0xB7}, {0x7C, 0x35, 0x23}, {0x88, 0x4E, 0x41}, {0xDF, 0xD6, 0xD4}, {0x20, 0x07, 0x00}, {0x58, 0x2F, 0x23}, {0x80, 0x71, 0x6D}, {0x77, 0x1C, 0x00}, {0x68, 0x19, 0x00}, {0x65, 0x18, 0x00}, {0x61, 0x17, 0x00}, {0x5D, 0x16, 0x00}, {0x5A, 0x15, 0x00}, {0x54, 0x14, 0x00}, {0x4B, 0x12, 0x00}, {0x46, 0x11, 0x00}, {0x42, 0x0F, 0x00}, {0x3D, 0x0E, 0x00}, {0x32, 0x0C, 0x00}, {0x2C, 0x0A, 0x00}, {0x26, 0x09, 0x00}, {0x22, 0x08, 0x00}, {0x1D, 0x07, 0x00}, {0x1A, 0x06, 0x00}, {0x64, 0x4D, 0x46}, {0x38, 0x0E, 0x00}, {0x40, 0x2B, 0x23}, {0xA7, 0xA3, 0xA1}, {0xEC, 0xEB, 0xEA}, {0x27, 0x27, 0x27}};

    static short[] compressed_title = {-120, 11, 19, 39, 63, 1, 6, 39, 30, 1, 12, 39, 30, 1, 3, 39, 29, 1, 8, 39, 16, 39, 68, 1, 3, 39, 33, 1, 7, 39, 34, 1, -39, 32, 1, 6, 39, 15, 39, 3, 1, -13, -9, -4, 28, 17, -4, -1, -9, 28, 17, -11, -9, -7, 2, 1, 2, 39, -1, -7, 27, 17, -4, -5, -2, 2, 1, 5, 39, 3, 1, -13, -9, -11, 27, 17, -2, 3, 1, -10, -4, 25, 17, -12, -9, -7, 2, 1, 5, 39, 14, 39, 2, 1, -2, -4, 31, 17, -5, -1, -12, 31, 17, -13, 2, 1, -39, -1, -10, 29, 17, -12, -7, 2, 1, 2, 39, 3, 1, -8, -4, 29, 17, -4, 3, 1, -9, 29, 17, -11, -7, -1, 5, 39, 13, 39, 2, 1, -8, -11, 32, 17, -6, -8, 32, 17, -11, -7, 3, 1, -9, 30, 17, -4, 2, 1, -39, 3, 1, -5, 31, 17, -5, -1, -2, -4, 31, 17, -9, 2, 1, 4, 39, 13, 39, 2, 1, -12, 33, 17, -2, -10, 33, 17, -13, 3, 1, -11, 31, 17, 4, 1, -2, -4, 32, 17, -13, -1, -5, 32, 17, -4, 2, 1, 4, 39, 13, 39, -1, -10, 33, 17, -4, -1, -9, 33, 17, -13, 2, 1, -8, 32, 17, 3, 1, -2, -4, 33, 17, -1, -7, 33, 17, -9, 2, 1, 4, 39, 12, 39, 2, 1, -9, 33, 17, -5, -1, 34, 17, -2, 2, 1, -5, 31, 17, -4, 2, 1, -2, -4, 33, 17, -9, -1, -5, 33, 17, -10, -1, 5, 39, 12, 39, 2, 1, 34, 17, -8, -13, 33, 17, -4, 3, 1, -4, 31, 17, -5, 2, 1, -5, 34, 17, -10, -1, -12, 33, 17, -8, -1, 5, 39, 12, 39, -1, -13, 33, 17, -11, -1, -5, 33, 17, -5, 3, 1, 32, 17, -13, -1, -13, 35, 17, 2, 8, 33, 17, -12, 2, 1, 5, 39, 11, 39, 2, 1, -5, 33, 17, -9, -1, -4, 33, 17, -6, 2, 1, -13, 32, 17, 2, 1, -4, 34, 17, -11, -1, -5, 33, 17, -9, 2, 1, 5, 39, 11, 39, 2, 1, -4, 10, 17, -12, -6, -2, 21, 1, -2, 11, 17, 12, 5, -11, 10, 17, 23, 1, -6, 11, 17, -4, -1, -13, 13, 17, -4, 21, 5, -10, -1, -4, 10, 17, -9, 11, 5, -4, 10, 17, -10, -1, 6, 39, 11, 39, -1, -2, 11, 17, -8, 23, 1, -6, 10, 17, -9, 12, 1, -13, 9, 17, -4, 2, 1, -39, 21, 1, -4, 10, 17, -10, -1, -9, 11, 17, -4, -7, 23, 1, -2, 11, 17, 13, 1, -4, 9, 17, -7, -1, 6, 39, 11, 39, -1, -6, 10, 17, -12, 18, 13, 6, 1, -5, 10, 17, -10, 12, 1, -5, 9, 17, -5, 5, 1, -2, 17, 13, -6, -11, 10, 17, -8, -2, 11, 17, -4, -2, 24, 1, -6, 10, 17, -9, 12, 1, -2, -11, 8, 17, -4, 2, 1, 6, 39, 10, 39, 2, 1, -9, 30, 17, -9, -2, 3, 1, -12, 10, 17, -8, 10, 1, -2, -5, 10, 17, -8, 2, 1, -2, -5, -12, 29, 17, -11, -1, -10, 10, 17, -11, -2, 2, 1, 21, 39, 2, 1, -5, 10, 17, -10, 11, 1, -8, -4, 9, 17, -5, 2, 1, 6, 39, 10, 39, 2, 1, -12, 31, 17, -12, -2, -1, -7, 33, 17, -11, 2, 1, -8, -11, 31, 17, -9, -1, -9, 10, 17, -10, 2, 1, 22, 39, 2, 1, -12, 33, 17, -6, -1, 7, 39, 10, 39, -1, -7, 33, 17, -5, -1, -10, 33, 17, -9, -1, -13, -11, 32, 17, -10, -2, 10, 17, -12, 2, 1, 23, 39, -1, -7, 34, 17, -2, -1, 7, 39, 10, 39, -1, -10, 33, 17, -4, -1, -9, 33, 17, -10, -1, -12, 33, 17, -7, -6, 10, 17, -5, 2, 1, 23, 39, -1, -10, 33, 17, -4, 2, 1, 7, 39, 9, 39, 2, 1, -9, 33, 17, -5, -1, -11, 33, 17, -7, -13, 33, 17, -12, -1, -5, 10, 17, -7, -1, 23, 39, 2, 1, -9, 33, 17, -5, 2, 1, 7, 39, 9, 39, 2, 1, -4, 33, 17, -13, -8, 33, 17, -12, -1, -5, 33, 17, -9, -1, -4, 9, 17, -11, 2, 1, 23, 39, 2, 1, -11, 33, 17, -8, -1, 8, 39, 9, 39, 2, 1, -4, 33, 17, -1, -10, 33, 17, -13, -1, -4, 33, 17, -6, -2, 10, 17, -4, 2, 1, 23, 39, -1, -8, 33, 17, -9, 2, 1, 8, 39, 9, 39, 2, 1, -10, 32, 17, -4, -1, -9, 31, 17, -11, -6, -1, -2, 34, 17, -2, -10, 10, 17, -4, 2, 1, 23, 39, -1, -10, 32, 17, -9, 2, 1, 9, 39, 10, 39, 2, 1, -5, 31, 17, -10, -1, 30, 17, -11, -5, -2, 2, 1, -6, 9, 17, -11, -9, -6, 10, 13, -10, 10, 17, -4, -1, -9, 10, 17, -4, 2, 1, 22, 39, 2, 1, -9, 30, 17, -12, -10, 2, 1, 10, 39, 11, 39, 2, 1, -7, 20, 5, -4, 9, 17, -8, -13, 10, 17, -11, 17, 4, -9, -10, -2, 4, 1, -9, 9, 17, -10, 12, 1, -5, 10, 17, -5, -1, -12, 10, 17, -12, 2, 1, 22, 39, 2, 1, 11, 17, -9, 18, 5, -13, -2, 2, 1, 11, 39, 8, 39, 26, 1, -4, 8, 17, -11, -1, -5, 10, 17, -5, 21, 1, -39, 2, 1, -12, 9, 17, 13, 1, -4, 10, 17, -13, -1, 12, 17, -6, 24, 1, -13, 10, 17, -4, 22, 1, 12, 39, 7, 39, 25, 1, -2, -10, 9, 17, -9, -1, -4, 10, 17, -8, 18, 1, 4, 39, -1, -7, 10, 17, -5, 12, 13, 11, 17, 2, 1, 13, 17, -9, -6, 20, 13, -8, -1, -5, 11, 17, -8, 21, 1, 12, 39, 7, 39, 2, 1, -4, 33, 17, -10, -2, 10, 17, -11, 2, 1, 21, 39, -1, -10, 33, 17, -9, 2, 1, -4, 34, 17, -9, -1, -4, 11, 17, -12, -9, 18, 5, -13, -1, 12, 39, 7, 39, -1, -2, 34, 17, -7, -6, 10, 17, -9, 2, 1, 20, 39, 2, 1, -9, 33, 17, -10, 2, 1, -4, 34, 17, -10, -2, 32, 17, -13, -1, 12, 39, 7, 39, -1, -6, 33, 17, -12, -1, -9, 10, 17, -10, -1, 21, 39, 2, 1, -11, 33, 17, -7, 2, 1, -9, 34, 17, -7, -6, 32, 17, 2, 1, 12, 39, 6, 39, 2, 1, -5, 33, 17, -5, -1, -12, 10, 17, -7, -1, 21, 39, -1, -8, 33, 17, -4, 3, 1, -10, 33, 17, -4, -1, -9, 31, 17, -9, 2, 1, 12, 39, 6, 39, 2, 1, -4, 33, 17, -6, -7, 10, 17, -12, 2, 1, 20, 39, 2, 1, -5, 33, 17, -10, 3, 1, -7, 33, 17, -5, -1, -12, 31, 17, -10, -1, 13, 39, 6, 39, -1, -2, 33, 17, -12, -1, -10, 10, 17, -9, 2, 1, 20, 39, 2, 1, -5, 32, 17, -12, 2, 1, -39, 2, 1, -4, 32, 17, -6, -1, 32, 17, -8, -1, 13, 39, 6, 39, -1, -10, 33, 17, -10, -1, -9, 10, 17, -10, -1, 21, 39, 2, 1, -5, 32, 17, -13, 2, 1, -39, 2, 1, -13, 32, 17, -2, -1, -4, 30, 17, -11, 2, 1, 13, 39, 5, 39, 2, 1, -9, 32, 17, -10, 2, 1, -11, 10, 17, -2, -1, 22, 39, -1, -8, 30, 17, -11, -8, 2, 1, 3, 39, 2, 1, -9, 30, 17, -4, 2, 1, -8, 30, 17, -9, 2, 1, 13, 39, 5, 39, 2, 1, -12, 30, 17, -9, -13, 2, 1, -8, 10, 17, -4, 2, 1, 22, 39, 2, 1, -6, -12, 27, 17, -12, -8, 2, 1, 5, 39, 2, 1, -5, 29, 17, -5, 3, 1, -8, -4, 28, 17, -10, -1, 14, 39, 5, 39, 2, 1, 28, 5, -10, -13, -7, 4, 1, -8, 10, 5, -6, 2, 1, 23, 39, 3, 1, -13, -10, 23, 5, -10, -8, 3, 1, 7, 39, 2, 1, -7, -10, 27, 5, -7, -1, -39, 3, 1, -7, -10, 26, 5, -7, -1, 14, 39, 6, 39, 33, 1, 2, 39, 14, 1, 25, 39, 31, 1, 9, 39, 32, 1, 2, 39, 32, 1, 14, 39, 7, 39, 28, 1, 8, 39, 10, 1, 31, 39, 23, 1, 16, 39, 27, 1, 8, 39, 26, 1, 16, 39, -120, 4, 33, 39, 30, 1, 2, 39, 29, 1, 5, 39, 44, 1, 10, 39, 29, 1, 18, 39, 30, 39, 66, 1, 2, 39, 47, 1, 7, 39, 32, 1, 16, 39, 28, 39, 4, 1, -6, -9, -11, 27, 18, 3, 1, -9, 26, 18, -11, -9, -13, 4, 1, -9, 41, 18, -11, -9, -2, 2, 1, 5, 39, 2, 1, -10, -4, 25, 18, -12, -9, -7, 2, 1, 15, 39, 27, 39, 3, 1, -10, -12, 29, 19, -9, 3, 1, -12, 29, 19, -37, 3, 1, 44, 19, -11, -8, 2, 1, 3, 39, 2, 1, -9, 29, 19, -11, -7, -1, 15, 39, 26, 39, 2, 1, -7, -4, 31, 19, -10, 2, 1, -7, 31, 19, -13, -1, -13, 45, 19, -11, -2, -1, 2, 39, 2, 1, -9, 31, 19, -9, 2, 1, 14, 39, 25, 39, 2, 1, -8, -11, 32, 20, -8, 2, 1, -10, 31, 20, -5, -1, -5, 46, 20, -9, 4, 1, -5, 32, 20, -4, 2, 1, 14, 39, 24, 39, 2, 1, -8, -11, 32, 20, -11, 3, 1, -9, 31, 20, -5, -1, -12, 47, 20, -7, 2, 1, -2, 33, 20, -4, 2, 1, 14, 39, 23, 39, 2, 1, -7, -11, 33, 21, -9, 3, 1, -11, 31, 21, -10, -2, 48, 21, -10, 2, 1, -10, 33, 21, -5, 2, 1, 14, 39, 23, 39, 2, 1, -4, 34, 22, -10, 2, 1, -8, 32, 22, -7, -6, 48, 22, -9, 2, 1, -12, 33, 22, -8, -1, 15, 39, 22, 39, 2, 1, -10, 35, 22, -38, 2, 1, -10, 31, 22, -12, -1, -37, 48, 22, -16, -1, -38, 33, 22, -15, 2, 1, 15, 39, 22, 39, 2, 1, -34, 34, 23, -34, 3, 1, -9, 31, 23, -37, -1, -34, 48, 23, -16, -1, -10, 33, 23, -9, 2, 1, 15, 39, 21, 39, 2, 1, -10, 13, 23, -12, 21, 5, -6, 22, 1, -2, -9, 11, 23, -6, -38, 49, 23, -9, -1, -9, 10, 23, -9, 11, 5, -16, 10, 23, -10, -1, 16, 39, 21, 39, 2, 1, -12, 11, 23, -9, -38, 24, 1, 2, 39, 20, 1, -8, 11, 23, -2, -10, 11, 23, -10, 7, 1, -10, 10, 23, -16, 2, 1, -8, -13, -10, -12, 12, 23, -10, -1, 11, 23, -2, 12, 1, -12, 9, 23, -8, -1, 16, 39, 21, 39, -1, -8, 11, 24, -12, 25, 1, -39, 4, 1, -8, 17, 13, -9, 10, 24, -16, -1, -9, 11, 24, -38, 7, 1, -9, 10, 24, -5, 5, 1, -3, -34, 11, 24, -8, -13, 10, 24, -16, 12, 1, -3, -34, 8, 24, -34, 2, 1, 16, 39, 20, 39, 2, 1, -5, 10, 24, -34, -3, 3, 1, -34, 19, 24, -9, 3, 1, -8, -9, -15, 29, 24, -5, -1, -15, 10, 24, -34, 2, 1, 4, 39, 2, 1, -34, 10, 24, -8, -1, 3, 39, 2, 1, -37, 10, 24, -15, -1, -5, 10, 24, -5, 11, 1, -38, -16, 9, 24, -9, 2, 1, 16, 39, 20, 39, 2, 1, -34, 10, 24, -10, 3, 1, -38, 20, 24, -10, 2, 1, -37, 32, 24, 2, 8, 11, 24, -9, 2, 1, 4, 39, -1, -8, 10, 24, -15, 2, 1, 3, 39, 2, 1, -16, 10, 24, -9, -1, -34, 33, 24, -10, -1, 17, 39, 20, 39, -1, -38, 10, 25, -16, 2, 1, -39, -1, -10, 20, 25, -8, -1, -9, 32, 25, -15, -1, -10, 11, 25, -6, -1, 5, 39, -1, -10, 10, 25, -16, 2, 1, 3, 39, 2, 1, -15, 10, 25, -10, -3, 34, 25, -38, -1, 17, 39, 20, 39, -1, -10, 10, 25, -10, 4, 1, -9, 19, 25, -15, -1, -10, 33, 25, -16, -1, -16, 11, 25, -3, -1, 4, 39, 2, 1, -16, 10, 25, -10, -1, 4, 39, -1, -13, 11, 25, -38, -6, 33, 25, -34, 2, 1, 17, 39, 19, 39, 2, 1, -16, 9, 26, -15, -3, -1, -39, 2, 1, -15, 19, 26, -9, -1, -16, 33, 26, -5, -1, 11, 26, -34, 2, 1, 4, 39, 2, 1, -15, 10, 26, -38, -1, 3, 39, 2, 1, -37, 10, 26, -34, -1, -9, 33, 26, -37, 2, 1, 17, 39, 19, 39, 2, 1, -15, 9, 27, -16, 2, 1, -39, -1, -8, 20, 27, -10, -1, -15, 33, 27, -38, -13, 11, 27, -37, 2, 1, 4, 39, -1, -13, 10, 27, -34, 2, 1, 3, 39, 2, 1, -16, 10, 27, -37, -1, -34, 33, 27, -6, -1, 18, 39, 19, 39, -1, -8, 10, 27, -37, 2, 1, -39, -1, -5, 20, 27, -38, -13, 33, 27, -34, -1, -37, 11, 27, -13, -1, 4, 39, 2, 1, -37, 10, 27, -9, 2, 1, 3, 39, -1, -3, 11, 27, -6, -38, 33, 27, -16, 2, 1, 18, 39, 18, 39, 2, 1, -37, 10, 35, -37, 4, 1, -16, 19, 35, -34, -1, -37, 33, 35, -9, -1, -34, 11, 35, 2, 1, 4, 39, 2, 1, -34, 10, 35, -10, -1, 4, 39, -1, -6, 11, 35, -3, -10, 32, 35, -16, -3, 2, 1, 18, 39, 18, 39, 2, 1, -16, 10, 35, -37, 4, 1, 20, 35, -37, -1, -16, 9, 35, -34, -5, 11, 13, -34, 10, 35, -10, -3, 11, 35, -16, 2, 1, 4, 39, -1, -3, 11, 35, -3, -1, 3, 39, 2, 1, -37, 10, 35, -16, -1, -16, 30, 35, -36, -5, 3, 1, 19, 39, 18, 39, 2, 1, 11, 28, -9, 3, 1, -8, 9, 16, 11, 28, -6, -3, 9, 28, -34, -3, 12, 1, -36, 10, 28, -3, -6, 11, 28, -5, -1, 5, 39, -1, -6, 10, 28, -34, 2, 1, 3, 39, 2, 1, -34, 10, 28, -37, -1, -36, 10, 28, -16, 18, 37, -13, -3, 2, 1, 21, 39, 18, 39, 2, 1, 12, 28, -38, 11, 1, -38, 11, 28, -3, -6, 9, 28, -37, 12, 1, -8, 10, 28, -34, -1, -9, 11, 28, -8, -1, 4, 39, 2, 1, -37, 10, 28, -37, 2, 1, 3, 39, -1, -38, 11, 28, -13, -8, 10, 28, -36, 22, 1, 22, 39, 18, 39, 2, 1, 12, 28, -34, -37, 10, 13, -37, 10, 28, -34, -1, -37, 9, 28, -34, -6, 11, 13, -37, 10, 28, -37, -1, -34, 10, 28, -36, 2, 1, 4, 39, 2, 1, -34, 10, 28, -6, -1, 4, 39, -1, -10, 10, 28, -36, -1, -5, 11, 28, -8, 21, 1, 22, 39, 18, 39, 2, 1, 35, 29, -37, -1, -34, 33, 29, -6, -38, 11, 29, -9, 2, 1, 4, 39, -1, -38, 11, 29, 2, 1, 3, 39, 2, 1, -9, 10, 29, -16, -1, -16, 11, 29, -36, -9, 18, 37, -13, -1, 22, 39, 18, 39, 2, 1, -34, 34, 29, -13, -38, 34, 29, -1, -10, 11, 29, -10, -1, 5, 39, -1, -10, 10, 29, -16, 2, 1, 3, 39, 2, 1, -36, 10, 29, -5, -1, 32, 29, -6, -1, 22, 39, 18, 39, 2, 1, -16, 34, 30, -1, -10, 33, 30, -16, -1, -16, 11, 30, -38, -1, 4, 39, 2, 1, -9, 10, 30, -5, 2, 1, 3, 39, -1, -8, 11, 30, -38, -13, 32, 30, -3, -1, 22, 39, 18, 39, 2, 1, -37, 33, 30, -16, -1, -9, 33, 30, -10, -1, -36, 10, 30, -34, 2, 1, 4, 39, 2, 1, -36, 10, 30, -8, -1, 4, 39, -1, -5, 10, 30, -34, -1, -37, 31, 30, -34, 2, 1, 22, 39, 19, 39, -1, -13, 33, 31, -5, -1, -36, 32, 31, -36, -1, -8, 11, 31, -37, 2, 1, 4, 39, -1, -8, 10, 31, -36, 2, 1, 3, 39, 2, 1, -16, 10, 31, -9, -1, -16, 31, 31, -37, 2, 1, 22, 39, 19, 39, 2, 1, -34, 32, 14, -8, -1, 33, 14, -37, -1, -5, 11, 14, -6, -1, 5, 39, -1, -5, 10, 14, -16, 2, 1, 3, 39, 2, 1, 11, 14, -10, -1, -16, 31, 14, -13, -1, 23, 39, 19, 39, 2, 1, -10, 31, 32, -36, 2, 1, 32, 32, -34, 2, 1, -16, 11, 32, -3, -1, 4, 39, 2, 1, -16, 10, 32, -10, -1, 4, 39, -1, -13, 11, 32, -3, -1, -16, 30, 32, -36, 2, 1, 23, 39, 20, 39, 2, 1, -16, 30, 33, -9, 2, 1, -16, 30, 33, -16, -3, 2, 1, 11, 33, -34, 2, 1, 4, 39, 2, 1, 11, 33, -38, -1, 3, 39, 2, 1, -37, 10, 33, -34, 2, 1, -8, 30, 33, -16, 2, 1, 23, 39, 20, 39, 2, 1, -3, -16, 29, 33, -10, 2, 1, -3, -9, 28, 33, -37, 3, 1, -6, 11, 33, -37, 2, 1, 4, 39, -1, -13, 10, 33, -34, 2, 1, 3, 39, 2, 1, -34, 10, 33, -37, 3, 1, -8, -34, 28, 33, -5, -1, 24, 39, 21, 39, 3, 1, -8, -5, 27, 37, -38, 4, 1, -38, -13, 24, 37, -10, -38, 4, 1, -13, 11, 37, -38, -1, 5, 39, -1, -13, 10, 37, -10, 2, 1, 3, 39, 2, 1, 11, 37, -38, -1, -39, 3, 1, -38, -10, 26, 37, -38, -1, 24, 39, 23, 39, 32, 1, 2, 39, 30, 1, 2, 39, 15, 1, 5, 39, 14, 1, 5, 39, 14, 1, 2, 39, 32, 1, 24, 39, 26, 39, 27, 1, 7, 39, 24, 1, 7, 39, 11, 1, 9, 39, 10, 1, 8, 39, 11, 1, 8, 39, 26, 1, 26, 39, -120, 9};

    static short[] compressed_ship = {-120, 4, 17, 14, -2, 18, 14, 16, 14, -12, -13, -12, 17, 14, 15, 14, -2, -5, -12, -5, -2, 16, 14, 15, 14, -2, -5, -12, -5, -2, 16, 14, 14, 14, -2, -5, -3, -11, -3, -5, -2, 15, 14, 14, 14, -2, -5, -6, -11, -6, -5, -2, 15, 14, 14, 14, -2, -5, -6, -11, -6, -5, -2, 15, 14, 14, 14, -4, -3, -8, -11, -8, -3, -4, 15, 14, 14, 14, -4, -3, -6, -12, -6, -3, -4, 15, 14, 13, 14, -2, -5, -6, -12, -4, -12, -6, -5, -2, 14, 14, 13, 14, -2, -3, -6, -12, -3, -12, -6, -3, -2, 14, 14, 7, 14, 3, 12, 2, 14, -2, -5, -3, -12, -5, -7, -5, -12, -3, -5, -2, 2, 14, 3, 12, 8, 14, 6, 14, -12, 2, 11, -10, -12, -14, -2, -5, -3, -12, -5, -7, -5, -12, -3, -5, -2, -14, -12, -10, 2, 11, -12, 7, 14, 6, 14, -12, -10, -9, -12, -11, 2, 13, -2, -13, -12, -4, -6, -4, -12, -13, -2, 2, 13, -11, -12, -9, -10, -12, 7, 14, 6, 14, -12, -11, -10, -12, -11, -13, 2, 14, -2, -13, -2, -3, -2, -13, -2, 2, 14, -13, -11, -12, -10, -11, -12, 7, 14, 7, 14, 2, 12, -13, -12, 2, 14, -12, -11, -2, -12, -2, -12, -2, -11, -12, 2, 14, -12, -13, 2, 12, 8, 14, 6, 14, -12, -9, -7, -9, -11, -14, -12, -13, -10, -5, -6, -7, -6, -5, -10, -13, -12, -14, -11, -9, -7, -9, -12, 7, 14, 6, 14, -12, -11, -9, -10, -11, -12, 2, 13, -10, -5, -8, -7, -8, -5, -10, 2, 13, -12, -11, -10, -9, -11, -12, 7, 14, 6, 14, -12, -11, -10, -11, -2, -11, -10, 2, 9, -5, -3, -7, -3, -5, 2, 9, -10, -11, -2, -11, -10, -11, -12, 7, 14, 7, 14, -12, -11, -2, -3, -12, -11, 2, 10, -5, -6, -7, -6, -5, 2, 10, -11, -12, -5, -2, -11, -12, 8, 14, 7, 14, -12, -2, -5, -3, -12, -11, -10, -9, -10, -5, -6, -5, -10, -9, -10, -11, -12, -3, -5, -2, -12, 8, 14, 6, 14, 2, 2, -5, -3, -6, -12, -11, -9, -7, -9, -5, -3, -5, -9, -7, -9, -11, -12, -6, -3, -5, 2, 2, 7, 14, 5, 14, -2, -5, -3, 2, 4, -2, -12, -11, -10, -7, -9, -11, -5, -11, -9, -7, -10, -11, -12, -2, 2, 4, -3, -5, -2, 6, 14, 5, 14, -2, -3, -4, 2, 8, 2, 2, -12, -11, -10, 2, 11, -14, 2, 11, -10, -11, -12, 2, 2, 2, 8, -4, -3, -2, 6, 14, 5, 14, 5, 2, 2, 14, 2, 12, 2, 11, -12, -14, -12, 2, 11, 2, 12, 2, 14, 5, 2, 6, 14, 12, 14, -13, -4, -5, -2, 3, 14, -2, -5, -4, -13, 13, 14, 12, 14, 2, 2, -4, -2, 3, 14, -2, -4, 2, 2, 13, 14, -120, 5};

    static short[][] ship_palette = {{0xFF, 0xFF, 0xFF}, {0x89, 0x01, 0x1D}, {0xFF, 0x94, 0x6A}, {0xCC, 0x3E, 0x00}, {0xD9, 0x67, 0x00}, {0xFD, 0xCA, 0x31}, {0xFF, 0xFA, 0xEA}, {0x1C, 0x5B, 0xF0}, {0xBE, 0xBE, 0xBE}, {0x94, 0x94, 0x94}, {0x6B, 0x6B, 0x6B}, {0x40, 0x40, 0x40}, {0x1F, 0x1F, 0x1F}, {0xFF, 0xFF, 0xFF}, {0x27, 0x27, 0x27}};

    static short[] compressed_enemy = {-120, 3, 15, 9, 4, 0, 4, 9, 14, 9, 0, -8, -7, -8, 0, 4, 9, 13, 9, 0, -8, -6, -7, -6, 0, 4, 9, 12, 9, 0, -8, -6, -8, -7, 2, 0, 4, 9, 6, 9, 3, 0, 2, 9, 0, -8, -6, 5, 0, 4, 9, 4, 9, 2, 0, -8, -7, -8, 0, -8, -7, -6, 5, 0, 5, 9, 4, 9, 0, 2, 7, -6, 3, 0, -2, 5, 0, 6, 9, 4, 9, 2, 0, 4, 2, 0, 4, 2, 2, 0, 6, 9, 3, 9, -8, -3, -2, 4, 1, -5, 4, 1, -2, -6, -8, 5, 9, 3, 9, 0, -4, -2, -1, -5, -8, -1, -2, -1, -5, -8, -1, -2, -4, 0, 5, 9, 3, 9, -8, -3, -7, -1, -8, -7, -1, -2, -1, -8, -7, -1, -7, -3, -8, 5, 9, 4, 9, 0, -7, 4, 1, -6, 4, 1, -8, 0, 6, 9, 5, 9, 0, -8, 2, 2, -5, -4, -5, 2, 2, 2, 8, 7, 9, 6, 9, -8, -6, -4, -6, -7, -3, -5, -3, -8, 8, 9, 7, 9, 0, -8, -4, -5, -4, -8, 0, 9, 9, 9, 9, 0, -8, 0, 11, 9, -120, 4};

    static short[][] enemy_palette = {{0x00, 0x00, 0x00}, {0xFF, 0xFF, 0xFF}, {0x87, 0x51, 0x53}, {0xC0, 0x85, 0x87}, {0xFF, 0xBE, 0xC0}, {0xC1, 0xC1, 0xC1}, {0x87, 0x87, 0x87}, {0x53, 0x53, 0x53}, {0x26, 0x26, 0x26}, {0x27, 0x27, 0x27}};

    static short[] compressed_ship2 = {-120, 2, 17, 5, 2, 4, 17, 5, 17, 5, 2, 4, 17, 5, 17, 5, 2, 4, 17, 5, 17, 5, 2, 4, 17, 5, 17, 5, 2, 4, 17, 5, 17, 5, 2, 4, 17, 5, 15, 5, 6, 4, 15, 5, 15, 5, 6, 4, 15, 5, 15, 5, 6, 4, 15, 5, 15, 5, 6, 4, 15, 5, 9, 5, 2, 3, 4, 5, 6, 4, 4, 5, 2, 3, 9, 5, 9, 5, 2, 3, 4, 5, 6, 4, 4, 5, 2, 3, 9, 5, 9, 5, 2, 3, 4, 5, 6, 4, 4, 5, 2, 3, 9, 5, 9, 5, 2, 3, 4, 5, 6, 4, 4, 5, 2, 3, 9, 5, 9, 5, 2, 4, 2, 5, 10, 4, 2, 5, 2, 4, 9, 5, 9, 5, 2, 4, 2, 5, 10, 4, 2, 5, 2, 4, 9, 5, 3, 5, 2, 3, 4, 5, 2, 4, 2, 2, 4, 4, 2, 3, 4, 4, 2, 2, 2, 4, 4, 5, 2, 3, 3, 5, 3, 5, 2, 3, 4, 5, 2, 4, 2, 2, 4, 4, 2, 3, 4, 4, 2, 2, 2, 4, 4, 5, 2, 3, 3, 5, 3, 5, 2, 3, 4, 5, 2, 2, 4, 4, 6, 3, 4, 4, 2, 2, 4, 5, 2, 3, 3, 5, 3, 5, 2, 3, 4, 5, 2, 2, 4, 4, 6, 3, 4, 4, 2, 2, 4, 5, 2, 3, 3, 5, 3, 5, 2, 4, 4, 5, 6, 4, 2, 3, 2, 4, 2, 3, 6, 4, 4, 5, 2, 4, 3, 5, 3, 5, 2, 4, 4, 5, 6, 4, 2, 3, 2, 4, 2, 3, 6, 4, 4, 5, 2, 4, 3, 5, 3, 5, 2, 4, 2, 5, 22, 4, 2, 5, 2, 4, 3, 5, 3, 5, 2, 4, 2, 5, 22, 4, 2, 5, 2, 4, 3, 5, 3, 5, 10, 4, 2, 3, 6, 4, 2, 3, 10, 4, 3, 5, 3, 5, 10, 4, 2, 3, 6, 4, 2, 3, 10, 4, 3, 5, 3, 5, 6, 4, 2, 5, 4, 3, 6, 4, 4, 3, 2, 5, 6, 4, 3, 5, 3, 5, 6, 4, 2, 5, 4, 3, 6, 4, 4, 3, 2, 5, 6, 4, 3, 5, 3, 5, 4, 4, 4, 5, 4, 3, 2, 5, 2, 4, 2, 5, 4, 3, 4, 5, 4, 4, 3, 5, 3, 5, 4, 4, 4, 5, 4, 3, 2, 5, 2, 4, 2, 5, 4, 3, 4, 5, 4, 4, 3, 5, 3, 5, 2, 4, 12, 5, 2, 4, 12, 5, 2, 4, 3, 5, 3, 5, 2, 4, 12, 5, 2, 4, 12, 5, 2, 4, 3, 5, -120, 2};

    static short[][] ship2_palette = {{0x00, 0x00, 0x00}, {0xFF, 0xFF, 0xFF}, {0xCC, 0x66, 0x00}, {0x00, 0x00, 0xFF}, {0xDD, 0xDD, 0xDD}, {0x27, 0x27, 0x27}};

    static short[] compressed_boss = {-120, 4, 16, 39, -33, 2, 20, -19, 2, 34, 14, 39, 14, 39, -34, -33, -20, 2, 21, 2, 33, -32, 2, 34, -28, -34, -25, 2, 3, -30, 6, 39, 11, 39, -33, -37, -34, -18, -33, 2, 21, -20, 3, 33, -37, 2, 29, -37, 3, 6, -33, -28, -3, -34, 3, 39, 3, 39, 3, 38, -5, 2, 32, -30, -33, -16, -37, -5, -37, -33, -21, -20, -14, 2, 33, -19, -37, 2, 35, -36, -3, 2, 34, -7, -33, -2, -33, -32, -26, 1, 39, -39, -38, 5, 11, -31, -34, -30, 2, 20, -28, -12, -32, -33, -16, -23, -22, -33, -21, -19, -3, 2, 9, -30, 2, 33, -4, -7, -33, -4, 2, 33, -37, 1, 39, -39, 3, 13, -11, -13, -31, -28, -19, -14, -21, 2, 20, -33, 2, 16, -21, -22, -20, -33, 2, 20, -28, -35, -36, -10, -6, -7, 2, 6, -33, -29, -20, -33, -3, 1, 39, -39, 4, 13, -26, -28, -15, -16, -33, -22, -23, -21, -16, 4, 20, -22, -21, 2, 22, -15, -24, -27, -10, -6, -7, 2, 6, -2, -30, -20, -33, -37, 1, 39, -39, 3, 13, -11, -3, -28, -19, -16, -20, -22, -25, -23, -21, 2, 20, 2, 16, -20, -19, -21, -22, -20, -15, -17, -2, -34, -6, -33, -2, -29, -24, -33, -28, -26, 1, 39, -39, 3, 13, -30, -18, -15, -16, -20, -21, -23, -25, -23, -21, 2, 16, 2, 20, -16, -21, -22, -21, 2, 20, 2, 16, 2, 19, -15, -18, -14, -18, -14, -37, -35, 1, 39, -39, 3, 13, -29, -28, -15, -16, -21, -22, -23, -25, -21, -22, -20, -16, 2, 21, -22, -23, -22, -21, 5, 20, -16, -19, -15, -18, -28, -17, -30, -36, 1, 39, -38, 3, 13, -37, -28, -15, -20, 3, 22, -23, 2, 20, -34, -33, -22, 3, 23, -22, -21, 3, 20, -16, 2, 19, -15, -28, -14, -17, -26, -27, 2, 39, -38, 2, 13, -11, -29, 2, 28, -20, -21, -22, -33, -22, -20, -28, -25, -3, -21, 2, 25, -23, -22, -21, -20, -21, -16, -15, 3, 18, -17, 2, 29, -30, -36, 2, 39, -11, 2, 13, -11, -26, -18, -28, 2, 20, -21, -34, 2, 22, -34, -38, -30, -21, -23, -25, -23, -22, 2, 21, -20, -15, -18, -14, -28, -14, -24, -17, -29, -35, 3, 39, -11, 2, 13, -11, -30, -17, -28, -33, -19, -20, -29, -21, -23, -34, -8, -31, -21, 2, 23, -22, 2, 21, -20, -19, 2, 18, -28, -18, 2, 17, -29, 2, 27, 3, 39, -11, 3, 13, -38, -29, -18, 2, 15, -19, -34, -33, -22, -15, -29, -8, -16, -20, 2, 22, -21, -20, -19, -15, -19, -15, -18, -17, -24, -26, 2, 27, 4, 39, -11, 3, 13, -38, -26, -17, -18, -28, 2, 19, -37, -21, -16, -14, -24, -19, -16, -20, -21, -20, 2, 19, 2, 15, -18, -17, -29, -26, -30, -27, 5, 39, -11, 2, 13, -9, -11, -30, -29, -14, -18, -28, 2, 18, -17, -16, -19, -15, -19, -16, 2, 19, -15, -17, -18, 2, 14, -17, -26, -30, -27, -30, 6, 39, -39, -13, 2, 9, -13, -38, -26, -17, -18, 2, 14, 2, 18, -24, 2, 15, 2, 19, -15, -28, 3, 14, -17, 2, 29, 3, 26, 7, 39, -39, 4, 9, -13, -30, -24, 5, 14, -18, 5, 15, -14, 2, 17, -29, -26, 2, 27, -26, -29, 8, 39, -39, -13, 4, 9, -35, -30, -24, 4, 17, 2, 18, 3, 15, -18, -29, -27, -35, 2, 38, -31, -34, -26, 9, 39, -39, -13, 5, 9, -36, -30, -26, 3, 29, -17, -14, 3, 17, -29, -35, 4, 11, -9, -31, 10, 39, -39, -11, 6, 9, -13, 2, 11, 2, 38, -30, 4, 26, -30, -38, -11, -1, -12, -8, -31, -38, 10, 39, -39, -11, -13, 8, 9, 2, 36, -11, 2, 38, -30, -26, -34, -11, 3, 8, -32, -31, -36, 10, 39, 2, 39, -13, 10, 9, -36, -11, 3, 38, -5, -12, -1, -12, -8, -31, -5, 11, 39, 2, 39, -13, 12, 9, -13, 3, 39, -38, -5, 2, 8, -32, 12, 39, 3, 39, 11, 9, -13, 21, 39, 3, 39, 11, 9, 22, 39, 5, 39, 7, 9, -36, 23, 39, 6, 39, 5, 9, -36, 24, 39, 8, 39, 3, 9, 25, 39, -120, 2};

    static short[][] boss_palette = {{0x00, 0x00, 0x00}, {0xFF, 0xFF, 0xFF}, {0x2D, 0x29, 0x52}, {0x3D, 0x39, 0x57}, {0x36, 0x2D, 0x59}, {0x2E, 0x2D, 0x32}, {0x6C, 0x56, 0x96}, {0x83, 0x6B, 0xAA}, {0x7A, 0x77, 0x7F}, {0x0D, 0x0C, 0x0E}, {0x2C, 0x14, 0x40}, {0x16, 0x13, 0x17}, {0xB7, 0xB1, 0xB8}, {0x13, 0x10, 0x13}, {0x29, 0x39, 0x59}, {0x35, 0x48, 0x6D}, {0x43, 0x59, 0x84}, {0x26, 0x34, 0x51}, {0x2F, 0x3F, 0x60}, {0x3D, 0x50, 0x78}, {0x4E, 0x62, 0x8E}, {0x59, 0x6C, 0x97}, {0x62, 0x75, 0x9F}, {0x6D, 0x80, 0xA8}, {0x21, 0x2E, 0x4D}, {0x7E, 0x8C, 0xAE}, {0x1F, 0x26, 0x39}, {0x12, 0x18, 0x2A}, {0x37, 0x42, 0x64}, {0x24, 0x2B, 0x43}, {0x1B, 0x1E, 0x30}, {0x37, 0x39, 0x44}, {0x56, 0x59, 0x6C}, {0x51, 0x56, 0x7F}, {0x47, 0x4A, 0x65}, {0x0F, 0x0F, 0x1E}, {0x0C, 0x0C, 0x14}, {0x30, 0x30, 0x4C}, {0x1A, 0x1A, 0x21}, {0x27, 0x27, 0x27}};

    static short[] compressed_boss2 = {-120, 7, 9, 39, -3, 2, 6, -9, -4, -5, 3, 11, -6, -3, 16, 39, 7, 39, 2, 5, -13, -4, -5, 2, 13, -9, -37, -17, -29, -26, -19, -24, -27, -33, -29, 12, 39, 4, 39, -9, -13, -17, 3, 11, -13, -10, -3, -24, -3, -31, -30, -20, -21, -20, -19, -24, -31, 2, 27, -29, -26, 9, 39, 3, 39, -4, -16, -15, -11, -15, 2, 17, -14, -7, -24, -12, -31, -23, -38, -19, -22, -20, -25, 5, 35, -31, -33, -29, -15, 6, 39, 2, 39, -6, -5, -15, 2, 11, -8, 2, 17, -13, -12, -20, -10, -29, 2, 26, -30, -20, -21, -25, 3, 35, 2, 31, -35, -23, -31, -33, 6, 39, -39, -32, -5, -17, 4, 11, 2, 17, -13, -12, -21, -6, -25, -36, -15, -34, -19, -20, 2, 19, -24, -25, -26, -25, 2, 33, -29, -27, -25, -37, 4, 39, -39, -5, 2, 13, 4, 11, 3, 17, -25, -18, -27, -30, -38, -14, -34, -20, 3, 19, -20, -30, -25, -33, 2, 36, -30, -31, 2, 30, 2, 29, 2, 39, -39, -5, -13, -17, -15, 3, 11, -13, -14, -13, -26, -22, -32, -24, -27, -16, -15, -19, -24, 3, 21, -24, -25, -27, 2, 36, -17, -36, -25, -35, -30, -33, -29, 1, 39, -3, -7, -4, -13, -11, 2, 17, 4, 13, -10, 2, 18, 2, 21, -37, -14, 4, 22, -21, -32, -31, -33, -37, -26, -37, -26, -38, 2, 30, -25, -30, 1, 39, -7, -6, -7, -13, -11, -17, -5, -13, -5, -4, -13, -36, -32, 2, 18, -21, -16, -8, -31, -18, 3, 21, -27, -33, -23, -29, -37, -29, -37, -38, -27, -30, 3, 25, 2, 7, -6, -7, -10, -13, -10, -9, -4, -13, -14, -13, -28, -21, -24, -33, -34, -23, -20, -36, 2, 32, -22, -27, -31, -32, -37, -29, -37, 2, 38, -35, -25, -38, 2, 25, -5, -10, -6, -12, -10, -7, 2, 10, -4, -14, -16, -13, -32, -20, -30, -23, 2, 37, -21, -33, -27, -24, -32, -31, -23, -32, -26, 2, 37, -36, -25, -35, -30, -25, -30, 1, 27, -5, -25, -3, -6, -7, 3, 10, -4, -14, -15, -36, -18, 2, 30, -26, -37, -29, -20, -21, 3, 24, -30, -35, -23, 2, 17, -36, -30, 2, 28, -31, -27, -28, 1, 39, -10, -6, -3, -12, 3, 10, -6, -14, 2, 16, -9, -22, -6, -23, -34, -16, -29, -20, -21, -20, -24, -32, -31, -30, -27, 4, 23, -33, -27, -31, 2, 23, 1, 39, -39, -19, -3, -7, 2, 6, -9, -10, 3, 16, -26, -22, -9, -28, -29, -37, -33, -22, -18, 3, 32, -28, -31, 2, 33, -31, -33, 2, 30, 2, 25, -26, 2, 39, -39, 2, 3, -10, 2, 26, -9, -5, 2, 16, -15, -10, -18, -25, -28, -30, -29, -28, -22, -21, 2, 28, -32, 4, 28, 3, 23, -33, 5, 39, -39, -3, -26, -6, 2, 9, -14, 3, 16, -15, -13, -28, -20, -27, -29, -28, -18, -22, -27, 2, 32, 4, 28, 3, 23, 7, 39, 2, 39, 2, 6, -9, -5, -4, 3, 16, 2, 11, -26, -30, -29, -33, -13, -23, -21, 3, 32, 2, 28, -23, 2, 33, 9, 39, 2, 39, -2, 2, 6, -5, -4, -16, -17, -16, 2, 15, 2, 16, -37, -34, -17, -32, -20, -32, 2, 28, -35, -31, 2, 33, 10, 39, 3, 39, -3, -9, -4, -16, -34, -14, -16, -15, -11, 4, 15, -34, -23, -24, -31, -35, 2, 31, 2, 33, 11, 39, 4, 39, 2, 6, -4, -9, -4, -14, -16, 2, 15, -11, -15, 2, 11, -13, -36, -6, -27, -31, -33, 13, 39, 5, 39, -3, -4, -6, 2, 4, -9, -14, -17, -11, -8, -11, -16, -33, -30, -25, -32, -28, 14, 39, 8, 39, -2, -3, -6, 2, 4, -5, -17, -36, 2, 38, -24, -32, 16, 39, -120, 6};

    static short[][] boss2_palette = {{0x00, 0x00, 0x00}, {0xFF, 0xFF, 0xFF}, {0x4A, 0x6E, 0x81}, {0x35, 0x5B, 0x78}, {0x25, 0x3C, 0x4F}, {0x19, 0x33, 0x4A}, {0x2C, 0x4B, 0x68}, {0x18, 0x37, 0x57}, {0x0A, 0x13, 0x1D}, {0x2D, 0x44, 0x5D}, {0x24, 0x3F, 0x60}, {0x10, 0x1C, 0x2B}, {0x1B, 0x3B, 0x67}, {0x18, 0x2A, 0x46}, {0x25, 0x32, 0x47}, {0x1B, 0x23, 0x30}, {0x21, 0x2B, 0x3C}, {0x16, 0x23, 0x3D}, {0x74, 0x8F, 0xC4}, {0x3A, 0x56, 0x92}, {0x5A, 0x77, 0xB4}, {0x65, 0x84, 0xC4}, {0x7B, 0x97, 0xD5}, {0x63, 0x72, 0x9B}, {0x53, 0x67, 0xA4}, {0x42, 0x51, 0x80}, {0x3C, 0x49, 0x70}, {0x52, 0x62, 0x94}, {0x6B, 0x7A, 0xA7}, {0x57, 0x62, 0x85}, {0x4C, 0x5A, 0x8A}, {0x5B, 0x69, 0x99}, {0x70, 0x7F, 0xB4}, {0x5C, 0x68, 0x92}, {0x3A, 0x41, 0x58}, {0x62, 0x6D, 0xA3}, {0x24, 0x2C, 0x56}, {0x4F, 0x54, 0x73}, {0x2B, 0x31, 0x6E}, {0x27, 0x27, 0x27}};
}
