package br.com.gabrielcouto.ledtec.model.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import br.com.gabrielcouto.ledtec.model.entity.LuminariaLugar;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String databaseNome ="ledTec.db";
    private static final int databaseVersao = 8;
    private static DatabaseHelper instance;


    public DatabaseHelper(Context context) {
        super(context, databaseNome, null, databaseVersao);
    }

    public static DatabaseHelper getInstance(Context context){
        if(instance ==null){
            instance = new DatabaseHelper(context);
        }
        return instance;
    }



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, LuminariaLugar.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, LuminariaLugar.class, true);
            onCreate(sqLiteDatabase,connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void close() {
        super.close();
    }
}
