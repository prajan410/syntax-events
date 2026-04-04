package com.example.syntaxappproject;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * Service class for generating and handling QR codes using the ZXing library.
 */
public class QRCodeService {
    private static String openAppString = "syntaxappproject://open";

    /**
     * Public interface for generating a QR code from a string.
     * @param deep_link is the content to be encoded in the QR code (e.g., event ID).
     * @return a Bitmap representing the generated QR code.
     */
    public static Bitmap generateQRCode (String deep_link) {
        BitMatrix matrix = makeQRCodeMatrix(deep_link);
        return toBitmap(matrix);
    }

    /**
     * Generates a BitMatrix for a QR code using the provided string.
     * @param deep_link is the content to encode.
     * @return a BitMatrix of the QR code, or null if encoding fails.
     */
    private static BitMatrix makeQRCodeMatrix(String deep_link) {
        // Code mostly from https://lknuth.dev/writings/generating_qrcodes_with_zxing/
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = null;
        try {
            matrix = writer.encode(
                    deep_link, BarcodeFormat.QR_CODE, 512, 512
            );
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return matrix;
    }

    /**
     * Converts a BitMatrix into a Bitmap for display in Android UI.
     * @param matrix the BitMatrix to convert.
     * @return a Bitmap representation of the matrix.
     */
    // Code from https://lknuth.dev/writings/generating_qrcodes_with_zxing/
    private static Bitmap toBitmap(BitMatrix matrix){
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }
}
