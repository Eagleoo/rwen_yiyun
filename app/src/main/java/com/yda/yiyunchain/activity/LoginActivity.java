package com.yda.yiyunchain.activity;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.yda.yiyunchain.Bean.LoginInfoBean;
import com.yda.yiyunchain.Bean.UserInfoBean;
import com.yda.yiyunchain.DataUtil.SaveGetSharedPreferencesInfo;
import com.yda.yiyunchain.HttpUtil.HttpManger;
import com.yda.yiyunchain.MyTextWatcher;
import com.yda.yiyunchain.R;
import com.yda.yiyunchain.util.ACache;
import com.yda.yiyunchain.util.Constant;
import com.yda.yiyunchain.util.MD5;
import com.yda.yiyunchain.util.SelectorFactory;
import com.yda.yiyunchain.util.Util;
import com.yda.yiyunchain.util.Web;
import com.yda.yiyunchain.widget.UpDataVersionDialog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.android.api.JPushInterface;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.username)
    EditText eUsername;
    @BindView(R.id.password)
    EditText ePassword;
    @BindView(R.id.login)
    TextView login;
    @BindView(R.id.phoneline)
    View phoneline;
    @BindView(R.id.passwordline)
    View passwordline;

    @BindView(R.id.center)
    TextView center;

    @BindView(R.id.share_iv)
    View share;

    @BindView(R.id.refresh)
    View refresh;
    @BindView(R.id.back)
    View back;
    @BindView(R.id.tologinyunda)
    TextView tologinyunda;
    @BindView(R.id.lin2)
    View lin2;
    @BindView(R.id.toRegistered)
    View toRegistered;
    @BindView(R.id.toForgetPassword)
    View toForgetPassword;
    String version;
    String content;
    String url;
    String createTime;
    String forgetGesture;
    private ACache aCache;

    @Override
    public boolean isWidth() {
        return false;
    }


    MyTextWatcher myTextWatcher = new MyTextWatcher() {
        @Override
        public void afterTextChanged(Editable editable) {
            super.afterTextChanged(editable);
            login.setEnabled(false);
            Pattern num= Pattern.compile("[0-9]");
            Pattern english= Pattern.compile("[a-zA-Z]");
            Pattern chinese= Pattern.compile("[\u4e00-\u9fa5]");
            StringBuffer stringBuffer=new StringBuffer();
            stringBuffer.append(editable);
            for(int i=0;i<stringBuffer.length();i++){
                char answer = stringBuffer.charAt(i);
                if(!chinese.matcher(String.valueOf(answer)).matches()&&!english.matcher(String.valueOf(answer)).matches()
                        &&!num.matcher(String.valueOf(answer)).matches()&&!String.valueOf(answer).equals("_")&&!String.valueOf(answer).equals("@")
                        &&!String.valueOf(answer).equals(".")){
                   Util.show(context,"输入有误，只能为汉字、数字、字母和指定符号！");
                }
            }
            if (!TextUtils.isEmpty(eUsername.getText().toString()) && !TextUtils.isEmpty(ePassword.getText().toString())) {
                login.setEnabled(true);
            }
        }
    };

    Drawable focusdrawable;

    Drawable nofocusdrawable;
    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            phoneline.setBackground(nofocusdrawable);
            passwordline.setBackground(nofocusdrawable);
            switch (view.getId()) {
                case R.id.username:
                    Log.e("输入框", "用户名");
                    phoneline.setBackground(focusdrawable);
                    break;
                case R.id.password:
                    Log.e("输入框", "密码");
                    passwordline.setBackground(focusdrawable);
                    break;
            }
        }
    };

    private boolean isNotificationEnabled(Context context) {
        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        Class appOpsClass = null;
        /* Context.APP_OPS_MANAGER */
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void requestNotification(BaseActivity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            // 进入设置系统应用权限界面
            Intent intent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", activity.getPackageName());
            intent.putExtra("app_uid", activity.getApplicationInfo().uid);
            activity.startActivity(intent);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 运行系统在5.x环境使用
            // 进入设置系统应用权限界面
            Intent intent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", activity.getPackageName());
            activity.startActivity(intent);
            return;
        }
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
        ButterKnife.bind(this);
        aCache = ACache.get(this);
        forgetGesture=getIntent().getStringExtra("forgetGesture");
        //NotificationManager: notify: id corrupted: sent 1616487783, got back 0
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Log.e("通知栏", "areNotificationsEnabled" + isNotificationEnabled(context));

        }

        if (!areNotificationsEnabled) {
            requestNotification(this, 200);
        }


        focusdrawable = SelectorFactory.newShapeSelector()
                .setDefaultStrokeColor(Color.parseColor("#faf0e1"))
                .setStrokeWidth(Util.dpToPx(context, 0.5f))
                .setDefaultBgColor(Color.parseColor("#1ac4aa9e"))
                .setCornerRadius(Util.dpToPx(context, 2))
                .create(context);

        nofocusdrawable = SelectorFactory.newShapeSelector()
                .setDefaultBgColor(Color.parseColor("#1ac4aa9e"))
                .setCornerRadius(Util.dpToPx(context, 2))
                .create(context);

        LoginInfoBean loginInfoBean = SaveGetSharedPreferencesInfo.getLoginInfo(context);
        if (loginInfoBean != null) {
            eUsername.setText(loginInfoBean.getUserid());
            ePassword.setText(loginInfoBean.getPassword());
        }
        back.setVisibility(View.VISIBLE);
        center.setText("会员登录");
        share.setVisibility(View.GONE);
        refresh.setVisibility(View.GONE);

        login.setEnabled(false);
        eUsername.setOnFocusChangeListener(focusChangeListener);
        ePassword.setOnFocusChangeListener(focusChangeListener);
        eUsername.addTextChangedListener(myTextWatcher);
        ePassword.addTextChangedListener(myTextWatcher);


        login.setBackground(SelectorFactory.newShapeSelector()
                .setDefaultBgColor(Color.parseColor("#e8b979"))
                .setPressedBgColor(Color.parseColor("#edcb97"))
                .setDisabledBgColor(Color.GRAY)
                .setCornerRadius(Util.dpToPx(context, 2))
                .create(context));

        tologinyunda.setBackground(SelectorFactory.newShapeSelector()
                .setDefaultStrokeColor(Color.parseColor("#faf0e1"))
                .setStrokeWidth(Util.dpToPx(context, 0.5f))
                .setDefaultBgColor(Color.parseColor("#00ffffff"))
                .setPressedBgColor(Color.parseColor("#edcb97"))
                .setCornerRadius(Util.dpToPx(context, 2))
                .create(context));
        toRegistered.setBackground(SelectorFactory.newShapeSelector()
                .setDefaultStrokeColor(Color.parseColor("#faf0e1"))
                .setStrokeWidth(Util.dpToPx(context, 0.5f))
                .setDefaultBgColor(Color.parseColor("#f9f6f5"))
                .setPressedBgColor(Color.parseColor("#edcb97"))
                .setCornerRadius(Util.dpToPx(context, 2), 0, 0, Util.dpToPx(context, 2))
                .create(context));
        toForgetPassword.setBackground(SelectorFactory.newShapeSelector()
                .setDefaultStrokeColor(Color.parseColor("#faf0e1"))
                .setStrokeWidth(Util.dpToPx(context, 0.5f))
                .setDefaultBgColor(Color.parseColor("#00ffffff"))
                .setPressedBgColor(Color.parseColor("#edcb97"))
                .setCornerRadius(0, Util.dpToPx(context, 2), Util.dpToPx(context, 2), 0)
                .create(context));

        if (!sp.getString("username","").equals("")){
            eUsername.setText(sp.getString("username",""));
        }

        checkVersion();
    }


    @OnClick({R.id.login, R.id.tologinyunda, R.id.toRegistered, R.id.toForgetPassword,R.id.back})
    public void click(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.login:
                login(false);
                break;
            case R.id.tologinyunda:
                login(true);
                break;

            case R.id.back:
                startActivity(new Intent(this,MainActivity.class));
                break;
            case R.id.toRegistered:
                intent = new Intent(context, MainActivity.class);
                intent.putExtra("url", "/register.aspx");
                startActivity(intent);
                break;
            case R.id.toForgetPassword:
                intent = new Intent(context, MainActivity.class);
                intent.putExtra("url", "/repassword.aspx");
                intent.putExtra("forget", "forget");
                startActivity(intent);
                break;
        }

    }

    public void login(boolean isYD) {
        final String lName = eUsername.getText().toString();
        final String lpwd = ePassword.getText().toString();
        if (Util.isNull(lName)) {
            Util.show(context, "请输入用户名");
            return;
        } else if (Util.isNull(lpwd)) {
            Util.show(context, "请输入密码");
            return;
        } else if ("".equals(lpwd.trim())) {
            Util.show(context, "空格不能作为登录密码");
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("txtUserName", lName);
        params.put("txtPassword", isYD ? new MD5().getMD5ofStr(lpwd) : lpwd);
        params.put("site_id", "1");
        String mAction = isYD ? "user_login_yd" : "user_login";
        //user_login
        HttpManger.postRequest(context, "/tools/submit_api.ashx?action=" + mAction, params, "登录中", new HttpManger.DoRequetCallBack() {
//        HttpManger.postRequest(context, "/asp/login.asp", params, "登录中", new HttpManger.DoRequetCallBack() {


            @Override
            public void onSuccess(String o) {
                Log.e("请求结果", "str" + o);

//                if (true) {
//                    Gson gson = new Gson();
//                    TestBean testBean = gson.fromJson(o, TestBean.class);
//                    HashMap<String, String> params = new HashMap<>();
//                    params.put("site_id1", "1");
//                    params.put("site_id2", "2");
//                    params.put("token", testBean.getMsg().getToken());
//                    HttpManger.postRequest(context, "/asp/test.asp", params, "测试中", new HttpManger.DoRequetCallBack() {
//                        @Override
//                        public void onSuccess(String t) {
//
//                        }
//
//                        @Override
//                        public void onError(String t) {
//
//                        }
//                    });
//                    return;
//                }

                Gson gson = new Gson();
                UserInfoBean userInfoBean = gson.fromJson(o, UserInfoBean.class);
                UserInfoBean.setUser(userInfoBean);
//                Log.e("是否唯恐", "asa" + (UserInfoBean.getUser() == null));
                if (userInfoBean != null) {
//                    Log.e("请求结果 status", "status" + userInfoBean.getStatus());
                    if (userInfoBean.getStatus() == 1) {
//                        Log.e("请求结果 status1", "status 開始跳轉" + Util.isMainThread());
//                        Util.show(context, "開始跳轉2" + Util.getCurProcessName(context));
//                        Log.e("请求结果 status3", "status 開始跳轉" + Util.getCurProcessName(context));

                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra("login", "yes");
                        startActivity(intent);
                        SharedPreferences sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("user_id", 1);
                        editor.putString("username",lName);
                        editor.putString("password",lpwd);
                        editor.putString("token",userInfoBean.getToken());
                        editor.putString("path","");
                        if (userInfoBean.getModel().getAvatar().equals("")){
                            editor.putString("portrait","default");
                        }
                        else {
                            editor.putString("portrait",userInfoBean.getModel().getAvatar());
                        }
                        editor.commit();
                        Util.islock=true;
                        if (forgetGesture!=null){
                            startActivity(new Intent(context,CreateGestureActivity.class));
                        }
                        String gesturePassword = aCache.getAsString(Constant.GESTURE_PASSWORD);
                        if(gesturePassword == null || "".equals(gesturePassword)) {
                            Intent intent1 = new Intent(context, CreateGestureActivity.class);
                            intent1.putExtra("ignore","ignore");
                            startActivity(intent1);
                        }
                        JPushInterface.setAlias(LoginActivity.this, 200, lName);

                        finish();
                    }
                    Util.show(context, userInfoBean.getMsg());

                }


            }

            @Override
            public void onError(String o) {

            }
        });


    }

    private void checkVersion() {
        OkGo.<String>get(Web.url + Web.version).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                version = response.body().split("<Version>")[1].split("</Version>")[0].trim();
                content = response.body().split("<Content>")[1].split("</Content>")[0].trim();
                url = response.body().split("<Url>")[1].split("</Url>")[0].trim();
                createTime = response.body().split("<CreateTime>")[1].split("</CreateTime>")[0].substring(0, 10).trim();
                String str1=Util.getString(Util.getString(version,"."),".");//服务器版本
                String str2=Util.getString(Util.getString(Util.getVerName(context),"."),".");//当前版本
                if (Integer.valueOf(str1)>Integer.valueOf(str2)) {
                    new UpDataVersionDialog(context, content, url).show();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            new AlertDialog.Builder(LoginActivity.this).setTitle("提示：").setMessage("您确定要退出吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
                    System.exit(0);
                }
            }).setNegativeButton("取消", null).setIcon(R.drawable.escc).show();
        }
        return super.onKeyDown(keyCode, event);
    }


}
