package com.DualTech.PhoMix_Filter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SurfaceViewRenderer implements GLSurfaceView.Renderer {
    Editor editor;
    GLSurfaceView glView;
    int currentEffect;
    public Effect mEffect;
    public EffectContext mEffectContext;
    public TextureRenderer mTexRenderer = new TextureRenderer();
    public int[] mTextures = new int[2];
    int textureWidth, textureHeight;
    public boolean saveFrame;
    public boolean mInitialized = false;
    static public boolean sendImage;
    static public boolean rotateOn;
    static public boolean undoBool, applyOn, clear;

    public SurfaceViewRenderer(Editor editor, GLSurfaceView glView){
        rotateOn = undoBool = applyOn = false;
        this.editor = editor;
        this.glView = glView;
        this.glView.setEGLContextClientVersion(2);
        this.glView.setRenderer(this);
        this.glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        currentEffect = 0;
        sendImage = false;
    }

    public void loadTextures(){
        GLES20.glGenTextures(2, mTextures, 0);
        textureHeight = Editor.currentImage.getHeight();
        textureWidth = Editor.currentImage.getWidth();

        mTexRenderer.updateTextureSize(textureWidth, textureHeight);

        // Upload to texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, Editor.currentImage, 0);

        // Set texture parameters
        GLToolBox.initTexParams();
    }

    public void applyEffect() {
        mEffect.apply(mTextures[0], textureWidth, textureHeight, mTextures[1]);
    }

    public void renderResult() {
        if (Editor.effectOn) {
            // if no effect is chosen, just render the original bitmap
            mTexRenderer.renderTexture(mTextures[1]);
        }
        else {
            //saveFrame=true;
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[0]);
        }
    }

    public Bitmap takeScreenshot(GL10 mGL) {
        final int width = glView.getWidth();
        final int height = glView.getHeight();
        IntBuffer ib = IntBuffer.allocate(width * height);
        IntBuffer ibt = IntBuffer.allocate(width * height);

        mGL.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

        // Convert upside down mirror-reversed image to right-side up normal image.
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                ibt.put((height - i - 1) * width + j, ib.get(i * width + j));
            }
        }

        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//for the surface view
        mBitmap.copyPixelsFromBuffer(ibt);
        Editor.picsTaken++;
        return mBitmap;
    }

    public void initEffect(){
        EffectFactory effectFactory = mEffectContext.getFactory();
        switch (Editor.currentEffect){
            case R.id.bt1:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_BRIGHTNESS);
                mEffect.setParameter("brightness", editor.vBright);
                break;
            case R.id.bt2:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_CONTRAST);
                mEffect.setParameter("contrast", editor.vContrast);
                break;
            case R.id.bt3:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_NEGATIVE);
                break;
            case R.id.bt4:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAYSCALE);
                break;
            case R.id.bt5:
                /*mEffect = effectFactory.createEffect(EffectFactory.EFFECT_ROTATE);
                mEffect.setParameter("angle", editor.angle);*/
                break;
            case R.id.bt6:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SATURATE);
                mEffect.setParameter("scale", editor.vSat);
                break;
            case R.id.bt7:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SEPIA);
                break;
            case R.id.bt8:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FLIP);
                mEffect.setParameter("horizontal", true);
                break;
            case R.id.bt9:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAIN);
                mEffect.setParameter("strength", editor.vGrain);
                break;
            case R.id.bt10:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FILLLIGHT);
                mEffect.setParameter("strength", editor.vFillLight);
                break;
        }
        glView.requestRender();
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTexRenderer != null) {
            mTexRenderer.updateViewSize(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //Initializes in the beginning
        if (!mInitialized) {
            //Only need to do this once
            mEffectContext = EffectContext.createWithCurrentGlContext();
            mTexRenderer.init();
            loadTextures();
            mInitialized = true;
        }

        //If user selects photo using select button
        //Load the textures again so the textures uses new bitmap
        if (Editor.changeImage) {
            loadTextures();
            Editor.changeImage = false;
        }
        if(Editor.picChosen){
            loadTextures();
            Editor.picChosen = false;
        }

        //Apply Effect if used
        if (Editor.currentEffect != 0) {
            //if an effect is chosen initialize it and apply it to the texture
            initEffect();
            applyEffect();
        }
        renderResult();

        //Save the Photo
        if (saveFrame) {
            if (Editor.picsTaken == 0) {

                editor.l1.setDrawingCacheEnabled(true);
                Bitmap imgLyt= Bitmap.createBitmap(editor.l1.getDrawingCache());//for the linear layout
                editor.l1.setDrawingCacheEnabled(false);

                Bitmap glBitmap = Bitmap.createBitmap(imgLyt.getWidth(), imgLyt.getHeight(), imgLyt.getConfig());
                Canvas canvas = new Canvas(glBitmap);

                final float scale = editor.getResources().getDisplayMetrics().density;
                // convert the DP into pixel
                int pixel =  (int)(editor.l1.getPaddingTop() * scale + 0.5f);
                canvas.drawBitmap(imgLyt, 0, 0, null);
                canvas.drawBitmap(takeScreenshot(gl),pixel , pixel, null);

                editor.saveBitmap(glBitmap);
                saveFrame = false;
            }
        }

        if (sendImage) {
            Editor.lastPicTaken = takeScreenshot(gl);
            editor.share("image/*", "@PhoMix Filter");
            sendImage = false;
        }

        if(rotateOn){
            Editor.currentImage = Bitmap.createBitmap(editor.rotate(Editor.currentImage));
            Editor.picsTaken = 0;
            rotateOn = false;
            loadTextures();
        }

        if(applyOn){
            Editor.previousImage = Editor.currentImage.copy(Editor.currentImage.getConfig(), true);
            Bitmap bmp = takeScreenshot(gl);
            Editor.currentImage = bmp.copy(bmp.getConfig(), true);
            Editor.picsTaken = 0;
            editor.resetValues();
            Editor.effectOn = false;
            Editor.onlyPic = false;
            //TextureRenderer.clearScreen();
            applyOn = false;
            loadTextures();
        }

        if(undoBool){
            if(!editor.isOnlyPic()){
                undo();
                Editor.picsTaken = 0;
                loadTextures();
            }
            undoBool = false;
        }

        if(clear){
            Editor.currentImage = Editor.chosenPhoto;
            clear = false;
            loadTextures();
        }
    }

    public static void undo(){
        Editor.currentImage = Editor.previousImage.copy(Editor.previousImage.getConfig(), true);
    }

}
