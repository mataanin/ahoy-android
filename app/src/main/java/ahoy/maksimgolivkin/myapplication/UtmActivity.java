package ahoy.maksimgolivkin.myapplication;

import android.content.Intent;
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
        final Map<String, Object> utmParams = new ArrayMap<>();
        utmParams.put(Visit.UTM_CAMPAIGN, utmCampaign.getText());
        utmParams.put(Visit.UTM_CONTENT, utmContent.getText());
        utmParams.put(Visit.UTM_MEDIUM, utmMedium.getText());
        utmParams.put(Visit.UTM_SOURCE, utmSource.getText());
        utmParams.put(Visit.UTM_TERM, utmTerm.getText());

        switch (view.getId()) {
            case R.id.save_utm_params:
                AhoySingleton.scheduleSaveExtras(utmParams);
                break;
            case R.id.new_visit_with_utm:
                AhoySingleton.scheduleNewVisit(utmParams);
                break;
            case R.id.next_utm_activity:
                startActivity(new Intent(this, UtmActivity.class));
                break;
            default: break;
        }
    }
}
