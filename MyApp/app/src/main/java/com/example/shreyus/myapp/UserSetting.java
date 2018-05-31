package com.example.shreyus.myapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class UserSetting extends AppCompatActivity {
    public static final String PREFS_NAME = "MyContact";
    public static final String person1_id = "person1";
    public static final String person2_id = "person2";
    public static final String person3_id = "person3";
    public static final String person1_name= "person1_name";
    public static final String person2_name = "person2_name";
    public static final String person3_name = "person3_name";
    public static final int RESULT_UPDATE = 886;

    public static final int SELECT_IMG_1 = 123;
    public static final int SELECT_IMG_2 = 234;
    public static final int SELECT_IMG_3 = 345;
    public static final String person1_img= "person1_img";
    public static final String person2_img= "person2_img";
    public static final String person3_img= "person3_img";
    Button selectImg1,selectImg2,selectImg3;
    ImageView imageView1,imageView2,imageView3;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        //Intent intent = getIntent();
        SharedPreferences dataSaved = getSharedPreferences(PREFS_NAME, 0);

        /******** Select Image *********/
        selectImg1 = findViewById(R.id.btnSetImg_1);
        selectImg2 = findViewById(R.id.btnSetImg_2);
        selectImg3 = findViewById(R.id.btnSetImg_3);

        imageView1 = findViewById(R.id.imageView_1);
        imageView2 = findViewById(R.id.imageView_2);
        imageView3 = findViewById(R.id.imageView_3);

        selectImg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Photo: "), SELECT_IMG_1);
            }
        });
        selectImg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Photo: "), SELECT_IMG_2);
            }
        });
        selectImg3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Photo: "), SELECT_IMG_3);
            }
        });
        /******** End Select Image Intent *********/

        /******* Update Image *********/
        UpdateImage(dataSaved,person1_img,imageView1);
        UpdateImage(dataSaved,person2_img,imageView2);
        UpdateImage(dataSaved,person3_img,imageView3);

        /******** Display info *********/
        EditText editNum1 = findViewById(R.id.editNum1);
        EditText editNum2 = findViewById(R.id.editNum2);
        EditText editNum3 = findViewById(R.id.editNum3);
        EditText personName1 = findViewById(R.id.person1);
        EditText personName2 = findViewById(R.id.person2);
        EditText personName3 = findViewById(R.id.person3);


        String num1 = dataSaved.getString(person1_id,null);
        String num2 = dataSaved.getString(person2_id,null);
        String num3 = dataSaved.getString(person3_id,null);
        String name1 = dataSaved.getString(person1_name,null);
        String name2 = dataSaved.getString(person2_name,null);
        String name3 = dataSaved.getString(person3_name,null);

        editNum1.setText(num1);
        editNum2.setText(num2);
        editNum3.setText(num3);
        personName1.setText(name1);
        personName2.setText(name2);
        personName3.setText(name3);
        /******** End Display info *********/
    }

    public void SaveInfo(View view) {

        Intent tomainIntent = new Intent(this, MainActivity.class);

        EditText editNum1 = findViewById(R.id.editNum1);
        EditText editNum2 = findViewById(R.id.editNum2);
        EditText editNum3 = findViewById(R.id.editNum3);
        EditText personName1 = findViewById(R.id.person1);
        EditText personName2 = findViewById(R.id.person2);
        EditText personName3 = findViewById(R.id.person3);

        String num1 = editNum1.getText().toString();
        String num2 = editNum2.getText().toString();
        String num3 = editNum3.getText().toString();
        String name1 = personName1.getText().toString();
        String name2 = personName2.getText().toString();
        String name3 = personName3.getText().toString();

        //saved to file
        SharedPreferences savedFile = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = savedFile.edit();
        editor.putString(person1_id, num1);
        editor.putString(person2_id,num2);
        editor.putString(person3_id,num3);
        editor.putString(person1_name,name1);
        editor.putString(person2_name,name2);
        editor.putString(person3_name,name3);
        editor.commit();


        setResult(RESULT_UPDATE,tomainIntent);
        finish();

    }

    /******** Intent result from Photo Gallery ********************/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == SELECT_IMG_1 && resultCode == RESULT_OK  && data != null && data.getData() != null){
            Uri uri = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                //Bitmap compressedBitmap = resize(uri,bitmap);
                Bitmap compressedBitmap = scaleDownAndRotatePic(uri,bitmap);
                setImage(imageView1,compressedBitmap,person1_img);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(requestCode == SELECT_IMG_2 && resultCode == RESULT_OK  && data != null && data.getData() != null){
            Uri uri = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                Bitmap compressedBitmap = resize(uri,bitmap);
                setImage(imageView2,compressedBitmap,person2_img);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(requestCode == SELECT_IMG_3 && resultCode == RESULT_OK  && data != null && data.getData() != null){
            Uri uri = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                Bitmap compressedBitmap = resize(uri,bitmap);
                setImage(imageView3,compressedBitmap,person3_img);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void setImage(ImageView imageView, Bitmap bitmap, String person){
        imageView.setImageBitmap(bitmap);
        SharedPreferences savedFile = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = savedFile.edit();
        editor.putString(person,encodeTobase64(bitmap));
        editor.commit();
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void UpdateImage(SharedPreferences dataSaved, String person, ImageView img){

        String bitmapFromSetting = dataSaved.getString(person, null);
        if(bitmapFromSetting!=null && !bitmapFromSetting.isEmpty()) {
            Bitmap bitmap = decodeBase64(bitmapFromSetting);
            img.setImageBitmap(bitmap);
        }
    }
    //Image encoding
    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }
    // Decode image string base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static Bitmap resize(Uri uri,Bitmap bitmap){
        int maxSize = 960;
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        Bitmap compressedBitmap = Bitmap.createScaledBitmap(bitmap,outWidth,outHeight,true);

        return compressedBitmap;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Bitmap scaleDownAndRotatePic(Uri uri, Bitmap bitmap) {//you can provide file path here
        int orientation;
        InputStream in;
        try {

            Bitmap bm = resize(uri,bitmap);
            in = getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(in);

            //orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            //Toast.makeText(getApplicationContext(), "Exif orientation: " + orientation,Toast.LENGTH_LONG).show();
            //Bitmap rotatedBitmap = rotateBitmap(bm, orientation);
            //Toast.makeText(getApplicationContext(), "Exif rotation: " + exif.toString(),Toast.LENGTH_LONG).show();

            //return bm;

            Log.e("ExifInteface .........", "rotation =" + orientation);


            Log.e("orientation", "" + orientation);
            Matrix m = new Matrix();

            if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                m.postRotate(180);
                //m.postScale((float) bm.getWidth(), (float) bm.getHeight());
                // if(m.preRotate(90)){
                Log.e("in orientation", "" + orientation);
                Toast.makeText(getApplicationContext(), "Exif orientation: " + orientation,Toast.LENGTH_LONG).show();
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),bm.getHeight(), m, true);
                return bitmap;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                m.postRotate(90);
                Log.e("in orientation", "" + orientation);
                Toast.makeText(getApplicationContext(), "Exif orientation: " + orientation,Toast.LENGTH_LONG).show();
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),bm.getHeight(), m, true);
                return bitmap;
            }
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                m.postRotate(270);
                Log.e("in orientation", "" + orientation);
                Toast.makeText(getApplicationContext(), "Exif orientation: " + orientation,Toast.LENGTH_LONG).show();
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),bm.getHeight(), m, true);
                return bitmap;
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();

            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}
