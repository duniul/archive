package se.su.dsv.viking_prep_pvt_15_group9.helper;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.parse.Parse;
import com.parse.ParseACL;

import com.parse.ParseUser;


/**
 * Singleton-klass som körs när applikationen startas, och används för att hantera request queues.
 * Endast en instans av klassen kan köras på en och samma gång. Använder HTTP-biblioteket Volley
 * för att göra HTTP-requests. Extendar Application.
 *
 * @author Daniel
 */
public class AppRequestManager extends Application {

    // LogCat-tagg
    public static final String TAG = AppRequestManager.class.getSimpleName();

    // Instansen av AppController, variabelnamn enligt konvention.
    private static AppRequestManager mInstance;

    // Skapar en request queue.
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    /**
     * Körs när AppRequestHandler skapas för första gången.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // Add your initialization code here
        Parse.initialize(this, "Tt4WuJKTxGxpzKF49tesSCYFojnlAMfkzPkyB8B2", "uOAwCoyxJB6Q3KDRdjzDjBjbTOWXCexfhQgEAUH2");

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();

        // If you would like all objects to be private by default, remove this
        // line.
        defaultACL.setPublicReadAccess(true);

        ParseACL.setDefaultACL(defaultACL, true);
    }

    /**
     * Returnerar AppRequestHandler-instansen.
     *
     * @return mInstance konstant innehållande den nuvarande instansen.
     */
    public static synchronized AppRequestManager getInstance() {
        return mInstance;
    }

    /**
     * Hämtar request queue.
     * @return mRequestQueue existerande request queue.
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Lägger till en request till request queue.
     * @param req den request som ska läggas till.
     * @param tag taggen på requesten (för att veta vad requesten gäller).
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        if (tag.isEmpty()) {
            tag = TAG;
        }

        req.setTag(tag);
        getRequestQueue().add(req);
    }

    /**
     * Lägger till en request till request queue utan specificerad tagg.
     * @param req den request som ska läggas till.
     */
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    /**
     * Avbryter väntande requests.
     * @param tag taggen för objektet som vill avbryta requests.
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }
}