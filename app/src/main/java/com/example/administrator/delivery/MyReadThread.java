package com.example.administrator.delivery;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MyReadThread extends Thread {

    public static final String PATH="/sdcard/xxxx.xml";
    private Handler handler;
    private HashMap<String, String> infos =new HashMap<>();

    public MyReadThread(Handler handler){
        this.handler=handler;
    }

//root?
    private static boolean isRoot(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); // 切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    //copy app xml to sdcard
    private static ArrayList execCmdsforResult(String[] cmds) {
        ArrayList<String> list = new ArrayList<String>();
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (Exception e) {
            return null;
        }
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        process.getErrorStream();
        File file = new File(PATH);
        if (!MainActivity.isDebug&&file.exists()) {
            file.delete();
        }
        try {
            out.write(cmds[0]);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (Exception e) {

            }
        }
        return list;
    }

    private HashMap<String ,String> readXml() {
        HashMap<String ,String> map=new HashMap<>();
        StringBuilder sb = new StringBuilder("");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(PATH));
            String content = null;
            while ((content = br.readLine()) != null) {
                sb.append(content);
            }
            String str = sb.toString().replace("&quot;", "");
            String result = str.substring(str.indexOf("{")+1, str.indexOf("}"));
            String[] infs = result.split(",");
            for (String info : infs) {
                String[] s = info.split(":");
                map.put(s[0], s[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    return map;
    }
    @Override
    public void run() {
        execCmdsforResult(new String[]{"cat /data/data/cn.chinapost.jdpt.pda.pickup/shared_prefs/user_desk_chair.xml > "+PATH});
        File file = new File(PATH);
        while(!file.exists()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        infos.clear();
        infos.putAll(readXml());
        Message msg=new Message();
        msg.what=0;
        msg.obj=infos;
        handler.sendMessage(msg);
    }
}
