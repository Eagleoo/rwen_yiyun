package com.yda.yiyunchain.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.yda.yiyunchain.Bean.BiBean;
import com.yda.yiyunchain.Bean.UserInfoBean;
import com.yda.yiyunchain.HttpUtil.HttpManger;
import com.yda.yiyunchain.MyTextWatcher;
import com.yda.yiyunchain.R;
import com.yda.yiyunchain.util.CommonUtil;
import com.yda.yiyunchain.util.MD5;
import com.yda.yiyunchain.util.Util;
import com.yda.yiyunchain.widget.BottomDialogOnclickListener;
import com.yda.yiyunchain.widget.BottomDialogView;
import com.yda.yiyunchain.widget.DialogUtil;
import com.yda.yiyunchain.widget.ListViewForScrollView;
import com.yda.yiyunchain.zxing.activity.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ZhuanZang extends BaseActivity {
    @BindView(R.id.edt_username)
    EditText edt_username;
    @BindView(R.id.sao)
    ImageView sao;
    @BindView(R.id.linear_username)
    LinearLayout linear_username;
    @BindView(R.id.username)
    TextView username;
    @BindView(R.id.linear_phone)
    LinearLayout linear_phone;
    @BindView(R.id.phone)
    TextView phone;
    @BindView(R.id.select_type)
    TextView select_type;
    @BindView(R.id.img_escc)
    ImageView img_escc;
    @BindView(R.id.img_usd)
    ImageView img_usd;
    @BindView(R.id.img_usdt)
    ImageView img_usdt;
    @BindView(R.id.edt_sellnum)
    EditText edt_sellnum;
    @BindView(R.id.receive_money)
    TextView receive_money;
    @BindView(R.id.need_money)
    TextView need_money;
    @BindView(R.id.edt_ma)
    EditText edt_ma;
    @BindView(R.id.btn_ma)
    Button btn_ma;
    @BindView(R.id.ok)
    Button ok;
    @BindView(R.id.relative_escc)
    RelativeLayout relative_escc;
    @BindView(R.id.relative_usd)
    RelativeLayout relative_usd;
    @BindView(R.id.relative_usdt)
    RelativeLayout relative_usdt;
    @BindView(R.id.linear_tu)
    LinearLayout linear_tu;
    @BindView(R.id.img_tu)
    ImageView img_tu;
    @BindView(R.id.edt_tu)
    EditText edt_tu;
    @BindView(R.id.listview)
    ListViewForScrollView listview;
    @BindView(R.id.tv_money)
    TextView tv_money;
    @BindView(R.id.linear_money)
    LinearLayout linear_money;

    boolean flag=true;
    String edt_name="";
    private TimeCount time;
    int id ;//货币id
    double tips ;//货币tips
    String showimg="";
    String money="";//账户余额
    //打开扫描界面请求码
    private int REQUEST_CODE = 0x01;
    //扫描成功返回码
    private int RESULT_OK = 0xA1;
    private SharedPreferences sp;
    List<BiBean> list=new ArrayList<>();
    MyTextWatcher textWatcher = new MyTextWatcher() {
        @Override
        public void afterTextChanged(Editable editable) {
            super.afterTextChanged(editable);
           if (editable.toString().length()==16){
               edt_name=editable.toString().trim();
               Req_User();
           }
           else {
               linear_phone.setVisibility(View.GONE);
               linear_username.setVisibility(View.GONE);
           }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        ButterKnife.bind(this);

        Intent intent=getIntent();
        sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
        if (intent.getStringExtra("result")!=null){
            edt_name=intent.getStringExtra("result");
            edt_username.setText(edt_name);
            Req_User();
        }
        Req_Token(false);
        Req_USDT();

        time = new TimeCount(60000, 1000);
        edt_username.addTextChangedListener(textWatcher);
        edt_sellnum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")){
                    receive_money.setText(Double.valueOf(s.toString())+Double.valueOf(s.toString())*tips+"");
                    need_money.setText(Double.valueOf(s.toString())*tips+"");
                }
                else {
                    receive_money.setText("");
                    need_money.setText("");
                }

            }
        });


    }



    @OnClick({R.id.edt_username, R.id.sao, R.id.select_type, R.id.img_escc, R.id.img_usd, R.id.img_usdt, R.id.btn_ma, R.id.ok,R.id.edt_sellnum,R.id.img_tu})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.edt_username:
                break;
            case R.id.sao:
//                startActivity(new Intent(ZhuanZang.this,SaoActivity.class));
//                finish();
                try{
                    final String[] requestPermissionstr = {

                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,

                    };
                    Util.checkpermissions(requestPermissionstr, context, new Util.PermissionsCallBack() {
                        @Override
                        public void success() {
                            if (CommonUtil.isCameraCanUse()) {
                                Intent intent = new Intent(context, CaptureActivity.class);
                                startActivityForResult(intent, REQUEST_CODE);
                            } else {
                                Toast.makeText(context, "请打开此应用的摄像头权限！", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure() {

                        }
                    });
                }catch (Exception e){
                    Util.show(context,e.getMessage());
                }


                break;

            case R.id.select_type:
//                relative_escc.setVisibility(View.VISIBLE);
//                relative_usd.setVisibility(View.VISIBLE);
//                relative_usdt.setVisibility(View.VISIBLE);
                //listview.setVisibility(View.VISIBLE);
                //Req_Type();
                break;
            case R.id.img_escc:
               Req_ESCC();
                break;
            case R.id.img_usd:
                Req_USD();
                break;
            case R.id.img_usdt:
                Req_USDT();
                break;
            case R.id.btn_ma:
                Req_Message();
                break;
            case R.id.ok:

                Req_Transfer();
                break;

            case R.id.edt_sellnum:

                break;
            case R.id.img_tu:
                    Req_Ma();
                break;

        }

    }

    public void Req_User(){
        HashMap<String, String> params = new HashMap<>();
        params.put("guidno",edt_name);
        //user_login
        HttpManger.postRequest(ZhuanZang.this, "/tools/submit_api.ashx?action=GetUserByGuid", params, "请稍后...", new HttpManger.DoRequetCallBack() {

            @Override
            public void onSuccess(String o) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(o);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String user_name = jsonObject.optString("user_name") + "";
                String mobile = jsonObject.optString("mobile") + "";
                String status = jsonObject.optString("status") + "";
                username.setText(user_name);
                phone.setText(mobile);
                if (!status.equals("0")){
                    linear_phone.setVisibility(View.VISIBLE);
                    linear_username.setVisibility(View.VISIBLE);
                }
                else {
                    Util.show(ZhuanZang.this, "没有找到该用户");
                }

                Log.e("请求结果", "str" + o);
            }

            @Override
            public void onError(String o) {
                Log.e("请求结果", o);
            }
        });
    }

    public void Req_Type(){

        HashMap<String, String> params = new HashMap<>();
        params.put("site_id", "1");
        HttpManger.postRequest(ZhuanZang.this, "/tools/submit_api.ashx?action=get_moneyType_list", params, "请稍后...", new HttpManger.DoRequetCallBack() {

            @Override
            public void onSuccess(String o) {
                list=Util.getJsonArrays("list",o);
                List<String> type_list=new ArrayList<>();

                for (int i=0;i<list.size();i++){
                    if (list.get(i).getName().equals("USDT")){
                        type_list.add(list.get(i).getName());
                    }
                }
                TypeAdapter adapter=new TypeAdapter(type_list,context);
                listview.setAdapter(adapter);
                Log.e("请求结果", "str" + o);
            }

            @Override
            public void onError(String o) {

            }
        });
    }

    public void Req_ESCC(){

        HashMap<String, String> params = new HashMap<>();
        params.put("site_id", "1");
        HttpManger.postRequest(ZhuanZang.this, "/tools/submit_api.ashx?action=get_moneyType_list", params, "请稍后...", new HttpManger.DoRequetCallBack() {

            @Override
            public void onSuccess(String o) {
                list=Util.getJsonArrays("list",o);
                for (int i=0;i<list.size();i++){
                    if (list.get(i).getId()==1){
                        tips=list.get(i).getTips();
                        id=list.get(i).getId();
                        select_type.setText(list.get(i).getName());
                        relative_escc.setVisibility(View.GONE);
                        relative_usd.setVisibility(View.GONE);
                        relative_usdt.setVisibility(View.GONE);
                        if (!edt_sellnum.getText().toString().equals("")){
                            receive_money.setText((Double.valueOf(edt_sellnum.getText().toString())+Double.valueOf(edt_sellnum.getText().toString())*tips)+"");
                            need_money.setText(Double.valueOf(edt_sellnum.getText().toString())*tips+"");
                        }

                        break;
                    }
                }
                Log.e("请求结果", "str" + o);
            }

            @Override
            public void onError(String o) {

            }
        });
    }

    public void Req_USD(){

        HashMap<String, String> params = new HashMap<>();
        params.put("site_id", "1");
        HttpManger.postRequest(ZhuanZang.this, "/tools/submit_api.ashx?action=get_moneyType_list", params, "请稍后...", new HttpManger.DoRequetCallBack() {

            @Override
            public void onSuccess(String o) {
                list=Util.getJsonArrays("list",o);
                for (int i=0;i<list.size();i++){
                    if (list.get(i).getId()==2){
                        tips=list.get(i).getTips();
                        id=list.get(i).getId();
                        select_type.setText(list.get(i).getName());
                        relative_escc.setVisibility(View.GONE);
                        relative_usd.setVisibility(View.GONE);
                        relative_usdt.setVisibility(View.GONE);
                            if (!edt_sellnum.getText().toString().equals("")){
                                receive_money.setText((Double.valueOf(edt_sellnum.getText().toString())+Double.valueOf(edt_sellnum.getText().toString())*tips)+"");
                                need_money.setText(Double.valueOf(edt_sellnum.getText().toString())*tips+"");
                            }

                        break;
                    }
                }
                Log.e("请求结果", "str" + o);
            }

            @Override
            public void onError(String o) {

            }
        });
    }

    public void Req_USDT(){

        HashMap<String, String> params = new HashMap<>();
        params.put("site_id", "1");
        HttpManger.postRequest(ZhuanZang.this, "/tools/submit_api.ashx?action=get_moneyType_list", params, "请稍后...", new HttpManger.DoRequetCallBack() {

            @Override
            public void onSuccess(String o) {
                list=Util.getJsonArrays("list",o);
                for (int i=0;i<list.size();i++){
                    if (list.get(i).getId()==3){
                        tips=list.get(i).getTips();
                        id=list.get(i).getId();
                        select_type.setText(list.get(i).getName());
                        relative_escc.setVisibility(View.GONE);
                        relative_usd.setVisibility(View.GONE);
                        relative_usdt.setVisibility(View.GONE);
                        if (!edt_sellnum.getText().toString().equals("")){
                            receive_money.setText((Double.valueOf(edt_sellnum.getText().toString())+Double.valueOf(edt_sellnum.getText().toString())*tips)+"");
                            need_money.setText(Double.valueOf(edt_sellnum.getText().toString())*tips+"");
                        }

                        break;
                    }
                }
                Req_Money();
                linear_money.setVisibility(View.VISIBLE);
                listview.setVisibility(View.GONE);
                if (!edt_sellnum.getText().toString().equals("")){
                    receive_money.setText((Double.valueOf(edt_sellnum.getText().toString())+Double.valueOf(edt_sellnum.getText().toString())*tips)+"");
                    need_money.setText(Double.valueOf(edt_sellnum.getText().toString())*tips+"");
                }
                Log.e("请求结果", "str" + o);
            }

            @Override
            public void onError(String o) {

            }
        });
    }

    public void Req_Ma(){

        edt_tu.setText("");
            Random r=new Random();
            int i = r.nextInt(10);//0-9随机数
        Glide.with(ZhuanZang.this).load("http://apphk.esccclub.com/tools/verify_code.ashx/"+i+"?token="+ UserInfoBean.getUser().getToken()).into(img_tu);

            linear_tu.setVisibility(View.VISIBLE);
    }

    public void Req_Message(){
        if (edt_username.getText().toString().equals("")||select_type.getText().toString().equals("")
                ||edt_sellnum.getText().toString().equals("")||receive_money.getText().toString().equals("")
                ||need_money.getText().toString().equals("")){
            Util.show(ZhuanZang.this,"请填写完整数据！");
            flag=false;

        }
        else if (showimg.equals("1") && edt_tu.getText().toString().equals("")) {
            flag=false;
            Util.show(context, "请输入图形验证码!");
            return;
        }
        else {
            HashMap<String, String> params = new HashMap<>();
            params.put("mobile",  sp.getString("username",""));
            params.put("imgcode", edt_tu.getText().toString());
            params.put("moneytype", String.valueOf(id));
            params.put("num", edt_sellnum.getText().toString());
            params.put("token", sp.getString("token",""));
            HttpManger.postRequest(ZhuanZang.this, "/tools/submit_api.ashx?action=user_verify_smscode_img", params, "请稍后...", new HttpManger.DoRequetCallBack() {

                @Override
                public void onSuccess(String o) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(o);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showimg = jsonObject.optString("showimg") + "";
                    String msg = jsonObject.optString("msg") + "";
                    String status = jsonObject.optString("status") + "";
                    if (showimg.equals("1")){
                        Req_Ma();
                    }
                    if (msg.contains("对不起，图形验证码超时或已过期！")){
                        flag=false;
                        Util.show(context, msg);
                    }
                    if (status.equals("-1")){
                        Util.show(context, msg);
                    }
                    if (flag){
                        time.start();
                    }
                    flag=true;

                    Log.e("请求结果", "str" + o);
                }

                @Override
                public void onError(String o) {
                    Log.e("请求结果", "erro" + o);
                }
            });
        }


    }


    public void Req_Transfer(){

        if (edt_username.getText().toString().equals("")||select_type.getText().toString().equals("")
                ||edt_sellnum.getText().toString().equals("")||receive_money.getText().toString().equals("")
                ||need_money.getText().toString().equals("")|| edt_ma.getText().toString().equals("")){
            Util.show(ZhuanZang.this,"请填写完整数据！");
        }
        else {
            HashMap<String, String> params = new HashMap<>();
            params.put("toUser",  edt_username.getText().toString());
            params.put("imgyzcode", "1");
            params.put("moneyTypeid", String.valueOf(id));
            params.put("num", receive_money.getText().toString());
            params.put("token",sp.getString("token",""));
            params.put("yzcode", edt_ma.getText().toString());
            DialogUtil.showBottomDialog(ZhuanZang.this, R.id.pay ,params,new BottomDialogOnclickListener() {

                @Override
                public void onPositiveClick(String contentStr, BottomDialogView dialogView) {

                }
            });
        }

//        else {
//            SharedPreferences sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
//            HashMap<String, String> params = new HashMap<>();
//            params.put("toUser",  edt_username.getText().toString());
//            params.put("imgyzcode", edt_tu.getText().toString());
//            params.put("moneyTypeid", String.valueOf(id));
//            params.put("num", receive_money.getText().toString());
//            params.put("token",sp.getString("token",""));
//            //params.put("spassword", password.getText().toString());
//            params.put("yzcode", edt_ma.getText().toString());
//            HttpManger.postRequest(ZhuanZang.this, "/tools/submit_api.ashx?action=moneyTransfer", params, "请稍后...", new HttpManger.DoRequetCallBack() {
//
//                @Override
//                public void onSuccess(String o) {
//
//                    JSONObject jsonObject = null;
//                    try {
//                        jsonObject = new JSONObject(o);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    String msg = jsonObject.optString("msg") + "";
//                    Util.show(ZhuanZang.this,msg);
//
//                    Log.e("请求结果", "**********str**********" + o);
//                }
//
//                @Override
//                public void onError(String o) {
//                    Log.e("请求结果", "erro" + o);
//                }
//            });
//        }

    }
    public void Req_Money(){

        HashMap<String, String> params = new HashMap<>();
        params.put("site_id", "1");
        HttpManger.postRequest(ZhuanZang.this, "/tools/submit_api.ashx?action=get_user_info", params, "请稍后...", new HttpManger.DoRequetCallBack() {
            @Override
            public void onSuccess(String t) {

                JSONObject json = null;
                try {
                    json = new JSONObject(t);
                    JSONObject array=json.getJSONObject("model");
                    money=array.optString("usdt");
                    tv_money.setText("可用余额："+ money);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("请求结果", "str" + t);
            }

            @Override
            public void onError(String t) {

            }
        });
}

    //token过期重新获取
    public void Req_Token(boolean isYD){
        String lName = sp.getString("username","");
        String lpwd = sp.getString("password","");
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

                Gson gson = new Gson();
                UserInfoBean userInfoBean = gson.fromJson(o, UserInfoBean.class);
                UserInfoBean.setUser(userInfoBean);
                if (userInfoBean != null) {
                  if (userInfoBean.getStatus() == 1) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("token",userInfoBean.getToken());
                        editor.commit();
                    }


                }


            }

            @Override
            public void onError(String o) {

            }
        });
    }


    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btn_ma.setClickable(false);
            btn_ma.setText("("+millisUntilFinished / 1000 +")s后重新发送");
        }

        @Override
        public void onFinish() {
            btn_ma.setText("重新获取");
            btn_ma.setClickable(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (resultCode == RESULT_OK) { //RESULT_OK = -1
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("qr_scan_result");
            //将扫描出的信息显示出来
            Log.e("将扫描出的信息显示出来", "scanResult:" + scanResult);
            edt_username.setText(scanResult);
            Req_User();
        }
    }

    public class TypeAdapter extends BaseAdapter {
        private List<String> mData;
        private Context mContext;

        public TypeAdapter(List<String> mData, Context mContext) {
            this.mData = mData;
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;

            if (view == null) {
                //创建缓冲布局界面，获取界面上的组件
                view = LayoutInflater.from(mContext).inflate(
                        R.layout.type_layout, viewGroup, false);
                //  Log.v("AnimalAdapter","改进后调用一次getView方法");
                holder = new ViewHolder();
                holder.type = (TextView) view.findViewById(R.id.type);
                holder.img_type = (ImageView) view.findViewById(R.id.img_type);

                view.setTag(holder);
            } else {
                //用原有组件
                holder = (ViewHolder) view.getTag();
            }
            holder.type.setText(mData.get(i));

            holder.img_type.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (list.get(i).getName().equals("USDT")){
                                Req_Money();
                                linear_money.setVisibility(View.VISIBLE);
                                tips=list.get(i).getTips();
                                id=list.get(i).getId();
                                select_type.setText(list.get(i).getName());
                                listview.setVisibility(View.GONE);
                                if (!edt_sellnum.getText().toString().equals("")){
                                    receive_money.setText((Double.valueOf(edt_sellnum.getText().toString())+Double.valueOf(edt_sellnum.getText().toString())*tips)+"");
                                    need_money.setText(Double.valueOf(edt_sellnum.getText().toString())*tips+"");
                                }
                            }
                            else {
                                Util.show(context, "目前暂支持USDT转账");
                            }

                        }
                    }
            );

            return view;
        }

        private final class ViewHolder {

            TextView type;
            ImageView img_type;

        }
    }
}
