package com.moonlight.cartographe.CardPack;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.moonlight.cartographe.R;

import java.io.ByteArrayOutputStream;
import java.util.List;


public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.MyViewHolder> {

    private Context mContext;
    private List<CloudObjekt> CloudObjektList;
    public static final String EXTRA_MESSAGE = "Tag";
    public static CloudObjekt cloudObjekt_View;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count;
        public ImageView thumbnail, overflow;
        public ProgressBar progressBar;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.count);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
            progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        }
    }


    public AlbumsAdapter(Context mContext, List<CloudObjekt> CloudObjektList) {
        this.mContext = mContext;
        this.CloudObjektList = CloudObjektList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final CloudObjekt cloudObjekt_dec = CloudObjektList.get(position);
        holder.title.setText(cloudObjekt_dec.getName());
        // holder.count.setText(cloudObjekt_dec.getResult());
        if(cloudObjekt_dec.getResponse()){
            Toast.makeText(mContext,"Response Ok" , Toast.LENGTH_LONG).show();
            holder.progressBar.setIndeterminate(false);
            holder.progressBar.setProgress(100);
            //holder.progressBar.setEnabled(false);
        }


        // loading album cover using Glide library
        Glide.with(mContext).load(bitmapToByte(cloudObjekt_dec.getImage())).asBitmap().override(300, 300).fitCenter().into(holder.thumbnail);
        // Glide.with(mContext).load(cloudObjekt_dec.getThumbnail()).into(holder.thumbnail);

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewerCloudObjectActivity.class);
                //To pass:
                cloudObjekt_View = cloudObjekt_dec;
                mContext.startActivity(intent);

            }
        });

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow);
            }
        });
    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    Toast.makeText(mContext, "Add to favourite", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_play_next:
                    Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return CloudObjektList.size();
    }
}
