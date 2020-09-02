package com.huawei.image.vision.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import com.huawei.imagekit.vision.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditPhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditPhotoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static final String TAG = "EditPhotoFragment";
    String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int mRequestCode = 100;
    List<String> mPermissionList = new ArrayList<>();
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    protected ImageView iv;
    private TextView tv;
    private TextView tv2;
    private Spinner spinner;
    private String filterType;
    private String intensity;
    private String compressRate;
    private SeekBar intensitySeekBar;
    private SeekBar compressRateSeekBar;

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
    protected Bitmap bitmap;
    private int initCodeState = -2;
    private int stopCodeState = -2;

    /**
     * The Image vision api.
     */
    ImageVisionImpl imageVisionAPI = null;


    public EditPhotoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditPhotoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditPhotoFragment newInstance(String param1, String param2) {
        EditPhotoFragment fragment = new EditPhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_photo, container, false);
        if (Build.VERSION.SDK_INT >= 23) {
            initPermission();
        }

        tv = v.findViewById(R.id.tv);
        tv2 = v.findViewById(R.id.tv2);
        iv = v.findViewById(R.id.iv);

        spinner = (Spinner) v.findViewById(R.id.filter_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] selectedItem = adapterView.getSelectedItem().toString().split(" ");
                filterType = selectedItem[1];
                if (initCodeState != 0 | stopCodeState == 0) {
                    tv2.setText("The service has not been initialized. Please initialize the service before calling it.");
                }
                startFilter(filterType, intensity, compressRate, authJson);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        intensitySeekBar = v.findViewById(R.id.intensity_seekbar);
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
                    Toast.makeText(getContext(), "Intensity is " + intensity, Toast.LENGTH_SHORT).show();
                    if (initCodeState != 0 | stopCodeState == 0) {
                        tv2.setText("The service has not been initialized. Please initialize the service before calling it.");
                    }
                    startFilter(filterType, intensity, compressRate, authJson);
                }
            });
        }

        compressRateSeekBar = v.findViewById(R.id.compress_rate_seekbar);
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
                    Toast.makeText(getActivity(), "Compress rate is " + compressRate, Toast.LENGTH_SHORT).show();
                    if (initCodeState != 0 | stopCodeState == 0) {
                        tv2.setText("The service has not been initialized. Please initialize the service before calling it.");
                    }
                    startFilter(filterType, intensity, compressRate, authJson);
                }
            });
        }
        initFilter(getActivity());
        return v;
    }

    public void onResume() {
        super.onResume();
        if (imageVisionAPI == null)
            initFilter(getContext());
    }

    public void onPause() {
        super.onPause();
        stopFilter();
    }

    //Verify and apply for permissions.
    private void initPermission() {
        // Clear the permissions that fail the verification.
        mPermissionList.clear();
        //Check whether the required permissions are granted.
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(getContext(), permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                // Add permissions that have not been granted.
                mPermissionList.add(permissions[i]);
            }
        }
        //Apply for permissions.
        if (mPermissionList.size() > 0) {//The permission has not been granted. Please apply for the permission.
            ActivityCompat.requestPermissions(getActivity(), permissions, mRequestCode);
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
    private void stopFilter() {
        if (null != imageVisionAPI) {
            int stopCode = imageVisionAPI.stop();
            tv2.setText("stopCode:" + stopCode);
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
        imageVisionAPI = ImageVision.getInstance(getContext());
        imageVisionAPI.setVisionCallBack(new ImageVision.VisionCallBack() {
            @Override
            public void onSuccess(int successCode) {
                int initCode = imageVisionAPI.init(context, authJson);
                tv2.setText("initCode:" + initCode);
                long start2 = System.currentTimeMillis();
                initCodeState = initCode;
                stopCodeState = -2;
            }

            @Override
            public void onFailure(int errorCode) {
                Log.e(TAG, "getImageVisionAPI failure, errorCode = " + errorCode);
                tv2.setText("initFailed");
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
                            tv.setText(
                                    visionResult.getResponse().toString() + "resultCode:" + visionResult
                                            .getResultCode());
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException: " + e.getMessage());
                }
            }
        };
        executorService.execute(runnable);
    }

//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (null != data) {
//            if (resultCode == Activity.RESULT_OK) {
//                switch (requestCode) {
//                    case 801:
//                        try {
//                            Uri uri = data.getData();
//                            iv.setImageURI(uri);
//                            bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
//                            break;
//                        } catch (Exception e) {
//                            Log.e(TAG, "Exception: " + e.getMessage());
//                        }
//
//                }
//            }
//        }
//    }
}