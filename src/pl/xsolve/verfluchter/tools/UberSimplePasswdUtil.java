package pl.xsolve.verfluchter.tools;

/**
 * Be warned, this is absolutely NOT a real security measure! ;-)
 * Just some randomly googled pseudo-encryption class...
 * Think of it as an placeholder for some real encryption mechanism ;-)
 */
public class UberSimplePasswdUtil implements PasswdUtil {
    static final String key = "v1GHuGJGidNuPYDmjX5OU4ccGO0moifqH3RYpFsGS4N2CwqLH42GTjAnmPuf5Qk";

    public String encrypt(String str) {
        StringBuffer sb = new StringBuffer(str);

        int lenStr = str.length();
        int lenKey = key.length();

        // For each character in our string, encrypt it...
        for (int i = 0, j = 0; i < lenStr; i++, j++) {
            if (j >= lenKey) {
                j = 0;  // Wrap 'round to beginning of key string.
            }
            // XOR the chars together. Must cast back to char to avoid compile error.
            sb.setCharAt(i, (char) (str.charAt(i) ^ key.charAt(j)));
        }

        return sb.toString();
    }

    public String decrypt(String str) {
        // To 'decrypt' the string, simply apply the same technique.
        return encrypt(str);
    }

}
