package com.mumumusuc;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Disposable;

public class GameObject extends ModelInstance implements Disposable {
    private Config cfg;
    public final btRigidBody rigidBody;
    public final btMotionState motionState;

    private GameObject(Model model, btRigidBody.btRigidBodyConstructionInfo info) {
        super(model);
        rigidBody = new btRigidBody(info);
        motionState = new MotionState();
        rigidBody.setMotionState(motionState);
    }

    private GameObject(Config cfg) {
        super(cfg.model);
        rigidBody = new btRigidBody(cfg.constructInfo);
        motionState = new MotionState();
        rigidBody.setMotionState(motionState);
        this.cfg = cfg;
    }

    @Override
    public void dispose() {
        rigidBody.dispose();
        motionState.dispose();
        if (cfg != null) {
            cfg.dispose();
        }
    }

    private static class Config implements Disposable {
        private float mass;
        private Vector3 localInertia;
        private Model model;
        private btCollisionShape shape;
        private btRigidBody.btRigidBodyConstructionInfo constructInfo;

        @Override
        public void dispose() {
            model.dispose();
            shape.dispose();
            constructInfo.dispose();
        }
    }

    public static class Builder {
        private final Config cfg;

        public Builder() {
            cfg = new Config();
        }

        public Builder setModel(Model model) {
            cfg.model = model;
            return this;
        }

        public Builder setShape(btCollisionShape shape) {
            cfg.shape = shape;
            return this;
        }

        public Builder setMass(float mass) {
            cfg.mass = mass;
            return this;
        }

        public GameObject create() {
            cfg.localInertia = new Vector3();
            if (cfg.mass > 0f) {
                cfg.shape.calculateLocalInertia(cfg.mass, cfg.localInertia);
            } else {
                cfg.localInertia.set(0f, 0f, 0f);
            }
            cfg.constructInfo = new btRigidBody.btRigidBodyConstructionInfo(
                    cfg.mass,
                    null,
                    cfg.shape,
                    cfg.localInertia);
            return new GameObject(cfg);
        }


    }

    private class MotionState extends btMotionState {
        @Override
        public void getWorldTransform(Matrix4 worldTrans) {
            worldTrans.set(transform);
        }

        @Override
        public void setWorldTransform(Matrix4 worldTrans) {
            transform.set(worldTrans);
        }
    }
}
