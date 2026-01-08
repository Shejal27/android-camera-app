package com.s.cameraapp;   // ✅ Package declaration starts here

// ✅ Import statements start here
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
// ✅ Import statements end here

// ✅ Class declaration starts here
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    // ✅ Variable declarations start here
    private List<String> photoList;              // list of photo paths
    private List<String> favoriteList = new ArrayList<>(); // list of favorite photos
    private OnPhotoClickListener listener;       // click listener interface
    // ✅ Variable declarations end here

    // ✅ Interface declaration starts here
    public interface OnPhotoClickListener {
        void onPhotoClick(String path);
    }
    // ✅ Interface declaration ends here

    // ✅ Constructor starts here
    public PhotoAdapter(List<String> photoList, OnPhotoClickListener listener) {
        this.photoList = photoList;
        this.listener = listener;
    }
    // ✅ Constructor ends here

    // ✅ onCreateViewHolder() method starts here
    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
        return new PhotoViewHolder(view);
    }
    // ✅ onCreateViewHolder() method ends here

    // ✅ onBindViewHolder() method starts here
    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String path = photoList.get(position);
        holder.imgPic.setImageURI(Uri.fromFile(new File(path)));
        holder.imgFav.setVisibility(favoriteList.contains(path) ? View.VISIBLE : View.GONE);

        holder.imgPic.setOnClickListener(v -> listener.onPhotoClick(path));
    }
    // ✅ onBindViewHolder() method ends here

    // ✅ getItemCount() method starts here
    @Override
    public int getItemCount() {
        return photoList.size();
    }
    // ✅ getItemCount() method ends here

    // ✅ addPhoto() method starts here
    public void addPhoto(String path) {
        photoList.add(path);
        notifyItemInserted(photoList.size() - 1);
    }
    // ✅ addPhoto() method ends here

    // ✅ removePhoto() method starts here
    public void removePhoto(String path) {
        int index = photoList.indexOf(path);
        if (index != -1) {
            photoList.remove(index);
            notifyItemRemoved(index);
        }
    }
    // ✅ removePhoto() method ends here

    // ✅ toggleFavorite() method starts here
    public void toggleFavorite(String path) {
        if (favoriteList.contains(path)) {
            favoriteList.remove(path);
        } else {
            favoriteList.add(path);
        }
        int index = photoList.indexOf(path);
        if (index != -1) notifyItemChanged(index);
    }
    // ✅ toggleFavorite() method ends here

    // ✅ ViewHolder class starts here
    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPic, imgFav;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPic = itemView.findViewById(R.id.imgPic);
            imgFav = itemView.findViewById(R.id.imgFav);
        }
    }
    // ✅ ViewHolder class ends here
}
// ✅ Class ends here