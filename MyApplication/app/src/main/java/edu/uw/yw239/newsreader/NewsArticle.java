package edu.uw.yw239.newsreader;

/**
 * Created by yunwu on 10/21/17.
 */

import android.os.Parcel;
import android.os.Parcelable;
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
public class NewsArticle implements Parcelable {

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

    protected NewsArticle(Parcel in) {
        headline = in.readString();
        description = in.readString();
        publishedTime = in.readLong();
        webUrl = in.readString();
        imageUrl = in.readString();
        sourceId = in.readString();
        sourceName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(headline);
        dest.writeString(description);
        dest.writeLong(publishedTime);
        dest.writeString(webUrl);
        dest.writeString(imageUrl);
        dest.writeString(sourceId);
        dest.writeString(sourceName);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NewsArticle> CREATOR = new Parcelable.Creator<NewsArticle>() {
        @Override
        public NewsArticle createFromParcel(Parcel in) {
            return new NewsArticle(in);
        }

        @Override
        public NewsArticle[] newArray(int size) {
            return new NewsArticle[size];
        }
    };
}