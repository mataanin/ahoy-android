package ahoy.maksimgolivkin.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.instacart.ahoy.AhoySingleton;
import com.github.instacart.ahoy.Visit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class VisitView extends LinearLayout {

    @BindView(R.id.visit_fetch_progress) ProgressBar visitFetchProgress;
    @BindView(R.id.visit_token) TextView visitTokenView;
    @BindView(R.id.visitor_token) TextView visitorTokenView;

    private Subscription mSubscription;

    public VisitView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mSubscription = AhoySingleton.visitStream()
                .startWith(AhoySingleton.visit())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Visit>() {
                    @Override public void call(Visit visit) {
                        updateViews(AhoySingleton.visitorToken(), visit);
                    }
                });
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mSubscription.unsubscribe();
    }

    public void updateViews(String visitorToken, Visit visit) {
        Resources resources = getResources();
        visitorTokenView.setText(resources.getString(R.string.visit_token, visitorToken));
        boolean isVisitValid = visit != null && visit.isValid();
        visitFetchProgress.setVisibility(isVisitValid ? View.INVISIBLE : View.VISIBLE);
        if (isVisitValid) {
            visitTokenView.setText(resources.getString(R.string.visit_token, visit.visitToken()));
        } else {
            visitTokenView.setText(resources.getString(R.string.visit_token, ""));
        }
    }
}
