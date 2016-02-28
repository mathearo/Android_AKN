package com.kshrd.android_akn.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.util.AKNNavigationView;
import com.kshrd.android_akn.util.BitmapEfficient;
import com.kshrd.android_akn.util.MultipartUtility;
import com.kshrd.android_akn.util.Session;
import com.kshrd.android_akn.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProfileActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 1;
    private Toolbar mToolbar;
    private EditText editName, editEmail;
    private Button btnUpdate, btnBrowsePhoto, btnRemovePhoto;
    private TextView tvChangePassword;
    private CircularImageView imgProfile;
    private ImageLoader imageLoader;
    private String picturePath = null;
    private String userId;
    private int userIdInt;
    private String username,userimg;
    private Bitmap bitmap,oldBitmap;
    private ProgressBar progressSpinning;
    private ProgressDialog proDialog;
    private AKNNavigationView aknNavigationView;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if user changed language
        SharedPreferences sharedPref = getSharedPreferences("setting",
                Context.MODE_PRIVATE);
        Locale locale = null;
        Configuration config = null;

        if (locale == null) locale = new Locale(sharedPref.getString("LANGUAGE", "en"));
        Locale.setDefault(locale);
        if (config == null) config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());

        setContentView(R.layout.drawer_layout);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_profile, frameLayout);
        aknNavigationView = new AKNNavigationView(this);

        // Init Progress Dialog
        proDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        // Google Analytics
        //Util.setDefaultTracker(this, "AKN, Screen Name", "ProfileActivity");
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name ProfileActivity");
        mTracker.setScreenName("AKN, Screen Name ProfileActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // Set up Toolbar
        mToolbar = (Toolbar)findViewById(R.id.profile_toolbar);
        mToolbar.setTitle(R.string.profile);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgProfile =(CircularImageView)findViewById(R.id.profile_picture);
        //tvRemovePicture = (TextView)findViewById(R.id.remove_picture);

        editName = (EditText)findViewById(R.id.edit_profile_name);
        tvChangePassword = (TextView)findViewById(R.id.tv_change_password);
        btnUpdate = (Button)findViewById(R.id.btn_update);
        btnBrowsePhoto = (Button) findViewById(R.id.btnBrowsePhoto);
        btnRemovePhoto = (Button) findViewById(R.id.btnRemovePhoto);
        btnBrowsePhoto.setVisibility(View.VISIBLE);
        imageLoader = AppController.getInstance().getImageLoader();
        progressSpinning = (ProgressBar)findViewById(R.id.progressSpinning);

        Session.readUserSession(this);
        userId = Session.ENCRYPTED_USER_ID;
        username = Session.USER_NAME;
        userimg =  Session.PROFILE_IMAGE_URL;

        try {
            String cacheFile = getCacheDir() + "/mathearo_avatar.png";
            File f = new File(cacheFile);
            if (f.exists()) {
                Bitmap tmp = BitmapFactory.decodeFile(cacheFile);
                imgProfile.setImageBitmap(tmp);
            } else {
                if(!userimg.equals("")) {
                    //imageLoader.get("http://api.khmeracademy.org/resources/upload/file/" + userimg, new ImageLoader.ImageListener() {
                    imageLoader.get(Util.BASE_PROFILE_IMAGE_URL + userimg, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                imgProfile.setImageBitmap(response.getBitmap());
                                oldBitmap = response.getBitmap();
                                bitmap = oldBitmap;
                                if (proDialog != null) proDialog.dismiss();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        progressSpinning.setVisibility(View.GONE);
        editName.setText(username);

        // set events
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString().trim();
                if (name != null) {
                    if(oldBitmap == bitmap) {
                        Log.d("update_profile","update" + userimg);
                        try {
                            requestUpdate(userimg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        Log.d("update_profile", "upload  " + picturePath);
                        new UploadTask().execute(picturePath);
                    }
                    picturePath = "";
                }
            }
        });

        tvChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        btnBrowsePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        btnRemovePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgProfile.setImageBitmap(oldBitmap);
                btnRemovePhoto.setVisibility(View.GONE);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Util.setDefaultTracker(this, "AKN, Screen Name", "ProfileActivity");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name ProfileActivity");
        mTracker.setScreenName("AKN, Screen Name ProfileActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    // Capture image from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data!=null){
            Uri selectedImage = data.getData();

            Log.d("test_profile","getData: " + data.getData().toString());

            if (data.getData() == null) {
                Log.d("test_profile", "image null");
            }

            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath,options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            String imageType = options.outMimeType;

            if(imageHeight >400 && imageWidth > 400){
                bitmap = BitmapEfficient.decodeSampledBitmapFromFile(picturePath,400,400);
            }else bitmap = BitmapFactory.decodeFile(picturePath);

            // Prevent null on ImageView
            if (bitmap != null) {
                imgProfile.setImageBitmap(bitmap);
                btnRemovePhoto.setVisibility(View.VISIBLE);
            } else {
                //Log.d("test_profile", "bitmap null");
                Util.showAlertDialog(this, "Invalid Image Path", "Please choose other image!", true, SweetAlertDialog.ERROR_TYPE);
            }

          }
    }

    // Upload image process background
    private class UploadTask extends AsyncTask<String, Void, Void> {
        /*private String url = Util.BASE_URL + "/api/user/editupload?id=" + userId;*/
        private String urlUpload = "http://api.khmeracademy.org/api/uploadfile/upload?url=user";
        private String urlUpdate = "http://api.khmeracademy.org/api/uploadfile/update?url=user&filename="+userimg.replace("user/","");
        String charset = "UTF-8";
        String responseContent = null;
        File file = null;

        @Override
        protected Void doInBackground(String... params) {
            sendFileToServer(params[0]);
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressSpinning.setVisibility(View.VISIBLE);
            super.onPreExecute();
            file = BitmapEfficient.persistImage(bitmap,getApplicationContext());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // update user image url
            if(responseContent != null) {
                try {
                    JSONObject objResponse = new JSONObject(responseContent);
                    //responseContent = responseContent.substring(0,22);
                    boolean status = objResponse.getBoolean("STATUS");
                    if(status) {
                        String imgUrl = objResponse.getString("IMG");
                        imgUrl = imgUrl.replace("/resources/upload/file/","");
                        Log.d("URL Update", imgUrl);
                        requestUpdate(imgUrl);
                    }else  {
                        Toast.makeText(ProfileActivity.this, "Upload Failed.", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(ProfileActivity.this, "Uploaded Fail!", Toast.LENGTH_SHORT).show();
            }
            btnRemovePhoto.setVisibility(View.GONE);
            progressSpinning.setVisibility(View.GONE);

        }

        // upload large file size
        public void sendFileToServer(String filePath) {
            try {
                String url = "";
                if(userimg.equals("user/avatar.jpg")) {
                    url = urlUpload;
                }else url = urlUpdate;
                MultipartUtility multipart = new MultipartUtility(url, charset);
                multipart.addFormField("description", "Cool Pictures");
                multipart.addFormField("keywords", "Java,upload,Spring");

                multipart.addFilePart("fileUpload", file);

                List<String> response = multipart.finish();

                System.out.println("SERVER REPLIED:");

                for (String line : response) {
                    if(line!=null){
                        responseContent = line;
                        Log.d("Upload",responseContent);
                        break;
                    }
                    System.out.println(line);
                }

            } catch (IOException ex) {
                System.err.println(ex);
            }

        }

    }

    public void requestUpdate(final String imageUrl) throws JSONException {
        proDialog.show();
        String url = "http://api.khmeracademy.org/api/user";
        JSONObject object = new JSONObject();
        username = editName.getText().toString().trim();
        object.put("username",username);
        object.put("gender","male");
        object.put("dateOfBirth","2016-02-10");
        object.put("phoneNumber","010010010");
        object.put("userImageUrl",imageUrl);
        object.put("universityId","MQ==");
        object.put("departmentId","MQ==");
        object.put("userId", userId);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Resopnse",": Doing");
                try {
                    boolean status = response.getBoolean("STATUS");
                    Log.d("STATUS",status+"");
                    if (status) {
                        oldBitmap = bitmap;
                        Session.readUserSession(ProfileActivity.this);
                        Log.d("testing_profile", imageUrl);
                        Session.updateProfileImageUrl(imageUrl);
                        Session.updateName(username);

                        // Update user profile on Navigation Drawer (Side Bar)
                        if (bitmap != null) {
                            Log.d("test_profile", "updateProfileWithBitmap");
                            aknNavigationView.updateProfileOnNavHeader(oldBitmap);
                        } else {
                            aknNavigationView.updateProfileOnNavHeader();
                        }

                        //aknNavigationView.updateProfileOnNavHeader();
                        Util.showAlertDialog(ProfileActivity.this, getString(R.string.success_update_profile), true, SweetAlertDialog.SUCCESS_TYPE);

                        //Toast.makeText(ProfileActivity.this, "Successfully Updated profile.", Toast.LENGTH_SHORT).show();
                    }else{
                        Util.showAlertDialog(ProfileActivity.this, "Failed to update profile.", true, SweetAlertDialog.WARNING_TYPE);
                        //Toast.makeText(ProfileActivity.this, "Updated profile failed!", Toast.LENGTH_SHORT).show();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                } finally {
                    if (proDialog != null) proDialog.dismiss();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                header.put("Authorization","Basic S0FBUEkhQCMkOiFAIyRLQUFQSQ==");
                return header;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return Util.getRetryPolicy();
            }
        };
        //request.setShouldCache(false);
        AppController.getInstance().getRequestQueue().add(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
