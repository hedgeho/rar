package ru.gurhouse.sch;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Toast;

import java.net.URISyntaxException;

/**
 * Скопировано с сайта https://stackoverflow.com/questions/27602986/convert-a-file-path-to-uri-in-android
 */

public class ImageFilePath {
    public static String getPath(final Context context, final Uri uri) {
        try{
            String s = _getPath(context,uri);
            if(s == null || s.equals("NOT FOUND")) {
                Toast toast = Toast.makeText(context,
                        "Действие невозможно! Выберите другой файл",
                        Toast.LENGTH_LONG);
                toast.show();
                return null;
            }
            return s;
        }catch (IllegalArgumentException e){
            Toast toast = Toast.makeText(context,
                    "Действие невозможно! Выберите другой файл",
                    Toast.LENGTH_LONG);
            toast.show();
            return null;
        }
    }


    private static String _getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();
                return getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {
        final String column = "_data";
        final String[] projection = { column };
        try (Cursor cursor = context.getContentResolver().query(uri, projection,
                selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }
}