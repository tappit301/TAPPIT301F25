package com.example.eventapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 *This is thee Fragment that generates and displays a QR code based on event data.
 * The QR code is created from text passd through a bundle argument,
 * usually containing event details. If no data is provided,
 * an error message is displayed instead.
 *
 * Author: tappit
 */
public class QrCodeFragment extends Fragment {


    /**
     * Inflates the layout that contains the QR code image and description text.
     *
     * @param inflater LayoutInflater used to inflate the layout
     * @param container The parent container for this fragment
     * @param savedInstanceState Saved state, if any
     * @return The inflated layout view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_code, container, false);
    }


    /**
     * Helps in Retrieving the event data from the bundle and generates
     * a QR code to display on the screen.
     *
     * @param view The root view of this fragment
     * @param savedInstanceState Previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView qrImage = view.findViewById(R.id.qrImage);
        TextView qrText = view.findViewById(R.id.qrText);
        ImageButton btnBack = view.findViewById(R.id.btnBack);


        btnBack.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );

        String qrData = "";
        if (getArguments() != null) {
            qrData = getArguments().getString("qrData", "");
        }

        if (qrData.isEmpty()) {
            qrText.setText("⚠️ No QR data available");
        } else {
            qrText.setText("Scan this QR to view event details");
            generateQRCode(qrData, qrImage);
        }
    }

    /**
     * This Creates a QR code from the given data and sets it
     * as a bitmap in the provided ImageView.
     *
     * @param data The text data to encode into the QR code
     * @param qrImage The ImageView to display the generated QR code
     */
    private void generateQRCode(String data, ImageView qrImage) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            int size = 600;
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size);

            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            qrImage.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
