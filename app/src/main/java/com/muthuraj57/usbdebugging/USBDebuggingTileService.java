package com.muthuraj57.usbdebugging;

import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class USBDebuggingTileService extends TileService {

    public void onTileAdded() {
        super.onTileAdded();

    }

    public void onTileRemoved() {
        super.onTileRemoved();
    }

    public void onStartListening() {
        super.onStartListening();

        if (hasPermission()) {
            int status;
            try {
                status = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED);
            } catch (Settings.SettingNotFoundException e) {
                throw new RuntimeException(e);
            }
            Tile tile = this.getQsTile();

            if (status == USBDebuggingConfigActivity.ADB_SETTING_ON) {
                refreshTile(tile, Tile.STATE_ACTIVE, getString(R.string.debugging_on), R.drawable.ic_debugging_on);
            } else {
                refreshTile(tile, Tile.STATE_INACTIVE, getString(R.string.debugging_off), R.drawable.ic_debugging_off);
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_no_permission), Toast.LENGTH_SHORT).show();
        }
    }

    public void onStopListening() {
        super.onStopListening();
    }

    public void onClick() {
        super.onClick();

        if (hasPermission()) {
            int status;
            try {
                status = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED);
            } catch (Settings.SettingNotFoundException e) {
                throw new RuntimeException(e);
            }

            Tile tile = this.getQsTile();
            if (status == USBDebuggingConfigActivity.ADB_SETTING_ON) {
                changeTileState(tile, Tile.STATE_INACTIVE, getString(R.string.debugging_off), R.drawable.ic_debugging_off, USBDebuggingConfigActivity.ADB_SETTING_OFF);
            } else {
                changeTileState(tile, Tile.STATE_ACTIVE, getString(R.string.debugging_on), R.drawable.ic_debugging_on, USBDebuggingConfigActivity.ADB_SETTING_ON);
            }

        } else {
            Toast.makeText(this, getString(R.string.toast_no_permission), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean hasPermission() {
        return checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != PackageManager.PERMISSION_DENIED;
    }

    public void changeTileState(Tile tile, int state, String label, int icon, int status) {
        tile.setLabel(label);
        tile.setState(state);
        tile.setIcon(Icon.createWithResource(this, icon));
        Settings.Global.putInt(getContentResolver(), Settings.Global.ADB_ENABLED, status);
        tile.updateTile();
    }

    public void refreshTile(Tile tile, int state, String label, int icon) {
        tile.setState(state);
        tile.setLabel(label);
        tile.setIcon(Icon.createWithResource(this, icon));
        tile.updateTile();
    }


}
