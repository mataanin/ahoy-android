package ahoy.maksimgolivkin.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.instacart.ahoy.AhoySingleton;
import com.github.instacart.ahoy.Visit;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.functions.Action1;

public class Activity extends AppCompatActivity {

    @BindView(R.id.visit_fetch_progress) ProgressBar visitFetchProgress;
    @BindView(R.id.visit_token) TextView visitTokenView;
    @BindView(R.id.visitor_token) TextView visitorTokenView;

    private Subscription mSubscription;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.setDebug(true);
        ButterKnife.bind(this);

        updateViews(AhoySingleton.getVisitorToken(), AhoySingleton.getVisit());
    }

    @Override protected void onResume() {
        super.onResume();
        mSubscription = AhoySingleton.visitStream().subscribe(new Action1<Visit>() {
            @Override public void call(Visit visit) {
                updateViews(AhoySingleton.getVisitorToken(), visit);
            }
        });
    }

    @Override protected void onPause() {
        super.onPause();
        mSubscription.unsubscribe();
    }

    @OnClick({R.id.reset_button, R.id.next_activity_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reset_button:
                AhoySingleton.resetVisit(Collections.<String, Object>emptyMap());
                updateViews(AhoySingleton.getVisitorToken(), null);
                break;
            case R.id.next_activity_button:
                startActivity(new Intent(this, Activity.class));
                break;
            default: break;
        }
    }

    private void updateViews(String visitorToken, Visit visit) {
        visitorTokenView.setText("Visitor token " + visitorToken);

        visitFetchProgress.setVisibility(visit != null ? View.INVISIBLE : View.VISIBLE);
        if (visit != null) {
            visitTokenView.setText("Visit token " + visit.visitToken());
        } else {
            visitTokenView.setText("Visit token ");
        }
    }
}
