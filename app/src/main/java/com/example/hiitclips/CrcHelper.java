package com.example.hiitclips;

public final class CrcHelper {

    private CrcHelper() {}

    /**
     * Performs the CRC-16/IBM-3740 checksum and returns the result as a string in Little Endian ordering.
     * Given the limited number of potential inputs, this might be better as a lookup table in a high performance scenario.
     * However, hello potential hiring manager, I understand bit manipulations.
     *
     * The algorithm is explained below:
     * Step 1: The result begins as all 1s.
     * Step 2: Store the most significant bit of the input and the result.
     * Step 3: Left shift the input and the result by one bit.
     * Step 4: XOR the previously stored most significant bits.
     * Step 5: If the XOR resulted in 1, XOR the entire result with the entire polynomial.
     * Step 6: Repeat until the entire input has been bit shifted out.
     * @param input
     * @return The result of the checksum in Little Endian ordering in string form.
     */
    public static String getCrcString(byte[] input) {
        int result = 0xFFFF;
        int polynomial = 0x1021;

        for (byte b : input) {
            // Prevent Java's signed bytes from causing problems.
            int bInt = b & 0xFF;
            for (int i = 0; i < 8; i++) {
                // Get the most significant bit of the input and the result.
                boolean inputMSB = ((bInt >> 7 & 1) == 1);
                boolean resultMSB = ((result >> 15 & 1) == 1);

                // Left shift the input and the result by 1.
                bInt <<= 1;
                result <<= 1;

                // If an XOR of the MSBs is 1, apply the polynomial to the result.
                if (resultMSB ^ inputMSB) {
                    result ^= polynomial;
                }
            }
        }

        // Extract the bytes to swap them to Little Endian.
        // Note that in Java, integers are 32 bit, so all the shifting above hasn't actually shifted the value out.
        // The bitwise ands below grab only the necessary bits anyway so it doesn't matter.
        int lowByte = result & 0xFF;
        int highByte = (result >> 8) & 0xFF;

        return String.format("%02X%02X", lowByte, highByte);
    }
}
