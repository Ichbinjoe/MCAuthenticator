package io.ibj.mcauthenticator.map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/**
 * @author Joseph Hirschfeld
 * @date 1/11/2016
 */
public final class ImageMapRenderer extends MapRenderer {

    private static final byte FILL_COLOR = MapPalette.GRAY_2;
    private static final String TOTP_URL_FORMAT = "otpauth://totp/%s@%s?secret=%s";

    public ImageMapRenderer(String username, String secret, String serverip) throws WriterException {
        this.bitMatrix = getQRMap(username, secret, serverip);
    }

    private final BitMatrix bitMatrix;

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                mapCanvas.setPixel(x, z, bitMatrix.get(x, z) ? FILL_COLOR : MapPalette.WHITE);
            }
        }
    }

    private BitMatrix getQRMap(String username, String secret, String serverIp) throws WriterException {
        return new QRCodeWriter().encode(String.format(TOTP_URL_FORMAT,
                username,
                serverIp,
                secret),
                BarcodeFormat.QR_CODE, 128, 128);
    }
}
