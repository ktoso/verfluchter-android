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

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: ktoso
 * Date: 2010-09-18
 * Time: 21:48:43
 * To change this template use File | Settings | File Templates.
 */
class OMG{
    int lol;
}

enum enz{
    lol(":"){

    }, lollol("df"){

    },sdfsdfds("sdf"){
        void hahaha(){

        }
    };

    enz(String sdf){

    }

    void hahaha(){

    }
}

public class Test extends OMG {
    int lol;
    private int prov;

    public static void main(String[] args) {
        TreeSet<String> treeSet = new TreeSet<String>();
        treeSet.add("df");
        treeSet.pollFirst();

        enz.sdfsdfds.hahaha();

        new Test().metoda();
    }

    public void metoda(){
        class Doh {
            int hhh;
            private int pooooo;
            Doh() {
                int omg = prov;
                hhh = lol;
            }
        }

//        static class omgBezSensu{
//
//        }

        int dsfsdf = new Doh().pooooo;

    }

    class Fud{
        int omg;

        Fud() {
            int sss = prov;
            this.omg = Test.this.lol;
            Test.super.lol = lol;
        }
    }
}