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

import android.util.Pair;

/**
 * @author Konrad Ktoso Malawski
 */
public class HourMin {
    private int hour;
    private int min;

    public HourMin(int hour, int min) {
        this.hour = hour;
        this.min = min;
    }

    public HourMin addMin(int minutes) {
        min += 1;
        if (min >= 60) { //thought I hope > will never happen ;-)
            hour += 1;
            min = 0;
        }
        return this;
    }

    public HourMin addHour(int hours) {
        hour += hours;
        return this;
    }

    public String print() {
        if (hour == 0) {
            return String.format("%dm", min);
        } else {
            return String.format("%dh %dm", hour, min);
        }
    }
}
