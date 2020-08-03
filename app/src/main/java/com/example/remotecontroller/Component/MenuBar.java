package com.example.remotecontroller.Component;

import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.remotecontroller.R;
import com.example.remotecontroller.Resource;

public class MenuBar implements View.OnClickListener{
    private final static String TAG= "Menu bar";


    private RelativeLayout menuBar;
    private Button [] menuButton =new Button[6];
    private Button btn_menu_hide;
    private ImageView menuBarBackground;
    private ImageView resourceDisplay;
    private PaintingView paintingView;

    private boolean isHide =true;


    public MenuBar(RelativeLayout menuBar,ImageView resourceDisplay,PaintingView paintingView)
    {
        this.paintingView=paintingView;
        this.menuBar=menuBar;
        this.resourceDisplay = resourceDisplay;
        linkButton();
        hideManuBar();
    }
    private void linkButton ()
    {

        menuButton[0] = menuBar.findViewById(R.id.btn_menu_calendar);
        menuButton[1] = menuBar.findViewById(R.id.btn_menu_note);
        menuButton[2] = menuBar.findViewById(R.id.btn_menu_sketch);
        menuButton[3] = menuBar.findViewById(R.id.btn_menu_translate);
        menuButton[4] = menuBar.findViewById(R.id.btn_menu_record);
        menuButton[5] = menuBar.findViewById(R.id.btn_menu_musicpad);
        for (int i =0;i<menuButton.length ;i++)
        {
            menuButton[i].setOnClickListener(this);
        }
        btn_menu_hide=menuBar.findViewById(R.id.btn_menu_hide);
        btn_menu_hide.setOnClickListener(this);
        menuBarBackground=menuBar.findViewById(R.id.menu_bar_background);

    }

    public void onClick (View view)
    {
        if (view.getId() != R.id.btn_menu_hide) {
            for (int menuButtonIndex = 0; menuButtonIndex < menuButton.length; menuButtonIndex++) {
                if (view.getId() == menuButton[menuButtonIndex].getId()) {
                    resourceDisplay.setVisibility(View.VISIBLE);
                    menuButton[menuButtonIndex].setBackgroundResource(Resource.menuBarButtonImageId[menuButtonIndex * 2+1]);
                    resourceDisplay.setImageResource(Resource.resourceImageId[menuButtonIndex]);
                    if (menuButton[menuButtonIndex].getId()==R.id.btn_menu_note)
                    {
                        paintingView.setVisibility(View.VISIBLE);
                        paintingView.clearCanvas();
                    }


                }
                else
                {
                    menuButton[menuButtonIndex].setBackgroundResource(Resource.menuBarButtonImageId[menuButtonIndex * 2+0]);
                }
            }
        }
        else
        {
            isHide =!isHide;
            if (isHide) {

                hideManuBar();
            } else {
                showManuBar();
            }
        }
    }
    public void setToPage (int PageIndex)
    {

        for (int menuButtonIndex = 0; menuButtonIndex < menuButton.length; menuButtonIndex++) {
            if (menuButtonIndex ==PageIndex) {
                menuButton[menuButtonIndex].setBackgroundResource(Resource.menuBarButtonImageId[menuButtonIndex * 2 + 1]);
            }
            else
            {
                menuButton[menuButtonIndex].setBackgroundResource(Resource.menuBarButtonImageId[menuButtonIndex * 2+0]);
            }
        }

        resourceDisplay.setImageResource(Resource.resourceImageId[PageIndex]);
    }

    public void init()
    {
        menuBar.setVisibility(View.VISIBLE);
        for (int menuButtonIndex = 0; menuButtonIndex < menuButton.length; menuButtonIndex++) {
            if (menuButtonIndex==1) {
                menuButton[menuButtonIndex].setBackgroundResource(Resource.menuBarButtonImageId[menuButtonIndex * 2+1]);
            }
            else
            {
                menuButton[menuButtonIndex].setBackgroundResource(Resource.menuBarButtonImageId[menuButtonIndex * 2+0]);
            }
        }
        resourceDisplay.setVisibility(View.INVISIBLE);
        //resourceDisplay.setImageResource(Resource.resourceImageId[1]);
    }
    public void setVisibility(int visbility)
    {
        menuBar.setVisibility(visbility);
    }



    private void hideManuBar ()
    {
        menuBarBackground.setImageResource(Resource.menuBarHideBackground);
        ObjectAnimator animation = ObjectAnimator.ofFloat(menuBar, "translationX", 1100);
        animation.setDuration(500);
        animation.start();
    }
    private void showManuBar ()
    {
        menuBarBackground.setImageResource(Resource.menuBarShowBackground);
        ObjectAnimator animation = ObjectAnimator.ofFloat(menuBar, "translationX", 0);
        animation.setDuration(500);
        animation.start();
    }

}
