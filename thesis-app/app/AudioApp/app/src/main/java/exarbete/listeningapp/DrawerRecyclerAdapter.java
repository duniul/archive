package exarbete.listeningapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InputStream;

/**
 * This adapter class handles the header and items of the navigation drawer.
 */
public class DrawerRecyclerAdapter extends RecyclerView.Adapter<DrawerRecyclerAdapter.ViewHolder> implements RecyclerView.OnItemTouchListener {

    private static final String TAG = "DrawerRecyclerAdapter";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // The activity that uses the adapter.
    private Activity activity;

    // Variables used by the header.
    private String headerPictureURL;
    private String headerName;
    private String headerEmail;
    private int headerExpandButtonIcon = R.drawable.ic_drawer_header_drop_down_button;

    // Variables used by items.
    private String[] itemLabels;
    private int[] itemIcons;

    /**
     * Instantiates a new DrawerRecyclerAdapter.
     * @param activity            the activity
     * @param itemIcons           the item icons
     * @param itemLabelsIds       the item labels resource ids
     */
    public DrawerRecyclerAdapter(Activity activity, int[] itemIcons, int[] itemLabelsIds) {
        this.activity = activity;
        setDrawerItems(itemIcons, itemLabelsIds);
        updateHeaderInfo();
    }

    @Override
    public DrawerRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        // Creates the correct type of view holder depending on the view type.
        switch (viewType) {
            case TYPE_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);
                return new ViewHolder(view, viewType);

            case TYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_item, parent, false);
                return new ViewHolder(view, viewType);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(DrawerRecyclerAdapter.ViewHolder holder, int position) {

        // Chooses what values to change depending on the view type.
        switch (holder.holderId) {
            case TYPE_HEADER:
                setupHeader(holder);
                break;

            case TYPE_ITEM:
                // Attaches icons to labels.
                Drawable itemIcon = ContextCompat.getDrawable(activity, itemIcons[position - 1]);
                holder.itemLabel.setCompoundDrawablesWithIntrinsicBounds(itemIcon, null, null, null);
                holder.itemLabel.setText(itemLabels[position - 1]);
                break;
        }
    }

    private void setupHeader(DrawerRecyclerAdapter.ViewHolder headerHolder) {

        if (!SharedPrefsHandler.getInstance().isUserLoggedIn()) {
            headerHolder.headerPicture.setVisibility(View.GONE);
            headerHolder.headerEmail.setCompoundDrawables(null, null, null, null);
        } else {
            if (headerPictureURL.isEmpty()) {
                headerHolder.headerPicture.setImageDrawable(activity.getDrawable(R.mipmap.ic_default_profile_picture));
            } else {
                new LoadProfileImage(headerHolder.headerPicture, headerHolder.headerPictureProgress).execute(headerPictureURL);
            }

            headerHolder.headerPicture.setVisibility(View.VISIBLE);
            headerHolder.headerEmail.setCompoundDrawablesWithIntrinsicBounds(null, null, activity.getDrawable(headerExpandButtonIcon), null);
        }

        headerHolder.headerName.setText(headerName);
        headerHolder.headerEmail.setText(headerEmail);
    }

    public void updateHeaderInfo() {
        SharedPrefsHandler sharedPrefsHandler = SharedPrefsHandler.getInstance();

        if (sharedPrefsHandler.isUserLoggedIn()) {
            headerPictureURL = sharedPrefsHandler.getString(SharedPrefsHandler.USER_GOOGLE_PICTURE_URL_KEY, "");
            headerName = sharedPrefsHandler.getString(SharedPrefsHandler.USER_GOOGLE_NAME_KEY, "Name not available.");
            headerEmail = sharedPrefsHandler.getString(SharedPrefsHandler.USER_GOOGLE_EMAIL_KEY, "Email not available.");
        } else {
            headerPictureURL = "";
            headerName = activity.getString(R.string.drawer_header_no_user);
            headerEmail = activity.getString(R.string.drawer_header_no_user2);
        }
    }

    public void setDrawerItems(int[] itemIcons, int[] itemLabelIds) {
        this.itemIcons = itemIcons;

        this.itemLabels = new String[itemLabelIds.length];
        for(int i = 0; i < itemLabelIds.length; i++) {
            this.itemLabels[i] = activity.getString(itemLabelIds[i]);
        }
    }

    public void setHeaderExpandButtonIcon(int iconId) {
        this.headerExpandButtonIcon = iconId;
    }

    @Override
    public int getItemCount() {
        return itemLabels.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        // First position is always the header, otherwise it's an item.
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    /**
     * The type View holder.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        // Holder id to identify the view type it holds.
        private int holderId;

        // Views used in the header.
        private ImageView headerPicture;
        private TextView headerName;
        private TextView headerEmail;

        // View used by items.
        private TextView itemLabel;
        public ProgressBar headerPictureProgress;

        /**
         * Instantiates a new ViewHolder.
         *
         * @param itemView the item view
         * @param viewType the view type
         */
        public ViewHolder(View itemView, int viewType) {
            super(itemView);

            if (viewType == TYPE_HEADER) {
                holderId = TYPE_HEADER;
                headerPicture = (ImageView) itemView.findViewById(R.id.header_circle_image);
                headerName = (TextView) itemView.findViewById(R.id.header_name);
                headerEmail = (TextView) itemView.findViewById(R.id.header_email);
                headerPictureProgress = (ProgressBar) itemView.findViewById(R.id.header_image_progress_indicator);

            } else {
                holderId = TYPE_ITEM;
                itemLabel = (TextView) itemView.findViewById(R.id.drawer_item_label);
            }
        }
    }

    /**
     * This inner class loads the users Google profile image in the background and adds it to
     * the ImageView in the header when finished.
     */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {

        ImageView profileImageView;
        ProgressBar progressBar;

        /**
         * Instantiates a new LoadProfileImage task.
         *
         * @param profileImageView the ImageView to show the profile picture
         */
        public LoadProfileImage(ImageView profileImageView, ProgressBar progressBar) {
            this.profileImageView = profileImageView;
            this.progressBar = progressBar;
            this.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            String pictureURL = url[0];
            Bitmap picture;

            try {
                InputStream inputStream = new java.net.URL(pictureURL).openStream();
                picture = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                return null;
            }

            return picture;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            progressBar.setVisibility(View.INVISIBLE);

            if (result == null) {
                profileImageView.setImageDrawable(activity.getDrawable(R.mipmap.ic_default_profile_picture));
            } else {
                profileImageView.setImageBitmap(result);
            }
        }
    }
}
