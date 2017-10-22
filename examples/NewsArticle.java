http://beta.newsapi.org/v2/top-headlines?sources=searchTerm&country=us&language=en&apiKey=9686280ead3e47d7b6820ba9644a1bb7

http://beta.newsapi.org/v2/top-headlines?q=church&country=us&language=en&apiKey=9686280ead3e47d7b6820ba9644a1bb7


import android.util.Log;

import android.util.Log;
import android.webkit.URLUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a single news item (article). Can be parsed from 
 * the News API aggregator
 * @author joelross
 */
public class NewsArticle {

    public static final String TAG = "NewsArticle";

    public String headline = "";
    public String description = "";
    public long publishedTime = 0;
    public String webUrl = "";
    public String imageUrl = "";
    public String sourceId = "";
    public String sourceName = "";

    //default empty constructor
    public NewsArticle() {}

    //convenience constructor for testing
    public NewsArticle(String headline, String imageUrl){
        this.headline = headline;
        this.imageUrl = imageUrl;
    }

    public String toString() {
        //can modify this to include more or different details
        return this.headline;// + " " + imageUrl;
    }

    /********************************
     *  API response parsing methods
     ********************************/

    /**
     * Parses the query response from the News API aggregator
     * http://beta.newsapi.org/
     */
    public static List<NewsArticle> parseNYTTopStories(JSONObject response){
        ArrayList<NewsArticle> stories = new ArrayList<NewsArticle>();

        try {
            JSONArray jsonResults = response.getJSONArray("results"); //response.results

            for(int i=0; i<jsonResults.length(); i++){
                JSONObject resultItemObj = jsonResults.getJSONObject(i);

                String headline = resultItemObj.getString("title");
                String webUrl = resultItemObj.getString("url");
                String snippet = resultItemObj.getString("abstract");

                //date handling
                String pubDateString = resultItemObj.getString("published_date");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
                long publishTime = formatter.parse(pubDateString).getTime();

                //image extracting
                JSONArray jsonMultimedia = resultItemObj.getJSONArray("multimedia");
                String imageUrl = extractImageUrl(jsonMultimedia, MAX_IMAGE_WIDTH);

                NewsArticle story = new NewsArticle(headline, webUrl, publishTime, snippet, imageUrl);
                stories.add(story);
            } //end for loop

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing json", e); //Android log the error
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date", e); //Android log the error
        }

        return stories;
    }


   /********************************
     *  API response parsing methods
     ********************************/

    /**
     * Parses the query response from the NYT Article Search API
     * http://developer.nytimes.com/article_search_v2.json
     */
    public static List<NewsArticle> parseNewsAPI(JSONObject response){
        ArrayList<NewsArticle> stories = new ArrayList<NewsArticle>();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        try {
            JSONArray jsonArticles = response.getJSONArray("articles"); //response.articles

            for(int i=0; i<Math.min(jsonArticles.length(), 20); i++){
                JSONObject articleItemObj = jsonArticles.getJSONObject(i);

                NewsArticle story = new NewsArticle();

                story.headline = articleItemObj.getString("title");
                story.webUrl = articleItemObj.getString("url");
                story.description = articleItemObj.getString("description");
                story.imageUrl = articleItemObj.getString("urlToImage");
                if(story.imageUrl.equals("null") || !URLUtil.isValidUrl(story.imageUrl)){
                    story.imageUrl = null; //make actual null value
                }

                try {
                    String pubDateString = articleItemObj.getString("publishedAt");
                    if(!pubDateString.equals("null"))
                        story.publishedTime = formatter.parse(pubDateString).getTime();
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date", e); //Android log the error
                }

                JSONObject sourceObj = articleItemObj.getJSONObject("source");
                story.sourceId = sourceObj.getString("id");
                story.sourceName = sourceObj.getString("name");

                stories.add(story);
            } //end for loop
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing json", e); //Android log the error
        }
        return stories;
    }
}