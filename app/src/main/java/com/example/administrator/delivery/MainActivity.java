package com.example.administrator.delivery;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static boolean isDebug=true;

    public TextView tv_tip;
    public TextView tv_info;
    public Button btn_serach;
    public Button btn_feedback;
    public ArrayList<HashMap> wayNoList=new ArrayList();
    public ArrayList<HashMap> stateList=new ArrayList();
    private HashMap<String,String> infos=new HashMap<>();

    public Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://read xml
                    infos.clear();
                    infos.putAll((HashMap<String, String>) msg.obj);
                    tv_tip.setText(infos.values().toString());
                    break;
                case 1://netThread search
                    String result= (String) msg.obj;
                    wayNoList.clear();
                    stateList.clear();
                    stateList=parser(result,wayNoList);
                    tv_info.setText(stateList.get(0).get("basecount").toString());
                    break;
                case 2://netThread feedback
                    String res= (String) msg.obj;
                    Toast.makeText(MainActivity.this,res,Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new MyReadThread(handler).start();
        tv_tip = findViewById(R.id.tip);
        tv_info = findViewById(R.id.textView);
        btn_serach = findViewById(R.id.button);
        btn_feedback = findViewById(R.id.button2);
        btn_serach.setOnClickListener(this);
        btn_feedback.setOnClickListener(this);

    }

    /***
     *
     * @param str source
     * @param data reusult
     * @return state
     */
    public ArrayList parser(String str,ArrayList data) {
        data.clear();
        if (str == null) {
            return null;
        }
        str=str.replace("\\", "");
        str=str.replace("\"", "");
        String dataStr=str.substring(str.indexOf("[")+1,str.indexOf("]"));
        String state=str.substring(str.indexOf("]")+2,str.indexOf("msg")-2);
        ArrayList wayNoList=findStr(dataStr,"waybillNo");
        data.addAll(wayNoList);
        ArrayList state_basecount=findStr(state,"basecount");

        return state_basecount;
    }

    private ArrayList findStr(String data,String target) {

        ArrayList list = new ArrayList();
        int fromIndex = 0;
        int endIndex=0;
        boolean life=true;
        while (fromIndex < data.length()&&life) {
            int tempStart = data.indexOf(target, fromIndex);
            int tempEnd= 0;
            if (tempStart==-1){
                life=false;
                return list;
            }else {
                fromIndex=tempStart;
                tempEnd=data.indexOf(",", tempStart);
            }
            if (tempEnd == -1) {
                tempEnd = data.indexOf("}", fromIndex);
                if (endIndex==-1){
                    return list;
                }
                endIndex=tempEnd;
                life=false;
            }else{
                endIndex=tempEnd;
            }
            Map<String,String> map=new HashMap<>();
            String s=data.substring(fromIndex, endIndex);
            String[] ss=s.split(":");
            map.put(ss[0],ss[1]);
            list.add(map);
            fromIndex=endIndex;
        }

        return list;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:
                doSearch();
                break;
            case R.id.button2:
                doFeedback();
                break;
        }
    }

    private void doSearch(){
        String url=url = "http://211.156.195.15/delivery-app/p/delivery/pdataskquery/findpdasignwaybillinfo.do";
        String data="{\"waybillState\":\"2\"," +
                "\"pageNo\":\"\"," +
                "\"waybillNo\":\"\"," +
                "\"businessProductCode\":\"\"," +
                "\"importRegionDate\":\"\"," +
                " \"dlvState\":\"\"" +
                "}";
        MyNetThread myNetThread=new MyNetThread(handler,url,data,infos.get("name"),infos.get("token"),MyNetThread.SEARCH);

        myNetThread.start();
    }

    private void doFeedback(){
        String url = "http://211.156.195.15/delivery-app/p/delivery/pdanondeliverybatch/updatenondeliverybatch.do";
        String st="\"";
        for (HashMap map: wayNoList) {
            st+=map.get("waybillNo");
            st+=",";
        }
        st+="\"";

        String data = "{\"appointmentDlvDate\":\"\",\"dlvNextAction\":\"2\",\"waybillNo\":"+st+",\"nondlvReason\":\"46\",\"dlvNotes\":\"次日再投\"}";
        MyNetThread myNetThread=new MyNetThread(handler,url,data,infos.get("name"),infos.get("token"),MyNetThread.FEEDBACK);

        myNetThread.start();
    }

}