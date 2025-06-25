package com.autodialer.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
    
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.CAMERA  // Adicionar esta linha
    };

    // Permissões que podem não estar disponíveis em todas as versões
    private static final String[] OPTIONAL_PERMISSIONS = {
        Manifest.permission.MODIFY_PHONE_STATE,
        Manifest.permission.MANAGE_OWN_CALLS,
        Manifest.permission.ANSWER_PHONE_CALLS
    };

    public static String[] getAllPermissions() {
        return REQUIRED_PERMISSIONS.clone();
    }

    public static boolean hasAllPermissions(Context context) {
        // Verificar permissões básicas obrigatórias
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        // Verificar permissão de sobreposição (overlay)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static String[] getMissingPermissions(Context context) {
        java.util.List<String> missing = new java.util.ArrayList<>();
        
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }
        
        return missing.toArray(new String[0]);
    }

    public static boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true; // Não necessário em versões antigas
    }
}
