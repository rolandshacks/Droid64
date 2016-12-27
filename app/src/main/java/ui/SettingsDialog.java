package ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import org.codewiz.droid64.R;

import system.Preferences;
import util.LogManager;
import util.Logger;

public class SettingsDialog extends DialogFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private final static Logger logger = LogManager.getLogger(FullscreenActivity.class.getName());
    private View view;

    public SettingsDialog() {
        ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        view = inflater.inflate(R.layout.settings_dialog, null);

        builder.setTitle("Settings");
        builder.setView(view)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ;
                    }
                });

        Dialog dialog = builder.create();
        dialog.setCancelable(false);

        captureClicks(view);

        loadSettings(view);

        return dialog;
    }

    private void captureClicks(View view) {

        if (!(view instanceof  ViewGroup)) {
            return;
        }

        ViewGroup vg = (ViewGroup) view;
        for (int i=0; i<vg.getChildCount(); i++) {

            View v = vg.getChildAt(i);

            if (v instanceof Button) {
                v.setOnClickListener(this);
            } else if (v instanceof SeekBar) {
                ((SeekBar) v).setOnSeekBarChangeListener(this);
            }

            if (v instanceof ViewGroup) {
                captureClicks(v);
            }

        }

    }

    @Override
    public void onClick(View view) {
        handleClick(view);
    }

    private void loadSettings(View view) {

        if (null == view) return;

        Preferences prefs = Preferences.instance();

        setItem(view, R.id.enable_antialiasing, prefs.isAntialiasingEnabled());
        setItem(view, R.id.enable_drive_emulation, prefs.isDriveEmulationEnabled());
        setItem(view, R.id.enable_warp, prefs.isWarpEnabled());
        setItem(view, R.id.enable_swap_joysticks, prefs.isJoystickSwapEnabled());
        setItem(view, R.id.enable_audio_reverb, prefs.isReverbEnabled());
        setItem(view, R.id.enable_gamma_correction, prefs.isGammaCorrectionEnabled());
        setItem(view, R.id.enable_zip_scan, prefs.isZipScanEnabled());

        logger.info("set audio volume slider to " + prefs.getAudioVolumePercent());
        setItem(view, R.id.value_audio_volume, prefs.getAudioVolumePercent());

    }

    private void setItem(View parent, int id, boolean check) {

        View view = parent.findViewById(id);

        if (!(view instanceof CompoundButton)) {
            return;
        }

        CompoundButton widget = (CompoundButton) view;

        widget.setChecked(check);

    }

    private void setItem(View parent, int id, int value) {

        View view = parent.findViewById(id);

        if (!(view instanceof ProgressBar)) {
            return;
        }

        ProgressBar widget = (ProgressBar) view;

        int progress = (int) (0.5f + (float) value * (float) widget.getMax() / 100.0f);

        logger.info("set progress widget: " + value + " / " + progress);

        widget.setProgress(progress);

    }

    private void storeSettings(View view) {
        //Preferences prefs = Preferences.instance();
    }

    private void handleClick(View widget) {

        Preferences prefs = Preferences.instance();

        int id = widget.getId();

        boolean checked = false;
        if (widget instanceof CompoundButton) {
            checked = ((CompoundButton) widget).isChecked();
        }

        switch (id) {
            case R.id.enable_drive_emulation:
                prefs.setDriveEmulationEnabled(checked);
                break;
            case R.id.enable_antialiasing:
                prefs.setAntialiasingEnabled(checked);
                break;
            case R.id.enable_warp:
                prefs.setWarpEnabled(checked);
                break;
            case R.id.enable_swap_joysticks:
                prefs.setJoystickSwapEnabled(checked);
                break;
            case R.id.enable_audio_reverb:
                prefs.setReverbEnabled(checked);
                break;
            case R.id.enable_gamma_correction:
                prefs.setGammaCorrectionEnabled(checked);
                break;
            case R.id.enable_zip_scan:
                prefs.setZipScanEnabled(checked);
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar widget, int i, boolean fromUser) {

        if (!fromUser) return;

        int progress = widget.getProgress();

        logger.info("OnProgressChanged: " + progress);

        Preferences prefs = Preferences.instance();

        int id = widget.getId();

        int max = widget.getMax(); if (0 == max) max = 1;
        int value = progress * 100 / max;

        switch (id) {
            case R.id.value_audio_volume:
                prefs.setAudioVolumePercent(value);
                break;
            default:
                break;
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
