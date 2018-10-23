package com.yda.yiyunchain.widget;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/11/30.
 */

public class DialogUtil {

    public static void showBottomDialog(Activity activity , int dialogLayoutId , HashMap<String, String> params, BottomDialogOnclickListener bottomDialogOnclickListener){

        BottomDialogView bottomDialogView = new BottomDialogView(activity, params,bottomDialogOnclickListener);
        bottomDialogView.showAtLocation(activity.findViewById(dialogLayoutId), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置窗口显示在parent布局的位置并显示
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);//自动打开软键盘
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

    }


}


