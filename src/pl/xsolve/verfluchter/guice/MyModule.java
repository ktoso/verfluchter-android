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

import pl.xsolve.verfluchter.tools.AutoSettings;
import pl.xsolve.verfluchter.tools.AutoSettingsImpl;
import pl.xsolve.verfluchter.tools.PasswdUtil;
import pl.xsolve.verfluchter.tools.UberSimplePasswdUtil;
import roboguice.config.AbstractAndroidModule;
import roboguice.inject.SharedPreferencesName;

/**
 * @author Konrad Ktoso Malawski
 */
public class MyModule extends AbstractAndroidModule {

    @Override
    protected void configure() {
        // core stuff
        bind(AutoSettings.class).to(AutoSettingsImpl.class);

        // utils
        bind(PasswdUtil.class).to(UberSimplePasswdUtil.class);

        // BUG need a better way to set default preferences context
        bindConstant().annotatedWith(SharedPreferencesName.class).to("pl.xsolve.verfluchter");
    }
}
