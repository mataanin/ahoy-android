package ahoy.maksimgolivkin.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Collections;

import ahoy.maksimgolivkin.myapplication.ahoy.Ahoy.VisitListener;
import ahoy.maksimgolivkin.myapplication.ahoy.AhoySingleton;
import ahoy.maksimgolivkin.myapplication.ahoy.Visit;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Activity extends AppCompatActivity implements VisitListener {

    @BindView(R.id.visit_fetch_progress) ProgressBar visitFetchProgress;
    @BindView(R.id.visit_token) TextView visitTokenView;
    @BindView(R.id.visitor_token) TextView visitorTokenView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.setDebug(true);
        ButterKnife.bind(this);

        updateViews(AhoySingleton.getVisitorToken(), AhoySingleton.getVisit());
        AhoySingleton.addVisitListener(this);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        AhoySingleton.removeVisitListener(this);
    }

    @Override public void onVisitUpdated(Visit visit) {
        updateViews(AhoySingleton.getVisitorToken(), visit);
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
