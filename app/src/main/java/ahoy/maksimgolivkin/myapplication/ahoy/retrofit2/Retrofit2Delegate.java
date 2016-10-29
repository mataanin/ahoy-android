package ahoy.maksimgolivkin.myapplication.ahoy.retrofit2;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ahoy.maksimgolivkin.myapplication.ahoy.AhoyDelegate;
import ahoy.maksimgolivkin.myapplication.ahoy.Visit;
import ahoy.maksimgolivkin.myapplication.ahoy.VisitConsts;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static java.util.UUID.randomUUID;

public class Retrofit2Delegate implements AhoyDelegate {

    private static final String TAG = "retrofit-delegate";

    private static final long VISIT_DURATION = TimeUnit.MILLISECONDS.convert(45, TimeUnit.SECONDS);
    private final ApiRetrofit2 api;

    public Retrofit2Delegate(String baseUrl, boolean loggingEnabled) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(loggingEnabled ? Level.BODY : Level.NONE);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(150, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();
        api = retrofit.create(ApiRetrofit2.class);
    }

    @Override public String newVisitorToken() {
        return randomUUID().toString();
    }

    @Override public Observable<Visit> newVisit(String visitorToken) {
        String visitToken = UUID.randomUUID().toString();
        Visit visit = Visit.create(visitToken, System.currentTimeMillis() + VISIT_DURATION);
        return Observable.just(visit);
    }

    @Override public void registerVisit(final String visitorToken, final Visit visit, Map<String, Object> extraParams) {
        Map<String, Object> request = new HashMap<>();
        request.put(VisitConsts.OS, VisitConsts.OS_ANDROID);
        request.put(VisitConsts.VISIT_TOKEN, visit.visitToken());
        request.put(VisitConsts.VISITOR_TOKEN, visitorToken);
        request.putAll(extraParams);

        api.registerVisit(request)
                .compose(RxBackoff.<VisitResponse>backoff())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<VisitResponse>() {
            @Override public void call(VisitResponse visitResponse) {
                    Log.d(TAG, String.format("registered visitor %s visit %s", visitorToken, visit.visitToken()));
                }
            }, new Action1<Throwable>() {
                @Override public void call(Throwable throwable) {
                    throwable.printStackTrace();

                    String message = throwable.getMessage();
                    Log.d(TAG, String.format("failed registering visitor %s, reason:", visitorToken, message));
                }
            });
    }

    @Override public void updateVisit(String visitorToken, Visit visit, Map<String, Object> additionalParams) {




    }
}