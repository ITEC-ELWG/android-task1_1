package cn.edu.hust.kexife.note;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class NoteList extends ListActivity {

    private DatabaseHelper dbHelper;
    private SimpleCursorAdapter listAdapter;

    private ActionMode actionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(this);
        Cursor dbCursor = updateCursor();

        listAdapter = new SimpleCursorAdapter( this , R.layout.list_item , dbCursor ,
                new String[]{Constant.COLUMN_NAME_COLOR, Constant.COLUMN_NAME_NOTE ,Constant.COLUMN_NAME_MODIFY_TIME } , new int[]{R.id.list_item_layout, R.id.list_item_title, R.id.list_item_time} ,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER );

        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder(){
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex){
                switch(view.getId()){
                    case R.id.list_item_title:
                        String note = cursor.getString( cursor.getColumnIndex(Constant.COLUMN_NAME_NOTE));
                        ((TextView)view).setText(note);
                        return true;
                    case R.id.list_item_time:
                        long modifyTime = cursor.getLong( cursor.getColumnIndex(Constant.COLUMN_NAME_MODIFY_TIME) );
                        Date date = new Date(modifyTime*1000);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String dateString = dateFormat.format(date);
                        ((TextView)view).setText( dateString );
                        return true;
                    case R.id.list_item_layout:
                        int color = cursor.getInt( cursor.getColumnIndex(Constant.COLUMN_NAME_COLOR) );

                        //setBackgroundColor(color) will break the highlight when click , so using StateListDrawable instead
                        ColorfulListItemDrawable colorfullDraw = new ColorfulListItemDrawable(color);

                        int sdk = android.os.Build.VERSION.SDK_INT;
                        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
                            view.setBackgroundDrawable(colorfullDraw);
                        else  view.setBackground(colorfullDraw);
                        return true;
                    default:
                }
                return false;
            }
        };

        listAdapter.setViewBinder(binder);
        setListAdapter(listAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateCursor();     //update list view if database changed
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(dbHelper != null) dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_list_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.note_list_menu_add:
                addNote();
                return true;
            case R.id.note_list_menu_delete:
                getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                actionMode = startActionMode(new ActionModeCallback());
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if(actionMode == null) {
            editeNote(id);
        }else{
            int checkedCount = getListView().getCheckedItemCount();
            actionMode.setTitle(checkedCount + getString(R.string.items_selected));
        }
    }

    private Cursor updateCursor(){
        Cursor cursor = dbHelper.getReadableDatabase().query(Constant.TABLE_NAME,
                new String[]{Constant.COLUMN_NAME_ID, Constant.COLUMN_NAME_COLOR, Constant.COLUMN_NAME_NOTE, Constant.COLUMN_NAME_MODIFY_TIME},
                null,null,null,null,Constant.COLUMN_NAME_MODIFY_TIME+" DESC");
        if(listAdapter!=null) listAdapter.changeCursor(cursor);
        return cursor;
    }

    private void addNote(){
        Intent intent = new Intent(this, NoteEditor.class);
        Bundle data = new Bundle();
        data.putSerializable("EDIT_MODE", Constant.EDIT_MODE.ADD);
        intent.putExtras(data);
        startActivity(intent);
    }

    private void editeNote(long id){
        Intent intent = new Intent(this,NoteEditor.class);
        Bundle data = new Bundle();
        data.putSerializable("EDIT_MODE", Constant.EDIT_MODE.EDIT);
        data.putLong("ID",id);
        intent.putExtras(data);
        startActivity(intent);
    }

    public class ColorfulListItemDrawable extends StateListDrawable {
        private final PaintDrawable mColor;

        public ColorfulListItemDrawable(int color) {
            mColor = new PaintDrawable(color);
            initialize();
        }
        private void initialize() {
            Drawable color = mColor;

            addState(new int[]{ android.R.attr.state_pressed }, new ColorDrawable(Color.TRANSPARENT) );
            addState(new int[]{android.R.attr.state_activated }, new ColorDrawable(0xff7ccae6) );

            addState(new int[]{}, color);
        }
        public void setColor(int color) {
            mColor.getPaint().setColor(color);
            mColor.invalidateSelf();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.note_list_select_menu, menu);
            mode.setTitle( getString(R.string.select_item) );
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.note_list_select_menu_delete:
                    long[] checkedIDs = getListView().getCheckedItemIds();
                    if(checkedIDs.length>0) {
                        String whereArgs = Arrays.toString(checkedIDs).replace('[', '(').replace(']', ')');
                        dbHelper.getWritableDatabase().delete(Constant.TABLE_NAME, Constant.COLUMN_NAME_ID+ " IN "+ whereArgs,null );
                    }
                    mode.finish();
                    break;
                case R.id.note_list_select_menu_all:
                    int checkedCount = getListView().getCheckedItemCount();
                    if( checkedCount<getListView().getCount() )
                        for (int index = 0; index < getListView().getCount(); index++) getListView().setItemChecked(index, true);
                    else
                        for (int index = 0; index < getListView().getCount(); index++) getListView().setItemChecked(index, false);

                    actionMode.setTitle(getListView().getCheckedItemCount() + getString(R.string.items_selected));
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for(int i=0; i<getListView().getCount(); i++) {
                getListView().setItemChecked(i, false);
                getListView().getChildAt(i).setActivated(false);    // remove multiple selected color
            }
            getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
            updateCursor();
            actionMode = null;
        }

    }

}
