package cn.edu.hust.kexife.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String DATABESE_NAME="note.db";
    private static final int DATABASE_VERSION=1;

    public DatabaseHelper(Context context){
        super(context,DATABESE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + Constant.TABLE_NAME + "("
            + Constant.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Constant.COLUMN_NAME_NOTE + " TEXT,"
            + Constant.COLUMN_NAME_COLOR + " INTEGER,"
            + Constant.COLUMN_NAME_CREATE_TIME + " INTEGER,"
            + Constant.COLUMN_NAME_MODIFY_TIME + " INTEGER"
            + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + Constant.TABLE_NAME);
        onCreate(db);
    }
}
