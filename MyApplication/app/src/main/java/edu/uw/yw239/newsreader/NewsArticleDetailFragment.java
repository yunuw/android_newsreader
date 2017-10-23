package edu.uw.yw239.newsreader;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.uw.yw239.newsreader.dummy.DummyContent;

/**
 * A fragment representing a single NewsArticle detail screen.
 * This fragment is either contained in a {@link NewsArticleListActivity}
 * in two-pane mode (on tablets) or a {@link NewsArticleDetailActivity}
 * on handsets.
 */
public class NewsArticleDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    public static final String TAG = "NewsArticleDetailFragment";
    public static final String NEWS_PARCEL_KEY = "news_article_parcel";
    private HasCollapsableImage callback;
    private ListViewAdapter listViewAdapter;
    private NewsArticle currentArticle;

    interface UpdateDetailFragment{
        void updateFragment(NewsArticle article);
    }

    interface HasCollapsableImage{
        void setUpToolbar(NewsArticle newsArticle);
    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewsArticleDetailFragment() {

    }

    public static NewsArticleDetailFragment newInstance(NewsArticle newsArticle) {

        Bundle args = new Bundle();
        args.putParcelable(NEWS_PARCEL_KEY, newsArticle);
        NewsArticleDetailFragment fragment = new NewsArticleDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.newsarticle_detail, container, false);

        Bundle bundle = getArguments();
        NewsArticle article = (NewsArticle)bundle.getParcelable(NEWS_PARCEL_KEY);
        currentArticle = article;

        // Check if the current attached Activity implements the interface
        if(getContext() instanceof HasCollapsableImage) {
            try {
                callback = (HasCollapsableImage) getContext();
                callback.setUpToolbar(article);
            } catch (ClassCastException cce) {
                throw new ClassCastException(getContext().toString() + " must implement HasCollapsableImage");
            }
        }

        ((TextView) rootView.findViewById(R.id.fragment_headline)).setText(article.headline);
        ((TextView) rootView.findViewById(R.id.fragment_description)).setText(article.description);
        ((TextView) rootView.findViewById(R.id.fragment_news_source)).setText(article.sourceName);

        ArrayList<NewsArticle> listOfNewsArticle = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(getContext(), listOfNewsArticle);

        ListView listView = (ListView)rootView.findViewById(R.id.list_of_related_articles);
        listView.setAdapter(listViewAdapter);

        getRelatedArticles(article.sourceId);

        return rootView;
    }

    public void getRelatedArticles(String source){
        String sourceUrl  = "http://beta.newsapi.org/v2/everything?" + "sources=" + source
                + "&language=en" + "&apiKey=" + getResources().getString(R.string.api_key);

        Request request = new JsonObjectRequest(Request.Method.GET, sourceUrl, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        List<NewsArticle> newsArticles = NewsArticle.parseNewsAPI(response);
                        listViewAdapter.clear();
                        int articleAdded = 0;
                        for(int i = 0; i < newsArticles.size(); i++) {
                            if (currentArticle != null && currentArticle.headline != null
                                    && currentArticle.headline.equals(newsArticles.get(i).headline)) {
                                continue;
                            }

                            listViewAdapter.add(newsArticles.get(i));
                            articleAdded++;
                            if (articleAdded == 5) {
                                break;
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = new String(error.networkResponse.data);
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
        RequestSingleton.getInstance(getContext()).add(request);
    }

    public class ListViewAdapter extends ArrayAdapter<NewsArticle> {
        // View lookup cache
        private class ViewHolder {
            TextView sourceHeadlines;
        }

        public ListViewAdapter(Context context, ArrayList<NewsArticle> users) {
            super(context, R.layout.newsarticle_detail, R.id.fragment_news_source, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final NewsArticle newsArticle = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag
            if (convertView == null) {
                // If there's no view to re-use, inflate a brand new view for row
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.related_articles, parent, false);
                viewHolder.sourceHeadlines = (TextView) convertView.findViewById(R.id.related_article_headline);
                // Cache the viewHolder object inside the fresh view
                convertView.setTag(viewHolder);
            } else {
                // View is being recycled, retrieve the viewHolder object from tag
                viewHolder = (ViewHolder) convertView.getTag();
            }
            // Populate the data from the data object via the viewHolder object
            // into the template view.

            viewHolder.sourceHeadlines.setText(newsArticle.headline);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder viewHolder = (ViewHolder) v.getTag();
                    if(getContext() instanceof UpdateDetailFragment){
                        ((UpdateDetailFragment) getContext()).updateFragment(newsArticle);
                    }
                }
            });

            // Return the completed view to render on screen
            return convertView;
        }
    }

}
