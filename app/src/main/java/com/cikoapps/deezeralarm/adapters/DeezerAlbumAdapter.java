package com.cikoapps.deezeralarm.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cikoapps.deezeralarm.Activities.RingtoneActivity;
import com.cikoapps.deezeralarm.Fragments.DeezerAlbumFragment;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.models.Album;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DeezerAlbumAdapter extends RecyclerView.Adapter<DeezerAlbumAdapter.DeezerAlbumViewHolder> {

    Context context;
    ArrayList<Album> albumsList;
    LayoutInflater inflater;
    ArrayList<Bitmap> images;
    static Typeface notoRegular;
    public static int selectedPosition = -1;

    public DeezerAlbumAdapter(Context mContext, ArrayList<Album> albums) {
        context = mContext;
        albumsList = albums;
        albums.add(null);
        inflater = LayoutInflater.from(context);
        images = new ArrayList<>();
        for (int i = 0; i < albumsList.size(); i++) {
            images.add(null);
        }
        notoRegular = Typeface.createFromAsset(context.getAssets(), "NotoSerif-Regular.ttf");
    }

    @Override
    public DeezerAlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.deezer_albums_item_layout, parent, false);
        return new DeezerAlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeezerAlbumViewHolder holder, final int position) {
        final Album album = albumsList.get(position);
        if (album == null) {
            holder.albumRadioButton.setWillNotDraw(true);
            holder.albumImageView.setWillNotDraw(true);
            holder.albumArtistTextView.setWillNotDraw(true);
            holder.albumTitleTextView.setWillNotDraw(true);
            holder.albumImageView.setImageBitmap(null);
        } else {
            holder.albumRadioButton.setWillNotDraw(false);
            holder.albumImageView.setWillNotDraw(false);
            holder.albumArtistTextView.setWillNotDraw(false);
            holder.albumTitleTextView.setWillNotDraw(false);
            holder.albumTitleTextView.setText(album.title);
            holder.albumTitleTextView.setTypeface(notoRegular);
            holder.albumArtistTextView.setText(album.artist);
            holder.albumArtistTextView.setTypeface(notoRegular);
            if (album.imageUrlMedium.length() > 1) {
                holder.albumImageView.setImageResource(R.drawable.weather_sunny);
                if (album.selected && RingtoneActivity.selectedRingtone.type == 2) {
                    holder.albumRadioButton.setChecked(true);
                } else holder.albumRadioButton.setChecked(false);

                if (images.get(position) != null) {
                    holder.albumImageView.setImageBitmap(images.get(position));
                } else {
                    new ImageLoadTask(album.imageUrlMedium, holder.albumImageView, position).execute();
                }
            }
            holder.albumRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition == -1) {
                        album.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerAlbumFragment.updateSelectedRingtone(album.id, album.title);
                    } else if (selectedPosition != -1 && selectedPosition == position) {
                        album.selected = false;
                        selectedPosition = -1;
                        notifyItemChanged(position);
                        DeezerAlbumFragment.updateSelectedRingtone(-1, "");
                    } else {
                        albumsList.get(selectedPosition).selected = false;
                        notifyItemChanged(selectedPosition);
                        album.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeezerAlbumFragment.updateSelectedRingtone(album.id, album.title);
                    }

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return albumsList.size();
    }

    class DeezerAlbumViewHolder extends RecyclerView.ViewHolder {


        ImageView albumImageView;
        TextView albumTitleTextView;
        TextView albumArtistTextView;
        RadioButton albumRadioButton;

        public DeezerAlbumViewHolder(View itemView) {
            super(itemView);
            albumImageView = (ImageView) itemView.findViewById(R.id.albumImage);
            albumTitleTextView = (TextView) itemView.findViewById(R.id.albumTitleTextView);
            albumArtistTextView = (TextView) itemView.findViewById(R.id.albumArtistTextView);
            albumRadioButton = (RadioButton) itemView.findViewById(R.id.albumRadioButton);

        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;
        private int position;

        public ImageLoadTask(String url, ImageView imageView, int position) {
            this.url = url;
            this.position = position;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            images.set(position, result);
            notifyItemChanged(position);
        }

    }

}
