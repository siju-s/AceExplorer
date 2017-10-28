package com.siju.acexplorer.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.helper.FileUtils;

import java.io.File;
import java.util.List;

import static com.siju.acexplorer.model.helper.AppUtils.getAppIcon;

/**
 * Created by sj on 28/10/17.
 */

public class PasteConflictAdapter extends BaseAdapter {

    private List<ConflictFileInfo> fileInfo;
    private Context                context;

    public PasteConflictAdapter(Context context, List<ConflictFileInfo> fileInfo) {
        this.context = context;
        this.fileInfo = fileInfo;
    }


    @Override
    public int getCount() {
        return fileInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return fileInfo.get(position);
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
                                                             parent,false);
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
        String filePath = fileInfo.get(position).getFilePath();
        Drawable drawable = getAppIcon(context, filePath);
        if (drawable != null) {
            fileInfoHolder.imageIcon.setImageDrawable(drawable);
        }
        fileInfoHolder.titleText.setText(fileInfo.get(position).getFileName());
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

        FileInfoHolder(View view) {
            imageIcon = view.findViewById(R.id.imageFileIcon);
            headerText = view.findViewById(R.id.header);
            titleText = view.findViewById(R.id.textFileName);
            dateText = view.findViewById(R.id.textFileDate);
            sizeText = view.findViewById(R.id.textFileSize);
        }
    }
}
