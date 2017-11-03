package com.siju.acexplorer.view;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.helper.FileUtils;

import java.io.File;
import java.util.List;

import static com.siju.acexplorer.utils.ThumbnailUtils.displayThumb;

/**
 * Created by sj on 28/10/17.
 */

public class PasteConflictAdapter extends BaseAdapter {

    private List<FileInfo> conflictFileInfoList;
    private Context        context;

    public PasteConflictAdapter(Context context, List<FileInfo> conflictFileInfoList) {
        this.context = context;
        this.conflictFileInfoList = conflictFileInfoList;
    }


    @Override
    public int getCount() {
        return conflictFileInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return conflictFileInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileInfoHolder fileInfoHolder;
        if (convertView == null) {
            View view = convertView = LayoutInflater.from(context).inflate(R.layout.paste_conflict_file_info,
                                                                           parent, false);
            fileInfoHolder = new FileInfoHolder(view);
            convertView.setTag(fileInfoHolder);
        } else {
            fileInfoHolder = (FileInfoHolder) convertView.getTag();
        }

        if (position == 0) {
            fileInfoHolder.headerText.setText(context.getString(R.string.header_source));
        } else {
            fileInfoHolder.headerText.setText(context.getString(R.string.header_destination));
        }

        FileInfo conflictFileInfo = conflictFileInfoList.get(position);
        String filePath = conflictFileInfo.getFilePath();

        displayThumb(context, conflictFileInfo, conflictFileInfo.getCategory(), fileInfoHolder
                .imageIcon, null);

        fileInfoHolder.titleText.setText(conflictFileInfo.getFileName());
        fileInfoHolder.pathText.setText(filePath);
        File sourceFile = new File(filePath);
        long date = sourceFile.lastModified();
        String fileModifiedDate = FileUtils.convertDate(date);
        long size = sourceFile.length();
        String fileSize = Formatter.formatFileSize(context, size);
        fileInfoHolder.dateText.setText(fileModifiedDate);
        fileInfoHolder.sizeText.setText(fileSize);

        return convertView;
    }


    private static class FileInfoHolder {
        ImageView imageIcon;
        TextView  headerText;
        TextView  titleText;
        TextView  dateText;
        TextView  sizeText;
        TextView  pathText;

        FileInfoHolder(View view) {
            imageIcon = view.findViewById(R.id.imageFileIcon);
            headerText = view.findViewById(R.id.header);
            titleText = view.findViewById(R.id.textFileName);
            pathText = view.findViewById(R.id.textFilePath);
            dateText = view.findViewById(R.id.textFileDate);
            sizeText = view.findViewById(R.id.textFileSize);
        }
    }
}
