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
 * @author Konrad Ktoso Malawski
 */
public interface AutoSettings {
    String SERVER_DOMAIN_S = "SERVER_DOMAIN_S";
    String MY_AUTH_USER_S = "MY_AUTH_USER_S";
    String MY_AUTH_PASS_S = "MY_AUTH_PASS_S";
    String BASIC_AUTH_USER_S = "BASIC_AUTH_USER_S";
    String BASIC_AUTH_PASS_S = "BASIC_AUTH_PASS_S";
    String USE_REMINDER_SERVICE_B = "USE_REMINDER_SERVICE_B";
    String USE_REFRESHER_SERVICE_B = "USE_REFRESHER_SERVICE_B";
    String WORKING_HOURS_START_HOUR_I = "WORKING_HOURS_START_HOUR_I";
    String WORKING_HOURS_START_MIN_I = "WORKING_HOURS_START_MIN_I";
    String WORKING_HOURS_END_HOUR_I = "WORKING_HOURS_END_HOUR_I";
    String WORKING_HOURS_END_MIN_I = "WORKING_HOURS_END_MIN_I";
    String USE_SOUND_B = "USE_SOUND_B";

    void restoreSettings();

    void persistSettings();

    @SuppressWarnings("unchecked")
    <T> T getSetting(String key, Class<T> clazz);

    <T> void setSetting(String key, T value);

    String print();
}
