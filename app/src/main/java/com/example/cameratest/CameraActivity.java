package com.example.cameratest;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends Activity {
    // UI相关
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button okButton, photoButton, videoButton, browseButton;
    private Spinner channelSpinner;
    private TextView tv_jd, tv_wd;


    private int height = 290;// 制式通道的改变
    private int width = 0;
    private int mNumberOfCameras;

    // 功能相关
    private MediaRecorder mediaRecorder;
    private String videoFile = null;
    private boolean isRecord = false;// 判断是否录像

    private String dateString = null;// 保存文件
    public static String extsd_path = null;
    public static String sata_path = null;

    private Camera camera = null;
    private Camera.Parameters param = null;
    private boolean previewRunning = false;
    private boolean isOpen = false;

    // 数据保存
    private String[] channelList = {
            "单通道1", "单通道2", "单通道3", "单通道4", "双通道", "四通道",
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 取消标题栏和状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        // 初始化界面
        view_init();

        // 初始化surfaceView
        surfaceView();


    }

    private void surfaceView() {
        surfaceView = (SurfaceView) findViewById(R.id.surface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new MySurfaceViewCallback());
        surfaceHolder.setType(surfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private class MySurfaceViewCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            surfaceHolder = holder;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            surfaceHolder = holder;

            ok_choice();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            surfaceView = null;
            surfaceHolder = null;
        }
    }


    private void view_init() {

        File sdCardDir = Environment.getExternalStorageDirectory();
        extsd_path = sdCardDir.toString() + "/LGS_VEDIO/";
//
//        SDCardStatus path = new SDCardStatus();
//
//        sata_path = path.getPath2();
//
//        extsd_path = sata_path + "/LGS_VEDIO/";

        File new_path = new File(extsd_path);
        if (new_path.exists()) {
            File files[] = new_path.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        } else {
            if (!new_path.mkdirs()) {
                Toast.makeText(CameraActivity.this, "创建文件夹/LGS_VEDIO/失败，请检查！",
                        Toast.LENGTH_SHORT).show();
            }
        }
        System.out.println("------view_init------");

        channelSpinner = (Spinner) findViewById(R.id.channel_spinner);
        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new buttonClick());
//        photoButton = (Button) findViewById(R.id.photo_button);
//        photoButton.setOnClickListener(new buttonClick());
//        videoButton = (Button) findViewById(R.id.video_button);
//        videoButton.setOnClickListener(new buttonClick());
//        browseButton = (Button) findViewById(R.id.browse_button);
//        browseButton.setOnClickListener(new buttonClick());

        tv_jd = (TextView) findViewById(R.id.tv_jd);
        tv_wd = (TextView) findViewById(R.id.tv_wd);

        SpinnerAdapter channelAdapter = new SpinnerAdapter(CameraActivity.this,
                android.R.layout.simple_spinner_item, channelList);
        channelSpinner.setAdapter(channelAdapter);

        ok_choice();

    }

    public class buttonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.ok_button:
                    ok_choice();
//                    browseButton.setClickable(true);
//                    videoButton.setClickable(true);
                    break;
//                case R.id.photo_button:
//                    photo(v);
//                    break;
//                case R.id.video_button:
//                    video();
//                    browseButton.setClickable(false);
//                    videoButton.setClickable(false);
//                    break;
//                case R.id.browse_button:
//                    browse();
//                    break;
                default:
                    break;
            }
        }
    }

    private void ok_choice() {
        System.out.println("------ok button down------");

        String channel = channelSpinner.getSelectedItem().toString();


        if (channel.equals("单通道1")) {
            width = 500;
        } else if (channel.equals("单通道2")) {
            width = 501;
        } else if (channel.equals("单通道3")) {
            width = 502;
        } else if (channel.equals("单通道4")) {
            width = 503;
        } else if (channel.equals("双通道")) {
            width = 505;
        } else if (channel.equals("四通道")) {
            width = 504;
        }

        CloseVideo();
        CloseCamera();

        InitCamera();

        mNumberOfCameras = Camera.getNumberOfCameras();
        Log.e("摄像头", String.valueOf(mNumberOfCameras));
    }

    // 初始化camera
    private void InitCamera() {
        System.out.println("------InitCamera------");

            camera = Camera.open(); // 取得第一个摄像头
            param = camera.getParameters();// 获取param
            param.setPreviewSize(width, height);// 设置预览大小
            param.setPreviewFpsRange(4, 10);// 预览照片时每秒显示多少帧的范围张
            param.setPictureFormat(ImageFormat.JPEG);// 图片形式
            param.set("jpeg-quality", 95);
            param.setPictureSize(1600, 900);
            camera.setParameters(param);
            try {
                camera.setPreviewDisplay(surfaceHolder);// 设置预览显示
            } catch (IOException e) {
            }
            // 进行预览
            if (!previewRunning) {
                camera.startPreview(); // 进行预览
                previewRunning = true; // 已经开始预览
            }

            isOpen = true;
    }

    // 关闭摄像头
    private void CloseCamera() {
        if (camera != null) {
            System.out.println("------CloseCamera------");
            if (previewRunning) {
                camera.stopPreview(); // 停止预览
                previewRunning = false;
            }
            camera.release();
            camera = null;
            isOpen = false;
        }
    }

    // 关闭录像
    private void CloseVideo() {
        if (isRecord == true) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecord = false;

            Toast.makeText(CameraActivity.this, "录像成功!", Toast.LENGTH_SHORT).show();
        }
    }

    private void browse() {
        // TODO Auto-generated method stub
        System.out.println("------browse button down------");

        Intent intent = new Intent(CameraActivity.this, SDfileExplorer.class);
        startActivity(intent);
    }

    private void video() {
        // TODO Auto-generated method stub
        System.out.println("------video button down------");

        if (isRecord == false) {
            System.out.println("------mediaRecorder_setting------");
            camera.unlock();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.reset();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(12200);
            mediaRecorder.setAudioSamplingRate(8000);
            mediaRecorder.setVideoSize(width, height);
            mediaRecorder.setVideoFrameRate(25);
            mediaRecorder.setVideoEncodingBitRate(1500000);

            mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

            // 以时间格式保存文件
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA);
            dateString = sDateFormat.format(new java.util.Date());

            videoFile = extsd_path + dateString + ".mp4";
            mediaRecorder.setOutputFile(videoFile);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();

                Toast.makeText(CameraActivity.this, "开始录像", Toast.LENGTH_SHORT).show();

                isRecord = true;

                System.out.println("------mediaRecorder_start------");
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                System.out.println("IllegalStateException.......");
                Toast.makeText(CameraActivity.this, "录像失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("IOException.......");
                Toast.makeText(CameraActivity.this, "录像失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void photo(View v) {
        // TODO Auto-generated method stub
        System.out.println("------photo button down------");

        if (isRecord == false) {
            System.out.println("------此时单独拍照------");
            camera.autoFocus(autoFocusCallback);
        } else {
            // 如果拍照时正在录像，将拍照改为截图
            // jietu(v);
            System.out.println("------此时录像拍照------");
            camera.autoFocus(autoFocusCallback);
        }
    }

    private void jietu(View v) {
        // TODO Auto-generated method stub
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA);
        dateString = sDateFormat.format(new java.util.Date());

        String fileName = extsd_path + dateString + ".jpg";
        File file = new File(fileName);

        View view = v.getRootView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        Bitmap bitmap2 = view.getDrawingCache();

        System.out.println(bitmap2.getHeight());
        System.out.println(bitmap2.getWidth());

        Bitmap bitmap1 = Bitmap.createBitmap(bitmap2, 10, 10, 300, 300);

        if (bitmap1 != null) {
            System.out.println("bitmap    got!");
            try {
                FileOutputStream out = new FileOutputStream(fileName);
                bitmap1.compress(Bitmap.CompressFormat.PNG, 100,
                        out);
                System.out.println("file" + fileName + "outputdone.");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("no");
            }

        } else {
            System.out.println("bitmap  is NULL!");
        }
    }

    AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
            if (success) {
                camera.takePicture(new ShutterCallback() {
                    public void onShutter() {
                        // 按下快门瞬间会执行此处代码
                    }
                }, new PictureCallback() {
                    public void onPictureTaken(byte[] data, Camera c) {
                        // 此处代码可以决定是否需要保存原始照片信息
                    }
                }, myJpegCallback);
            }
        }
    };

    PictureCallback myJpegCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) { // 保存图片的操作
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            // 以时间格式保存文件
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA);
            dateString = sDateFormat.format(new java.util.Date());

            String fileName = extsd_path + dateString + ".jpg";
            File file = new File(fileName);

            try {
                BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(file));
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos); // 向缓冲区之中压缩图片
                bos.flush();
                bos.close();

                Toast.makeText(CameraActivity.this, "拍照成功！", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(CameraActivity.this, "拍照失败！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        System.out.println("------onDestroy------");

        CloseCamera();
        CloseVideo();

    }


}
