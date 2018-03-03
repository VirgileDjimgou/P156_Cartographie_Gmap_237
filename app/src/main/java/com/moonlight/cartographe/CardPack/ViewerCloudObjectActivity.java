package com.moonlight.cartographe.CardPack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

// import net.alhazmy13.example.R;
import net.alhazmy13.wordcloud.ColorTemplate;
import net.alhazmy13.wordcloud.WordCloud;
import net.alhazmy13.wordcloud.WordCloudView;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.moonlight.cartographe.R;



public class ViewerCloudObjectActivity extends AppCompatActivity {
    private static final String TAG = "ViewerCloudObjectActivity";
    List<WordCloud> list ;
    String text = "Test:celo:hut";
    private ImageView imageview;
    private Button CallSpeakSetting;
    private CloudObjekt cloudObjekt_dec;
    BatchAnnotateImagesResponse Response = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer_cloud_object);


        CallSpeakSetting = (Button)findViewById(R.id.button);
        CallSpeakSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewerCloudObjectActivity.this, SpeakSettingActivity.class);
                //To pass:
                startActivity(intent);

            }
        });



        try {


            cloudObjekt_dec = AlbumsAdapter.cloudObjekt_View;
            Response = cloudObjekt_dec.getResult();
            System.out.println(Response);
           //  Toast.makeText(this, "Classe  : "+Response, Toast.LENGTH_SHORT).show();
        }catch(Exception ex){
            ex.printStackTrace();
        }

        try {
            generateCLoudWord(Response);
            imageview = (ImageView) findViewById(R.id.imageView_view);
            imageview.setImageBitmap(cloudObjekt_dec.getImage());
            WordCloudView wordCloud = (WordCloudView) findViewById(R.id.wordCloud);
            wordCloud.setDataSet(list);
            wordCloud.setSize(400, 400);
            wordCloud.setColors(ColorTemplate.MATERIAL_COLORS);
            wordCloud.notifyDataSetChanged();
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private void generateCLoudWord(BatchAnnotateImagesResponse Response) {
        // List<String> LabelList = Util.convertResponseToString(Response);
        list = new ArrayList<>();

        List<EntityAnnotation> labels = Response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                int ValueLabel= Math.round(label.getScore()*25);
                list.add(new WordCloud(label.getDescription(),ValueLabel));
                // message.add( String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
            }
        }
    }

}


