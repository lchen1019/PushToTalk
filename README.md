# push to talk 支持语音、文字聊天

## 1. 代码实现

### 1.1 权限申请

```java
// 申请多播权限, 略
// 申请录音权限
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
```

### 1.2 录音功能实现

```java
// voice是一个按钮，按下录音，抬起发送
// 对话框，提示正在录音
AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
AlertDialog alertDialog = builder.create();
alertDialog.setMessage("录音中，松开发送~");
// 录音文件名，录音文件地址
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
                        Log.e(LOG_TAG, "prepare() failed");
                    }
                    recorder.start();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    recorder.stop();
                    recorder.release();
                    endTime = System.currentTimeMillis();
                    alertDialog.cancel();
                    // 获取录音时长
                    double time = (double)((endTime - startTime) / 100) / 10;
                    new VoiceSender(filePath, time + "").start();
                }
                return false;
            }
        });
```

### 1.3 播放录音

```java
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
```

## 2 结果展示

P1: 端口，用户名设置

P2：文本输入模式、语音输入模式可切换，在语音模式下长按录音，松开发送

P3, P4：在宿舍路由器下，正常接收和发送，录音可播放

<img src="https://s2.loli.net/2022/04/25/5UchwxlrkfGz2ne.jpg" alt="Screenshot_20220405_163030_clqwq.press.push_to_talk" style=" float:left; height:800px; float:left;" /><img src="https://s2.loli.net/2022/04/25/gK85HtIqvaYXQ6E.jpg" alt="img" style="float:left; height:800px;float:left;" />

<img src="https://s2.loli.net/2022/04/25/n3yQLjx5W7hRbGm.jpg" style="height:800px;" /><img src="https://s2.loli.net/2022/04/25/4z2aWRjH9xZBc1r.jpg" alt="img" style="height:800px;" />

## 3 分析

### 3.1 MediaRecorder

已经集成了录音、编码、压缩等，支持少量的录音音频格式，大概有.aac（API = 16） .amr .3gp

优点：大部分以及集成，直接调用相关接口即可，代码量小

缺点：无法实时处理音频；输出的音频格式不是很多，例如没有输出mp3格式文件

### 3.2 录音文件大小测试 (.3gp格式)

| 录音时长 | 文件大小 |
| -------- | -------- |
| 10.2s    | 18KB     |
| 1.1s     | 4KB      |
| 3.5s     | 8KB      |
| 33.1s    | 61KB     |

### 3.3 实际测试结果

1. 使用sdu_net时候，测试教学楼和宿舍之间是的确可行的，文字可以接收到，语音有时候不行，有可能是太大了，UDP多播协议不能像TCP协议一样保证送达。手机传给PC存在同样的情况。
2. 连接同一个路由器，的确可行，传输较长语音也是可行的。 

## 4 参考资料

1. [MediaPlayer 概览  | Android 开发者  | Android Developers](https://developer.android.com/guide/topics/media/mediaplayer?hl=zh-cn)
2. [MediaRecorder 概览  | Android 开发者  | Android Developers](https://developer.android.com/guide/topics/media/mediarecorder?hl=zh-cn)

3. [Android之录音--AudioRecord、MediaRecorder_12458355的技术博客_51CTO博客](https://blog.51cto.com/u_12468355/3390114)
