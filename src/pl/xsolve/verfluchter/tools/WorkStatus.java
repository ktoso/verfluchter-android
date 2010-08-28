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

import pl.xsolve.verfluchter.R;

/**
 * Date: 2010-08-28
 *
 * @author Konrad Malawski
 */
public enum WorkStatus {
    NOT_YET_WORKING(R.string.NOT_YET_WORKING),
    YOU_SHOULD_START_WORKING(R.string.YOU_SHOULD_START_WORKING),
    WORKING(R.string.WORKING),
    YOU_SHOULD_STILL_BE_WORKING(R.string.YOU_SHOULD_STILL_BE_WORKING),
    YOU_CAN_STOP_WORKING(R.string.YOU_CAN_STOP_WORKING),
    NOT_WORKING(R.string.NOT_WORKING);

    public final int contextTitle;
    public final int contentText;

    WorkStatus(int contentText) {
        this.contextTitle = R.string.app_name;
        this.contentText = contentText;
    }
}
