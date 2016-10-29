package ahoy.maksimgolivkin.myapplication;

import android.app.Application;

import ahoy.maksimgolivkin.myapplication.ahoy.AhoySingleton;
import ahoy.maksimgolivkin.myapplication.ahoy.retrofit2.Retrofit2Delegate;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        AhoySingleton.init(this, new Retrofit2Delegate("http://192.168.1.17:3000", true));
    }
}
