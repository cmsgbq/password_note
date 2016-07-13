package vip.chengchao.tools.mypwnode;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vip.chengchao.tools.mypwnode.menu.MoveTouchMenu;
import vip.chengchao.tools.mypwnode.store.DBAccountStore;

/**
 * Created by chengchao on 16/7/6.
 */
public class BaseActivity extends Activity {
    public static final String PASSWORD_KEY = "password";
    private static List<Activity> activities;

    protected static DBAccountStore accountStore;

    protected static ClipboardManager clipboardManager;
    protected static MoveTouchMenu moveTouchMenu;
    protected static View menuView;
    private boolean showHoverMenu = true;
    private boolean openPasswordProtection = true;
    protected static SharedPreferences sharedPreferences;
    protected static String md5Password;
    protected static String password;
    protected static ImageView lockImageView;
    protected static boolean locked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initField();
        Log.i(this.getClass().getSimpleName(), "onCreate");
        if (!(this instanceof ProtectionActivity)) {
            if (isOpenPasswordProtection() && TextUtils.isEmpty(md5Password)) {
                ProtectionActivity.startActivityForResult(this, ProtectionActivity.ACTION_CHANGE);
                return;
            }
            if (isOpenPasswordProtection() && !TextUtils.isEmpty(md5Password) && TextUtils.isEmpty(BaseActivity.password)) {
                ProtectionActivity.startActivityForResult(this, ProtectionActivity.ACTION_CONFIRM);
                return;
            }
        }
    }

    protected void initField() {
        synchronized (BaseActivity.class) {
            accountStore = accountStore == null ? new DBAccountStore(this) : accountStore;
            clipboardManager = clipboardManager == null ? (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE) : clipboardManager;
            sharedPreferences = sharedPreferences == null ? getSharedPreferences("setting", Context.MODE_PRIVATE) : sharedPreferences;
            activities = activities == null ? new ArrayList<Activity>() : activities;
            showHoverMenu = sharedPreferences.getBoolean("show_hover_menu", showHoverMenu);
            openPasswordProtection = sharedPreferences.getBoolean("open_password_protection", openPasswordProtection);
            //TODO showHoverMenu
            if (moveTouchMenu == null) {
                menuView = LayoutInflater.from(this).inflate(R.layout.menu_view, null);
                moveTouchMenu = new MoveTouchMenu(this)
                        .setTouchView(R.layout.menu_flag)
                        .addMenuView(menuView);
                moveTouchMenu.show();

            }
            md5Password = sharedPreferences.getString(PASSWORD_KEY, null);
            locked = TextUtils.isEmpty(password);
            lockImageView = (ImageView) menuView.findViewById(R.id.image_lock_menu);
            changeLockImageView();
            activities.add(this);
        }
    }

    public void changeLockImageView() {
        lockImageView.setImageResource(locked ? R.drawable.locked_96 : R.drawable.unlocked_96);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        locked = TextUtils.isEmpty(password);
        changeLockImageView();
    }

    public void onMenuClick(View view) {
        switch (view.getId()) {
            case R.id.image_lock_menu:
                finishAll();
                if (locked) {
                    ProtectionActivity.startActivityForResult(this, ProtectionActivity.ACTION_UNLOCK);
                } else {
                    password = null;
                    ProtectionActivity.startActivityForResult(this, ProtectionActivity.ACTION_LOCK);
                }
                closeMenu();
                break;
            case R.id.textview_main:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                closeMenu();
                break;
            case R.id.textview_add:
                intent = new Intent(getApplicationContext(), AddActivity.class);
                startActivity(intent);
                closeMenu();
                break;
            case R.id.textview_exit:
                closeMenu();
                break;
            case R.id.textview_setting:
                intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
                closeMenu();
            default:
        }
    }

    protected void closeMenu() {
        moveTouchMenu.removeMenuView();
        finish();
    }

    protected void finishAll() {
        for (Activity activity : activities) {
            activity.finish();
        }
        activities.clear();
    }

    @Override
    protected void onDestroy() {
        activities.remove(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("show_hover_menu", showHoverMenu);
        editor.putBoolean("open_password_protection", openPasswordProtection);
        editor.commit();
        super.onPause();
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void toast(@StringRes int resouce) {
        Toast.makeText(this, resouce, Toast.LENGTH_SHORT).show();
    }

    protected void copyToClipboard(CharSequence content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }

        ClipData clipData = ClipData.newPlainText(content, content);
        clipboardManager.setPrimaryClip(clipData);
    }

    protected CharSequence pasteFromClipboard() {
        return clipboardManager.getPrimaryClip().getItemAt(0).getText();
    }


    public boolean isShowHoverMenu() {
        return showHoverMenu;
    }

    protected void setShowHoverMenu(boolean showHoverMenu) {
        this.showHoverMenu = showHoverMenu;
    }

    public boolean isOpenPasswordProtection() {
        return openPasswordProtection;
    }

    protected void setOpenPasswordProtection(boolean openPasswordProtection) {
        this.openPasswordProtection = openPasswordProtection;
    }
}