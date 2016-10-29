package com.github.instacart.ahoy.retrofit2;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.instacart.ahoy.AhoyDelegate;
import com.github.instacart.ahoy.Visit;
import com.github.instacart.ahoy.VisitConsts;
import com.github.instacart.ahoy.VisitParams;
import com.github.instacart.ahoy.utils.TypeUtil;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
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

    @Override public void newVisit(VisitParams visitParams, Callback callback) {
        String visitToken = UUID.randomUUID().toString();
        Visit visit = Visit.create(visitToken, System.currentTimeMillis() + VISIT_DURATION);
        callback.onSuccess(visit);
    }

    @Override public void updateVisit(final VisitParams visitParams, Callback callback) {
        final String visitToken = visitParams.visitToken();
        final String visitorToken = visitParams.visitorToken();

        if (TypeUtil.isEmpty(visitToken) || TypeUtil.isEmpty(visitorToken)) {
            throw new IllegalArgumentException("Please provide visit & visitor token");
        }

        Map<String, Object> request = new HashMap<>();
        request.put(VisitConsts.OS, VisitConsts.OS_ANDROID);
        request.put(VisitConsts.VISIT_TOKEN, visitToken);
        request.put(VisitConsts.VISITOR_TOKEN, visitorToken);
        request.putAll(visitParams.extraParams());

        api.registerVisit(request)
                .compose(RxBackoff.<VisitResponse>backoff())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<VisitResponse>() {
            @Override public void call(VisitResponse visitResponse) {
                    Log.d(TAG, String.format("registered visitor %s visit %s", visitorToken, visitToken));
                }
            }, new Action1<Throwable>() {
                @Override public void call(Throwable throwable) {
                    throwable.printStackTrace();

                    String message = throwable.getMessage();
                    Log.d(TAG, String.format("failed registering visitor %s, reason:", visitorToken, message));
                }
            });
    }
}
