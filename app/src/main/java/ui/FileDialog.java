package ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.codewiz.droid64.R;

import emu.Image;
import emu.ImageFilter;
import emu.ImageManager;
import util.LogManager;
import util.Logger;

import java.io.File;

public class FileDialog extends DialogFragment {

    private static int lastSelectedFilterPos;
    private static int lastSelectedDiskPos;

    private final static Logger logger = LogManager.getLogger(FullscreenActivity.class.getName());
    private View dialog;
    private OnDiskSelectHandler handler;
    private ImageFilter currentDiskFilter;

    public FileDialog() {
        ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof OnDiskSelectHandler) {
            handler = (OnDiskSelectHandler) activity;
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        logger.info("onCreateDialog");

        logger.info("use last list positions: " + lastSelectedFilterPos + " / " + lastSelectedDiskPos);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialog = inflater.inflate(R.layout.file_dialog, null);

        builder.setTitle("Select Disk");
        builder.setView(dialog)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ;
                    }
                });

        Dialog dialog = builder.create();
        dialog.setCancelable(false);

        ListView fileList = (ListView) this.dialog.findViewById(R.id.selector_disk_list);
        if (null != fileList) {

            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    handleItemClick(parent, view, position, id, false);
                }
            };

            fileList.setOnItemClickListener(listener);

            AdapterView.OnItemLongClickListener listener2 = new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    handleItemClick(parent, view, position, id, true);
                    return true;
                }
            };

            fileList.setOnItemLongClickListener(listener2);
        }

        ObjectListView setList = (ObjectListView) this.dialog.findViewById(R.id.selector_set_list);
        if (null != setList) {

            ImageFilter noDiskFilter = new ImageFilter('\0', '\0', "All");
            setList.add(noDiskFilter);

            setList.add(new ImageFilter("snap", "Snapshots"));

            setList.add(new ImageFilter('0', '9'));
            for (char c = 'a'; c <= 'z'; c++) {
                setList.add(new ImageFilter(c));
            }

            currentDiskFilter = (ImageFilter) setList.getItemAtPosition(lastSelectedFilterPos);
            if (null != currentDiskFilter) {
                setList.setSelection(lastSelectedFilterPos);
            }

            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    handleSetClick(parent, view, position, id);
                }
            };

            setList.setOnItemClickListener(listener);

        }

        this.dialog.findViewById(R.id.buttonScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadContent(FileDialog.this.dialog, true);
            }
        });

        loadContent(this.dialog, false);

        if (null != currentDiskFilter) {
            if (null != fileList) {
                fileList.requestFocus();
            }
        } else {
            if (null != setList) {
                setList.requestFocus();
            }
        }

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        logger.info("onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        logger.info("onDetach");
    }

    private void loadContent(View view, boolean rescan) {

        if (null == view) return;

        ImageManager diskManager = ImageManager.instance();
        if (null == diskManager) return;

        View list = view.findViewById(R.id.selector_disk_list);
        if (null == list || !(list instanceof ObjectListView)) {
            return;
        }

        Toast t = null;
        t = Toast.makeText(getActivity(), "Retrieving Disks...", Toast.LENGTH_LONG);
        t.show();

        ObjectListView listView = (ObjectListView) list;

        listView.clear();

        if (rescan) {
            diskManager.invalidateList();
        }

        listView.addAll(diskManager.getList(currentDiskFilter));
        listView.setSelection(lastSelectedDiskPos);

        t.cancel();

    }

    private void handleSetClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() != R.id.selector_set_list) return;
        logger.info("on set click: " + position + " / " + id);

        ListView list = (ListView) parent;

        Object item = list.getItemAtPosition(position);
        if (null != item && item instanceof ImageFilter) {
            currentDiskFilter = (ImageFilter) item;
            String text = currentDiskFilter.getLabel();
            logger.info("selected filter: " + text);

            lastSelectedFilterPos = position;
            lastSelectedDiskPos = 0;

            ImageManager diskManager = ImageManager.instance();
            if (null == diskManager) return;

            ObjectListView diskList = (ObjectListView) dialog.findViewById(R.id.selector_disk_list);
            if (null == diskList) {
                return;
            }

            diskList.clear();
            diskList.addAll(diskManager.getList(currentDiskFilter));
            diskList.requestFocus();
            diskList.setSelection(0);
        }

    }

    private void handleItemClick(AdapterView<?> parent, View view, int position, long id, boolean longClick) {
        if (parent.getId() != R.id.selector_disk_list) return;
        logger.info("on item click: " + position + " / " + id);

        ListView list = (ListView) parent;

        Object item = list.getItemAtPosition(position);
        if (null != item && item instanceof Image) {
            Image diskImage = (Image) item;
            logger.info(diskImage.getUrl());

            if (longClick && diskImage.getType() == Image.TYPE_SNAPSHOT) {
                editSnapshot(diskImage);
                return;
            }

            lastSelectedDiskPos = position;

            logger.info("store list positions: " + lastSelectedFilterPos + " / " + lastSelectedDiskPos);

            if (null != diskImage && null != handler) {
                dismiss();
                handler.onDiskSelect(diskImage, currentDiskFilter, longClick);
            }
        }

    }

    private void editSnapshot(Image snapshotImage) {
        logger.info("delete snapshot: " + snapshotImage.getUrl());

        File snapshotFile = new File(snapshotImage.getUrl());
        if (snapshotFile.isFile()) {
            if (true == snapshotFile.delete()) {
                Toast t = null;
                t = Toast.makeText(getActivity(), "Deleted snapshot " + snapshotImage.getName(), Toast.LENGTH_LONG);
                t.show();
            } else {
                return;
            }
        }

        if (null != currentDiskFilter) {

            ImageManager diskManager = ImageManager.instance();
            if (null == diskManager) return;

            ObjectListView diskList = (ObjectListView) dialog.findViewById(R.id.selector_disk_list);
            if (null == diskList) {
                return;
            }

            diskList.clear();
            diskManager.invalidateList();
            diskList.addAll(diskManager.getList(currentDiskFilter));
            diskList.requestFocus();
            diskList.setSelection(0);
        }

    }

    public interface OnDiskSelectHandler {
        void onDiskSelect(Image diskImage, ImageFilter diskFilter, boolean longClick);
    }
}
