package com.github.instacart.ahoy;

import com.github.instacart.ahoy.delegate.AhoyDelegate.Callback;

import java.util.ArrayList;
import java.util.List;

import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

class VisitCallbackOnSubscribe implements Callback, OnSubscribe<Visit> {

    List<Subscriber<? super Visit>> subscribers = new ArrayList<>();

    public VisitCallbackOnSubscribe() {
    }

    @Override public void call(final Subscriber<? super Visit> subscriber) {
        if (subscriber.isUnsubscribed()) {
            return;
        }
        subscriber.add(Subscriptions.create(new Action0() {
            @Override public void call() {
                subscribers.remove(subscriber);
            }
        }));
        subscribers.add(subscriber);
    }

    @Override public void onSuccess(Visit visit) {
        List<Subscriber<? super Visit>> listCopy = new ArrayList<>(subscribers);
        for (Subscriber<? super Visit> subscriber : listCopy) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(visit);
                subscriber.onCompleted();
            }
        }
    }

    @Override public void onFailure(Throwable throwable) {
        List<Subscriber<? super Visit>> listCopy = new ArrayList<>(subscribers);
        for (Subscriber<? super Visit> subscriber : listCopy) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(throwable);
            }
        }
    }
}
