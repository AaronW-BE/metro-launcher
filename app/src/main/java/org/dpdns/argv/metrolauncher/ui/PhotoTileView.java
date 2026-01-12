package org.dpdns.argv.metrolauncher.ui;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.graphics.Color;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.dpdns.argv.metrolauncher.model.TileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoTileView extends LiveTileView {

    private ImageView frontImage;
    private TextView frontDate;
    private ImageView backImage;
    private TextView backDate;
    
    // Track the containers to identify them in onFlipStart
    private View frontContainer;
    private View backContainer;
    
    private static class PhotoItem {
        long id;
        Uri baseUri;
        long dateAdded;
        PhotoItem(long id, Uri baseUri, long dateAdded) { 
            this.id = id; 
            this.baseUri = baseUri; 
            this.dateAdded = dateAdded;
        }
    }
    
    private final List<PhotoItem> photoItems = new ArrayList<>();
    private final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault());

    public PhotoTileView(Context context) {
        super(context);
    }

    @Override
    protected View createFrontView() {
        android.widget.FrameLayout layout = new android.widget.FrameLayout(getContext());
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        frontImage = new ImageView(getContext());
        frontImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        frontImage.setLayoutParams(new android.widget.FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layout.addView(frontImage);
        
        frontDate = new TextView(getContext());
        frontDate.setTextColor(Color.WHITE);
        frontDate.setTextSize(12);
        frontDate.setShadowLayer(2, 1, 1, Color.BLACK);
        android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.RIGHT;
        lp.setMargins(8, 8, 16, 16);
        frontDate.setLayoutParams(lp);
        layout.addView(frontDate);
        
        frontContainer = layout;
        return layout;
    }

    @Override
    protected View createBackView() {
        android.widget.FrameLayout layout = new android.widget.FrameLayout(getContext());
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        backImage = new ImageView(getContext());
        backImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        backImage.setLayoutParams(new android.widget.FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layout.addView(backImage);
        
        backDate = new TextView(getContext());
        backDate.setTextColor(Color.WHITE);
        backDate.setTextSize(12);
        backDate.setShadowLayer(2, 1, 1, Color.BLACK);
        android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.RIGHT;
        lp.setMargins(8, 8, 16, 16);
        backDate.setLayoutParams(lp);
        layout.addView(backDate);
        
        backContainer = layout;
        return layout;
    }

    @Override
    public void bind(TileItem item, boolean isEditMode) {
        super.bind(item, isEditMode);
        initViews(); 

        // Always show default color from item
        setBackgroundColor(item.color);

        // Initial state
        if (frontContainer != null) frontContainer.setVisibility(GONE);
        if (iconView != null) iconView.setVisibility(VISIBLE);
        if (titleView != null) {
            titleView.setText(item.title);
            titleView.setVisibility(VISIBLE);
        }

        if (photoItems.isEmpty()) {
            loadImages();
        } else {
            showImages();
        }
    }
    
    private void showImages() {
        if (frontContainer != null) {
            frontContainer.setVisibility(VISIBLE);
            frontContainer.setAlpha(0f);
            frontContainer.animate().alpha(1f).setDuration(500).start();
        }
        if (iconView != null) iconView.setVisibility(GONE);
        if (titleView != null) titleView.setVisibility(GONE);
        updateFront();
    }
    
    private void loadImages() {
        boolean hasImages = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        boolean hasPartial = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            hasPartial = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED;
        }
        boolean hasLegacy = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!hasImages && !hasPartial && !hasLegacy) {
            Log.d("PhotoTileView", "Missing storage permissions");
            return;
        }

        Log.d("PhotoTileView", "Loading images...");
        
        new Thread(() -> {
            try {
                // Query ID and DATE_ADDED
                String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED};
                Uri[] contentUris = {MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.INTERNAL_CONTENT_URI};
                
                for (Uri uri : contentUris) {
                    Cursor cursor = getContext().getContentResolver().query(
                            uri, projection, null, null, null);
                    
                    if (cursor != null) {
                        int idCol = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                        int dateCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                        
                        while(cursor.moveToNext()) {
                            long id = cursor.getLong(idCol);
                            long date = 0;
                            if (dateCol != -1) {
                                date = cursor.getLong(dateCol);
                            }
                            photoItems.add(new PhotoItem(id, uri, date * 1000)); // date is seconds, convert to ms
                        }
                        cursor.close();
                    }
                }
                
                if (!photoItems.isEmpty()) {
                    Log.d("PhotoTileView", "Found total " + photoItems.size() + " images");
                    post(this::showImages);
                } else {
                    Log.d("PhotoTileView", "No images found in MediaStore");
                }
            } catch (Exception e) {
                 Log.e("PhotoTileView", "Query Error", e);
            }
        }).start();
    }
    
    private void updateFront() {
         if (photoItems.isEmpty()) return;
         PhotoItem item = photoItems.get((int)(Math.random() * photoItems.size()));
         displayItem(frontImage, frontDate, item);
    }
    
    @Override
    protected void onFlipStart(View nextView) {
        if (photoItems.isEmpty()) return;
        PhotoItem item = photoItems.get((int)(Math.random() * photoItems.size()));
        
        if (nextView == frontContainer) {
            displayItem(frontImage, frontDate, item);
        } else if (nextView == backContainer) {
            displayItem(backImage, backDate, item);
        }
    }

    private void displayItem(ImageView iv, TextView tv, PhotoItem p) {
        if (iv == null || p == null) return;
        
        if (tv != null) {
            tv.setText(dateFormat.format(new java.util.Date(p.dateAdded)));
        }
        
        try {
            Uri contentUri = ContentUris.withAppendedId(p.baseUri, p.id);
            Bitmap bitmap = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                bitmap = getContext().getContentResolver().loadThumbnail(contentUri, new Size(512, 512), null);
            } else {
                bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(), p.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            }

            if (bitmap != null) {
                iv.setImageBitmap(bitmap);
                iv.setBackgroundColor(Color.TRANSPARENT);
            } else {
                iv.setBackgroundColor(0x33000000);
            }
        } catch (Exception e) {
            Log.e("PhotoTileView", "Error in displayItem", e);
            iv.setBackgroundColor(0x44000000);
        }
    }
}
