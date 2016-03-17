package com.example.osx.bledesign;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by osx on 07/11/15.
 */
public class ProgressDialogg {
    public static ProgressDialog dialog;

    public static void getDialog(Context context){
        dialog=new ProgressDialog(context,ProgressDialog.THEME_HOLO_LIGHT);
        if(!dialog.isShowing()){
            dialog.setMessage("Loading...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    public static void dismiss(){
        if(dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
