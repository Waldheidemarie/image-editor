/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.image.vision.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.image.vision.*;
import com.huawei.hms.image.vision.bean.ImageVisionResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Filter activity.
 *
 * @author huawei
 * @since 1.0.2.301
 */
public class ImageKitVisionDemoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final String TAG = "FilterActivity";
    String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int mRequestCode = 100;
    List<String> mPermissionList = new ArrayList<>();
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    private Button btn_submit;
    private Button btn_init;
    private Button btn_picture;
    private Button btn_stop;
    private EditText btn_filter;
    private EditText btn_compress;
    private EditText btn_intensity;
    private Button btn_gallary;
    private Button btn_send;
    private ImageView iv;
    private TextView tv;
    private TextView tv2;
    private TextView tv_intensity;
    private TextView tv_compress;
    private Spinner spinner;
    private String filterType;
    private String intensity;
    private String compressRate;
    private SeekBar intensitySeekBar;
    private SeekBar compressRateSeekBar;
    private FragmentManager fManager;
    private Fragment choosePhotoFragment;
    private EditPhotoFragment editPhoto;
    String string = "{\"projectId\":\"projectIdTest\",\"appId\":\"appIdTest\",\"authApiKey\":\"authApiKeyTest\",\"clientSecret\":\"clientSecretTest\",\"clientId\":\"clientIdTest\",\"token\":\"tokenTest\"}";
    private JSONObject authJson;

    {
        try {
            authJson = new JSONObject(string);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    private Context context;
    private Bitmap bitmap;
    private int initCodeState = -2;
    private int stopCodeState = -2;

    /**
     * The Image vision api.
     */
    ImageVisionImpl imageVisionAPI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
//        fManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fManager.beginTransaction();
//        choosePhotoFragment =  new ChoosePhotoFragment();
//        fragmentTransaction.add(R.id.view1, choosePhotoFragment);
//        fragmentTransaction.commit();
//
//        editPhoto = new EditPhotoFragment();

        if (Build.VERSION.SDK_INT >= 23) {
            initPermission();
        }
        iv = findViewById(R.id.iv);
        tv = findViewById(R.id.tv);
        tv2 = findViewById(R.id.tv2);
        tv_compress = findViewById(R.id.tv_compress);
        tv_intensity = findViewById(R.id.tv_intensity);
        tv_compress.setVisibility(View.GONE);
        tv_intensity.setVisibility(View.GONE);

        btn_gallary = findViewById(R.id.btn_gallary);
        btn_gallary.setOnClickListener(this);
        btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
        btn_send.setVisibility(View.GONE);
//        btn_init = findViewById(R.id.btn_init);
//        btn_picture = findViewById(R.id.btn_picture);
//        btn_submit = findViewById(R.id.btn_submit);
//        btn_stop = findViewById(R.id.btn_stop);
//
//        btn_submit.setOnClickListener(this);
//        btn_init.setOnClickListener(this);
//        btn_stop.setOnClickListener(this);
//        btn_picture.setOnClickListener(this);

        spinner = (Spinner) findViewById(R.id.filter_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setVisibility(View.GONE);

        intensitySeekBar = findViewById(R.id.intensity_seekbar);
        intensitySeekBar.setVisibility(View.GONE);
        if (intensitySeekBar != null) {
            intensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Write code to perform some action when progress is changed.
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is started.
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is stopped.
                    double progress = seekBar.getProgress() / 10.0;
                    intensity = String.valueOf(progress);
                    Toast.makeText(ImageKitVisionDemoActivity.this, "Intensity is " + intensity, Toast.LENGTH_SHORT).show();
                    if (initCodeState != 0 | stopCodeState == 0) {
                        Log.e("onStopTrackingTouch" ,"The service has not been initialized. Please initialize the service before calling it.");
                    }
                    startFilter(filterType, intensity, compressRate, authJson);
                }
            });
        }

        compressRateSeekBar = findViewById(R.id.compress_rate_seekbar);
        compressRateSeekBar.setVisibility(View.GONE);
        if (compressRateSeekBar != null) {
            compressRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Write code to perform some action when progress is changed.
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is started.
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is stopped.
                    double progress = seekBar.getProgress() / 10.0;
                    compressRate = String.valueOf(progress);
                    Toast.makeText(ImageKitVisionDemoActivity.this, "Compress rate is " + compressRate, Toast.LENGTH_SHORT).show();
                    if (initCodeState != 0 | stopCodeState == 0) {
                        Log.e("onStopTrackingTouch","The service has not been initialized. Please initialize the service before calling it.");
                    }
                    startFilter(filterType, intensity, compressRate, authJson);
                }
            });
        }

    }

    protected void onResume() {
        super.onResume();
        initFilter(this);
    }

    protected void onPause() {
        super.onPause();
        stopFilter();
    }

   //Verify and apply for permissions.
    private void initPermission() {
       // Clear the permissions that fail the verification.
        mPermissionList.clear();
       //Check whether the required permissions are granted.
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
               // Add permissions that have not been granted.
                mPermissionList.add(permissions[i]);
            }
        }
        //Apply for permissions.
        if (mPermissionList.size() > 0) {//The permission has not been granted. Please apply for the permission.
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        } else {
            //The permission has been granted and you can continue with subsequent operations.
        }
    }

    /**
     * @param requestCode Permission request code.
     * @param permissions  An array of requested permission names.
     * @param grantResults An array of granted permission names. The length is equal to length of the corresponding permission names. Value 0 indicates that the permission is granted, and value -1 indicates that the permission is disabled.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//Certain permissions have not been granted.
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
        }
    }

    /**
     * Process the obtained image.
     */

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (null != data) {
            if (resultCode == Activity.RESULT_OK) {
                switch (requestCode) {
                    case 801:
                        try {
                            Uri uri = data.getData();
                            iv.setImageURI(uri);
                            bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                            break;
                        } catch (Exception e) {
                            Log.e(TAG, "Exception: " + e.getMessage());
                        }

                }
            }
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_gallary:
            getByAlbum();
            btn_gallary.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            compressRateSeekBar.setVisibility(View.VISIBLE);
            intensitySeekBar.setVisibility(View.VISIBLE);
            btn_send.setVisibility(View.VISIBLE);
            tv_intensity.setVisibility(View.VISIBLE);
            tv_compress.setVisibility(View.VISIBLE);
            break;
        case R.id.btn_send:
            Bitmap image = ((BitmapDrawable) iv.getDrawable()).getBitmap();
            String storePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/Camera";
            File appDir = new File(storePath);
            if (!appDir.exists()) {
                boolean res = appDir.mkdir();
                if (!res) {
                    Log.e(TAG, "save photo to disk failed");
                    return;
                }
            }
            // Default jpeg file path
            File mFile = new File(appDir, System.currentTimeMillis() + "pic.jpg");
            try {
                FileOutputStream output = new FileOutputStream(mFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, output);
                output.flush();
                output.close();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mFile)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
//        case R.id.btn_picture:
//            getByAlbum(this);
//            break;
//        case R.id.btn_stop:
//            stopFilter();
//            break;
    }

}

    /**
     * Obtain images from the album.
     */

    private void stopFilter() {
        if (null != imageVisionAPI) {
            int stopCode = imageVisionAPI.stop();
            //tv2.setText("stopCode:" + stopCode);
            iv.setImageBitmap(null);
            bitmap = null;
            stopCodeState = stopCode;
        } else {
            tv2.setText("The service has not been enabled.");
            stopCodeState = 0;
        }
    }

    private Context mContext;
    private void initFilter(final Context context) {
        imageVisionAPI = ImageVision.getInstance(this);
        imageVisionAPI.setVisionCallBack(new ImageVision.VisionCallBack() {
            @Override
            public void onSuccess(int successCode) {
                int initCode = imageVisionAPI.init(context, authJson);
                //tv2.setText("initCode:" + initCode);
                long start2 = System.currentTimeMillis();
                initCodeState = initCode;
                stopCodeState = -2;
            }

            @Override
            public void onFailure(int errorCode) {
                Log.e(TAG, "getImageVisionAPI failure, errorCode = " + errorCode);
                //tv2.setText("initFailed");
            }
        });

    }

    private void startFilter(final String filterType, final String intensity,
                             final String compress, final JSONObject authJson) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long start3 = System.currentTimeMillis();
                JSONObject jsonObject = new JSONObject();
                JSONObject taskJson = new JSONObject();
                try {
                    taskJson.put("intensity", intensity);
                    taskJson.put("filterType", filterType);
                    taskJson.put("compressRate", compress);
                    jsonObject.put("requestId", "1");
                    jsonObject.put("taskJson", taskJson);
                    jsonObject.put("authJson", authJson);
                    long start4 = System.currentTimeMillis();
                    Log.e(TAG, "prepare request parameters cost" + (start4 - start3));
                    final ImageVisionResult visionResult = imageVisionAPI.getColorFilter(jsonObject,
                            bitmap);
                    long start5 = System.currentTimeMillis();
                    Log.e(TAG, "interface response cost" + (start5 - start4));
                    iv.post(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap image = visionResult.getImage();
                            iv.setImageBitmap(image);
//                            tv.setText(
//                                    visionResult.getResponse().toString() + "resultCode:" + visionResult
//                                            .getResultCode());
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException: " + e.getMessage());
                }
            }
        };
        executorService.execute(runnable);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String[] selectedItem = adapterView.getSelectedItem().toString().split(" ");
        filterType = selectedItem[1];
        if (initCodeState != 0 | stopCodeState == 0) {
            Log.e("onItemSelected","The service has not been initialized. Please initialize the service before calling it.");
        }
        startFilter(filterType, intensity, compressRate, authJson);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void getByAlbum() {
        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        String[] mimeTypes = {"image/jpeg", "image/png", "image/webp", "image/gif"};
        getAlbum.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        getAlbum.setType("image/*");
        getAlbum.addCategory(Intent.CATEGORY_OPENABLE);
        this.startActivityForResult(getAlbum, 801);
    }

    public static int getQualityNumber(Bitmap bitmap) {
        int size = bitmap.getByteCount();
        int percentage = 0;

        if (size > 500000 && size <= 800000) {
            percentage = 15;
        } else if (size > 800000 && size <= 1000000) {
            percentage = 20;
        } else if (size > 1000000 && size <= 1500000) {
            percentage = 25;
        } else if (size > 1500000 && size <= 2500000) {
            percentage = 27;
        } else if (size > 2500000 && size <= 3500000) {
            percentage = 30;
        } else if (size > 3500000 && size <= 4000000) {
            percentage = 40;
        } else if (size > 4000000 && size <= 5000000) {
            percentage = 50;
        } else if (size > 5000000) {
            percentage = 75;
        }

        return percentage;
    }

}
