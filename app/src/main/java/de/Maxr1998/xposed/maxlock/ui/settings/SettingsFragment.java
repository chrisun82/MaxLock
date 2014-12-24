package de.Maxr1998.xposed.maxlock.ui.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;
import android.view.View;
import android.webkit.WebView;

import com.commonsware.cwac.anddown.AndDown;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.Maxr1998.xposed.maxlock.Common;
import de.Maxr1998.xposed.maxlock.R;
import de.Maxr1998.xposed.maxlock.Util;
import de.Maxr1998.xposed.maxlock.ui.SettingsActivity;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences pref, keysPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //noinspection deprecation
        getPreferenceManager().setSharedPreferencesMode(Activity.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.preferences_main);
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        keysPref = getActivity().getSharedPreferences(Common.PREFS_KEY, Context.MODE_PRIVATE);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (key.equals(Common.HIDE_APP_FROM_LAUNCHER)) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) pref;
            if (checkBoxPreference.isChecked()) {
                PackageManager p = getActivity().getPackageManager();
                p.setComponentEnabledSetting(getActivity().getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            } else {
                PackageManager p = getActivity().getPackageManager();
                p.setComponentEnabledSetting(getActivity().getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        if (preference == findPreference(Common.LOCKING_TYPE_SETTINGS)) {
            if (SettingsActivity.IS_DUAL_PANE) {
                cleanBackStack();
                getActivity().findViewById(R.id.frame_container_scd).setVisibility(View.VISIBLE);
                getFragmentManager().beginTransaction().replace(R.id.frame_container_scd, new LockingTypeSettingsFragment()).addToBackStack(null).commit();
            } else {
                getFragmentManager().beginTransaction().replace(R.id.frame_container, new LockingTypeSettingsFragment()).addToBackStack(null).commit();
            }
            return true;
        } else if (preference == findPreference(Common.LOCKING_UI_SETTINGS)) {
            if (SettingsActivity.IS_DUAL_PANE) {
                cleanBackStack();
                getActivity().findViewById(R.id.frame_container_scd).setVisibility(View.VISIBLE);
                getFragmentManager().beginTransaction().replace(R.id.frame_container_scd, new LockingUISettingsFragment()).addToBackStack(null).commit();
            } else {
                getFragmentManager().beginTransaction().replace(R.id.frame_container, new LockingUISettingsFragment()).addToBackStack(null).commit();
            }
            return true;
        } else if (preference == findPreference(Common.TRUSTED_DEVICES)) {
            if (SettingsActivity.IS_DUAL_PANE) {
                cleanBackStack();
                getActivity().findViewById(R.id.frame_container_scd).setVisibility(View.VISIBLE);
                getFragmentManager().beginTransaction().replace(R.id.frame_container_scd, new TrustedDevicesFragment()).addToBackStack(null).commit();
            } else {
                getFragmentManager().beginTransaction().replace(R.id.frame_container, new TrustedDevicesFragment()).addToBackStack(null).commit();
            }
        } else if (preference == findPreference(Common.CHOOSE_APPS)) {
            if (SettingsActivity.IS_DUAL_PANE) {
                cleanBackStack();
                getActivity().findViewById(R.id.frame_container_scd).setVisibility(View.VISIBLE);
                getFragmentManager().beginTransaction().replace(R.id.frame_container_scd, new AppsListFragment()).addToBackStack(null).commit();
            } else {
                getFragmentManager().beginTransaction().replace(R.id.frame_container, new AppsListFragment()).addToBackStack(null).commit();
            }
            return true;
        } else if (preference == findPreference(Common.ABOUT)) {
            AlertDialog.Builder about = new AlertDialog.Builder(getActivity());
            WebView webView = new WebView(getActivity());
            String markdown = "";
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("about.md")));
                String line;
                while ((line = br.readLine()) != null) {
                    markdown = markdown + line + "\n";
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String html = new AndDown().markdownToHtml(markdown);
            webView.loadData(html, "text/html; charset=UTF-8", null);
            about.setView(webView).create().show();
        }
        return false;
    }

    @SuppressLint("InlinedApi")
    public void cleanBackStack() {
        if (Util.noGingerbread())
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        else
            getFragmentManager().popBackStack();
    }

    @SuppressLint("ValidFragment")
    public class LockingTypeSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            addPreferencesFromResource(R.xml.preferences_locking_type);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            super.onPreferenceTreeClick(preferenceScreen, preference);
            if (preference == findPreference(Common.LOCKING_TYPE_PASSWORD)) {
                Util.setPassword(getActivity(), null);
                return true;
            } else if (preference == findPreference(Common.LOCKING_TYPE_PIN)) {
                if (SettingsActivity.IS_DUAL_PANE) {
                    getFragmentManager().beginTransaction().replace(R.id.frame_container_scd, new PinSetupFragment()).addToBackStack(null).commit();
                } else {
                    getFragmentManager().beginTransaction().replace(R.id.frame_container, new PinSetupFragment()).addToBackStack(null).commit();
                }
                return true;
            } else if (preference == findPreference(Common.LOCKING_TYPE_KNOCK_CODE)) {
                if (SettingsActivity.IS_DUAL_PANE) {
                    getFragmentManager().beginTransaction().replace(R.id.frame_container_scd, new KnockCodeSetupFragment()).addToBackStack(null).commit();
                } else {
                    getFragmentManager().beginTransaction().replace(R.id.frame_container, new KnockCodeSetupFragment()).addToBackStack(null).commit();
                }
                return true;
            }
            return false;
        }
    }

    @SuppressLint("ValidFragment")
    public class LockingUISettingsFragment extends PreferenceFragment {

        private static final int READ_REQUEST_CODE = 42;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            addPreferencesFromResource(R.xml.preferences_locking_ui);

            ListPreference lp = (ListPreference) findPreference(Common.BACKGROUND);
            lp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (preference.getKey().equals(Common.BACKGROUND) && newValue.toString().equals("custom")) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, READ_REQUEST_CODE);
                    }
                    return true;
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                Uri uri = null;
                InputStream inputStream;
                if (data != null) {
                    uri = data.getData();
                }
                if (uri == null) {
                    throw new NullPointerException();
                }
                try {
                    inputStream = getActivity().getContentResolver().openInputStream(uri);
                    File destination = new File(getActivity().getApplicationInfo().dataDir + File.separator + "background" + File.separator + "image");
                    if (destination.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        destination.delete();
                    }
                    FileUtils.copyInputStreamToFile(inputStream, destination);
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            super.onPreferenceTreeClick(preferenceScreen, preference);

            return false;
        }
    }
}
