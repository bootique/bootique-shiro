package io.bootique.shiro.web.jwt.token;

import io.jsonwebtoken.io.IOException;

import java.util.Arrays;
import java.util.List;

public class JwtTokenClaim {

    private final String name;
    private final ClaimStringValueParser parser;

    JwtTokenClaim(String name, String regexp) {
        this.name = name;
        if (regexp == null) {
            regexp = ClaimStringValueParser.STRING_LIST_REGEXP;
        }
        this.parser = new ClaimStringValueParser(regexp);
    }

    public String getName() {
        return name;
    }

    public List<String> parse(Object claimValue) {
        return parser.parse(claimValue);
    }


    public static class ClaimStringValueParser {

        private static final String STRING_REGEXP = "string";
        private static final String STRING_LIST_REGEXP = String.format("%s:%s", STRING_REGEXP, "list");
        private static final String SINGLE_STRING_REGEXP = String.format("%s:%s", STRING_REGEXP, "single");
        private static final String SPACE_SEPARATED_STRING_LIST_REGEXP = String.format("%s:%s", STRING_REGEXP, "space");

        private final String regexp;

        private ClaimStringValueParser(String regexp) throws IllegalArgumentException {
            if (regexp.startsWith(STRING_REGEXP)) {
                if (regexp.equals(STRING_LIST_REGEXP) || regexp.equals(SINGLE_STRING_REGEXP)) {
                    this.regexp = regexp;
                } else if (regexp.equals(SPACE_SEPARATED_STRING_LIST_REGEXP)) {
                    this.regexp = " ";
                } else {
                    this.regexp = regexp.trim().substring(STRING_REGEXP.length() + 1);
                }
            } else {
                throw new IllegalArgumentException("Claim String Value Parser regexp must have format \"string:<regexp>\"");
            }
        }

        @SuppressWarnings("unchecked")
        public List<String> parse(Object value) throws IOException {
            try {
                if (STRING_LIST_REGEXP.equals(this.regexp)) {
                    return (List<String>) value;
                } else if (SINGLE_STRING_REGEXP.equals(this.regexp)) {
                    return List.of((String) value);
                } else {
                    return Arrays.asList(((String) value).split(this.regexp));
                }
            } catch (Exception e) {
                throw new IOException("Unable to parse claim value", e);
            }
        }
    }
}
