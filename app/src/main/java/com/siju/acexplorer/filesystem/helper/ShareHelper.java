package com.siju.acexplorer.filesystem.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.model.FileInfo;

import java.util.ArrayList;

import static com.siju.acexplorer.filesystem.groups.Category.checkIfFileCategory;
import static com.siju.acexplorer.filesystem.helper.UriHelper.createContentUri;


public class ShareHelper {

    public static void shareFiles(Context context, ArrayList<FileInfo> fileInfo, Category category) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        if (checkIfFileCategory(category)) {
            intent.setType("*/*");
        } else {
            String extension = fileInfo.get(0).getExtension();
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            intent.setType(mimeType);
        }

        ArrayList<Uri> files = new ArrayList<>();

        for (FileInfo info : fileInfo) {
            Uri uri = createContentUri(context, info.getFilePath());
            System.out.println("shareuri==" + uri);
            files.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        context.startActivity(intent);
    }

}
