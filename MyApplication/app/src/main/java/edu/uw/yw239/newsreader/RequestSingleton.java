package edu.uw.yw239.newsreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by yunwu on 10/21/17.
 */
class RequestSingleton {
    //the single instance of this singleton
    private static RequestSingleton instance;

    private static RequestQueue requestQueue = null; //the singleton's RequestQueue
    private static ImageLoader imageLoader = new ImageLoader(requestQueue,
            new ImageLoader.ImageCache() {  //define an anonymous Cache object
                //the cache instance variable
                private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

                //method for accessing the cache
                @Override
                public Bitmap getBitmap(String url) {
                    return cache.get(url);
                }

                //method for storing to the cache
                @Override
                public void putBitmap(String url, Bitmap bitmap) {
                    cache.put(url, bitmap);
                }
            });

    //private constructor; cannot instantiate directly
    private RequestSingleton(Context ctx){
        //create the requestQueue
        this.requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());

        //create the imageLoader
        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {  //define an anonymous Cache object
                    //the cache instance variable
                    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

                    //method for accessing the cache
                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    //method for storing to the cache
                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    //call this "factory" method to access the Singleton
    public static RequestSingleton getInstance(Context ctx) {
        //only create the singleton if it doesn't exist yet
        if(instance == null){
            instance = new RequestSingleton(ctx);
        }

        return instance; //return the singleton object
    }

    //get queue from singleton for direct action
    public RequestQueue getRequestQueue() {
        return this.requestQueue;
    }

    //convenience wrapper method
    public <T> void add(Request<T> req) {
        requestQueue.add(req);
    }

    public static ImageLoader getImageLoader() {
        return imageLoader;
    }
}