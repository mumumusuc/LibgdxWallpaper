package com.mumumusuc;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;

import java.util.ArrayList;
import java.util.List;


public class GdxRenderer implements ApplicationListener {
    private static final String TAG = "GdxRenderer";
    private static final float PXTM = 1f;
    private static final float PLANT_SIZE = 20f;
    private static final float PLANT_THICKNESS = 0.1f;
    private static final float CUBE_SIZE = PLANT_SIZE / 8;
    private static final short MAX_GAME_OBJECT_COUNT = 30;
    private static final short GROUND_FLAG = 1 << 8;
    private static final short OBJECT_FLAG = 1 << 9;
    private static final short ALL_FLAG = -1;
    private static final short GRAVITY_MULTIPLIER = -20;
    private AssetManager assets;

    private Vector3 mWG = new Vector3(0f, -10f, 0f);
    private btDynamicsWorld mWorld;
    private btDbvtBroadphase mBroadpharse;
    private btSequentialImpulseConstraintSolver mConstraintSolver;
    private PerspectiveCamera mCamera;
    private Environment mEnvironment;
    private Texture mTexture;
    private ModelBatch mModelBatch;
    private ModelBuilder mModelBuilder;
    private btCollisionConfiguration mColCfg;
    private btDispatcher mColDispatcher;
    private List<GameObject> mWalls = new ArrayList<>();
    private List<GameObject> mGameObjects = new ArrayList<>();

    public GdxRenderer() {

    }


    @Override
    public void create() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float cameraWidth = w / PXTM;
        float cameraHeight = h / PXTM;

        Bullet.init();

        assets = new AssetManager();
        assets.load("models/andy.g3db", Model.class);
        assets.update();

        mCamera = new PerspectiveCamera(70f, cameraWidth, cameraHeight);
        mCamera.lookAt(0f, 0f, 0f);
        mCamera.near = 1f;
        mCamera.far = 300f;
        mCamera.position.set(0f, 0f, PLANT_SIZE);
        mCamera.update();

        mEnvironment = new Environment();
        mEnvironment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
        mEnvironment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0f, 0f, -1f));

        mTexture = new Texture(Gdx.files.internal("ic_launcher.png"));
        mModelBuilder = new ModelBuilder();
        mModelBatch = new ModelBatch();
        mColCfg = new btDefaultCollisionConfiguration();
        mColDispatcher = new btCollisionDispatcher(mColCfg);
        mBroadpharse = new btDbvtBroadphase();
        mConstraintSolver = new btSequentialImpulseConstraintSolver();
        mWorld = new btDiscreteDynamicsWorld(mColDispatcher, mBroadpharse, mConstraintSolver, mColCfg);
        mWorld.setGravity(mWG);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        buildWalls();
        newCube();
        //newModel();
    }

    @Override
    public void dispose() {
        while (mGameObjects.size() > 0) {
            destroyGameObject(0);
        }
        mGameObjects.clear();
        for (GameObject obj : mWalls) {
            mWorld.removeRigidBody(obj.rigidBody);
            obj.dispose();
        }
        mWalls.clear();
        mColCfg.dispose();
        mBroadpharse.dispose();
        mColDispatcher.dispose();
        mConstraintSolver.dispose();
        mTexture.dispose();
        mModelBatch.dispose();
        assets.dispose();
        // mWorld.dispose();
    }

    @Override
    public void render() {
        if (mGameObjects.size() == 0) {
            return;
        }
        if (!assets.update()) {
            float p = assets.getProgress();
            Gdx.gl20.glClearColor(p, p, p, 0f);
        } else {
            Gdx.gl20.glClearColor(1f, 1f, 1f, 0f);
        }
        checkGameObjectCounts();
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl20.glBlendEquation(GL20.GL_FUNC_ADD);
        Gdx.gl20.glCullFace(GL20.GL_NONE);
        Gdx.gl20.glDepthMask(false);
        float dt = Math.min(1 / 30f, Gdx.app.getGraphics().getDeltaTime());
        mWorld.stepSimulation(dt, 5, 1 / 60f);
        mModelBatch.begin(mCamera);
        mModelBatch.render(mWalls, mEnvironment);
        mModelBatch.render(mGameObjects, mEnvironment);
        mModelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
    }


    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public void newModel() {
        if (!assets.isLoaded("models/andy.g3db", Model.class)) {
            return;
        }
        Model model = assets.get("models/andy.g3db", Model.class);
        btCollisionShape shape = new btBoxShape(new Vector3(1f, 1f, 1f));
        GameObject obj = new GameObject.Builder()
                .setMass(1f)
                .setModel(model, true)
                .setShape(shape)
                .create();
        mWorld.addRigidBody(obj.rigidBody, OBJECT_FLAG, ALL_FLAG);
        mGameObjects.add(obj);
    }

    public void newCube() {
        Model model = mModelBuilder.createBox(
                CUBE_SIZE, CUBE_SIZE, CUBE_SIZE,
                //new Material(TextureAttribute.createDiffuse(mTexture)),
                new Material(ColorAttribute.createDiffuse((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.95f)),
                Usage.Normal | Usage.Position | Usage.TextureCoordinates
        );
        model.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        btCollisionShape shape = new btBoxShape(new Vector3(CUBE_SIZE / 2, CUBE_SIZE / 2, CUBE_SIZE / 2));
        GameObject cube = new GameObject.Builder()
                .setMass(10f)
                .setModel(model)
                .setShape(shape)
                .create();
        mWorld.addRigidBody(cube.rigidBody, OBJECT_FLAG, ALL_FLAG);
        mGameObjects.add(cube);
    }


    private void buildWalls() {
        final float WHR = Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        final float w = PLANT_SIZE * WHR;
        final float h = PLANT_SIZE;
        //bottom
        buildWall(w, w, 0, -h / 2, 0, new Vector3(1f, 0, 0), 90f, 0.1f);
        //top
        buildWall(w, w, 0, h / 2, 0, new Vector3(1f, 0, 0), 90f, 0.1f);
        //left
        buildWall(w, h, -w / 2, 0, 0, new Vector3(0, 1f, 0), 90f, 0.1f);
        //right
        buildWall(w, h, w / 2, 0, 0, new Vector3(0, 1f, 0), 90f, 0.1f);
        //far
        buildWall(w, h, 0, 0, -w / 2, new Vector3(1f, 0, 0), 0f, 0.1f);
        //near
        buildWall(w, h, 0, 0, w / 2, new Vector3(1f, 0, 0), 0f, 0f);
    }


    private void buildWall(float w, float h, float x, float y, float z, Vector3 r, float d, float alpha) {
        Model model = mModelBuilder.createBox(
                w, h, PLANT_THICKNESS,
                new Material(ColorAttribute.createDiffuse(0.3f, 0.3f, 0.3f, alpha)),
                Usage.Normal | Usage.Position);
        model.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        btCollisionShape shape = new btBoxShape(new Vector3(PLANT_SIZE / 2, PLANT_SIZE, PLANT_THICKNESS / 2));
        GameObject obj = new GameObject.Builder()
                .setMass(0f)
                .setModel(model)
                .setShape(shape)
                .create();
        obj.transform.setToTranslation(x, y, z);
        obj.transform.rotate(r, d);
        obj.rigidBody.setCollisionFlags(obj.rigidBody.getFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        obj.rigidBody.setActivationState(Collision.DISABLE_DEACTIVATION);
        mWorld.addRigidBody(obj.rigidBody, GROUND_FLAG, ALL_FLAG);
        mWalls.add(obj);
    }


    private void checkGameObjectCounts() {
        if (mGameObjects.size() > MAX_GAME_OBJECT_COUNT) {
            destroyGameObject(0);
        }
    }

    private void destroyGameObject(int index) {
        GameObject obj = mGameObjects.remove(index);
        mWorld.removeRigidBody(obj.rigidBody);
        obj.dispose();
    }


    public void updateGravity(Vector3 g) {
        mWorld.setGravity(g);
    }


    public SensorEventListener getSensorEventListener() {
        return mListener;
    }

    private SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[SensorManager.DATA_X];
            float y = event.values[SensorManager.DATA_Y];
            float z = event.values[SensorManager.DATA_Z];
            updateGravity(new Vector3(
                    x * GRAVITY_MULTIPLIER,
                    y * GRAVITY_MULTIPLIER,
                    z * GRAVITY_MULTIPLIER));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
