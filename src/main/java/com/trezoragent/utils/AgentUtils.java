package com.trezoragent.utils;

import com.trezoragent.gui.StartAgentGUI;
import com.trezoragent.gui.TrayProcess;
import com.trezoragent.sshagent.DeviceWrapper;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import org.spongycastle.pqc.math.linearalgebra.ByteUtils;

/**
 *
 * @author martin.lizner
 */
public class AgentUtils {


    public static byte[] frameArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + array.length);
        buffer.putInt(array.length);
        buffer.put(array);
        return buffer.array();
    }

    public static byte[] frameArray(byte[] array1, byte[] array2) {
        return frameArray(ByteUtils.concatenate(array1, array2));
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdfDate.format(new Date());
    }

    public static List<? extends Image> getAllIcons() {
        List<Image> icons = new ArrayList<>();
        icons.add(new ImageIcon(StartAgentGUI.class.getResource(AgentConstants.ICON16_PATH)).getImage());
        icons.add(new ImageIcon(StartAgentGUI.class.getResource(AgentConstants.ICON24_PATH)).getImage());
        icons.add(new ImageIcon(StartAgentGUI.class.getResource(AgentConstants.ICON32_PATH)).getImage());
        icons.add(new ImageIcon(StartAgentGUI.class.getResource(AgentConstants.ICON48_PATH)).getImage());
        icons.add(new ImageIcon(StartAgentGUI.class.getResource(AgentConstants.ICON64_PATH)).getImage());
        icons.add(new ImageIcon(StartAgentGUI.class.getResource(AgentConstants.ICON72_PATH)).getImage());
        icons.add(new ImageIcon(StartAgentGUI.class.getResource(AgentConstants.ICON96_PATH)).getImage());
        icons.add(new ImageIcon(StartAgentGUI.class.getResource(AgentConstants.ICON128_PATH)).getImage());
        return icons;
    }

    public static boolean checkDeviceAvailable() {
        if (TrayProcess.deviceService.getHardwareWalletService().isDeviceReady()) {
            if (TrayProcess.deviceService.getHardwareWalletService().isWalletPresent()) {
                return true;
            } else {
                TrayProcess.createWarning(LocalizedLogger.getLocalizedMessage("WALLET_NOT_PRESENT_KEY"));
            }
        } else {
            TrayProcess.createWarning(LocalizedLogger.getLocalizedMessage("DEVICE_NOT_READY_KEY", new Object[]{TrayProcess.deviceType}));
        }
        return false;
    }

    public static Properties initSettingsFile(File settings) throws IOException {
        // create file with default settings        

        Properties properties = new Properties();
        properties.setProperty(AgentConstants.SETTINGS_KEY_DEVICE, AgentConstants.SETTINGS_TREZOR_DEVICE);
        properties.setProperty(AgentConstants.SETTINGS_KEY_BIP32_URI, AgentConstants.SETTINGS_BIP32_SSHURI);
        properties.setProperty(AgentConstants.SETTINGS_KEY_BIP32_INDEX, AgentConstants.SETTINGS_BIP32_INDEX);
        properties.setProperty(AgentConstants.SETTINGS_KEY_SESSION_TIMEOUT, AgentConstants.SETTINGS_SESSION_TIMEOUT);
        properties.setProperty(AgentConstants.SETTINGS_KEY_CURVE_NAME, AgentConstants.CURVE_NAME_NISTP256);

        Logger.getLogger(AgentUtils.class.getName()).log(Level.FINE, "Setting default properties: {0}", new Object[]{properties});

        try (FileOutputStream fileOut = new FileOutputStream(settings)) {
            properties.store(fileOut, null);
        }

        return properties;
    }

    public static String readSetting(Properties settings, String key, String defaultValue) {
        String property = settings.getProperty(key);
        if (property == null) {
            Logger.getLogger(AgentUtils.class.getName()).log(Level.WARNING, "Settings property {0} not found, defaulting to value: {1}", new Object[]{key, defaultValue});
            return defaultValue;
        }
        Logger.getLogger(AgentUtils.class.getName()).log(Level.FINE, "Returning settings property {0}={1}", new Object[]{key, property});
        return property;
    }

    public static void stopGUITimer() {
        // GUI workaround, TODO: replace timers and do-whiles with proper async messaging
        Timer timer = TrayProcess.deviceService.getTimer();
        if (timer != null && timer.isRunning()) {
            Logger.getLogger(AgentUtils.class.getName()).log(Level.FINE, "Stopping GUI timer", new Object[]{});
            timer.stop(); // stop swing timer
        }
    }

    public static void restartSessionTimer() {
        if (TrayProcess.sessionTimer != null) {
            Logger.getLogger(DeviceWrapper.class.getName()).log(Level.FINE, "Restarting session timer.");
            TrayProcess.sessionTimer.restart(); // TODO: change some deviceService fields to static?
        } else {
            Logger.getLogger(DeviceWrapper.class.getName()).log(Level.SEVERE, "Session timer not initialized!");
        }
    }

    public static byte[] frameArrayWithUnsignedInt(byte[] array) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + array.length); // unsigned int occupies only 1 byte
        buffer.put((byte) array.length);
        buffer.put(array);
        return buffer.array();
    }

    public static byte[] frameArrayWithUnsignedInt(byte[] array1, byte[] array2) {
        return AgentUtils.frameArrayWithUnsignedInt(ByteUtils.concatenate(array1, array2));
    }

}
