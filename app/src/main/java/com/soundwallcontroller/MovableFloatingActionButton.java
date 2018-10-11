package com.soundwallcontroller;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MovableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {

    private final static float CLICK_DRAG_TOLERANCE = 10; // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.

    private float downRawX, downRawY;
    private float dX, dY;

    public MovableFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(this);
    }

    public void setUnitLengthPosition(float x, float y){

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        int parentWidth = ((View)getParent()).getWidth();
        int parentHeight = ((View)getParent()).getHeight();

        float nx = (x * (float)parentWidth) - viewWidth * 0.5f;
        float ny = (y * (float)parentHeight) - viewHeight * 0.5f;

        //Log.i("unitpos", nx+","+ny+" | "+parentWidth+","+parentHeight);

        animate()
                .x(nx)
                .y(ny)
                .setDuration(0)
                .start();
    }
    float getUnitLengthPositionX(){

        int parentWidth = ((View)getParent()).getWidth();
        int parentHeight = ((View)getParent()).getHeight();

        float x = getX() / (float)parentWidth;
        float y = getY() / (float)parentHeight;

        return x;
    }
    float getUnitLengthPositionY(){

        int parentWidth = ((View)getParent()).getWidth();
        int parentHeight = ((View)getParent()).getHeight();

        float x = getX() / (float)parentWidth;
        float y = getY() / (float)parentHeight;

        return y;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent){

        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN) {

            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();
            dX = view.getX() - downRawX;
            dY = view.getY() - downRawY;

            return true; // Consumed

        }
        else if (action == MotionEvent.ACTION_MOVE) {

            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();

            View viewParent = (View)view.getParent();
            int parentWidth = viewParent.getWidth();
            int parentHeight = viewParent.getHeight();

            float newX = motionEvent.getRawX() + dX;
            newX = Math.max(0, newX); // Don't allow the FAB past the left hand side of the parent
            newX = Math.min(parentWidth - viewWidth, newX); // Don't allow the FAB past the right hand side of the parent

            float newY = motionEvent.getRawY() + dY;
            newY = Math.max(0, newY); // Don't allow the FAB past the top of the parent
            newY = Math.min(parentHeight - viewHeight, newY); // Don't allow the FAB past the bottom of the parent

            //((TextView)findViewById(R.id.textChanAL)).setText(Float.toString(newX / parentWidth));
            //((TextView)findViewById(R.id.textChanAR)).setText(Float.toString(newY / parentHeight));

            Log.i("move", newX+", "+newY);

            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start();

            return true; // Consumed

        }
        else if (action == MotionEvent.ACTION_UP) {

            float upRawX = motionEvent.getRawX();
            float upRawY = motionEvent.getRawY();

            float upDX = upRawX - downRawX;
            float upDY = upRawY - downRawY;

            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                return performClick();
            }
            else { // A drag
                return true; // Consumed
            }

        }
        else {
            return super.onTouchEvent(motionEvent);
        }

    }

}