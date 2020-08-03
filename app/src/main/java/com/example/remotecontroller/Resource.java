package com.example.remotecontroller;

public class Resource {

    public static final String s3VideoPath   = "android.resource://com.example.remotecontroller/" + R.raw.s3;

    public static final int [] menuBarButtonImageId =new int []{
            R.mipmap.btn_calendar,R.mipmap.btn_calendar_select,
            R.mipmap.btn_note,R.mipmap.btn_note_select,
            R.mipmap.btn_sketch,R.mipmap.btn_sketch_select,
            R.mipmap.btn_translate,R.mipmap.btn_translate_select,
            R.mipmap.btn_record,R.mipmap.btn_record_select,
            R.mipmap.btn_musicpad,R.mipmap.btn_musicpad_select
    };
    public static final int [] resourceImageId =new int []{
            R.mipmap.control_pad_calendar,
            R.mipmap.control_pad_note,
        R.mipmap.control_pad_sketch,
            R.mipmap.control_pad_translate,
            R.mipmap.control_pad_record,
            R.mipmap.control_pad_musicpad
    };
    public static final int [] s4ImageId = new int [] {
        R.drawable.chess,R.drawable.guess,R.mipmap.piano,R.mipmap.word
    };

    public static final int menuBarHideBackground =R.mipmap.control_pad_bar_2;
    public static final int menuBarShowBackground =R.mipmap.control_pad_bar_1;

    public static final int CALENDAR_PAGE_INDEX =0 ;
    public static final int NOTE_PAGE_INDEX =1;
    public static final int SKETCH_PAGE_INDEX=2;
    public static final int TRANSLATE_PAGE_INDEX =3;
    public static final int RECORD_PAGE_INDEX =4;
    public static final int MUSIC_PAGE_INDEX =5;







}
