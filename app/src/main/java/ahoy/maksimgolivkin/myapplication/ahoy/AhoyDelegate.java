package ahoy.maksimgolivkin.myapplication.ahoy;

public interface AhoyDelegate {

    interface Callback {
        void onSuccess(Visit visit);
        void onFailure(Throwable throwable);
    }

    String newVisitorToken();
    void newVisit(VisitParams params, Callback callback);
    void updateVisit(VisitParams params, Callback callback);
}

