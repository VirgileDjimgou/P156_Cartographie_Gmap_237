package com.moonlight.cartographe.CardPack;

import android.graphics.Bitmap;

import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;

/**
 * Created by Lincoln on 18/05/16.
 */
public class CloudObjekt {
    private String name;
    private int numOfSongs;
    private Bitmap bitmapImage;
    private BatchAnnotateImagesResponse Result;
    private boolean Response ;

    public CloudObjekt() {
    }

    public CloudObjekt(String name, int numOfSongs, Bitmap bitmapImage , BatchAnnotateImagesResponse Result , boolean Response ) {
        this.name = name;
        this.numOfSongs = numOfSongs;
        this.bitmapImage = bitmapImage;
        this.Result = Result;
        this.Response = Response;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumOfSongs() {
        return numOfSongs;
    }

    public void setNumOfSongs(int numOfSongs) {
        this.numOfSongs = numOfSongs;
    }

    public Bitmap getImage() {
        return this.bitmapImage;
    }

    public BatchAnnotateImagesResponse getResult() {
        return Result;
    }

    public void setResult(BatchAnnotateImagesResponse result) {
        Result = result;
    }

    public boolean getResponse() {
        return this.Response;
    }

    public void setResponse(boolean response) {
        this.Response = response;
    }

    public void setThumbnail(Bitmap bitmapImage) {
        this.bitmapImage = bitmapImage;
    }
}
