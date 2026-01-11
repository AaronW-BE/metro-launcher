package org.dpdns.argv.metrolauncher.ui;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import org.dpdns.argv.metrolauncher.model.TileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoTileView extends LiveTileView {

    private ImageView frontImage;
    private ImageView backImage;
    
    private final List<Long> imageIds = new ArrayList<>();

    public PhotoTileView(Context context) {
        super(context);
    }

    @Override
    protected View createFrontView() {
        frontImage = new ImageView(getContext());
        frontImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return frontImage;
    }

    @Override
    protected View createBackView() {
        backImage = new ImageView(getContext());
        backImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return backImage;
    }

    @Override
    public void bind(TileItem item) {
        super.bind(item);
        loadImages();
    }
    
    private void loadImages() {
         if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED && 
             ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
             return;
        }
        
        new Thread(() -> {
            try {
                String[] projection = new String[]{MediaStore.Images.Media._ID};
                Cursor cursor = getContext().getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT 20");
                
                if (cursor != null) {
                    imageIds.clear();
                    while(cursor.moveToNext()) {
                        imageIds.add(cursor.getLong(0));
                    }
                    cursor.close();
                    
                    if (!imageIds.isEmpty()) {
                        post(this::updateFront);
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }
    
    private void updateFront() {
         if (imageIds.isEmpty()) return;
         long id = imageIds.get((int)(Math.random() * imageIds.size()));
         setBitmap(frontImage, id);
    }
    
    @Override
    protected void onFlipStart(View nextView) {
        if (imageIds.isEmpty()) return;
        long id = imageIds.get((int)(Math.random() * imageIds.size()));
        setBitmap((ImageView) nextView, id);
    }

    private void setBitmap(ImageView iv, long id) {
        try {
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            // In a real app we'd use Glide/Picasso. Here we do crude loading.
            Bitmap bitmap = getContext().getContentResolver().loadThumbnail(
                    contentUri, new Size(512, 512), null);
            iv.setImageBitmap(bitmap);
        } catch (Exception ignored) {
            iv.setBackgroundColor(0xFF444444); // Fallback color
        }
    }
}
