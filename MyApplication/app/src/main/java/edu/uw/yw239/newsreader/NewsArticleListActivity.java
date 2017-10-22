package edu.uw.yw239.newsreader;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.yw239.newsreader.dummy.DummyContent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link NewsArticleDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class NewsArticleListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<NewsArticle> newsArticleInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsarticle_list);

        handleIntent(getIntent());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, new RecyclerView.State(), 0);
                view.setVisibility(View.INVISIBLE);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.newsarticle_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(this,2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        assert mRecyclerView != null;
        setupRecyclerView((RecyclerView) mRecyclerView);

        if (findViewById(R.id.newsarticle_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mTwoPane = true;
        }

        newsArticleInfo = new ArrayList<NewsArticle>();
        // Create the adapter to convert the array to views
        mAdapter = new NewsArticleAdapter(newsArticleInfo);
        // Attach the adapter to a RecyclerView
        mRecyclerView.setAdapter(mAdapter);
        newsSearch("");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            newsSearch("q=" + query + "&");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    private void newsSearch(String searchTerm){
        String urlString = "http://beta.newsapi.org/v2/top-headlines?" + searchTerm
                + "country=us" + "&language=en" + "&apiKey=9686280ead3e47d7b6820ba9644a1bb7";

        Request request = new JsonObjectRequest(Request.Method.GET, urlString, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        List<NewsArticle> newsArticles = NewsArticle.parseNewsAPI(response);
                        newsArticleInfo.clear();
                        newsArticleInfo.addAll(newsArticles);
                        mAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = new String(error.networkResponse.data);
                    }
                });
        RequestSingleton.getInstance(this).add(request);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new NewsArticleAdapter(new ArrayList<NewsArticle>()));
    }

    public class NewsArticleAdapter
            extends RecyclerView.Adapter<NewsArticleAdapter.ViewHolder> {

        private final List<NewsArticle> mValues;

        public NewsArticleAdapter(List<NewsArticle> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.newsarticle_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);


            String imageUrl = mValues.get(position).imageUrl;
            ImageLoader imageLoader = RequestSingleton.getImageLoader();
            if(imageUrl != null) {
                holder.mImageView.setImageUrl(imageUrl, imageLoader);
            }
            else {
                holder.mImageView.setDefaultImageResId(R.drawable.default_image);
            }
            holder.mHeadlineView.setText(mValues.get(position).headline);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(NewsArticleDetailFragment.ARG_ITEM_ID, holder.mItem.sourceId);
                        NewsArticleDetailFragment fragment = new NewsArticleDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.newsarticle_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, NewsArticleDetailActivity.class);
                        intent.putExtra(NewsArticleDetailFragment.ARG_ITEM_ID, holder.mItem.sourceId);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final NetworkImageView mImageView;
            public final TextView mHeadlineView;
            public NewsArticle mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (NetworkImageView) view.findViewById(R.id.image);
                mHeadlineView = (TextView) view.findViewById(R.id.headline);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mHeadlineView.getText() + "'";
            }
        }
    }
}
