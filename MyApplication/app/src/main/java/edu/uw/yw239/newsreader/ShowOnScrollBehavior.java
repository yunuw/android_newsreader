package edu.uw.yw239.newsreader;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * Created by yunwu on 10/21/17.
 */

class ShowOnScrollBehavior extends AppBarLayout.ScrollingViewBehavior {

    public ShowOnScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean layoutDependsOn(CoordinatorLayout parent,
                                   FloatingActionButton child, View dependency) {

        return dependency instanceof FloatingActionButton ||
                super.layoutDependsOn(parent, child, dependency);
    }

    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       View child, View directTargetChild, View target, int nestedScrollAxes) {
        if(nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL){
            return true;
        }
        else {
            return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
        }
    }

    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        List<View> dependencies = coordinatorLayout.getDependencies(child);
        FloatingActionButton fab = (FloatingActionButton)coordinatorLayout.findViewById(R.id.fab);

        if(target.getId() == R.id.newsarticle_list) {
            RecyclerView t = (RecyclerView) target;
            if (t.canScrollVertically(-1)) {
                fab.show();
            } else {
                fab.hide();
            }
        }
    }
}
