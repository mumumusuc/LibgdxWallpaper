package com.mumumusuc;

import android.graphics.PixelFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;

public class GLWallpaperService extends AndroidLiveWallpaperService implements InputProcessor {

    private GdxRenderer box2dEffectView;

    @Override
    public void onCreateApplication() {
        super.onCreateApplication();
        box2dEffectView = new GdxRenderer();
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.r = cfg.g = cfg.b = cfg.a = 8;
        getSurfaceHolder().setFormat(PixelFormat.TRANSLUCENT);
        initialize(box2dEffectView, cfg);
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

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
        if (Math.random() > 0.1)
            box2dEffectView.newCube();
        else
            box2dEffectView.newModel();
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
