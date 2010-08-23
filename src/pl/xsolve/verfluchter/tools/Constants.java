package pl.xsolve.verfluchter.tools;

/**
 * @author Konrad Ktoso Malawski
 */
public class Constants {

    public static final String VERSION = "0.1.3";
    public static final String USER_AGENT = "Verfluchter Android " + VERSION;

    // HTTP response codes
    public static final int HTTP_OK = 200; //OK
    public static final int HTTP_FOUND = 302; //Found, is also "ok"
    public static final int HTTP_BAD_REQUEST = 400; // Bad Request
    public static final int HTTP_UNAUTHORIZED = 401; // Unauthorized
    public static final int HTTP_FORBIDDEN = 403; // Forbidden
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_I_AM_A_TEAPOT = 418; // I'm a teapot
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500; // Internal Server Error
    public static final int HTTP_NOT_IMPLEMENTED = 501; // Not Implemented
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505; // HTTP Version Not Supported

    public static final int HTTP_SERVICE_UNAVAILABLE = 503; // Service Unavailable

    public static final String DATE_FORMAT_YYYYMMDD = "yyyy-MM-dd";

    public static final String[] weekDays = new String[]{"Poniedziałek", "Wtorek", "Środa",
            "Czwartek", "Piątek", "Sobota", "Niedziela"};

    public static class DEFAULT {
        public static final String SERVER_DOMAIN = "verfluchter.xsolve.pl";
        public static final String BASIC_AUTH_USER = "user";
        public static final String BASIC_AUTH_PASS = "";
        public static final Boolean USE_REMINDER_SERVICE = false;
        public static final Boolean USE_REFRESHER_SERVICE = false;
        public static final Integer WORKING_HOURS_START_HOUR = 8;
        public static final Integer WORKING_HOURS_START_MIN = 0;
        public static final Integer WORKING_HOURS_END_HOUR = 16;
        public static final Integer WORKING_HOURS_END_MIN = 0;

        private DEFAULT() {
        }
    }

    private Constants() {
    }

}
