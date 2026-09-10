package sh.kamath.url_service.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator codeGenerator = new ShortCodeGenerator("01");

    @Test
    void encodeZerosCorrectly() {
        long largeValue = 0;
        String encodedValue = codeGenerator.generateShortCode(largeValue);
        String base62EncodedLargeValue = "001";
        assertEquals(base62EncodedLargeValue, encodedValue);
    }

    @Test
    void encodeLargeValueCorrectly() {
        long largeValue = 20056732418480L;
        String encodedValue = codeGenerator.generateShortCode(largeValue);
        String base62EncodedLargeValue = "5H6OVfj201";
        assertEquals(base62EncodedLargeValue, encodedValue);
    }

    @Test
    void encodesOneAsFirstNonZeroCharacter() {
        // ID 1 should encode to the character at position 1 in your alphabet
        // (which is "1" if alphabet starts with 0-9)
        String encoded = codeGenerator.generateShortCode(1L);
        assertEquals("101", encoded);   // "1" + node "01"
    }

    @Test
    void encodesSixtyTwoAsRollover() {
        // 62 in base62 is "10" — first rollover
        String encoded = codeGenerator.generateShortCode(62L);
        assertEquals("1001", encoded);   // "10" + node "01"
    }

    @Test
    void appendsNodeIdSuffix() {
        // Sanity check that node ID always appears at the end
        String encoded = codeGenerator.generateShortCode(5L);
        assertTrue(encoded.endsWith("01"));
    }

}