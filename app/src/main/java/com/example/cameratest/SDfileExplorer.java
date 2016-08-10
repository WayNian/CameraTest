package com.example.cameratest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SDfileExplorer extends Activity {
    private ListView listView;
    private TextView contentsTextView;

    // 记录当前的父文件夹
    private File currentParent;
    // 记录当前路径下的所有文件的文件数组
    private File[] currentFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_showmain);

        // 获取列出全部文件的ListView
        listView = (ListView) findViewById(R.id.list);
        contentsTextView = (TextView) findViewById(R.id.ContentsTextView);
        contentsTextView.setText("文件保存在： "+CameraActivity.extsd_path.toString());

        // 获取系统的SD卡的目录
        File file = new File(CameraActivity.extsd_path);
        currentParent = file;
        currentFiles = file.listFiles();
        // 使用当前目录下的全部文件、文件夹来填充ListView
        inflateListView(currentFiles);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                File f = new File(currentFiles[arg2].getAbsolutePath());

                if (f.toString().endsWith(".mp4")) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(f), "video/*");
                    startActivity(intent);
                } else if (f.toString().endsWith(".jpg")) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(f), "image/*");
                    startActivity(intent);
                }
            }
        });
    }

    private void inflateListView(File[] files) // ①
    {
        // 创建一个List集合，List集合的元素是Map
        List<Map<String, Object>> listItems =
                new ArrayList<Map<String, Object>>();
        for (int i = 0; i < files.length; i++)
        {
            Map<String, Object> listItem =
                    new HashMap<String, Object>();
            // 如果当前File是文件夹，使用folder图标；否则使用file图标

            listItem.put("fileName", files[i].getName());
            // 添加List项
            listItems.add(listItem);
        }
        // 创建一个SimpleAdapter
        SimpleAdapter simpleAdapter = new SimpleAdapter(this
                , listItems, R.layout.camera_showline
                , new String[] {
                        "fileName"
                }
                , new int[] {
                        R.id.file_name
                });
        // 为ListView设置Adapter
        listView.setAdapter(simpleAdapter);
    }
}

