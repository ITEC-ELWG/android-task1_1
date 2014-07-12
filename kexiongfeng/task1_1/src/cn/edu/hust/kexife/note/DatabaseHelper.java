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
        db.execSQL("create table " + Constant.TABLE_NAME + "("
            + Constant.COLUMN_NAME_ID + " integer primary key autoincrement,"
            + Constant.COLUMN_NAME_NOTE + " text,"
            + Constant.COLUMN_NAME_COLOR + " integer,"
            + Constant.COLUMN_NAME_CREATE_TIME + " integer,"
            + Constant.COLUMN_NAME_MODIFY_TIME + " integer"
            + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("drop table if exists " + Constant.TABLE_NAME);
        onCreate(db);
    }
}
