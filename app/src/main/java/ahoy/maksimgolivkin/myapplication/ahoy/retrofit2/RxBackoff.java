package ahoy.maksimgolivkin.myapplication.ahoy.retrofit2;

import java.util.concurrent.TimeUnit;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.functions.Func1;

public class RxBackoff {

    private static final int DEFAULT_RECONNECT_ATTEMPTS = 3;

    static class RetryFunc1 implements Func1<Throwable, Observable<Long>> {

        private int mAttempts;
        private int mRetryCounter = 0;

        public RetryFunc1(int attempts) {
            mAttempts = attempts;
        }

        @Override public Observable<Long> call(final Throwable throwable) {

            if (!isRetryNeeded(throwable)) {
                return Observable.error(throwable);
            }

            if (mRetryCounter++ >= mAttempts) {
                return Observable.error(throwable);
            }

            return Observable.timer(getDelayTime(mRetryCounter - 1, mAttempts), TimeUnit.MILLISECONDS);
        }

        private int getDelayTime(int currentRetry, int maxRetries) {
            // exponential back-off
            return (int) (Math.pow(2, currentRetry) * 100);
        }

        private boolean isRetryNeeded(Throwable throwable) {

            if (!(throwable instanceof HttpException)) {
                return false;
            }

            int status = ((HttpException) throwable).code();
            if (isStatusWorthRetry(status)) {
                return true;
            }

            return false;
        }

        private boolean isStatusWorthRetry(int status) {
            return status == 0 || status == 500 || status == 501 || status == 503 || status == 504;
        }
    }

    private RxBackoff() {
    }

    public static <T> Observable.Transformer<T, T> backoff() {
        return backoff(DEFAULT_RECONNECT_ATTEMPTS - 1);
    }

    /**
     * Source: https://gist.github.com/sddamico/c45d7cdabc41e663bea1
     *
     * @param retryAttempts The max number of attempts to retry this task or -1 to try MAX_INT times,
     */
    public static <T> Observable.Transformer<T, T> backoff(final int retryAttempts) {
        return new Observable.Transformer<T, T>() {
            @Override public Observable<T> call(final Observable<T> observable) {
                return observable.retryWhen(retryFunc(retryAttempts));
            }
        };
    }

    /**
     *
     * Source: https://gist.github.com/sddamico/c45d7cdabc41e663bea1
     */
    private static Func1<? super Observable<? extends Throwable>, ? extends Observable<?>> retryFunc(
            final int attempts) {

        return new Func1<Observable<? extends Throwable>, Observable<Long>>() {
            @Override public Observable<Long> call(Observable<? extends Throwable> observable) {
                // zip our number of retries to the incoming errors so that we only produce retries
                // when there's been an error
                return observable
                        .flatMap(new RetryFunc1(attempts));
            }
        };
    }
}
