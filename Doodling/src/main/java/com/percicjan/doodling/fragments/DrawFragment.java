package com.percicjan.doodling.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.percicjan.doodling.R;
import com.percicjan.doodling.views.DrawView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * Created by janpercic on 12. 01. 14.
 * DrawFragments contains code for fragment instance that is displayed in viewpager
 * and includes everything below navigation tabs.
 */

public class DrawFragment extends Fragment {

    private Context mContext;
    private String mBackgroundColor;
    private String mForegroundColor;
    private boolean mFirstFragment;

    // Color variables if data from JSON is wrong
    private String mBackgroundBackupColor;
    private String mForegroundBackupColor;

    // View variables
    private DrawView mDrawView;
    private SeekBar mBrushSizeSeekBar;
    private ImageButton mInputModeButton;
    private ImageView mInputSizeView;
    private ImageView mInputSizeMaxView;

    // Initial setup
    private int mInitialBrushSize = 20;
    private boolean mInputIsBrush = true;
    private int mFragmentNumber;

    public DrawFragment(Context context, boolean firstFragment, String backgroundColor, String foregroundColor) {
        mContext = context;
        mFirstFragment = firstFragment;
        mBackgroundColor = backgroundColor;
        mForegroundColor = foregroundColor;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.draw_fragment, container, false);

        // Set up colors for backup for each of the two fragments
        if (mFirstFragment) {
            mBackgroundBackupColor = "#000000";
            mForegroundBackupColor = "#FFFFFF";
            mFragmentNumber = 1;
        } else {
            mBackgroundBackupColor = "#FFFFFF";
            mForegroundBackupColor = "#000000";
            mFragmentNumber = 2;
        }

        // Set up brush color initial size
        mDrawView = (DrawView) rootView.findViewById(R.id.activity_draw_drawing_canvas);
        mDrawView.setBrushSize(mInitialBrushSize);
        try {
            mDrawView.setBackgroundColor(Color.parseColor(mBackgroundColor));
        } catch (Exception ex) {
            Log.e("DOODLE", "Background color exception");
            mBackgroundColor = mBackgroundBackupColor;
            mDrawView.setBackgroundColor(Color.parseColor(mBackgroundColor));
        }
        try {
            mDrawView.setBrushColor(mForegroundColor);
        } catch (Exception ex) {
            Log.e("DOODLE", "Foreground color exception");
            mForegroundColor = mForegroundBackupColor;
            mDrawView.setBrushColor(mForegroundColor);
        }

        // Input button init for swap between brush and eraser
        mInputModeButton = (ImageButton) rootView.findViewById(R.id.activity_draw_input_button);
        mInputModeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                swapInputMode();
            }
        });

        // Get screen density to scale brush size
        final float scale = getResources().getDisplayMetrics().density;

        // Set up view for initial size of brush with respect for screen density
        mInputSizeView = (ImageView) rootView.findViewById(R.id.activity_draw_input_size);
        int initialBrushSize = ((int) (mInitialBrushSize * scale + 0.5f)); // Use screen density for correct size
        RelativeLayout.LayoutParams initialLayoutParams = new RelativeLayout.LayoutParams(initialBrushSize, initialBrushSize);
        mInputSizeView.setLayoutParams(initialLayoutParams);

        // Set up view for max size of brush with respect for screen density
        mInputSizeMaxView = (ImageView) rootView.findViewById(R.id.activity_draw_input_size_max);
        int maxBrushSize = ((int) (40 * scale + 0.5f)); // Use screen density for correct size
        RelativeLayout.LayoutParams maxLayoutParams = new RelativeLayout.LayoutParams(maxBrushSize, maxBrushSize);
        mInputSizeMaxView.setLayoutParams(maxLayoutParams);

        // Init seek bar to change brush size
        mBrushSizeSeekBar = (SeekBar) rootView.findViewById(R.id.activity_draw_size_seekbar);
        mBrushSizeSeekBar.setProgress(mInitialBrushSize);
        mBrushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress + 5; // Makes sure the brush size is not to small
                mDrawView.setBrushSize(progress);
                int brushSize = (int) (progress * scale + 0.5f); // Update view for brush size
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(brushSize, brushSize);
                mInputSizeView.setLayoutParams(layoutParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // While tracking display view for brush size
                mInputModeButton.setVisibility(View.GONE);
                mInputSizeView.setVisibility(View.VISIBLE);
                mInputSizeMaxView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // When not tracking show input mode view
                mInputModeButton.setVisibility(View.VISIBLE);
                mInputSizeView.setVisibility(View.GONE);
                mInputSizeMaxView.setVisibility(View.GONE);
            }
        });

        return rootView;
    }


    /**
     * Change between brush and eraser. Eraser is the color of current fragments background
     */
    public void swapInputMode() {
        if (mInputIsBrush) {
            mInputModeButton.setImageResource(R.drawable.holo_light_input_erase);
            mInputIsBrush = false;
            mDrawView.setBrushColor(mBackgroundColor);
            Toast.makeText(mContext, R.string.toast_eraser_selected, Toast.LENGTH_SHORT).show();
        } else {
            mInputModeButton.setImageResource(R.drawable.holo_light_input_draw);
            mInputIsBrush = true;
            mDrawView.setBrushColor(mForegroundColor);
            Toast.makeText(mContext, R.string.toast_brush_selected, Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * Save doodle on the current fragments canvas. Doodle is saved in doodle folder. It the
     * folder does not exist, the method will create it.
     * After the doodle is saved, broadcast will be sent to show the doodle in Gallery app.
     */
    public void saveDrawing() {
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(mContext);
        saveDialog.setTitle(getResources().getString(R.string.alert_title_save_doodle) + " " + mFragmentNumber + "?");
        saveDialog.setMessage(R.string.alert_message_save_doodle);

        saveDialog.setPositiveButton(R.string.alert_confirm, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                mDrawView.setDrawingCacheEnabled(true);
                File folderPath = new File(Environment.getExternalStorageDirectory() + "/Doodle/");
                if (!folderPath.exists()) { // If the folder does not exist
                    folderPath.mkdirs(); // Create folder
//                    Log.e("DOODLE", "Folder for doodles created");
                }
                File doodleFile = new File(folderPath, UUID.randomUUID().toString() + ".png"); // random name
                if (doodleFile.exists()) // If random name exist
                    doodleFile.delete(); // Delete it to be replaced
                try {
                    FileOutputStream out = new FileOutputStream(doodleFile);
                    mDrawView.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Toast.makeText(mContext, R.string.toast_doodle_saved, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(mContext, R.string.toast_doodle_saved_failed, Toast.LENGTH_SHORT).show();
                }
                mDrawView.destroyDrawingCache();

                // Send scan broadcast to scan for new image and display it in Gallery app
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(doodleFile);
                mediaScanIntent.setData(contentUri);
                mContext.sendBroadcast(mediaScanIntent);
            }
        });
        saveDialog.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });

        saveDialog.show();
    }



    /**
     * Discard doodle on the current fragments canvas.
     */
    public void deleteDrawing() {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(mContext);
        newDialog.setTitle(getResources().getString(R.string.alert_title_discard_doodle) + " " + mFragmentNumber + "?");
        newDialog.setMessage(R.string.alert_message_discard_doodle);

        newDialog.setPositiveButton(R.string.alert_confirm, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                mDrawView.startNew();
                dialog.dismiss();
            }
        });
        newDialog.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });

        newDialog.show();
    }
}