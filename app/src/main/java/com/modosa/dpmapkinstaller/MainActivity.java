package com.modosa.dpmapkinstaller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.Objects;


public class MainActivity extends Activity {

    private final String CMD = "dpm set-device-owner com.modosa.dpmapkinstaller/.receiver.AdminReceiver";
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName componentName;
    private SharedPreferences sharedPreferences;
    private String showname;
    private TextView textView;
    private TextView cmd;
    private TextView show_OrgName;
    private Button button;
    private long exitTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long intervals = 2000;
            if ((System.currentTimeMillis() - exitTime) > intervals) {
                Toast.makeText(getApplicationContext(), getString(R.string.exit_tip), Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        boolean canmod = Build.VERSION.SDK_INT > 23;
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        cmd = findViewById(R.id.cmd);
        show_OrgName = findViewById(R.id.show_OrgName);

        textView.setText("✘ " + getString(R.string.not_Admin) + "\n" + getString(R.string.copy_cmd));
        cmd.setText(CMD);
        cmd.setOnClickListener(view -> copyCMD());
        button.setText(getString(R.string.start_deactivate));
        button.setOnClickListener(view -> releaseDPM());

        cmd.setVisibility(View.GONE);
        show_OrgName.setOnClickListener(view -> editOrgName());
        show_OrgName.setVisibility(View.GONE);

        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);


        componentName = new ComponentName(getPackageName(), getPackageName() + ".receiver.AdminReceiver");

        if (isAdmin()) {
            if (canmod) {
                mDevicePolicyManager.setOrganizationName(componentName, sharedPreferences.getString("orgName", ""));
                show_OrgName.setVisibility(View.VISIBLE);
                showname = getString(R.string.show_OrgName) + ":" + sharedPreferences.getString("orgName", "");
                show_OrgName.setText(showname);
            }
            textView.setText("✔ " + getString(R.string.is_Admin));
            button.setVisibility(View.VISIBLE);

        } else {
            cmd.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        }
    }

    private void showToast(final String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
    }

    private void copyCMD() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, CMD);
        Objects.requireNonNull(clipboard).setPrimaryClip(clipData);
        Toast.makeText(getApplicationContext(), CMD, Toast.LENGTH_SHORT).show();
    }

    private boolean isAdmin() {
        return mDevicePolicyManager.isDeviceOwnerApp(getPackageName());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void editOrgName() {
        String getOrgName = sharedPreferences.getString("orgName", "");

        final EditText inputname = new EditText(this);
        inputname.setText(getOrgName);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.show_OrgName)
                .setView(inputname)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("orgName", inputname.getText() + "");
                    editor.apply();
                    mDevicePolicyManager.setOrganizationName(componentName, sharedPreferences.getString("orgName", ""));
                    showname = getString(R.string.show_OrgName) + ":" + sharedPreferences.getString("orgName", "");
                    show_OrgName.setText(showname);
                });
        builder.show();
    }


    private void releaseDPM() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.release_DPM)
                .setMessage(R.string.release_tip)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (cancelAdmin()) {
                        show_OrgName.setVisibility(View.GONE);
                        textView.setText("✘ " + getString(R.string.not_Admin) + "\n" + getString(R.string.copy_cmd));
                        cmd.setVisibility(View.VISIBLE);
                        button.setVisibility(View.GONE);
                        showToast(getString(R.string.success_deactivate));
                    } else {
                        showToast(getString(R.string.failed_deactivate));
                    }
                });
        builder.show();


    }

    private boolean cancelAdmin() {
        if (isAdmin()) {
            mDevicePolicyManager.clearDeviceOwnerApp(getPackageName());
            return !isAdmin();
        } else {
            return false;
        }
    }

}
