package tech.ioengine.Login.fotopicker.CardPack;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpicker.ImagePickerActivity;
import tech.ioengine.Login.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity_card extends AppCompatActivity {
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    ArrayList<String> images;
    ArrayList<Uri> uris = new ArrayList<>();
    ArrayAdapter<String> adapter_Images;

    EditText editTxt;
    ImageView prevImg;
    String filename;
    private ProgressBar progressBar;

    private File GIF_DIR;
    private String DEFAULT_TITLE;
    private int COMPRESSION; // not a big diff eh?
    private int SAMPLE_SIZE = 3; // ?? unclear to me...
    private int REPEAT;
    private int DELAY; // milliseconds

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_card);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initCollapsingToolbar();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        CloudObjektList = new ArrayList<>();
        adapter = new AlbumsAdapter(this, CloudObjektList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

       //  prepareAlbums();

        FloatingActionButton nativeCam = (FloatingActionButton) findViewById(R.id.native_cam);
        nativeCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity_card.this);
                builder
                        .setMessage(R.string.dialog_select_prompt)
                        .setPositiveButton(R.string.dialog_select_multiple_photo, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // startGalleryChooser();

                                showFileChooser();
                                Snackbar.make(view, "Select Image", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        })
                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCamera();
                            }

                        });
                builder.create().show();
            }
        });
        this.mContext = mContext;


        // mImageDetails = (TextView) findViewById(R.id.image_details);

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
                    Toast.makeText(this,"Number of Images : "+Num_images, Toast.LENGTH_LONG).show();

                    for(int i=0; i<image_uris.size(); i++) {

                        Uri uri = Uri.fromFile(new File(image_uris.get(i).toString()));// URI, not file, of selected File
                        uploadImage(uri);
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
                }
                break;
        }



        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
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

    public void uploadImage(Uri uri) {
        System.out.println(uri);
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri), 1200);
            } catch(Exception ex){
                    ex.printStackTrace();
            }

              try{
                //callCloudVision(bitmap);
                  System.out.println(bitmap.getDensity());
                  System.out.println(bitmap.getHeight());

                  // mMainImage.setImageBitmap(bitmap);
                //  Test das muss spater weg ...
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
                        try {
                            callCloudVision(newCloudObjekt , IndexObjekt);
                           //  Toast.makeText (mContext,"please wait .....", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
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
    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private void callCloudVision(final CloudObjekt CloudObjekt , final int IndexObjekt) throws IOException {
        // Switch text to loading
        // mImageDetails.setText(R.string.loading_message);
        // Toast.makeText(MainActivity_card.this,"Index new Objekt : " + IndexObjekt , Toast.LENGTH_LONG).show();

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
