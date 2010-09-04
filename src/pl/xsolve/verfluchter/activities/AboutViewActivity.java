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

package pl.xsolve.verfluchter.activities;

import android.os.Bundle;
import android.webkit.WebView;
import pl.xsolve.verfluchter.R;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Konrad Ktoso Malawski
 */
public class AboutViewActivity extends CommonViewActivity {

    @InjectView(R.id.about_webview)
    WebView aboutText;

    @InjectResource(R.string.about_text)
    String about;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.about);

        aboutText.loadData(about, "text/html", UTF_8.name());
    }
}
