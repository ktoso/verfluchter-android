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

package pl.xsolve.verfluchter.guice;

import com.google.inject.Module;
import roboguice.application.GuiceApplication;

import java.util.List;

/**
 * The "Application" class used to instanciate RoboGuice
 *
 * @author Konrad Ktoso Malawski
 */
public class MyApplication extends GuiceApplication {

    @Override
    protected void addApplicationModules(List<Module> modules) {
        modules.add(new MyModule());
    }
}
