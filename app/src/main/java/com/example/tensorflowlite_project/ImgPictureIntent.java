package com.example.tensorflowlite_project;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;

public class ImgPictureIntent {
    // 啟動相機拍照
    public static void dispatchTakePictureIntent(Context context) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            ((Activity) context).startActivityForResult(takePictureIntent, 1);
        }
    }

    // 從圖庫中選擇圖片
    public static void pickImageFromGallery(Context context) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((Activity) context).startActivityForResult(pickPhoto, 2);
    }
}
