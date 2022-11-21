package com.serenegiant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class YUVToBitmap {
    private final RenderScript rs;
    private final ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType;
    private Allocation in, out;


    public static void saveYuv2Jpeg(String path, byte[] data, int mWidth, int mHeight) {
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        boolean result = yuvImage.compressToJpeg(new Rect(0, 0, mWidth, mHeight), 100, bos);
        if (result) {
            byte[] buffer = bos.toByteArray();
            File file = new File(path);
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);
                // fixing bm is null bug instead of using BitmapFactory.decodeByteArray
                fos.write(buffer);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public YUVToBitmap(Context context) {
        rs = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
    }


    public Bitmap YUV2Bitmap(byte[] yuv, int width, int height) {
        if (yuvType == null) {
            yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuv.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
            Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }

        in.copyFrom(yuv);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        return bmpout;
    }

}
