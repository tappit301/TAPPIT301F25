package com.example.eventapp.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CSVUtils {

    /**
     * Saves CSV content into the device's Download folder.
     *
     * @param context The app context
     * @param fileName The name of the CSV file to write
     * @param data The CSV data (String)
     */
    public static void saveCSV(Context context, String fileName, String data) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File file = new File(downloadsDir, fileName);
            FileOutputStream fos = new FileOutputStream(file);

            fos.write(data.getBytes());
            fos.flush();
            fos.close();

            Toast.makeText(context, "CSV saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
