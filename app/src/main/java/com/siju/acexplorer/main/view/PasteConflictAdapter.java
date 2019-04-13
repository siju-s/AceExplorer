package com.siju.acexplorer.main.view;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.theme.ThemeUtils;

import java.io.File;
import java.util.List;

import static com.siju.acexplorer.utils.ThumbnailUtils.displayThumb;

public class PasteConflictAdapter extends BaseAdapter {

    private List<FileInfo> conflictFileInfoList;
    private Context        context;
    private Theme          theme;

    public PasteConflictAdapter(Context context, List<FileInfo> conflictFileInfoList) {
        this.context = context;
        this.conflictFileInfoList = conflictFileInfoList;
        this.theme = Theme.getTheme(ThemeUtils.getTheme(context));
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

        switch (theme) {
            case DARK:
                fileInfoHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color
                        .dark_home_card_bg));
                break;
            case LIGHT:
                fileInfoHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color
                        .light_home_card_bg));
                break;
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
        CardView  cardView;
        ImageView imageIcon;
        TextView  headerText;
        TextView  titleText;
        TextView  dateText;
        TextView  sizeText;
        TextView  pathText;

        FileInfoHolder(View view) {
            cardView = view.findViewById(R.id.cardView);
            imageIcon = view.findViewById(R.id.imageFileIcon);
            headerText = view.findViewById(R.id.header);
            titleText = view.findViewById(R.id.textFileName);
            pathText = view.findViewById(R.id.textFilePath);
            dateText = view.findViewById(R.id.textFileDate);
            sizeText = view.findViewById(R.id.textFileSize);
        }
    }
}
