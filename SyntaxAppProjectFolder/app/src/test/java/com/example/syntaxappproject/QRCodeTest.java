package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.graphics.Bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34}) // Using a specific SDK version can sometimes help with compatibility
public class QRCodeTest {

    @Test
    public void testStringConversion() {
        String testing = "Holy hand grenade";
        Bitmap result = QRCodeService.generateQRCode(testing);

        int width = result.getWidth();
        int height = result.getHeight();

        int[] pixels = new int[width * height];
        result.getPixels(pixels, 0, width, 0, 0, width, height);

        // Convert the pixels back to a form ZXing can read
        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result output = new MultiFormatReader().decode(binaryBitmap);
            assertEquals(testing, output.getText());
        } catch (NotFoundException e) {
            fail("No QR code found: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
}
