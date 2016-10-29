package ahoy.maksimgolivkin.myapplication.ahoy;

import java.util.ArrayList;
import java.util.List;

import ahoy.maksimgolivkin.myapplication.ahoy.AhoyDelegate.Callback;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class VisitCallbackOnSubscribe
        implements Callback, OnSubscribe<Visit> {

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
