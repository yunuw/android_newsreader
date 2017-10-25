package edu.uw.yw239.newsreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

/**
 * An activity representing a single NewsArticle detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link NewsArticleListActivity}.
 */
public class NewsArticleDetailActivity extends AppCompatActivity
        implements NewsArticleDetailFragment.HasCollapsableImage, NewsArticleDetailFragment.UpdateDetailFragment{

    private NewsArticle currentArticle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsarticle_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, currentArticle.headline + "\n" + currentArticle.webUrl);
                intent.setType("text/plain");

                startActivity(intent);
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Intent intent= getIntent();
            currentArticle = (NewsArticle) intent.getExtras().getParcelable(NewsArticleDetailFragment.NEWS_PARCEL_KEY);
            NewsArticleDetailFragment fragment = NewsArticleDetailFragment.newInstance(currentArticle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.newsarticle_detail_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }


    public void setUpToolbar(NewsArticle newsArticle){
        String imageUrl = newsArticle.imageUrl;
        ImageLoader imageLoader = RequestSingleton.getImageLoader();
        NetworkImageView imageView = (NetworkImageView)findViewById(R.id.network_image_view);

        if(imageUrl != null) {
            imageView.setImageUrl(imageUrl, imageLoader);
        }
        else {
           imageView.setImageResource(R.drawable.default_image);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, NewsArticleListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateFragment(NewsArticle article) {
        NewsArticleDetailFragment fragment = NewsArticleDetailFragment.newInstance(article);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.newsarticle_detail_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
