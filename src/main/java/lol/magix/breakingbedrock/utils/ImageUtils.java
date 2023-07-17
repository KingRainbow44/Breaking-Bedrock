package lol.magix.breakingbedrock.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface ImageUtils {
    /**
     * Get the width of an image.
     *
     * @param file The image file.
     * @return The width of the image.
     */
    static int getSize(File file) {
        try {
            return ImageIO.read(file).getWidth();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Divides an image into multiple images.
     *
     * @param sourceImage The source image.
     * @param divideAt The height of each image.
     * @return A list of images.
     */
    static List<BufferedImage> divide(File sourceImage, int divideAt)
            throws IOException {
        var image = ImageIO.read(sourceImage);

        // Read the image dimensions.
        var width = image.getWidth();
        var height = image.getHeight();

        // Calculate the number of images to create.
        var rowCount = (int) Math.ceil((double) height / divideAt);

        // Divide the images.
        var images = new ArrayList<BufferedImage>();
        for (var row = 0; row < rowCount; row++) {
            // Calculate the start & end coordinates.
            var startY = row * divideAt;
            var endY = Math.min(startY + divideAt, height);
            // Get the sub image representing the current row.
            images.add(image.getSubimage(0, startY, width, endY - startY));
        }

        return images;
    }

    /**
     * Crops an image.
     * Removes all transparent pixels from the top and bottom of the image.
     *
     * @param image The image to crop.
     * @return The cropped image.
     */
    static BufferedImage crop(BufferedImage image) {
        // Get the dimensions of the image.
        var width = image.getWidth();
        var height = image.getHeight();

        // Find the coordinates of the non-transparent pixels.
        var minY = height;
        var maxY = 0;
        for (var y = 0; y < height; y++) {
            var foundNonTransparentPixel = false;
            for (var x = 0; x < width; x++) {
                var pixel = image.getRGB(x, y);
                if ((pixel >> 24) != 0) { // Check if the pixel is not transparent.
                    foundNonTransparentPixel = true;
                    break;
                }
            }

            if (foundNonTransparentPixel) {
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }

        return image.getSubimage(0, minY, width, maxY - minY + 1);
    }

    /**
     * Saves an image to a file.
     *
     * @param file The file to save the image to.
     * @param image The image to save.
     */
    static void saveImage(File file, BufferedImage image)
            throws IOException {
        ImageIO.write(image, "png", file);
    }
}
