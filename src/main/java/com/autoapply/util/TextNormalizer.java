package com.autoapply.util;

public final class TextNormalizer {

    private TextNormalizer() {}

    /**
     * Lowercase + strip punctuation + suffix-strip lemmatization.
     * Handles common English inflections for keyword matching.
     */
    public static String normalize(String input) {
        if (input == null) return "";
        String s = input.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
        return lemmatize(s);
    }

    private static String lemmatize(String word) {
        // Order matters — check longer suffixes first
        if (word.endsWith("ization")) return word.substring(0, word.length() - 7);
        if (word.endsWith("ations"))  return word.substring(0, word.length() - 6);
        if (word.endsWith("nesses"))  return word.substring(0, word.length() - 5);
        if (word.endsWith("ments"))   return word.substring(0, word.length() - 5);
        if (word.endsWith("ation"))   return word.substring(0, word.length() - 5);
        if (word.endsWith("ness"))    return word.substring(0, word.length() - 4);
        if (word.endsWith("ment"))    return word.substring(0, word.length() - 4);
        if (word.endsWith("tion"))    return word.substring(0, word.length() - 4);
        if (word.endsWith("ing"))     return word.substring(0, word.length() - 3);
        if (word.endsWith("ers"))     return word.substring(0, word.length() - 2);
        if (word.endsWith("ed"))      return word.substring(0, word.length() - 2);
        if (word.endsWith("ly"))      return word.substring(0, word.length() - 2);
        if (word.endsWith("er"))      return word.substring(0, word.length() - 2);
        if (word.endsWith("es"))      return word.substring(0, word.length() - 2);
        if (word.endsWith("s") && word.length() > 3) return word.substring(0, word.length() - 1);
        return word;
    }
}
