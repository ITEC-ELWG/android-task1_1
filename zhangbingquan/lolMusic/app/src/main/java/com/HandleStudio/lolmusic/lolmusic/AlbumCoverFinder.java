package com.HandleStudio.lolmusic.lolmusic;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 2bab on 14-7-16.
 * Get Album Cover from File
 */

public class AlbumCoverFinder {

    private static final Uri ALBUM_COVER_URI = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
    private static Bitmap mCachedBit = null;
    public static boolean defaultCover = true;

    public static Bitmap getAlbumCover(Context context, long song_id, long album_id,
                                       boolean allowDefault) {

        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                Bitmap bm = getAlbumCoverFromFile(context, song_id, -1);
                if (bm != null) {
                    defaultCover = false;
                    return bm;
                }
            }
            if (allowDefault) {
                defaultCover = true;
                return getDefaultAlbumCover(context);
            }
            return null;
        }


        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(ALBUM_COVER_URI, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = contentResolver.openInputStream(uri);
                defaultCover = false;
                return BitmapFactory.decodeStream(in, null, bitmapOptions);
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getAlbumCoverFromFile(context, song_id, album_id);
                defaultCover = false;
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowDefault) {
                            defaultCover = true;
                            return getDefaultAlbumCover(context);
                        }
                    }
                } else if (allowDefault) {
                    bm = getDefaultAlbumCover(context);
                    defaultCover = true;
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }

        return null;
    }


    private static Bitmap getAlbumCoverFromFile(Context context, long songId, long albumId) {
        Bitmap bm = null;
        if (albumId < 0 && songId < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            if (albumId < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songId + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(ALBUM_COVER_URI, albumId);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (FileNotFoundException ex) {

        }
        if (bm != null) {
            mCachedBit = bm;
        }
        return bm;
    }


    public static Bitmap getDefaultAlbumCover(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(
                context.getResources().openRawResource(R.raw.album_pic_default), null, opts);
    }

}
