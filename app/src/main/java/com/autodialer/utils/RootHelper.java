package com.autodialer.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class RootHelper {
    private static final String TAG = "RootHelper";
    private static Boolean isRootAvailable = null;

    public static boolean isRootAvailable() {
        if (isRootAvailable == null) {
            isRootAvailable = checkRootAccess();
        }
        return isRootAvailable;
    }

    private static boolean checkRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("id\n");
            os.writeBytes("exit\n");
            os.flush();

            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar root: " + e.getMessage());
            return false;
        }
    }

    public static boolean executeRootCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();

            int exitValue = process.waitFor();
            
            if (exitValue == 0) {
                Log.d(TAG, "Comando executado com sucesso: " + command);
                return true;
            } else {
                Log.e(TAG, "Comando falhou com código: " + exitValue);
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao executar comando root: " + e.getMessage());
            return false;
        }
    }

    public static String executeRootCommandWithOutput(String command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            return output.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao executar comando com output: " + e.getMessage());
            return null;
        }
    }
}
