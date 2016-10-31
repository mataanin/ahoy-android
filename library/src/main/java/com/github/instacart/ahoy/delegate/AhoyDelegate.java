package com.github.instacart.ahoy.delegate;

import android.support.annotation.NonNull;

import com.github.instacart.ahoy.Visit;

public interface AhoyDelegate {

    interface Callback {
        void onSuccess(@NonNull Visit visit);
        void onFailure(Throwable throwable);
    }

    /***
     * New visitor token generator.
     *
     * @return Unique string identifying current client.
     */
    String newVisitorToken();

    /**
     * Start a new visit and save extra parameters.
     *
     * @param params Visit information & extra parameters (such as utm parameters, see {@link Visit}).
     * @param callback Callback reporting success or failure & returning new visit.
     */
    void saveVisit(VisitParams params, Callback callback);

    /**
     * Save extra parameters and optionally start a new visit.
     *
     * @param params Visit information & extra parameters (such as utm parameters, see {@link Visit}).
     * @param callback Callback reporting success or failure & returning updated or new visit.
     */
    void saveExtras(VisitParams params, Callback callback);
}

