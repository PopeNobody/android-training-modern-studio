/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.graphics;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Wrapper activity demonstrating the use of {@link GLSurfaceView}, a view
 * that uses OpenGL drawing into a dedicated surface.
 *
 * Shows:
 * + How to redraw in response to user input.
 */
public class TouchRotateActivity extends Activity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create our Preview view and set it as the content of our
        // Activity
        mGLSurfaceView = new TouchSurfaceView(this);
        setContentView(mGLSurfaceView);
        mGLSurfaceView.requestFocus();
        mGLSurfaceView.setFocusableInTouchMode(true);
    }

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }

    /**
     * Implement a simple rotation control.
     *
     */
    private static class TouchSurfaceView extends GLSurfaceView {

        private static final float TOUCH_SCALE_FACTOR = 180.0f / 320;
        private static final float TRACKBALL_SCALE_FACTOR = 36.0f;
        private CubeRenderer mRenderer;
        private float mPreviousX;
        private float mPreviousY;

        TouchSurfaceView(Context context) {
            super(context);
            mRenderer = new CubeRenderer();
            setRenderer(mRenderer);
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }

        @Override
        public boolean onTrackballEvent(MotionEvent e) {
            updateAngles(e.getX(), e.getY(), TRACKBALL_SCALE_FACTOR);
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            final int action = e.getActionMasked();
            if (action == MotionEvent.ACTION_MOVE) {
                updateAngles(e.getX() - mPreviousX, e.getY() - mPreviousY, TOUCH_SCALE_FACTOR);
            } else if (action == MotionEvent.ACTION_DOWN) {
                if (e.isFromSource(InputDevice.SOURCE_MOUSE)) {
                    requestPointerCapture();
                } else {
                    releasePointerCapture();
                }
            }
            mPreviousX = e.getX();
            mPreviousY = e.getY();
            return true;
        }

        @Override
        public boolean onCapturedPointerEvent(MotionEvent e) {
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                releasePointerCapture();
            } else {
                updateAngles(e.getX(), e.getY(), TOUCH_SCALE_FACTOR);
            }
            return true;
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            // Release pointer capture on any key press.
            releasePointerCapture();
            return super.onKeyDown(keyCode, event);
        }

        private void updateAngles(float dx, float dy, float scaleFactor) {
            if (dx != 0 && dy != 0) {
                mRenderer.mAngleX += dx * scaleFactor;
                mRenderer.mAngleY += dy * scaleFactor;
                requestRender();
            }
        }

        /**
         * Render a cube.
         */
        private static class CubeRenderer implements GLSurfaceView.Renderer {
            CubeRenderer() {
                mCube = new Cube();
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                /*
                 * Usually, the first thing one might want to do is to clear
                 * the screen. The most efficient way of doing this is to use
                 * glClear().
                 */

                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

                /*
                 * Now we're ready to draw some 3D objects
                 */

                gl.glMatrixMode(GL10.GL_MODELVIEW);
                gl.glLoadIdentity();
                gl.glTranslatef(0, 0, -3.0f);
                gl.glRotatef(mAngleX, 0, 1, 0);
                gl.glRotatef(mAngleY, 1, 0, 0);

                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

                mCube.draw(gl);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                gl.glViewport(0, 0, width, height);

                /*
                * Set our projection matrix. This doesn't have to be done
                * each time we draw, but usually a new projection needs to
                * be set when the viewport is resized.
                */

                float ratio = (float) width / height;
                gl.glMatrixMode(GL10.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
            }

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                /*
                 * By default, OpenGL enables features that improve quality
                 * but reduce performance. One might want to tweak that
                 * especially on software renderer.
                 */
                gl.glDisable(GL10.GL_DITHER);

                /*
                 * Some one-time OpenGL initialization can be made here
                 * probably based on features of this particular context
                 */
                gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                        GL10.GL_FASTEST);

                gl.glClearColor(1, 1, 1, 1);
                gl.glEnable(GL10.GL_CULL_FACE);
                gl.glShadeModel(GL10.GL_SMOOTH);
                gl.glEnable(GL10.GL_DEPTH_TEST);
            }
            private Cube mCube;
            float mAngleX;
            float mAngleY;
        }
    }
}
