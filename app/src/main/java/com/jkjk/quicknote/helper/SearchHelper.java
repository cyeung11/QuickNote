package com.jkjk.quicknote.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;

public class SearchHelper {
/* A helper that search through the database with the search text provided. Content and title are included in the search.
After querying the database, the helper convert the 2 array lists that included the search text into a unique result in string
*/

    private SearchHelper(){}

    public static String searchResult(SQLiteDatabase database, String searchText){
        ArrayList <String> result = new ArrayList<>();

        //Search if title column has search string
        Cursor title = searchTitle(database, searchText);
        if (title!=null && title.moveToFirst()){
            do{
                result.add(title.getString(0));
            }while (title.moveToNext());
        }

        //Search if content column has search string
        Cursor content = searchContent(database, searchText);
        if (content!=null && content.moveToFirst()){
            do{
                String id = content.getString(0);
                //Exclude duplicate
                if (!result.contains(id)){
                    result.add(id);
                }
            }while (content.moveToNext());
        }

        //Convert array list to string to SQL query
        StringBuilder stringBuilder = new StringBuilder();
        for (String resultId : result){
            stringBuilder.append(resultId).append(",");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.delete(stringBuilder.length()-1, stringBuilder.length() + 1);
        }
        return stringBuilder.toString();
    }

    private static Cursor searchTitle(SQLiteDatabase database, String inputText){
        Cursor cursor;
        // if no search input, avoid doing I/O
        if (inputText.length()>0) {
            cursor = database.query(DATABASE_NAME, new String[]{"_id"}, "title LIKE '%" + inputText + "%'", null, null
                    , null, null);
        }else cursor = null;
        return cursor;
    }

    private static Cursor searchContent(SQLiteDatabase database, String inputText){
        Cursor cursor;
        if (inputText.length()>0) {
            cursor = database.query(DATABASE_NAME, new String[]{"_id"}, "content LIKE '%"+inputText+"%'", null, null
                    , null, null);
        }else cursor = null;
        return cursor;
    }

}
