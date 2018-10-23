package com.yda.yiyunchain.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.yda.yiyunchain.Bean.UserInfoBean;
import com.yda.yiyunchain.ConfigurationInfor;
import com.yda.yiyunchain.MyChromeClient;
import com.yda.yiyunchain.R;
import com.yda.yiyunchain.WebViewJsInterface;
import com.yda.yiyunchain.util.ACache;
import com.yda.yiyunchain.util.Constant;
import com.yda.yiyunchain.util.DownLoadPicUtil;
import com.yda.yiyunchain.util.Util;
import com.yda.yiyunchain.util.Web;
import com.yda.yiyunchain.widget.ShapeLoadingDialog;
import com.yda.yiyunchain.wxapi.WXPayEntryActivity;

import org.apache.http.util.EncodingUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.sharesdk.onekeyshare.OnekeyShare;

import static com.yda.yiyunchain.MyChromeClient.mCameraFilePath;

public class MainActivity extends BaseActivity implements WebViewJsInterface.StrCallBack {

    @BindView(R.id.center)
    TextView center;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.nowifi)
    View nowifi;
    private ShapeLoadingDialog shapeLoadingDialog;
    private int state = 0; // 0登录 1 会员中心 2 商品
    private String shareurl = "";
    String url = "";
    SharedPreferences sp;
    private ACache aCache;
    private String gesturePassword;
    private HomeWatcherReceiver mHomeWatcherReceiver = null;
    public ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final int RESULT_CODE_PICK_FROM_ALBUM_BELLOW_LOLLILOP = 1;
    private final int RESULT_CODE_PICK_FROM_ALBUM_ABOVE_LOLLILOP = 2;
    private static final String APP_CACAHE_DIRNAME = "/webcache";
    boolean unbind = false;

    private void setNowifi(boolean isnowifi) {
        if (isnowifi) {
            nowifi.setVisibility(View.VISIBLE);
        } else {
            nowifi.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (getIntent().hasExtra("dealWith")) {
            int dealwithid = getIntent().getIntExtra("dealWith", -1);
//            Log.e("checkEquipment", getIntent().getIntExtra("dealWith", -1) + "");
            if (dealwithid == 1) {
                webView.loadUrl(Web.url + "/userbussions.aspx?action=list");
            } else if (dealwithid == 2) {
                webView.loadUrl(Web.url + "/userbussions_aplay.aspx?action=list");
            } else if (dealwithid == 3) {
                webView.loadUrl(Web.url + "/usercenter.aspx?action=index");
            } else if (dealwithid == 4) {
                webView.loadUrl(Web.url + "/useramount.aspx?action=recharge");
            } else if (dealwithid == 5) {
                webView.loadUrl(Web.url + "/useramount.aspx?action=silver");
            }

        }else if(getIntent().hasExtra("login")){
            String str= getIntent().getStringExtra("login");
            if(str.equals("yes")){
                StringBuilder builder1 = new StringBuilder();
                builder1.append("token=").append(UserInfoBean.getUser().getToken());
                String postData = builder1.toString();
                webView.postUrl(Web.url + Web.autologin, EncodingUtils.getBytes(postData, "UTF-8"));
                webView.addJavascriptInterface(new WebViewJsInterface(this), "webviewcall");
            }else if(str.equals("back")){
                webView.loadUrl(Web.url);
            }
            else if (str.equals("false")){
                webView.loadUrl(Web.url);
            }
        }
        else if (getIntent().hasExtra("url")){
            String str= getIntent().getStringExtra("url");
            webView.loadUrl(Web.url+str);
        }else if (getIntent().hasExtra("loginout")){
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("token");
            editor.commit();
            webView.loadUrl(Web.url+"/usercenter.aspx?action=exit");
        }
        else if (getIntent().hasExtra("about")){
            webView.loadUrl(Web.url+"/news_show.aspx?id=45");
        }
        else if (getIntent().hasExtra("notify_url")){
            String str= getIntent().getStringExtra("notify_url");
            webView.loadUrl(str);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_main);
        if (getIntent().hasExtra("url")) {
            url = getIntent().getStringExtra("url");
        }

        ButterKnife.bind(this);
        aCache = ACache.get(this);
        registerReceiver();
        sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
        setView();
//        if (sp.getInt("service",0)==1){
//            webView.loadUrl(Web.url+"/mobile/useramount.aspx?action=yw_list2");
//        }
        //setupService();
        //MobileInfoUtils.jumpStartInterface(MainActivity.this);
    }

    boolean errorurl = false;

    public void setView() {
        shapeLoadingDialog = new ShapeLoadingDialog.Builder(context)
                .loadText("加载中...")
                .build();
        //声明WebSettings子类
        WebSettings webSettings = webView.getSettings();
        if (Util.isNetworkAvalible(context)){
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//根据cache-control决定是否从网络上取数据。
        }
        else {
            //只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        webSettings.setUserAgentString(ConfigurationInfor.UserAgent);//设置代理
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setSupportZoom(false); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 开启 DOM storage API 功能
        webSettings.setDomStorageEnabled(true);
        //开启 database storage API 功能
        webSettings.setDatabaseEnabled(true);
        //开启 Application Caches 功能
        webSettings.setAppCacheEnabled(true);
        webView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        webView.setBackgroundResource(R.color.black80);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            Log.e("开启硬件加速","开启硬件加速");
        }
        webView.setWebChromeClient(new MyChromeClient(this));
        //如果不设置WebViewClient，请求会跳转系统浏览器
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e("错误", "onReceivedError" + view.getUrl());
                //单独处理拍照时特殊情况
                if (view.getUrl().equals(Web.url + "/usercenter.aspx?action=index")&&Util.isNetworkAvalible(MainActivity.this)){
                    webView.loadUrl(Web.url+"/usercenter.aspx?action=index");
                }
                else {
                    errorurl = true;
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                shareurl = url;
                Log.e("现在加载的页面", "url" + url);
                if (url.equals(Web.url + "/usercenter.aspx?action=index")&&sp.getString("token","").equals("")){
                    webView.loadUrl(Web.url+"/usercenter.aspx?action=exit");
                }
                //支付宝微信上传二维码时不能更新本地用户头像
                if (shareurl.equals(Web.url+"/usercenter.aspx?action=webcat")||shareurl.equals(Web.url+"/usercenter.aspx?action=alipay")){
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("other","no");
                    editor.commit();
                }
                else {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("other","yes");
                    editor.commit();
                }

                if (url.contains("login.aspx")) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.remove("token");
                    editor.commit();
                    Util.showIntent(context, LoginActivity.class);
                    Util.setIsLogin(false);
                    state = 0;
                    finish();
                    return true;
                } else if (url.contains(Web.url + "/mobile/usercenter.aspx")) {
                    state = 1;
                } else {
                    state = 2;
                }
                if (url.startsWith("weixin://wap/pay?") || url.startsWith("alipays://platformapi/startApp?")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(context, "暂未安装相关APP", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }

                if (url.contains("usercenter.aspx?action=account") && unbind) {
                    unbind = false;
                    webView.reload();
//                    webView.loadUrl("http://app.szcsf.cn/usercenter.aspx?action=index");
                }

                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                shapeLoadingDialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.e("errorurl", "errorurl:" + errorurl);
                if (errorurl) {
                    setNowifi(true);
                    errorurl = false;
                } else {
                    setNowifi(false);
                }

                if (view.getTitle().contains(Web.url)) {
                    center.setText(R.string.app_name);
                } else {
                    center.setText(Util.formatTitleStr(view.getTitle()));
                }
                String title=Util.formatTitleStr(view.getTitle());
                if (title.equals("首页")||title.equals("ESCC/USD")||title.equals("USD/CNY")||title.equals("会员中心")){
                    back.setVisibility(View.GONE);
                }
                else {
                    back.setVisibility(View.VISIBLE);
                }
                //webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                shapeLoadingDialog.dismiss();

            }
        });
        Intent intent = getIntent();

        if (intent.hasExtra("login")) {
            StringBuilder builder1 = new StringBuilder();
            builder1.append("token=").append(UserInfoBean.getUser().getToken());
            String postData = builder1.toString();
            webView.postUrl(Web.url + Web.autologin, EncodingUtils.getBytes(postData, "UTF-8"));
            webView.addJavascriptInterface(new WebViewJsInterface(this), "webviewcall");
        } else {
            if (!url.equals("")) {
                webView.loadUrl(Web.url + url);
            } else {
                webView.loadUrl(Web.url);
            }
            webView.addJavascriptInterface(new WebViewJsInterface(this), "webviewcall");

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                if (getIntent().getStringExtra("forget")!=null){
                    Util.showIntent(context, LoginActivity.class);
                }
                else {
                    if (webView.getUrl().contains("usercenter.aspx?action=password") || webView.getUrl().contains("usercenter.aspx?action=pay_password")) {
                        webView.loadUrl(Web.url + "/usercenter.aspx?action=index");
                    } else {
                        webView.goBack();
                    }
                }
            }else {
                alert();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick({R.id.back, R.id.refresh, R.id.share_iv, R.id.nowifi})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.back:
                Log.e("加载地址", "地址:" + webView.getUrl());
                if (getIntent().getStringExtra("forget")!=null){
                    Util.showIntent(context, LoginActivity.class);
                }
                else {
                    if (webView.getUrl().contains("usercenter.aspx?action=password") || webView.getUrl().contains("usercenter.aspx?action=pay_password")) {
                        webView.loadUrl(Web.url + "/usercenter.aspx?action=index");
                    } else if (webView.getUrl().contains("userbussions.aspx?action=yw_list&page=")) {
                        int page = Integer.parseInt(Util.getValueByName(webView.getUrl(), "page"));
                        if (page >= 2) {
                            webView.loadUrl(Web.url + "/userbussions.aspx?action=list");
                        }
                    } else if (webView.getUrl().contains("/userbussions_aplay.aspx?action=list&back=zc")) {
                        webView.loadUrl(Web.url + "/asset.aspx");
                    } else if (webView.canGoBack()) {

                        webView.goBack();
                    } else {
                        alert();
                    }
                }
                break;
            case R.id.refresh:
                Util.clearAllCache(context);
                webView.reload();
                break;
            case R.id.share_iv:
                if (state == 1) {
                    shareurl = "http://bdxt.demo.dnsrw.com/mobile/login.aspx";
                    showShare("注册", "");
                } else if (state == 2) {
                    showShare("商品", "");
                }
                break;
            case R.id.nowifi:
                webView.reload();
                break;
        }
    }

    public void alert() {
        new AlertDialog.Builder(context).setTitle("提示：").setMessage("您确定要退出吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        }).setNegativeButton("取消", null).setIcon(R.mipmap.logo1).show();
    }

    private void showShare(String sharetext, String commentText) {
        Random r=new Random();
        int i = r.nextInt(10);//0-9随机数
        OnekeyShare oks = new OnekeyShare();
        oks.setTitle(sharetext);
        oks.setTitleUrl(shareurl);
        oks.setUrl(shareurl);
        //oks.setImagePath(Environment.getExternalStorageDirectory()+"/LOGO.png");
        oks.setImageUrl("http://apphk.esccclub.com//templates/mobile/images/logo.png?i="+i);
//        oks.setAddress("10086");
        oks.setComment(commentText);
        oks.setText(commentText);
        oks.setSite(shareurl);
        oks.setSilent(false);
        oks.setSiteUrl(shareurl);
        oks.show(this);
    }

    boolean isRefresh = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (isRefresh) {
            Log.e("重新刷新", "isRefresh:" + isRefresh);
            webView.reload();
            isRefresh = false;
        }
        if (!sp.getString("token","").equals("")&&!Util.islock){
            gesturePassword = aCache.getAsString(Constant.GESTURE_PASSWORD);
            if(gesturePassword!=null){
                if (getIntent().getStringExtra("forget")==null){
                    Intent intent = new Intent(context, GestureLoginActivity.class);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void doCall(String str) {
        try {
            JSONObject jsonObject = new JSONObject(str);
            String type = jsonObject.optString("type") + "";
            String ptype = jsonObject.optString("ptype") + "";
            String page = jsonObject.optString("page") + "";
            if (type.equals("goMall")) {
                new AlertDialog.Builder(context).setTitle("注意")
                        .setMessage("即将跳转远大云商")
                        .setIcon(R.mipmap.logo1)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goMall();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show()
                        .setCancelable(false);
            }
            if (type.equals("share")) {
                String user = jsonObject.optString("url") + "";
                shareurl = user;
                showShare("【易云链】诚挚邀你携手共进", "易云链通过区块链应用技术，致力于打造全球最具影响力的分布式支付生态，并通过区块链全球结算系统、智能合约、数字钱包等应用，使其支付链的支付结算体系更为方便快捷。下载地址：" + shareurl);

            } else if (type.equals("pay")) {
//                Util.show(context,"暂时只能支持线下支付");

                String orderNo = jsonObject.optString(WXPayEntryActivity.ORDERNO) + "";

                Intent intent = new Intent(this, WXPayEntryActivity.class);
                intent.putExtra(WXPayEntryActivity.ORDERNO, orderNo);
                startActivity(intent);

            } else if (type.equals("bind") || (type.equals("onbind"))) {

                if (type.equals("onbind")) {
                    unbind = true;
                }
                if (page.equals("withdraw_deposit")) {
                    isRefresh = true;
                }

                Intent intent = new Intent(this, AddWeiXinAliActivity.class);
                if (!Util.isNull(ptype)) {
                    if (ptype.equals("ay")) {
                        intent.putExtra(AddWeiXinAliActivity.BINGTAG, AddWeiXinAliActivity.ALIACCOUNT);
                    } else if (ptype.equals("wx")) {
                        intent.putExtra(AddWeiXinAliActivity.BINGTAG, AddWeiXinAliActivity.WINXINACCOUNT);
                    }
                }
                startActivity(intent);

            }
            else if (type.equals("转账")){
                startActivity(new Intent(this, ZhuanZang.class));
            }
            else if (type.equals("shoushi")){
                gesturePassword = aCache.getAsString(Constant.GESTURE_PASSWORD);
                if (gesturePassword!=null){
                    Intent intent = new Intent(context, GestureLoginActivity.class);
                    intent.putExtra("update", "update");
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(context, CreateGestureActivity.class);
                    startActivity(intent);
                }

            }
            else if (type.equals("appshare")){
                shareurl = Web.url + "/index.aspx?action=app_download";
                showShare("请下载安装易云链App", "易云链通过区块链应用技术，致力于打造全球最具影响力的分布式支付生态，并通过区块链全球结算系统、智能合约、数字钱包等应用，使其支付链的支付结算体系更为方便快捷。下载地址：" + shareurl);
            }else if (type.equals("setting")){
                startActivity(new Intent(this, SettingActivity.class));
            }
            else if (type.equals("appewm")||type.equals("ewm")){
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                final String[] requestPermissionstr = {

                                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,

                                };
                                Util.checkpermissions(requestPermissionstr, context, new Util.PermissionsCallBack() {
                                    @Override
                                    public void success() {
                                        final WebView.HitTestResult htr = webView.getHitTestResult();//获取所点击的内容
                                        if (htr.getType() == WebView.HitTestResult.IMAGE_TYPE
                                                || htr.getType() == WebView.HitTestResult.IMAGE_ANCHOR_TYPE
                                                || htr.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                                            Log.e("是图片", "判断被点击的类型为图片  " + htr.getExtra());
                                            final Dialog dialog = new Dialog(context);
                                            View view1 = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
                                            dialog.setContentView(view1);
                                            TextView textView1 = view1.findViewById(R.id.downloadpic);
                                            final TextView textView2 = view1.findViewById(R.id.reQRcode);
                                            textView2.setVisibility(View.GONE);
                                            DownLoadPicUtil.download(htr.getExtra(), new DownLoadPicUtil.DownLoadCallBack() {

                                                @Override
                                                public void onSuccess(Bitmap bitmap, String downloadpath) {
                                                    final Result result = DownLoadPicUtil.handleQRCodeFormBitmap(bitmap);
                                                    if (result != null) {
                                                        textView2.setVisibility(View.VISIBLE);
                                                        textView2.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
//                                                                       goIntent(result);
                                                                dialog.dismiss();
                                                                webView.loadUrl(result.toString());
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onFailure(String message) {

                                                }
                                            });
                                            textView1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    dialog.dismiss();
                                                    DownLoadPicUtil.download(htr.getExtra(), new DownLoadPicUtil.DownLoadCallBack() {


                                                        @Override
                                                        public void onSuccess(Bitmap bitmap, String downloadpath) {
                                                            Util.show(context, "下载路径为:" + downloadpath);
                                                        }

                                                        @Override
                                                        public void onFailure(String message) {

                                                        }
                                                    });
                                                }
                                            });

                                            dialog.setTitle("Dialog");
                                            Window window = dialog.getWindow();
                                            WindowManager.LayoutParams lp = window.getAttributes();
                                            window.setGravity(Gravity.CENTER);
                                            WindowManager windowManager = getWindowManager();
                                            Display display = windowManager.getDefaultDisplay();
                                            lp.width = (int) (display.getWidth() * 0.8); //设置宽度
                                            window.setAttributes(lp);
                                            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//内部背景透明
                                            dialog.show();
                                        }
                                    }

                                    @Override
                                    public void failure() {
                                        Toast.makeText(context, "请求允许应用权限请求", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                );

            }

        } catch (Exception e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri;
        if (data == null && mCameraFilePath.equals("")
                &&mUploadMessage == null && mUploadCallbackAboveL == null) {
            return;
        }
        if (data == null) {
            File file = new File(mCameraFilePath);
            uri = Uri.fromFile(file);
        } else {
            uri = data.getData();
            //支付宝微信上传二维码时不能更新本地用户头像
            if (!shareurl.equals(Web.url+"/usercenter.aspx?action=webcat")&&!shareurl.equals(Web.url+"/usercenter.aspx?action=alipay")){
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("path", String.valueOf(uri));
                editor.putString("portrait","change");
                editor.commit();
            }

        }
        switch (requestCode) {
            case RESULT_CODE_PICK_FROM_ALBUM_BELLOW_LOLLILOP:
//                uri = afterChosePic(data);
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(uri);
                    mUploadMessage = null;
                }
                break;
            case RESULT_CODE_PICK_FROM_ALBUM_ABOVE_LOLLILOP:
                try {
//                    uri = afterChosePic(data);
                    if (uri == null) {
                        mUploadCallbackAboveL.onReceiveValue(new Uri[]{});
                        mUploadCallbackAboveL = null;
                        break;
                    }
                    if (mUploadCallbackAboveL != null && uri != null) {
                        mUploadCallbackAboveL.onReceiveValue(new Uri[]{uri});
                        mUploadCallbackAboveL = null;
                    }
                } catch (Exception e) {
                    mUploadCallbackAboveL = null;
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
            // destory()
            ViewParent parent = webView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(webView);
            }

            webView.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearHistory();
            webView.clearView();
            webView.removeAllViews();
            webView.destroy();
            if (mHomeWatcherReceiver != null) {
                try {
                    unregisterReceiver(mHomeWatcherReceiver);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        //if (isBind) this.unbindService(conn);
        super.onDestroy();
    }

    private void goMall() {
        final PackageManager pm = context.getPackageManager();
        Intent intent = new Intent();
        if (null == pm.getLaunchIntentForPackage("com.mall.view")) {//没有获取到intent
            Toast.makeText(context, "请先安装远大云商", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://down.yda360.com/Mall.apk")));
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.mall.view", "com.mall.view.Leading");
            intent.putExtra("openClassName", "com.yda.yiyunchain");
            startActivity(intent);
        }
    }

    public class HomeWatcherReceiver extends BroadcastReceiver {

        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {

            String intentAction = intent.getAction();
            if (TextUtils.equals(intentAction, Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (TextUtils.equals(SYSTEM_DIALOG_REASON_HOME_KEY, reason)) {
                    if (Util.isRecent){
                        Util.isRecent=false;
                    }
                    else {
                        Util.islock=false;
                    }
                }
                else if (TextUtils.equals(SYSTEM_DIALOG_REASON_RECENT_APPS, reason)) {
                    Util.isRecent=true;
                    Util.islock=true;
                }
            }
            else if (TextUtils.equals(intentAction, Intent.ACTION_SCREEN_OFF)){
                Util.islock=false;
            }
        }
    }
    private void registerReceiver() {
        mHomeWatcherReceiver = new HomeWatcherReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mHomeWatcherReceiver, filter);
    }


//    private boolean isBind = false;
//
//    private void setupService() {
//        Intent intent = new Intent(this, MyService.class);
//        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
//        startService(intent);
//
//    }
//
//    private ServiceConnection conn = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, final IBinder service) {
//            Log.e("服务绑定成功！","服务绑定成功！");
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//
//        }
//
//    };
}
