package com.jkjk.quicknote;

import android.database.Cursor;

import java.util.ArrayList;

import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;

public class SearchHelper {


    public String searchResult(String searchText){
        ArrayList <String> temp = new ArrayList<>();

        //Search if title column has search string
        Cursor title = searchTitle(searchText);
        if (title!=null && title.moveToFirst()){
            do{
                temp.add(title.getString(0));
            }while (title.moveToNext());
        }

        //Search if content column has search string
        Cursor content = searchContent(searchText);
        if (content!=null && content.moveToFirst()){
            do{
                String id = content.getString(0);
                //Exclude duplicate
                if (!temp.contains(id)){
                    temp.add(id);
                }
            }while (content.moveToNext());
        }

        //Convert to array
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < temp.size(); i++){
            stringBuilder.append(temp.get(i)).append(",");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.delete(stringBuilder.length()-1, stringBuilder.length() + 1);
        }
        return stringBuilder.toString();
    }

    private Cursor searchTitle(String inputText){
        Cursor cursor;
        // if no search input, avoid doing I/O
        if (inputText.length()>0) {
            cursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title"}, "title LIKE '%" + inputText + "%'", null, null
                    , null, null);
        }else cursor = null;
        return cursor;
    }

    private Cursor searchContent(String inputText){
        Cursor cursor;
        if (inputText.length()>0) {
            cursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "content"}, "content LIKE '%"+inputText+"%'", null, null
                    , null, null);
        }else cursor = null;
        return cursor;
    }

}
