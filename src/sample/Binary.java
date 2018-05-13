package sample;
/*FLOREA IONUT 342A3*/

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Binary
{
    /* Imaginea in niveluri de gri */
    private static BufferedImage grayscale;
    /* Imaginea in binar */
    private static BufferedImage binarized;

    public static BufferedImage getGrayscale()
    {
        return grayscale;
    }

    public static void setGrayscale(BufferedImage grayscale)
    {
        Binary.grayscale = grayscale;
    }

    public static BufferedImage getBinarized()
    {
        return binarized;
    }

    public static void setBinarized(BufferedImage binarized)
    {
        Binary.binarized = binarized;
    }

    public static void writeImage(File outputFile) throws IOException
    {
        ImageIO.write(binarized, "bmp", outputFile);
    }

    /* Conversia imaginii din niveluri de gri in binar */
    public static void binarize(int threshold)
    {
        int red;
        int newPixel;

        binarized = new BufferedImage(grayscale.getWidth(), grayscale.getHeight(), grayscale.getType());

        for (int i = 0; i < grayscale.getWidth(); i++)
        {
            for (int j = 0; j < grayscale.getHeight(); j++)
            {
                // Preiau pixeli
                red = new Color(grayscale.getRGB(i, j)).getRed();
                if (red > threshold)
                {
                    newPixel = 255;
                } else
                {
                    newPixel = 0;
                }
                newPixel = colorToRGB(newPixel, newPixel, newPixel);
                binarized.setRGB(i, j, newPixel);
            }
        }
    }

    /* Conversie R, G, B la standard 8 bit */
    public static int colorToRGB(int red, int green, int blue)
    {
        int newPixel = 0;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;
    }

    /* Calculare histograma niveluri de gri */
    private static int[] imageHistogram(BufferedImage input)
    {
        int[] histogram = new int[256];

        for (int i = 0; i < histogram.length; i++) histogram[i] = 0;

        for (int i = 0; i < input.getWidth(); i++)
        {
            for (int j = 0; j < input.getHeight(); j++)
            {
                int red = new Color(input.getRGB(i, j)).getRed();
                histogram[red]++;
            }
        }

        return histogram;
    }

    /* Calcularea celui mai bun prag de conversie */
    public static int automaticThreshold(BufferedImage original)
    {
        int[] histogram = imageHistogram(original);
        int total = original.getHeight() * original.getWidth();

        float sum = 0;
        for (int i = 0; i < 256; i++)
            sum += i * histogram[i];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++)
        {
            wB += histogram[i];
            if (wB == 0) continue;
            wF = total - wB;

            if (wF == 0) break;

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax)
            {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;
    }

}
