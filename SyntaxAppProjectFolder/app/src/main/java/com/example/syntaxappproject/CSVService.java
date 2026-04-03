package com.example.syntaxappproject;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.content.ContentResolver;
import android.util.Pair;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Service class for creating and storing CSV files.
 * Provides utility methods to generate CSV content from a list of user details
 * and save it to the device's external storage (Downloads folder).
 */
public class CSVService {

    /**
     * Creates a StringBuilder containing CSV-formatted data from a list of pairs.
     * Each pair represents an entrant's name and email.
     *
     * @param lotteryWinners A list of pairs where the first element is the name and the second is the email.
     * @return A StringBuilder containing the CSV data.
     */
    public static StringBuilder createCSV(List<Pair<String,String>> lotteryWinners) {
        StringBuilder csv = new StringBuilder();
        for(int i = 0; i < lotteryWinners.size(); i++){
            csv.append(lotteryWinners.get(i).first + "," + lotteryWinners.get(i).second);
            csv.append("\n");
        }
        if (csv.length() > 0) {
            csv.deleteCharAt(csv.length() - 1);
        }
        return csv;
    }

    /**
     * Generates a CSV from the provided list and stores it in the device's Downloads folder
     * using the MediaStore API.
     *
     * @param context        The application context.
     * @param lotteryWinners A list of pairs where the first element is the name and the second is the email.
     */
    public static void storeCSV(Context context, List<Pair<String,String>> lotteryWinners) {
        StringBuilder csv = createCSV(lotteryWinners);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, "Lottery Winners");
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri collection = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        }
        ContentResolver resolver =  context.getContentResolver();
        Uri fileUri = resolver.insert(collection, values);

        if (fileUri != null) {
            try (OutputStream os = context.getContentResolver().openOutputStream(fileUri)) {
                os.write(csv.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            resolver.update(fileUri, values, null, null);
        }
    }
}
