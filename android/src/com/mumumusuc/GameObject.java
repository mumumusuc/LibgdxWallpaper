package com.mumumusuc;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btTransform;
import com.badlogic.gdx.utils.Disposable;

public class GameObject extends ModelInstance implements Disposable {
    private Config cfg;
    private Vector3 centerOffset;
    private Vector3 scale;
    public final btRigidBody rigidBody;
    public final btMotionState motionState;

    private GameObject(Config cfg) {
        super(cfg.model);
        centerOffset = cfg.center.scl(-1f);
        scale = cfg.scale;
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
        private boolean withAsset = false;
        private float mass;
        private Vector3 center = new Vector3();
        private Vector3 scale = new Vector3(1f, 1f, 1f);
        private Vector3 localInertia;
        private Model model;
        private btCollisionShape shape;
        private Class<? extends btCollisionShape> shape_cls;
        private btRigidBody.btRigidBodyConstructionInfo constructInfo;

        @Override
        public void dispose() {
            if (!withAsset) {
                model.dispose();
            }
            shape.dispose();
            constructInfo.dispose();
        }
    }

    public static class Builder {
        private final Config cfg;

        public Builder() {
            cfg = new Config();
        }

        public Builder setModel(@NonNull Model model, boolean asset) {
            cfg.withAsset = asset;
            cfg.model = model;
            return this;
        }

        public Builder setModel(@NonNull Model model) {
            return setModel(model, false);
        }

        public Builder setShape(@NonNull btCollisionShape shape) {
            cfg.shape = shape;
            return this;
        }

        public Builder setShape(@NonNull Class<? extends btCollisionShape> shape) {
            cfg.shape_cls = shape;
            return this;
        }

        public Builder setMass(float mass) {
            cfg.mass = mass;
            return this;
        }

        public Builder setScale(@NonNull Vector3 scale) {
            cfg.scale.set(scale);
            return this;
        }

        public Builder setScale(float scale) {
            cfg.scale.set(new Vector3(scale, scale, scale));
            return this;
        }

        public GameObject create() {
            BoundingBox box = new BoundingBox();
            cfg.model.calculateBoundingBox(box);
            cfg.center = box.getCenter(cfg.center);
            //checkout shape
            if (cfg.shape == null) {
                Vector3 dim = new Vector3();
                dim = box.getDimensions(dim);
                if (cfg.shape_cls == btSphereShape.class) {
                    double r = Math.sqrt(dim.dot(dim)) / 2;
                    cfg.shape = new btSphereShape((float) r);
                } else if (cfg.shape_cls == btCylinderShape.class) {
                    cfg.shape = new btCylinderShape(dim.scl(0.5f));
                } else {
                    cfg.shape = new btBoxShape(dim.scl(0.5f));
                }
            }
            cfg.shape.setLocalScaling(cfg.scale);
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
            btTransform trans = new btTransform();
            trans.setIdentity();
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
            transform.scale(scale.x, scale.y, scale.z);
            transform.translate(centerOffset);
        }
    }
}
