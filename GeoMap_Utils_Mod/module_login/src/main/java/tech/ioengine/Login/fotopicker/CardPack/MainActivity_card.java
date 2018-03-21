package tech.ioengine.Login.fotopicker.CardPack;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpicker.Config;
import com.gun0912.tedpicker.ImagePickerActivity;
import com.irozon.sneaker.Sneaker;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import tech.ioengine.Login.MainActivity_map;
import tech.ioengine.Login.R;
import tech.ioengine.Login.activity.EmailLoginActivity;
import tech.ioengine.Login.activity.SplaschScreen;
import tech.ioengine.Login.data.FriendDB;
import tech.ioengine.Login.data.GroupDB;
import tech.ioengine.Login.data.SharedPreferenceHelper;
import tech.ioengine.Login.data.StaticConfig;
import tech.ioengine.Login.model.CustomPoint;
import tech.ioengine.Login.model.User;
import tech.ioengine.Login.service.ServiceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity_card extends AppCompatActivity {
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private int SAMPLE_SIZE = 3; // ?? unclear to me...


    private  int number_of_Images = 0 ;
    private static final String CLOUD_VISION_API_KEY = "AIzaSyDjBUzAYg2NrFS1cZkVkYKgCWoleOpI5Pg";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private static final String TAG = MainActivity_card.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    private Context mContext;

    private TextView mImageDetails;
    // private ImageView mMainImage;


    private RecyclerView recyclerView;
    private AlbumsAdapter adapter;
    private List<CloudObjekt> CloudObjektList;
    private Bitmap bitmap;
    private String  place_name ;
    private String latitude , longitude ;
    private LovelyProgressDialog waitingDialog;
    private Context context;
    private Activity ActivityInstance;
    // private Uri resultUri;
    public static FirebaseUser user;
    private DatabaseReference mDriverDatabase;
    public static FirebaseAuth mAuth;
    public static  FirebaseAuth.AuthStateListener mAuthListener;
    public static String requestIdUpload= "";
    private int COMPRESSION; // not a big diff eh?
    DatabaseReference PointsRef;
    private static List<String> listfilePath = new ArrayList<String>();

    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton   About  , TakeNewPoint;
    private com.github.clans.fab.FloatingActionButton  Licences;
    private int UpdateClidAfter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_card);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        CloudObjektList = new ArrayList<>();
        adapter = new AlbumsAdapter(this, CloudObjektList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        this.mContext = mContext;
        this.place_name = getIntent().getStringExtra("Place_name");
        this.latitude = getIntent().getStringExtra("Lat");
        this.longitude  = getIntent().getStringExtra("Long");

        Sneaker.with(this)
                .setTitle("Success!!")
                .setMessage(" infos ... " + this.place_name + this.latitude + this.longitude)
                .sneakSuccess();


        context = this.getApplicationContext();
        waitingDialog = new LovelyProgressDialog(context);
        ActivityInstance = this.getParent();

        mAuth = FirebaseAuth.getInstance();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child(StaticConfig.UserType+"/").child(StaticConfig.UID);
        PointsRef = FirebaseDatabase.getInstance().getReference().child("Points");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    mDriverDatabase = FirebaseDatabase.getInstance().getReference().child(StaticConfig.UserType+"/").child(StaticConfig.UID);
                    Sneaker.with(ActivityInstance)
                            .setTitle("Success!!")
                            .setMessage("valid User ")
                            .sneakSuccess();

                } else {
                    Sneaker.with(ActivityInstance)
                            .setTitle("Success!!")
                            .setMessage(" invalid User !  .... ")
                            .sneakError();
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        
        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        About = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.About);
        TakeNewPoint = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.SaveAnotherPoint);
        About.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu first item clicked

                new MaterialDialog.Builder(MainActivity_card.this)
                        .title(R.string.app_name)
                        .content(R.string.content)
                        .positiveText(R.string.agree)
                        .show();

            }
        });



        TakeNewPoint.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(MainActivity_card.this, MainActivity_map.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }

        );


        // initialize fab Button ...
        // start Ted Picker  ....
        showFileChooser();

    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Take a photos"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("Test");
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    //   ListView lv = (ListView) findViewById(R.id.listImage);


                    ArrayList<Uri> image_uris = data.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);
                    int Num_images = image_uris.size();
                    number_of_Images = number_of_Images + Num_images;
                    Toast.makeText(this,"Number of Images : "+Num_images, Toast.LENGTH_LONG).show();

                    for(int i=0; i<image_uris.size(); i++) {

                        Uri uri = Uri.fromFile(new File(image_uris.get(i).toString()));// URI, not file, of selected File
                        uploadImgetoFirebase(uri , i, image_uris.size());
                        Bitmap bitmap = null;
                        try {

                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Compress
                        File tmpFile = new File(Environment.getExternalStorageDirectory() +
                                File.separator + "tmp.jpeg");
                        FileOutputStream out = null;
                        try {
                            tmpFile.createNewFile();
                            out = new FileOutputStream(tmpFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION, out);
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = SAMPLE_SIZE;
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeFile(tmpFile.getPath(), options);
                        Log.d("tmpFile", tmpFile.getPath());
                        tmpFile.delete();
                        bitmaps.add(bitmap);
                        // TODO: FILE name?
                       //  String[] imageId = uri.getPath().split(":");
                       // images.add("Preview Image: " + imageId[imageId.length - 1]);
                       // uris.add(uri);
                    }

                    // upload new points with reference
                    // saveNewPoints();

                }
                break;
        }



        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            number_of_Images = number_of_Images +1;
            uploadImgetoFirebase(data.getData(), 0, number_of_Images);
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            number_of_Images = number_of_Images +1;
            uploadImgetoFirebase(photoUri ,0, number_of_Images);
        }
    }



    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }


    public void saveNewPoints(){


        try{

            Date date = new Date();
            CustomPoint Point = new CustomPoint();
            Point.namePoint = place_name;
            Point.latitude = latitude;
            Point.longitude = longitude;
            Point.TimeSpan = date.toString();

            String requestId = PointsRef.push().getKey();
            requestIdUpload = requestId;
            HashMap map = new HashMap();
            map.put("Place", place_name);
            map.put("UserID",StaticConfig.UID);
            map.put("UserName", StaticConfig.STR_EXTRA_USERNAME);
            map.put("Email" , StaticConfig.STR_EXTRA_EMAIL);
            map.put("timestamp", date.toString());
            map.put("lat", latitude);
            map.put("lng", longitude);


            for(int i = 0; i < listfilePath.size(); i++) {
                map.put("Image"+i, listfilePath.get(i).toString());
            }


            PointsRef.child(requestId).updateChildren(map);
            // FirebaseDatabase.getInstance().getReference().child("Driver/"+ user.getUid()).setValue(newUser);
            // FirebaseDatabase.getInstance().getReference().child(StaticConfig.UserType+"/"+ StaticConfig.UID+"/").setValue(Point);
            // end progress dialog ...
            Sneaker.with(this)
                    .setTitle("Success!!")
                    .setMessage(" SUcces  new google Point added !!!" + this.place_name + this.latitude + this.longitude)
                    .sneakSuccess();
        }catch(Exception ex){
            Sneaker.with(this)
                    .setTitle("Error !!")
                    .setMessage("Error ....  " + this.place_name + this.latitude + this.longitude)
                    .sneakError();
            ex.printStackTrace();

        }

    }


    public void uploadImgetoFirebase( Uri resultUri , int numbImg , int ImagesSize){



        // Update Card View  ....
        if (resultUri != null) {
            try {
                // scale the image to save on bandwidth
                bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri), 1200);
            } catch(Exception ex){
                ex.printStackTrace();
            }

            try{
                new Thread(new Runnable() {
                    public void run(){
                        Integer  IndexObjekt = CloudObjektList.size();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
                        // BatchAnnotateImagesResponse InitialResponse = null;
                        CloudObjekt newCloudObjekt = new CloudObjekt(date.toString(), 13, bitmap ,false);
                        try {
                            CloudObjektList.add(newCloudObjekt);
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }).start();

                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }

        // upload Images in Firebase storage ....
        try{

            if(resultUri != null) {


                String requestId = PointsRef.push().getKey();
                StorageReference filePath = FirebaseStorage.getInstance().getReference().child("GmapImages").child(requestId);


                // PointsRef.child("filePathPhoto"+numbImg).setValue(filePath);
                // StorageReference filePath = FirebaseStorage.getInstance().getReference().child("GmapImages").child(number_of_Images+"-#"+requestIdUpload);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                } catch (IOException e) {
                    // waitingDialog.dismiss();
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = filePath.putBytes(data);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // getActivity().finish();

                        // waitingDialog.dismiss();
                        Sneaker.with(MainActivity_card.this)
                                .setTitle("Success!!")
                                .setMessage("upload photo failed ")
                                .sneakError();

                        Log.d("Upload photo failed ", "failed");

                        return;
                    }
                });
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        // waitingDialog.dismiss();
                        Map newImage = new HashMap();
                        newImage.put("ImageUrl", downloadUrl.toString());
                        mDriverDatabase.updateChildren(newImage);
                        listfilePath.add( downloadUrl.toString());

                        if(numbImg == ImagesSize-1){
                            saveNewPoints();
                            Sneaker.with(MainActivity_card.this)
                                    .setTitle("Success!!")
                                    .setMessage("success !!")
                                    .sneakSuccess();


                        }
                    }
                });

            }else{
                // waitingDialog.dismiss();
                Sneaker.with(MainActivity_card.this)
                        .setTitle("Success!!")
                        .setMessage("please select a valid Image")
                        .sneakError();
            }

        }catch(Exception ex){
            // waitingDialog.dismiss();
            ex.printStackTrace();
        }
        // waitingDialog.dismiss();

    }


    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }



    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    private void showFileChooser() {
        Config config = new Config();
        config.setToolbarTitleRes(R.string.custom_title);
        config.setCameraHeight(R.dimen.app_defaulsize_w);
        config.setSelectionMin(1);
        config.setSelectionLimit(5);
        config.setFlashOn(true);
        ImagePickerActivity.setConfig(config);



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 0);
        }
        Intent intent = new Intent(MainActivity_card.this, ImagePickerActivity.class);
        startActivityForResult(intent, 0);

    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
