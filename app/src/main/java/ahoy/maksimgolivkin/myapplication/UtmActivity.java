package ahoy.maksimgolivkin.myapplication;

import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.github.instacart.ahoy.AhoySingleton;
import com.github.instacart.ahoy.Visit;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UtmActivity extends AppCompatActivity {

    @BindView(R.id.utm_campaign) TextView utmCampaign;
    @BindView(R.id.utm_content) TextView utmContent;
    @BindView(R.id.utm_medium) TextView utmMedium;
    @BindView(R.id.utm_source) TextView utmSource;
    @BindView(R.id.utm_term) TextView utmTerm;

    protected static int activityCount = 0;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.utm_activity);
        ButterKnife.bind(this);

        setTitle(getString(R.string.simple_activity, ++activityCount));
        showUtmParams();
    }

    private void showUtmParams() {
        utmCampaign.setText(getString(R.string.utm_campaign, activityCount));
        utmContent.setText(getString(R.string.utm_content, activityCount));
        utmMedium.setText(getString(R.string.utm_medium, activityCount));
        utmSource.setText(getString(R.string.utm_source, activityCount));
        utmTerm.setText(getString(R.string.utm_term, activityCount));
    }

    @OnClick({R.id.new_visit_with_utm, R.id.save_utm_params})
    public void onClick(View view) {
        final Map<String, String> landingUtm = new ArrayMap<>();
        landingUtm.put(Visit.UTM_CAMPAIGN, utmCampaign.getText().toString());
        landingUtm.put(Visit.UTM_CONTENT, utmContent.getText().toString());
        landingUtm.put(Visit.UTM_MEDIUM, utmMedium.getText().toString());
        landingUtm.put(Visit.UTM_SOURCE, utmSource.getText().toString());
        landingUtm.put(Visit.UTM_TERM, utmTerm.getText().toString());

        Map<String, Object> extraParams = new ArrayMap<>();
        extraParams.put(Visit.LANDING_PAGE, landingUtm);

        switch (view.getId()) {
            case R.id.save_utm_params:
                AhoySingleton.scheduleSaveExtras(extraParams);
                break;
            case R.id.new_visit_with_utm:
                AhoySingleton.scheduleNewVisit(extraParams);
                break;
            default: break;
        }
    }
}
