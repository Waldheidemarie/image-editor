package com.huawei.image.vision.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

//import com.huawei.imagekit.vision.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChoosePhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChoosePhotoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button mButtonGallary;
    private EditPhotoFragment editPhoto;

    public ChoosePhotoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChoosePhotoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChoosePhotoFragment newInstance(String param1, String param2) {
        ChoosePhotoFragment fragment = new ChoosePhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editPhoto = new EditPhotoFragment();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_choose_photo, container, false);
        mButtonGallary = v.findViewById(R.id.btn_gallary);
        mButtonGallary.setOnClickListener(this::getByAlbum);
        return v;
    }

    public void getByAlbum(View v) {
        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        String[] mimeTypes = {"image/jpeg", "image/png", "image/webp", "image/gif"};
        getAlbum.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        getAlbum.setType("image/*");
        getAlbum.addCategory(Intent.CATEGORY_OPENABLE);
        this.startActivityForResult(getAlbum, 801);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != data) {
            if (resultCode == Activity.RESULT_OK) {
                switch (requestCode) {
                    case 801:
                        try {
                            Uri uri = data.getData();
//                            Bundle args = new Bundle();
//                            args.putString("image", uri);
                            //Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.view1);
                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            Fragment editPhotoFragment = new EditPhotoFragment();
                            fragmentTransaction.replace(R.id.view1, editPhotoFragment);
                            fragmentTransaction.show(editPhotoFragment);
                            fragmentTransaction.commit();
                            ImageView iv = editPhotoFragment.getView().findViewById(R.id.iv);
                            iv.setImageURI(uri);
                            editPhoto.bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                            break;
                        } catch (Exception e) {
                            Log.e(editPhoto.TAG, "Exception: " + e.getMessage());
                        }

                }
            }
        }
    }

}