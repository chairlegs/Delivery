package com.example.administrator.delivery;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyNetThread extends Thread {

    public static final int SEARCH=1;
    public static final int FEEDBACK=2;
    public Handler handler;
    private String result;
    private  String headerData;
    private  String url;
    private String name;
    private String token;
    public  int what=1;

    public MyNetThread(Handler handler,String url, String headerData, String name, String token,int what){
        this.handler=handler;
        this.url=url;
        this.headerData =headerData;
        this.name=name;
        this.token=token;
        this.what=what;
    }

    private  String requestByPost(String strUrl,String data) throws Throwable {
        String path = strUrl;
        // 请求的参数转换为byte数组
        HttpURLConnection urlConn=null;
        byte[] postData = data.getBytes();
        BufferedReader br = null;
        InputStream is;
        StringBuffer sb = new StringBuffer();
        try {
            // 新建一个URL对象
            URL url = new URL(path);
            // 打开一个HttpURLConnection连接
            urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接超时时间
            urlConn.setConnectTimeout(5 * 1000);
            // Post请求必须设置允许输出
            urlConn.setDoOutput(true);
            // Post请求不能使用缓存
            urlConn.setUseCaches(false);
            // 设置为Post请求
            urlConn.setRequestMethod("POST");
            urlConn.setInstanceFollowRedirects(true);
            // 配置请求Content-Type
            urlConn.setRequestProperty("Connection",
                    "Keep - Alive");
            urlConn.setRequestProperty("Accept - Encoding",
                    "gzip");
            urlConn.setRequestProperty("Content-Type",
                    "application/json");
            urlConn.setRequestProperty("person_no", name);
            urlConn.setRequestProperty("pda_type", "A");
            urlConn.setRequestProperty("pda_sys", "Android");
            urlConn.setRequestProperty("pda_user_token", token);
            urlConn.setRequestProperty("longitude", "114.299618");
            urlConn.setRequestProperty("pda_sys_version", "4.4.4");
            urlConn.setRequestProperty("pda_version", "62");
            urlConn.setRequestProperty("latitude", "27.696155");
            urlConn.setRequestProperty("dlv_equipment_type", "Neolix");
            urlConn.setRequestProperty("pda_id", "862702031833369");
            urlConn.setRequestProperty("User-Agent", "Dalvik/1.6.0 (Linux; U; Android 4.4.4; Neolix 1-C-P Build/KTU84P)");
            // 开始连接
            urlConn.connect();
            // 发送请求参数
            DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
            dos.write(postData);
            dos.flush();
            dos.close();
            // 判断请求是否成功

            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 获取返回的数据
                is = urlConn.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String str = "";
                while ((str = br.readLine()) != null) {
                    sb.append(str);
                }
            } else {

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            urlConn.disconnect();
            br.close();
        }
        return sb.toString();
    }
    @Override
    public void run() {
        try {
            result=requestByPost(url, headerData);
            Message msg=new Message();
            msg.what=what;
            msg.obj=result;
            handler.sendMessage(msg);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
