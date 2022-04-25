package clqwq.press.push_to_talk.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IdUtils {

    public static String getIdByTime() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(date);
    }

    public static void main(String[] args) {
        System.out.println(getIdByTime());
    }
}
