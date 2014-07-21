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

    private Constant.EDIT_MODE editMode;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private EditText editor;
    private long editId;   //database _id of current note

    AlertDialog colorDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_editor);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        editor = (EditText)findViewById(R.id.note_editor_edittext);

        Intent intent = getIntent();
        editMode = (Constant.EDIT_MODE) intent.getSerializableExtra("EDIT_MODE");

        if(editMode == Constant.EDIT_MODE.EDIT){
            editId = intent.getLongExtra("ID", 0);
            Cursor cursor = db.rawQuery("SELECT * FROM " + Constant.TABLE_NAME + " WHERE "
                    + Constant.COLUMN_NAME_ID + "=" + editId , null);
            cursor.moveToFirst();
            String oldNote = cursor.getString(cursor.getColumnIndex(Constant.COLUMN_NAME_NOTE));
            int oldColor = cursor.getInt( cursor.getColumnIndex(Constant.COLUMN_NAME_COLOR) );

            editor.setText(oldNote);
            editor.setBackgroundColor(oldColor);
        }else{
            editor.requestFocus();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        switch(editMode){
            case ADD :
                addNote();
                break;
            case EDIT :
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
                if(editMode==Constant.EDIT_MODE.EDIT) deleteNote();
                editMode=Constant.EDIT_MODE.DELETE;
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

        db.update(Constant.TABLE_NAME, values, Constant.COLUMN_NAME_ID+"=?", new String[]{Long.toString(editId)});
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

            editId = db.insert(Constant.TABLE_NAME, null, values);
            editMode = Constant.EDIT_MODE.EDIT;
        }
    }

    private void deleteNote(){
        db.delete(Constant.TABLE_NAME, Constant.COLUMN_NAME_ID +"=?", new String[]{ Long.toString(editId) });
    }

    //color_picker onClick
    public void setColor(View view){
        int color = ( (ColorDrawable) view.getBackground() ).getColor();    //API 11+
        editor.setBackgroundColor(color);
        colorDialog.dismiss();
    }

}