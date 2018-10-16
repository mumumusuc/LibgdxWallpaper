package com.mumumusuc;


import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;


public class AndroidLauncher extends AndroidApplication implements InputProcessor {
    private final String TAG = "MainActivity";
    private LinearLayout mContainer;
    private SensorManager mSensorManager;
    private Sensor mGravitySensor;
    private GdxRenderer box2dEffectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        buildGDX();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(box2dEffectView.getSensorEventListener(), mGravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(box2dEffectView.getSensorEventListener());
        cleanGDX();
    }

    private View CreateGLAlpha(ApplicationListener application) {
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.numSamples = 2;
        cfg.r = cfg.g = cfg.b = cfg.a = 8;
        View view = initializeForView(application, cfg);
        if (view instanceof SurfaceView) {
            GLSurfaceView glView = (GLSurfaceView) graphics.getView();
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setZOrderMediaOverlay(true);
            glView.setZOrderOnTop(true);
        }
        return view;
    }


    public void buildGDX() {
        box2dEffectView = new GdxRenderer();
        View view = CreateGLAlpha(box2dEffectView);
        mContainer = findViewById(R.id.container);
        mContainer.addView(view);
        Gdx.input.setInputProcessor(this);
    }

    public void add() {
        if (Math.random() > 0.1)
            box2dEffectView.newCube();
        else
            box2dEffectView.newModel();
    }

    public void cleanGDX() {
        try {
            box2dEffectView.dispose();
        } catch (Exception e) {
        }
        mContainer.removeAllViews();
        box2dEffectView = null;
    }

    /**/
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        add();
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

}
