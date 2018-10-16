package com.mumumusuc;

import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Interpolation;

import static com.mumumusuc.CameraController.State.Fixed;
import static com.mumumusuc.CameraController.State.Free;
import static com.mumumusuc.CameraController.State.Pause;

public class CameraController extends CameraInputController {
    private static final String TAG = "CameraController";

    private static final float TRANSFORM_RESET_TIME = 1f;
    private static final float TRANSFORM_PAUSE_TIME = 0.25f;

    private InputProcessor stockListener;
    private final float[] stockParams = new float[9];
    private final float[] currentParams = new float[9];
    private final float[] pauseParams = new float[9];
    private final float[] tmpParams = new float[9];
    private State state;
    private StateTransformer stateTransformer;

    /*
     *  Fixed -> Pause -> Free ;
     *  Free  -> Reset -> Pause -> Free ;
     */
    enum State {
        Fixed, Free, Pause
    }

    public CameraController(Camera camera) {
        super(new Listener(), camera);
        stateTransformer = new StateTransformer();
        getCameraParams(stockParams);
        getCameraParams(pauseParams);
        pauseParams[2] *= 1.1f;
        state = Fixed;
        stockListener = Gdx.input.getInputProcessor();
        Log.i(TAG, "stock input = " + stockListener);
    }

    private static class Listener extends CameraGestureListener {
        @Override
        public boolean tap(float x, float y, int count, int button) {
            boolean flag = super.tap(x, y, count, button);
            ((CameraController) controller).onTaped(x, y, count, button);
            return flag;
        }

        @Override
        public boolean longPress(float x, float y) {
            boolean flag = super.longPress(x, y);
            ((CameraController) controller).onLongPressed(x, y);
            return flag;
        }

    }

    protected void onLongPressed(float x, float y) {
        if (state == Fixed || state == Free) {
            stateTransformer.transform(state, Pause);
        }
    }

    protected void onTaped(float x, float y, int count, int button) {
        if (stockListener != null) {
            stockListener.touchDown((int) x, (int) y, count, button);
        }
    }


    @Override
    public void update() {
        super.update();
        if (!stateTransformer.getNextValue(tmpParams)) {
            final State next = stateTransformer.getNextState();
            if (next != null && state != next) {
                state = next;
                if (state == Pause && stateTransformer.getPreState() == Free) {
                    stateTransformer.transform(state, Fixed);
                } else if (state == Pause && stateTransformer.getPreState() == Fixed) {
                    stateTransformer.transform(state, Free);
                }
            }
            return;
        }
        camera.position.set(tmpParams[0], tmpParams[1], tmpParams[2]);
        camera.direction.set(tmpParams[3], tmpParams[4], tmpParams[5]);
        camera.up.set(tmpParams[6], tmpParams[7], tmpParams[8]);
        camera.update();
    }

    @Override
    protected boolean process(float deltaX, float deltaY, int button) {
        if (state == Fixed) {
            return false;
        } else if (state == Free) {
            return super.process(deltaX, deltaY, button);
        }
        return false;
    }

    @Override
    public boolean zoom(float amount) {
        if (state == Fixed) {
            return false;
        } else if (state == Free) {
            return super.zoom(amount);
        }
        return false;
    }


    private float[] getCameraParams(float[] dst) {
        dst[0] = camera.position.x;
        dst[1] = camera.position.y;
        dst[2] = camera.position.z;
        dst[3] = camera.direction.x;
        dst[4] = camera.direction.y;
        dst[5] = camera.direction.z;
        dst[6] = camera.up.x;
        dst[7] = camera.up.y;
        dst[8] = camera.up.z;
        return dst;
    }

    private class StateTransformer {
        float baseTime = 0;
        float targetTime = 0;
        float[] from;
        float[] to;
        State fromState, targetState;

        void transform(State from, State to) {
            fromState = from;
            targetState = to;
            if (from == Fixed && to == Pause) {
                this.from = stockParams;
                this.to = pauseParams;
                baseTime = 0;
                targetTime = TRANSFORM_PAUSE_TIME;
            } else if (from == Pause && to == Free) {
                baseTime = 0;
                targetTime = 0;
            } else if (from == Free && to == Pause) {
                this.from = getCameraParams(currentParams);
                this.to = pauseParams;
                baseTime = 0;
                targetTime = TRANSFORM_RESET_TIME;
            } else if (from == Pause && to == Fixed) {
                this.from = pauseParams;
                this.to = stockParams;
                baseTime = 0;
                targetTime = TRANSFORM_PAUSE_TIME;
            }
            Log.i(TAG, "to = " + targetState + ", base = " + baseTime + ", target = " + targetTime);

        }

        boolean getNextValue(float[] dst) {
            //Log.i(TAG, "base = " + baseTime + ", target = " + targetTime);
            if (baseTime >= targetTime) {
                return false;
            }
            getInterpolationValues(from, to, dst, baseTime / targetTime);
            baseTime += Gdx.graphics.getDeltaTime();
            return true;
        }

        State getNextState() {
            return targetState;
        }

        State getPreState() {
            return fromState;
        }

        void getInterpolationValues(
                Interpolation interpolation,
                float[] from,
                float[] to,
                float[] dst,
                float t) {
            for (int i = 0; i < from.length; i++) {
                dst[i] = interpolation.apply(from[i], to[i], t);
            }
        }

        void getInterpolationValues(
                float[] from,
                float[] to,
                float[] dst,
                float t) {
            getInterpolationValues(Interpolation.pow2, from, to, dst, t);
        }
    }
}
