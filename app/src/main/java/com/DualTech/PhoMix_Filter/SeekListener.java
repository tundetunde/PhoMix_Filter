package com.DualTech.PhoMix_Filter;

import android.widget.SeekBar;

/**
 * Created by tunde_000 on 21/06/2015.
 */
public class SeekListener implements SeekBar.OnSeekBarChangeListener {
    Editor editor;

    public SeekListener(Editor editor){
        this.editor = editor;
    }

    //SeekBar so use can use the bar to choose value
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch(Editor.currentEffect){
            case R.id.bt1:
                editor.vBright = (float)progress + 1;
                editor.effectText.setText("Brightness: " + (editor.vBright) + "%");
                break;
            case R.id.bt2:
                editor.vContrast = (float)progress + 1;
                editor.effectText.setText("Contrast: " + (editor.vContrast) + "%");
                break;
            case R.id.bt6:
                if(progress <= 50){
                    float p = (float) (progress / 50.0);
                    editor.vSat = p - 1;
                }else{
                    float p = (float) (progress/2.0);
                    editor.vSat = p / 50;
                }
                editor.effectText.setText("Saturation: " + (editor.vSat * 100) + "%");
                break;
            case R.id.bt9:
                editor.vGrain = (float)progress / 100;
                editor.effectText.setText("Grain: " + (editor.vGrain * 100) + "%");
                break;
            case R.id.bt10:
                editor.vFillLight = (float)progress / 100;
                editor.effectText.setText("Fill-Light: " + (editor.vFillLight * 100) + "%");
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
