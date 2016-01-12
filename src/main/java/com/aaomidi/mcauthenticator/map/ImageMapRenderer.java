package com.aaomidi.mcauthenticator.map;

import com.aaomidi.mcauthenticator.model.User;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/**
 * @author Joseph Hirschfeld
 * @date 1/11/2016
 */
public class ImageMapRenderer extends MapRenderer {
    public ImageMapRenderer(User user, String username) throws WriterException {
        this.bitMatrix = user.generateQRCode(username);
    }

    private final BitMatrix bitMatrix;

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                mapCanvas.setPixel(x, z, bitMatrix.get(x, z) ? MapPalette.WHITE : MapPalette.DARK_GRAY);
            }
        }
    }
}
