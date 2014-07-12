package cn.edu.hust.kexife.note;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class NoteEditor extends Activity {

    private Constant.EDITE_MODE editeMode;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private EditText editor;
    private long editeId;   //database _id of current note

    AlertDialog colorDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_editor);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        editor = (EditText)findViewById(R.id.note_editor_edittext);

        Intent intent = getIntent();
        editeMode = (Constant.EDITE_MODE) intent.getSerializableExtra("EDITE_MODE");

        if(editeMode == Constant.EDITE_MODE.EDITE){
            editeId = intent.getLongExtra("ID", 0);
            Cursor cursor = db.rawQuery("select * from " + Constant.TABLE_NAME + " where "
                    + Constant.COLUMN_NAME_ID + "=" + editeId , null);
            cursor.moveToFirst();
            String oldNote = cursor.getString(cursor.getColumnIndex(Constant.COLUMN_NAME_NOTE));
            int oldColor = cursor.getInt( cursor.getColumnIndex(Constant.COLUMN_NAME_COLOR) );
            editor.setText(oldNote);
            editor.setBackgroundColor(oldColor);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        switch(editeMode){
            case ADD :
                addNote();
                break;
            case EDITE :
                if(isFinishing() && editor.getText().toString().trim().length()==0) deleteNote();
                else updateNote();
                break;
            default :
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(dbHelper != null) dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_editor_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.note_editor_menu_delete:
                if(editeMode==Constant.EDITE_MODE.EDITE) deleteNote();
                editeMode=Constant.EDITE_MODE.DELETE;
                finish();
                return true;
            case R.id.note_editor_menu_color:
                LinearLayout colorView = (LinearLayout) getLayoutInflater().inflate(R.layout.color_picker,null);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                colorDialog = alertBuilder.setView(colorView).create();
                colorDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateNote(){
        String note=editor.getText().toString();
        int color=((ColorDrawable) editor.getBackground()).getColor();   //API 11+
        long modifyTime=System.currentTimeMillis()/1000;

        ContentValues values = new ContentValues();
        values.put(Constant.COLUMN_NAME_NOTE, note);
        values.put(Constant.COLUMN_NAME_COLOR, color);
        values.put(Constant.COLUMN_NAME_MODIFY_TIME, modifyTime);

        db.update(Constant.TABLE_NAME, values, Constant.COLUMN_NAME_ID+"=?", new String[]{Long.toString(editeId)});
    }

    private void addNote(){
        if(editor.getText().toString().trim().length()>0){

            String note=editor.getText().toString();
            int color=((ColorDrawable) editor.getBackground()).getColor();   //API 11+
            long addTime=System.currentTimeMillis()/1000;

            ContentValues values =new ContentValues();
            values.put(Constant.COLUMN_NAME_NOTE, note);
            values.put(Constant.COLUMN_NAME_COLOR, color);
            values.put(Constant.COLUMN_NAME_CREATE_TIME, addTime);
            values.put(Constant.COLUMN_NAME_MODIFY_TIME, addTime);

            editeId = db.insert(Constant.TABLE_NAME, null, values);
            editeMode = Constant.EDITE_MODE.EDITE;
        }
    }

    private void deleteNote(){
        db.delete(Constant.TABLE_NAME, Constant.COLUMN_NAME_ID +"=?", new String[]{ Long.toString(editeId) });
    }

    //color_picker onClick
    public void setColor(View view){
        int color = ( (ColorDrawable) view.getBackground() ).getColor();    //API 11+
        editor.setBackgroundColor(color);
        colorDialog.dismiss();
    }

}