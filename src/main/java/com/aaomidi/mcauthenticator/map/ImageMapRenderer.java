package com.aaomidi.mcauthenticator.map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;

/**
 * @author Joseph Hirschfeld
 * @date 1/11/2016
 */
public final class ImageMapRenderer extends MapRenderer {

    private static final byte black = MapPalette.matchColor(Color.black);
    private static final String encodeFormat = "otpauth://totp/%s@%s?secret=%s";

    public ImageMapRenderer(String username, String secret, String serverip) throws WriterException {
        this.bitMatrix = getQRMap(username, secret, serverip);
    }

    private final BitMatrix bitMatrix;

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                mapCanvas.setPixel(x, z, bitMatrix.get(x, z) ? black : MapPalette.WHITE);
            }
        }
    }

    private BitMatrix getQRMap(String username, String secret, String serverIp) throws WriterException {
        return new QRCodeWriter().encode(String.format(encodeFormat,
                username,
                serverIp,
                secret),
                BarcodeFormat.QR_CODE, 128, 128);
    }
}
