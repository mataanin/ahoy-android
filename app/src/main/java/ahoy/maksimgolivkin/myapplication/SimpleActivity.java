package ahoy.maksimgolivkin.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.instacart.ahoy.AhoySingleton;

import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SimpleActivity extends AppCompatActivity {

    protected static int activityCount = 0;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_activity);
        ButterKnife.bind(this);

        setTitle(getString(R.string.simple_activity, ++activityCount));
    }

    @OnClick({R.id.new_visit, R.id.next_activity, R.id.next_utm_activity})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_visit:
                AhoySingleton.scheduleNewVisit(Collections.<String, String>emptyMap());
                break;
            case R.id.next_utm_activity:
                startActivity(new Intent(this, UtmActivity.class));
                break;
            case R.id.next_activity:
                startActivity(new Intent(this, SimpleActivity.class));
                break;
            default: break;
        }
    }
}