package com.xhaka.wasist.CardPack;

import android.widget.Toast;

import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.List;
import java.util.Locale;

/**
 * Created by virgile on 17.05.2017.
 */

public class Util {

    public static  List<String> convertResponseToString(BatchAnnotateImagesResponse response) {
        List<String> message = null;
        // String message = "I found these things:\n\n";
        // Toast.makeText (mContext,response, Toast.LENGTH_LONG).show();

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.add( String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
            }
        }
        // Debug message

        return message;
    }

    public static  long map(long x, long in_min, long in_max, long out_min, long out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}
