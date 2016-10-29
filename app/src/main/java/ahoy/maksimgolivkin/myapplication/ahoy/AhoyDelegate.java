package ahoy.maksimgolivkin.myapplication.ahoy;

import java.util.Map;

import rx.Observable;

public interface AhoyDelegate {

    String newVisitorToken();
    Observable<Visit> newVisit(String visitorToken);
    void registerVisit(String visitorToken, Visit visit, Map<String, Object> additionalParams);
    void updateVisit(String visitorToken, Visit visit, Map<String, Object> additionalParams);
}
