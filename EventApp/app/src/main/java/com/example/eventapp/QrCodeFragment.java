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
import androidx.navigation.fragment.NavHostFragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Fragment that displays a QR code for an event.
 */
public class QrCodeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView qrImage = view.findViewById(R.id.qrImage);
        TextView qrText = view.findViewById(R.id.qrText);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        boolean cameFromDetails = getArguments() != null &&
                getArguments().getBoolean("cameFromDetails", false);

        btnBack.setOnClickListener(v -> {
            if (cameFromDetails) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            } else {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_qrCodeFragment_to_organizerLandingFragment);
            }
        });

        String qrData = "";
        if (getArguments() != null) {
            qrData = getArguments().getString("qrData", "");
        }

        if (qrData.isEmpty()) {
            qrText.setText("⚠️ No QR data available");
        } else {
            qrText.setText("Scan this code to view event details");
            generateQRCode(qrData, qrImage);
        }
    }

    private void generateQRCode(String data, ImageView qrImage) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            int size = 600;
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y,
                            bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            qrImage.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
