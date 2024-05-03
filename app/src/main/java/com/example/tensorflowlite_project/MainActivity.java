package com.example.tensorflowlite_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tensorflowlite_project.ml.AutoModel2;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Drawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageView = findViewById(R.id.imageView);
        drawable = imageView.getDrawable();
        Button imgBtn = findViewById(R.id.button);
        imgBtn.setOnClickListener(view -> pickImageFromGallery());
        Button picBtn = findViewById(R.id.button2);
        picBtn.setOnClickListener(view->dispatchTakePictureIntent());
    }
    private void tensorFlowModel(){
        try {
            TextView textView = findViewById(R.id.textView);
            String resultText = "";
            AutoModel2 model = AutoModel2.newInstance(getApplicationContext());
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(bitmap);

            // Runs model inference and gets result.
            AutoModel2.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();
            List<String> imgTopClass = new ArrayList<String>();
            for (Category category : probability) {
                String label = category.getLabel();
                float score = category.getScore();
                imgTopClass.add(label+":"+score);
                // 將類別名稱和機率添加到顯示文字中
                resultText += label + ":" + score + "\n";
            }
            Comparator<String> comparator = new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    float score1 = Float.parseFloat(o1.split(":")[1]);
                    float score2 = Float.parseFloat(o2.split(":")[1]);
                    // 降序排序
                    return Float.compare(score2, score1);
                }
            };
            Collections.sort(imgTopClass,comparator);
            String finalResult = "";
            for (int i = 0; i < Math.min(imgTopClass.size(),1); i++) {
                String label = imgTopClass.get(i).split(":")[0];
                String confidence = imgTopClass.get(i).split(":")[1];
                finalResult += label + ":" + Float.parseFloat(confidence)*100 + "\n";
            }
            textView.setText(finalResult);
            Log.d("MyTag",resultText);
            Log.d("TagClass",imgTopClass.toString());
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }
    // 啟動相機拍照
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }
    }

    // 從圖庫中選擇圖片
    private void pickImageFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
                drawable = imageView.getDrawable();
                tensorFlowModel();
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    imageView.setImageBitmap(imageBitmap);
                    drawable = imageView.getDrawable();
                    tensorFlowModel();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}