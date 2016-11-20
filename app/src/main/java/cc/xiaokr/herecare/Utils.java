package cc.xiaokr.herecare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by "Bobby Wang <wbo4958@gmail.com>" on 11/20/2016.
 */

public class Utils {

    public static BitmapDrawable readBgDrawable(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new BitmapDrawable(context.getResources(), bitmap);
    }

}
