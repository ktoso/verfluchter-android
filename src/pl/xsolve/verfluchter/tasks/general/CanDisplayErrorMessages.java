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

package pl.xsolve.verfluchter.tasks.general;

/**
 * Used by classes that allow to display error messages
 *
 * @author Konrad Ktoso Malawski
 */
public interface CanDisplayErrorMessages {

    /**
     * Display some error message.
     * The message will disappear after some time or some kind of user interaction.
     *
     * @param error the error message to be displayed
     */
    void showErrorMessage(final String error);
}
