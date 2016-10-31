package com.github.instacart.ahoy.delegate.retrofit2;

import android.net.Uri;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.instacart.ahoy.Visit;
import com.github.instacart.ahoy.delegate.AhoyDelegate;
import com.github.instacart.ahoy.delegate.VisitParams;
import com.github.instacart.ahoy.utils.TypeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    private static final long VISIT_DURATION = TimeUnit.MILLISECONDS.convert(15, TimeUnit.SECONDS);
    private final ApiRetrofit2 api;

    private static final String[] utmParams = {
            Visit.UTM_CAMPAIGN,
            Visit.UTM_CONTENT,
            Visit.UTM_MEDIUM,
            Visit.UTM_SOURCE,
            Visit.UTM_TERM
    };

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

    private void makeRequest(String visitToken, final VisitParams visitParams, final Callback callback) {
        Map<String, Object> request = new HashMap<>();
        request.put(Visit.OS, Visit.OS_ANDROID);
        request.put(Visit.VISIT_TOKEN, visitToken);
        request.put(Visit.VISITOR_TOKEN, visitParams.visitorToken());
        request.putAll(visitParams.extraParams());

        Uri.Builder builder = new Uri.Builder();
        for (String key : utmParams) {
            Object value = visitParams.extraParams().get(key);
            if (value instanceof CharSequence && !TypeUtil.isEmpty((CharSequence) value)) {
                builder = builder.appendQueryParameter(key, Uri.encode(value.toString()));
            }
        }

        Uri landingParams = builder.build();
        if (!TypeUtil.isEmpty(landingParams.toString())) {
            request.put(Visit.LANDING_PAGE, landingParams.toString());
        }

        api.registerVisit(request)
                .compose(RxBackoff.<VisitResponse>backoff())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<VisitResponse>() {
                    @Override public void call(VisitResponse visitResponse) {
                        long expiresAt = System.currentTimeMillis() + VISIT_DURATION;
                        callback.onSuccess(Visit.create(visitResponse.visitId(), visitParams.extraParams(), expiresAt));
                    }
                }, new Action1<Throwable>() {
                    @Override public void call(Throwable throwable) {
                        callback.onFailure(throwable);
                    }
                });
    }

    @Override public String newVisitorToken() {
        return randomUUID().toString();
    }

    @Override public void saveVisit(VisitParams visitParams, Callback callback) {
        final String visitorToken = visitParams.visitorToken();
        if (TypeUtil.isEmpty(visitorToken)) {
            throw new IllegalArgumentException("Please provide visit & visitor token");
        }

        String visitToken = UUID.randomUUID().toString();
        makeRequest(visitToken, visitParams, callback);
    }

    @Override public void saveExtras(VisitParams visitParams, Callback callback) {
        String visitToken = visitParams.visitToken();
        if (TypeUtil.isEmpty(visitToken) || TypeUtil.isEmpty(visitParams.visitorToken())) {
            throw new IllegalArgumentException("Please provide visit & visitor token");
        }
        makeRequest(visitToken, visitParams, callback);
    }
}
