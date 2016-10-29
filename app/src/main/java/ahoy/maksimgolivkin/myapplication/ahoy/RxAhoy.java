package ahoy.maksimgolivkin.myapplication.ahoy;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class RxAhoy {

    public static class VisitorListenerOnSubscribe implements Ahoy.VisitListener, OnSubscribe<Visit> {

        private final Ahoy ahoy;
        List<Subscriber<? super Visit>> subscribers = new ArrayList<>();

        public VisitorListenerOnSubscribe(Ahoy ahoy) {
            this.ahoy = ahoy;
        }

        @Override public synchronized void call(final Subscriber<? super Visit> subscriber) {
            if (subscriber.isUnsubscribed()) {
                return;
            }

            if (subscribers.size() == 0) {
                ahoy.addVisitListener(this);
                return;
            }

            subscriber.add(Subscriptions.create(new Action0() {
                @Override public void call() {
                    synchronized(VisitorListenerOnSubscribe.this) {
                        subscribers.remove(subscriber);
                        if (subscribers.size() == 0) {
                            ahoy.removeVisitListener(VisitorListenerOnSubscribe.this);
                        }
                    }
                }
            }));

            subscribers.add(subscriber);
        }

        @Override public void onVisitUpdated(Visit visit) {
            List<Subscriber<? super Visit>> listCopy = new ArrayList<>(subscribers);
            for (Subscriber subscriber : listCopy) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(visit);
                }
            }
        }
    }

    private RxAhoy() {
    }

    public static Observable<Visit> stream(Ahoy ahoy) {
        return Observable.create(new VisitorListenerOnSubscribe(ahoy));
    }
}