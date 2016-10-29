package ahoy.maksimgolivkin.myapplication;

import android.app.Application;

import com.github.instacart.ahoy.AhoySingleton;
import com.github.instacart.ahoy.retrofit2.Retrofit2Delegate;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        AhoySingleton.init(this, new Retrofit2Delegate("http://192.168.1.17:3000", true));
    }
}