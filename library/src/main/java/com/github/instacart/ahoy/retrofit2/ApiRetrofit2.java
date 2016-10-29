package com.github.instacart.ahoy.retrofit2;

import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface ApiRetrofit2 {

    @POST("/ahoy/visits")
    Observable<VisitResponse> registerVisit(@Body Map<String, Object> body);
}
