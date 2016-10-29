package ahoy.maksimgolivkin.myapplication;

import android.app.Application;

import ahoy.maksimgolivkin.myapplication.ahoy.AhoySingleton;
import ahoy.maksimgolivkin.myapplication.ahoy.retrofit2.Retrofit2Delegate;

/**
 * TODO:
 *
 * # Read url from AndroidManifest?
 * # Rewrite json handling with Gson
 * # Remove observables and do everything through simple callback
 * # Method to explicitly register visit and pass utm params
 x # Make singleton a separate class and make all methods available through the instance for dependency injection
 * # Write AhoyRx.visits -- stream that replaces the listeners
 x # Retry/reconnect policy for network requests
 */

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        AhoySingleton.init(this, new Retrofit2Delegate("http://192.168.1.17:3000", true));
    }
}
