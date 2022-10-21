package com.muthuraj57.usbdebugging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.VideoView;

public class USBDebuggingConfigActivity extends Activity {

    public static final int ADB_SETTING_ON = 1;
    public static final int ADB_SETTING_OFF = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_debugging_config);

        final SharedPreferences togglestates = getSharedPreferences("togglestates", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = togglestates.edit();

        final CheckBox checkon = findViewById(R.id.check_on);

        if ((!hasPermission()) || togglestates.getBoolean("first_run", true)) {
            HelpMenu();
            editor.putBoolean("first_run", false).apply();
            return;
        }

        try {
            int status = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED);
            togglestates.edit()
                    .putBoolean("toggle_on", status == ADB_SETTING_ON)
                    .apply();
        } catch (Settings.SettingNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (togglestates.getBoolean("toggle_on", true)) {
            checkon.setChecked(true);
        }

        checkon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("toggle_on", checkon.isChecked());
            int status;
            if (isChecked) {
                status = ADB_SETTING_ON;
            } else {
                status = ADB_SETTING_OFF;
            }
            Settings.Global.putInt(getContentResolver(), "adb_enabled", status);
        });

    }

    public boolean hasPermission() {
        return checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != PackageManager.PERMISSION_DENIED;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_overflow, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_appinfo) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else if (id == R.id.action_help) {
            HelpMenu();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void HelpMenu() {
        LayoutInflater layoutInflater = LayoutInflater.from(USBDebuggingConfigActivity.this);
        View helpView = layoutInflater.inflate(R.layout.dialog_help, null);

        VideoView videoView = helpView.findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.terminal));
        videoView.start();

        AlertDialog helpDialog = new AlertDialog
                .Builder(USBDebuggingConfigActivity.this)
                .setMessage(R.string.message_help)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton("Copy shell command", (dialog, which) -> {
                    String command = "pm grant com.muthuraj57.usbdebugging android.permission.WRITE_SECURE_SETTINGS";
                    getSystemService(ClipboardManager.class)
                            .setPrimaryClip(ClipData.newPlainText("Shell command", command));
                })
                .setView(helpView)
                .create();
        helpDialog.show();

    }
}
