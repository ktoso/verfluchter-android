/*
 * This file is part of verfluchter-android.
 *
 * verfluchter-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * verfluchter-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

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
