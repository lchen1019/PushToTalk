package clqwq.press.push_to_talk;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

import clqwq.press.push_to_talk.utils.Address;
import clqwq.press.push_to_talk.utils.IdUtils;
import clqwq.press.push_to_talk.utils.MessageSender;
import clqwq.press.push_to_talk.utils.VoiceSender;

public class MainActivity extends AppCompatActivity {

    private Button voice;               // 语音发送按钮
    private Button send;                // 文本发送按钮
    private EditText input;             // 文本输入框
    private ImageView setting;          // 设置按钮
    private LinearLayout container;     // 消息的存储容器
    private MediaRecorder recorder;
    private long startTime;
    private long endTime;
    private ImageView changer;
    private LinearLayout voiceBox;
    private LinearLayout textBox;
    private boolean mode;               // false表示语音输入模式


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        voice = findViewById(R.id.voice);
        setting = findViewById(R.id.set);
        container = findViewById(R.id.container);
        changer = findViewById(R.id.changer);
        voiceBox = findViewById(R.id.voiceBox);
        textBox = findViewById(R.id.textBox);
        send = findViewById(R.id.send);
        input = findViewById(R.id.input);
        mode = false;
        // 获取多播权限
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();
        // 获取录音权限
        verifyPermissions(this);
        // 设置各种监听器
        addListener();
        listenMessage();
    }

    //申请录音权限
    private static final int GET_RECODE_AUDIO = 1;
    private static String[] PERMISSION_ALL = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    public static void verifyPermissions(Activity activity) {
        boolean permission = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
        if (permission) {
            ActivityCompat.requestPermissions(activity, PERMISSION_ALL,
                    GET_RECODE_AUDIO);
        }
    }

    // 根据文本内容解析出，是语音 or 文字
    private int modeTester(byte[] buff) {
        String text = new String(buff);
        String[] res = text.split("&&&");
        if (res.length == 1) {
            return 1;   // 为遵循格式，不是使用本软件发送的
        } else if (res.length == 2) {
            return 2;   // 语音格式
        } else {
            return 3;   // 文字格式
        }
    }

    // 监听多播地址，并显示消息
    private void listenMessage() {
        LinearLayout finalContainer = container;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                while (true) {
                    InetAddress group = null;
                    MulticastSocket socket = null;
                    try {
                        group = InetAddress.getByName(Address.ip);
                        socket = new MulticastSocket(Address.port);
                        socket.joinGroup(group);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    byte[] buff = new byte[1024 * 1024];
                    DatagramPacket packet = null;
                    packet = new DatagramPacket(buff, buff.length, group, Address.port);

                    // 显示的内容
                    TextView textView = new TextView(MainActivity.this);
                    TextView time = new TextView(MainActivity.this);
                    try {
                        socket.receive(packet);
                        byte[] bytes = packet.getData();
                        int mode = modeTester(buff);

                        String name = "";
                        String content = "";
                        if (mode == 1) {
                            String message = new String(buff);
                            String[] res = message.split("&&&");
                            name = "<font color=\"#12B7F5\">unknown：</font>";
                            content = res[0];
                        } else if (mode == 2) {
                            // 获取prefix, suffix
                            byte[] prefix = new byte[100];
                            System.arraycopy(bytes, 0, prefix, 0, 100);
                            byte[] suffix = new byte[bytes.length - 100];
                            System.arraycopy(bytes, 100, suffix, 0, bytes.length - 100);
                            String message = new String(prefix);
                            String[] res = message.split("&&&");
                            name = "<font color=\"#12B7F5\">"+res[0]+"：</font>";
                            content = res[1] + "s" + "<font color=\"#12B7F5\">" + "   点击播放" + "</font>";

                            Log.d("TAG", "KB:" + suffix.length);
                            // 将内容写入文件
                            String fileName = getExternalCacheDir().getAbsolutePath() + "/" + IdUtils.getIdByTime()+"O.3gp";
                            FileOutputStream out = new FileOutputStream(new File(fileName));
                            out.write(suffix);
                            System.out.println(new String(suffix));

                            // 点击播放，播放录音
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    MediaPlayer player = new MediaPlayer();
                                    try {
                                        player.setDataSource(fileName);
                                        player.prepare();
                                        player.start();
                                    } catch (IOException e) {
                                        Log.e("LOG_TAG", "prepare() failed");
                                    }
                                }
                            });
                        } else {
                            String message = new String(buff);
                            String[] res = message.split("&&&");
                            name = "<font color=\"#12B7F5\">" + res[0] +"：</font>";
                            content = res[1];
                        }

                        Date date = new Date();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy/M/dd HH:mm:ss");
                        time.setText(format.format(date));
                        textView.setText(Html.fromHtml(name+content));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finalContainer.addView(time);
                                finalContainer.addView(textView);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    @SuppressLint("ClickableViewAccessibility")
    public void addListener() {
        // 录音按钮
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog alertDialog = builder.create();
        alertDialog.setMessage("录音中，松开发送~");
        String fileName = IdUtils.getIdByTime() + ".3gp";
        String filePath = getExternalCacheDir().getAbsolutePath() + "/" + fileName;
        voice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 弹出对话框，提示
                    alertDialog.show();
                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setOutputFile(filePath);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    startTime = System.currentTimeMillis();
                    try {
                        recorder.prepare();
                    } catch (IOException e) {
                        Log.e("LOG_TAG", "prepare() failed");
                    }
                    recorder.start();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    recorder.stop();
                    recorder.release();
                    endTime = System.currentTimeMillis();
                    alertDialog.cancel();
                    double time = (double)((endTime - startTime) / 100) / 10;
                    new VoiceSender(filePath, time + "").start();
                }
                return false;
            }
        });
        //  设置按钮
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingActivity.class));
            }
        });
        // 设置输入模式的改变
        changer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode) {
                    changer.setImageResource(R.drawable.ic_baseline_keyboard_24);
                    voiceBox.setVisibility(View.GONE);
                    textBox.setVisibility(View.VISIBLE);
                } else {
                    changer.setImageResource(R.drawable.ic_baseline_keyboard_voice_24);
                    voiceBox.setVisibility(View.VISIBLE);
                    textBox.setVisibility(View.GONE);
                }
                mode = !mode;
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = String.valueOf(input.getText());
                System.out.println(text);
                new MessageSender(Address.name + "&&&" + text + "&&& tag").start();
            }
        });
    }
}