package wtf.color.spellcheck;

public class CaseCorrector {
    public String applyOriginalCasePattern(String original, String corrected) {
        StringBuilder result = new StringBuilder();
        int minLength = Math.min(original.length(), corrected.length());

        for (int i = 0; i < minLength; i++) {
            char originalChar = original.charAt(i);
            char correctedChar = corrected.charAt(i);

            if (Character.isUpperCase(originalChar)) {
                result.append(Character.toUpperCase(correctedChar));
            } else {
                result.append(Character.toLowerCase(correctedChar));
            }
        }

        if (corrected.length() > original.length()) {
            result.append(corrected.substring(original.length()).toLowerCase());
        }

        return result.toString();
    }
}
