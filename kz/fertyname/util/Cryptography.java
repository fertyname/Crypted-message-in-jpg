package kz.fertyname.util;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Cryptography {
    public static String dataEncrypto(String data) {
        StringBuilder sb = new StringBuilder(data).reverse();
        String reverseString = sb.toString();
        byte[] messageByte = reverseString.getBytes(StandardCharsets.UTF_8);
        for (int i = 1; i < messageByte.length; i++) {
            messageByte[i] ^= messageByte[i - 1];
        }
        return new String(Base64.getEncoder().encode(messageByte), StandardCharsets.UTF_8);
    }

    public static String dataDecrypt(String encryptedData) {
        byte[] messageByte = Base64.getDecoder().decode(encryptedData);
        for (int i = messageByte.length - 1; i > 0; i--) {
            messageByte[i] ^= messageByte[i - 1];
        }
        String reversedString = new String(messageByte, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(reversedString);
        return sb.reverse().toString();
    }

    public static void encode(String inputPath, String outputPath, String message) throws Exception {
        byte[] messageBytes = message.getBytes("UTF-8");
        int messageLength = messageBytes.length;

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(new byte[] {
                (byte) (messageLength >> 24),
                (byte) (messageLength >> 16),
                (byte) (messageLength >> 8),
                (byte) messageLength
        });
        byteStream.write(messageBytes);

        byte[] dataToHide = byteStream.toByteArray();

        File inputFile = new File(inputPath);
        BufferedImage image = Imaging.getBufferedImage(inputFile);

        int width = image.getWidth();
        int height = image.getHeight();

        int messageBitIndex = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                for (int colorIndex = 0; colorIndex < 3; colorIndex++) {
                    if (messageBitIndex < dataToHide.length * 8) {
                        int colorValue = (pixel >> (8 * colorIndex)) & 0xFF;
                        int bit = (dataToHide[messageBitIndex / 8] >> (7 - (messageBitIndex % 8))) & 1;
                        colorValue = (colorValue & 0xFE) | bit;
                        pixel = (pixel & ~(0xFF << (8 * colorIndex))) | (colorValue << (8 * colorIndex));
                        messageBitIndex++;
                    }
                }

                image.setRGB(x, y, pixel);
                if (messageBitIndex >= dataToHide.length * 8) {
                    break;
                }
            }
            if (messageBitIndex >= dataToHide.length * 8) {
                break;
            }
        }

        File outputFile = new File(outputPath);
        Imaging.writeImage(image, outputFile, ImageFormats.PNG);
    }

    public static String decode(String inputPath) throws Exception {
        File inputFile = new File(inputPath);
        BufferedImage image = Imaging.getBufferedImage(inputFile);

        int width = image.getWidth();
        int height = image.getHeight();

        int messageLength = 0;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int currentByte = 0;
        int bitCount = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                for (int colorIndex = 0; colorIndex < 3; colorIndex++) {
                    int colorValue = (pixel >> (8 * colorIndex)) & 0xFF;
                    int bit = colorValue & 1;

                    currentByte = (currentByte << 1) | bit;
                    bitCount++;

                    if (bitCount == 8) {
                        byteStream.write(currentByte);
                        currentByte = 0;
                        bitCount = 0;

                        if (byteStream.size() == 4 && messageLength == 0) {
                            byte[] lengthBytes = byteStream.toByteArray();
                            messageLength = ((lengthBytes[0] & 0xFF) << 24)
                                    | ((lengthBytes[1] & 0xFF) << 16)
                                    | ((lengthBytes[2] & 0xFF) << 8)
                                    | (lengthBytes[3] & 0xFF);
                            byteStream.reset();
                        } else if (byteStream.size() == messageLength) {
                            return new String(byteStream.toByteArray(), "UTF-8");
                        }
                    }
                }
            }
        }

        throw new IllegalArgumentException("Message could not be fully decoded");
    }
}
