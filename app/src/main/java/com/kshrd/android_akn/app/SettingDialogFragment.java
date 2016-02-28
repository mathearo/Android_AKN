package com.kshrd.android_akn.app;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.kshrd.android_akn.R;
import com.kshrd.android_akn.util.Setting;

import java.util.Locale;

/**
 * Created by Buth Mathearo on 2/14/2016.
 */
public class SettingDialogFragment extends DialogFragment {
    private Spinner mLanguageSpinner;
    private String languages[] = {"English", "Khmer"};
    private Button btnOk;
    private ToggleButton tgBtnScollAnimation;
    private View view;
    private int oldLanguage;
    private boolean oldScrollAnimationStatus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Check if user changed language
        Setting.loadLanguage(getActivity());


        SharedPreferences sharedPref = getActivity().getSharedPreferences("setting",
                Context.MODE_PRIVATE);
        Locale locale = null;
        Configuration config = null;

        if (locale == null) locale = new Locale(sharedPref.getString("LANGUAGE", "en"));
        Locale.setDefault(locale);
        if (config == null) config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());
        getDialog().setTitle(getString(R.string.nav_setting));

        View view = inflater.inflate(R.layout.fragment_setting_dialog, container, false);

        languages[0] = getString(R.string.english);
        languages[1] = getString(R.string.khmer);

        initWidgets(view);
        return view;

    }

    public void initWidgets(View view) {
        mLanguageSpinner = (Spinner) view.findViewById(R.id.language_spinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, languages);

        mLanguageSpinner.setAdapter(spinnerAdapter);

        // Load Setting that user has configured before.
        Setting.readSetting(view.getContext());
        if (Setting.LANGUAGE.equals("en")) mLanguageSpinner.setSelection(0);
        else mLanguageSpinner.setSelection(1);

        btnOk = (Button) view.findViewById(R.id.btnOk);
        tgBtnScollAnimation = (ToggleButton) view.findViewById(R.id.tgBtnScollAnimation);
        tgBtnScollAnimation.setChecked(Setting.IS_ANIMATED);

        oldLanguage = mLanguageSpinner.getSelectedItemPosition();
        oldScrollAnimationStatus = tgBtnScollAnimation.isChecked();

        initEvent();
    }

    public void initEvent() {
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isSettingChanged = false;
                if (mLanguageSpinner.getSelectedItemPosition() != oldLanguage) isSettingChanged = true;
                else if (tgBtnScollAnimation.isChecked() != oldScrollAnimationStatus) isSettingChanged = true;

                dismiss();

                if (isSettingChanged) {

                    Setting.save(getActivity(), Setting.languages[mLanguageSpinner.getSelectedItemPosition()],
                            tgBtnScollAnimation.isChecked());
                    //Setting.loadLanguage(getActivity());

                    SharedPreferences sharedPref = getActivity().getSharedPreferences("setting",
                            Context.MODE_PRIVATE);
                    Locale locale = null;
                    Configuration config = null;

                    if (locale == null) locale = new Locale(sharedPref.getString("LANGUAGE", "en"));
                    Locale.setDefault(locale);
                    if (config == null) config = new Configuration();
                    config.locale = locale;
                    getActivity().getResources().updateConfiguration(config,
                            getActivity().getResources().getDisplayMetrics());

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }

            }
        });
    }

}
