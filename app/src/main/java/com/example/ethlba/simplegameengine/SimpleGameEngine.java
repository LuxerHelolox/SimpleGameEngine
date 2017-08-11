package com.example.ethlba.simplegameengine;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import java.io.IOException;
import android.media.SoundPool;

public class SimpleGameEngine extends AppCompatActivity {

    // gameView will be the view of the game
    // It will also hold the logic of the game
    // and respond to screen touches as well
    GameView gameView;
    int soundID = -1;

    // This SoundPool is deprecated but don't worry
    SoundPool soundPool;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize gameView and set it as the view
        gameView = new GameView(this);
        setContentView(gameView);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        soundID = loadsound(soundPool);
    }

    // Here is our implementation of GameView
    // It is an inner class.
    // Note how the final closing curly brace }
    // is inside SimpleGameEngine

    // Notice we implement runnable so we have
    // A thread and can override the run method.
    class GameView extends SurfaceView implements Runnable {

        // This is our thread
        Thread gameThread = null;

        // This is new. We need a SurfaceHolder
        // When we use Paint and Canvas in a thread
        // We will see it in action in the draw method soon.
        SurfaceHolder ourHolder;

        // A boolean which we will set and unset
        // when the game is running- or not.
        volatile boolean playing;

        // A Canvas and a Paint object
        Canvas canvas;
        Paint paint;

        // This variable tracks the game frame rate
        long fps;

        // This is used to help calculate the fps
        private long timeThisFrame;

        // Declare an object of type Bitmap
        Bitmap bitmapBob;

        // Bob starts off not moving
        boolean isMoving = false;

        // He can walk at 150 pixels per second
        float walkSpeedPerSecond = 300;

        // He starts 10 pixels from the left
        float bobXPosition = 10;

        float bobYPosition = 10;

        // X position of the finger
        float fingerX;

        // Y position of the finger
        float fingerY;



        // When the we initialize (call new()) on gameView
        // This special constructor method runs
        public GameView(Context context) {
            // The next line of code asks the
            // SurfaceView class to set up our object.
            // How kind.
            super(context);

            // Initialize ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();

            // Load Bob from his .png file
            bitmapBob = BitmapFactory.decodeResource(this.getResources(), R.drawable.bob);

        }

        @Override
        public void run() {
            while (playing) {

                // Capture the current time in milliseconds in startFrameTime
                long startFrameTime = System.currentTimeMillis();

                // Update the frame
                update();

                // Draw the frame
                draw();

                // Calculate the fps this frame
                // We can then use the result to
                // time animations and more.
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame > 0) {
                    fps = 1000 / timeThisFrame;
                }

            }

        }

        // Everything that needs to be updated goes in here
        // In later projects we will have dozens (arrays) of objects.
        // We will also do other things like collision detection.
        public void update() {

            // If bob is moving (the player is touching the screen)
            // then move him to the right based on his target speed and the current fps.
            if (isMoving) {
                int delta = 1;
                if (fingerX < bobXPosition) delta = -1;
                bobXPosition = bobXPosition + delta * (walkSpeedPerSecond / fps);
                delta = 1;
                if (fingerY < bobYPosition) delta = -1;
                bobYPosition = bobYPosition + delta * (walkSpeedPerSecond / fps);

                if (bobXPosition > 1000 || bobXPosition < 0) {
                    walkSpeedPerSecond = - walkSpeedPerSecond;
                    soundPool.play(soundID, 1, 1, 0, 0, 1);}
            }

        }

        // Draw the newly updated scene
        public void draw() {

            // Make sure our drawing surface is valid or we crash
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                // Make the drawing surface our canvas object
                canvas = ourHolder.lockCanvas();

                // Draw the background color
                canvas.drawColor(Color.argb(255, 26, 128, 182));

                // Choose the brush color for drawing
                paint.setColor(Color.argb(255, 249, 129, 0));

                // Make the text a bit bigger
               //  paint.setTextSize(45);

                // Display the current fps on the screen
                // canvas.drawText("FPS:" + fps + "  X:" + bobXPosition + "  fingerX:" + fingerX + "  fingerY:" + fingerY, 20, 40, paint);

                // Draw bob at bobXPosition, bobYPosition pixels
                canvas.drawBitmap(bitmapBob, bobXPosition, bobYPosition, paint);

                // Draw everything to the screen
                // and unlock the drawing surface
                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        // If SimpleGameEngine Activity is paused/stopped
        // shutdown our thread.
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        // If SimpleGameEngine Activity is started theb
        // start our thread.
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        // The SurfaceView class implements onTouchListener
        // So we can override this method and detect screen touches.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                // Player has touched the screen
                case MotionEvent.ACTION_DOWN:

                    // Set isMoving so Bob is moved in the update method
                    isMoving = true;
                    fingerX = motionEvent.getX();
                    fingerY = motionEvent.getY();
                    break;

                //Player moved to finger
                case MotionEvent.ACTION_MOVE:
                    fingerX = motionEvent.getX();
                    fingerY = motionEvent.getY();
                    break;

                // Player has removed finger from screen
                case MotionEvent.ACTION_UP:

                    // Set isMoving so Bob does not move
                    isMoving = false;

                    break;
            }
            return true;
        }

    }
    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        gameView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        gameView.pause();
    }

    // This method only loads the sound clip
    int loadsound(SoundPool soundPool) {
        try{
            // Create objects of the 2 required classes
            AssetManager assetManager = this.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("warp.ogg");
            return soundPool.load(descriptor, 0);

        }catch(IOException e){
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
            return -1;
        }
    }
}
