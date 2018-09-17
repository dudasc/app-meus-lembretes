package br.com.esc.meuslembretes.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {

    private String[] scriptSQLCreate;
    private String scriptSQLDelete;

    public SQLiteHelper(Context context, String nomeBanco, int versaoBanco, String[] scriptSQLCreate, String scriptSQLDelete) {
        super(context, nomeBanco, null, versaoBanco);

        this.scriptSQLCreate = scriptSQLCreate;
        this.scriptSQLDelete = scriptSQLDelete;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        int qtdeScripts = scriptSQLCreate.length;

        for (int i = 0; i < qtdeScripts; i++) {
            String sql = scriptSQLCreate[i];
            db.execSQL(sql);
        }

        Log.d(null, "Banco Criado");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versaoAntiga, int novaVersao) {
        db.execSQL(scriptSQLDelete);
        onCreate(db);
        Log.d(null, "Banco Atualizado");
    }
}
