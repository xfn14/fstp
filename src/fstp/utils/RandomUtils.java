package fstp.utils;

import java.util.Random;
import java.util.UUID;

public class RandomUtils {
    /**
     * Generate a random UUID string
     * 
     * @return Random UUID string
     */
    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate a random integer between min and max
     * 
     * @param min Minimum value
     * @param max Maximum value
     * @return Random integer between min and max
     */
    public static int randomInt(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return new Random().nextInt(max - min) + min;
    }
}
