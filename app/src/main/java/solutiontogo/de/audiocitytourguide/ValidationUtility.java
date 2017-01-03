package solutiontogo.de.audiocitytourguide;

/**
 * Created by shivaramak on 02/01/2017.
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtility {

        private static Pattern pattern;
        private static Matcher matcher;
    //Email Pattern
    private static final String EMAIL_PATTERN ="^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

    /**
         * Validate Email with regular expression
         *
         * @param email
         * @return true for Valid Email and false for Invalid Email
         */
        public static boolean validate(String email) {
            pattern = Pattern.compile(EMAIL_PATTERN);
            matcher = pattern.matcher(email);
            return matcher.matches();

        }
        /**
         * Checks for Null String object
         *
         * @param txt
         * @return true for not null and false for null String object
         */
        public static boolean isNotNull(String txt){
            return txt!=null && txt.trim().length()>0 ? true: false;
        }
    }


