package com.yda.yiyunchain.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.yda.yiyunchain.R;


/**
 * @author: yzq
 * @date: 2017/10/26 15:17
 * @declare :
 */

public class SaoActivity extends AppCompatActivity{

    private int REQUEST_CODE_SCAN = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sao);
        //Sao();
    }

//
//    public void Sao(){
//        try {
//            AndPermission.with(this)
//                    .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
//                    .onGranted(new Action() {
//                        @Override
//                        public void onAction(List<String> permissions) {
//                            Intent intent = new Intent(SaoActivity.this, CaptureActivity.class);
//                            /*ZxingConfig是配置类
//                             *可以设置是否显示底部布局，闪光灯，相册，
//                             * 是否播放提示音  震动
//                             * 设置扫描框颜色等
//                             * 也可以不传这个参数
//                             * */
//                            ZxingConfig config = new ZxingConfig();
//                            config.setPlayBeep(true);//是否播放扫描声音 默认为true
//                            config.setShake(true);//是否震动  默认为true
////                                config.setDecodeBarCode(false);//是否扫描条形码 默认为true
////                                config.setReactColor(R.color.white);//设置扫描框四个角的颜色 默认为淡蓝色
////                                config.setFrameLineColor(R.color.white);//设置扫描框边框颜色 默认无色
////                                config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
//                            intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
//                            startActivityForResult(intent, REQUEST_CODE_SCAN);
//                        }
//                    })
//                    .onDenied(new Action() {
//                        @Override
//                        public void onAction(List<String> permissions) {
//                            Uri packageURI = Uri.parse("package:" + getPackageName());
//                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                            startActivity(intent);
//
//                            Toast.makeText(SaoActivity.this, "没有权限无法扫描呦", Toast.LENGTH_LONG).show();
//                        }
//                    }).start();
//        }catch (Exception e){
//
//            Log.e("************",e.getMessage());
//        }
//
//    }
//
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // 扫描二维码/条码回传
//        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
//            if (data != null) {
//
//                String content = data.getStringExtra(Constant.CODED_CONTENT);
//                Intent intent=new Intent();
//                intent.setClass(SaoActivity.this,ZhuanZang.class);
//                intent.putExtra("result",content);
//                startActivity(intent);
//                finish();
//            }
//        }
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



}