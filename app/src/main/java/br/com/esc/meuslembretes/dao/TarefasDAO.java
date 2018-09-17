package br.com.esc.meuslembretes.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.esc.meuslembretes.model.Tarefa;

public class TarefasDAO {
    private final Context context;

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor c;

    private static final int NENHUMA = 1, TODAS = 0;

    private final int version = 1;

    public TarefasDAO(Context context) {
        this.context = context;

        String[] sqlOnCreate = new String[]{
                "create table if not exists tarefas (_id integer primary key autoincrement, descricao text not null, data text not null, horario text not null, concluida int default 0, repetir int not null default 0, lembrar int not null default 0 );"
        };

        String sqlOnUpdate = null;

        this.dbHelper = new SQLiteHelper(context, "tarefas", version, sqlOnCreate, sqlOnUpdate);
        this.db = dbHelper.getWritableDatabase();
    }

    public boolean salvar(Tarefa tarefa) {
        tarefa.setConcluida(0);
        if (db.insert("tarefas", null, tarefa.toContentValues()) != -1) {
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        if (db.delete("tarefas", "_id = ?", new String[]{"" + id}) != 0) {
            return true;
        }
        return false;
    }

    public boolean end(Integer id) {
        db.execSQL("UPDATE tarefas SET concluida = 1 WHERE _id = ?", new String[]{"" + id});
        return true;
    }

    public List<Tarefa> findAll(Boolean concluidas, int antigas) {
        List<Tarefa> items = new ArrayList<>();
        String substrData = "substr(data, 7,4) || '-' || substr(data, 4, 2)|| '-' ||  substr(data, 1, 2) as dat";
        String substrDataHora = "substr(data, 7,4) || '-' || substr(data, 4, 2)|| '-' ||  substr(data, 1, 2) ||  time(horario)  as dataHora";

        String where = "";
        if (concluidas == false) {
            where = "concluida = 0";
            if (antigas == NENHUMA) {
                where += " AND strftime(dataHora) >= datetime('now')";
            }
        } else {
            if (antigas == NENHUMA) {
                where = "strftime(dataHora) >= datetime('now')";
            }
        }
        c = db.query("tarefas", new String[]{"_id", "descricao", "data", substrData, substrDataHora, "strftime(horario)", "concluida", "repetir"}, where, null, null, null, "date(dat) ASC, time(horario) ASC", null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            do {
                Tarefa item = new Tarefa();
                item.setId(c.getInt(0));
                item.setDescricao(c.getString(1));
                item.setData(c.getString(2));
                item.setHorario(c.getString(5));
                item.setConcluida(c.getInt(6));
                item.setRepetir(c.getInt(7));
                items.add(item);
            } while (c.moveToNext());
        }

        return items;
    }

    public int getLastId() {
        c = db.query("tarefas", new String[]{"_id"}, null, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToLast();
        }
        return c.getInt(0);
    }

    public Tarefa findById(int id) {
        c = db.query("tarefas", new String[]{"_id", "descricao", "data", "horario", "concluida", "repetir"}, "_id = ?", new String[]{"" + id}, null, null, null);

        Tarefa item = new Tarefa();
        if (c.getCount() > 0) {
            c.moveToFirst();
            item.setId(c.getInt(0));
            item.setDescricao(c.getString(1));
            item.setData(c.getString(2));
            item.setHorario(c.getString(3));
            item.setConcluida(c.getInt(4));
            item.setRepetir(c.getInt(5));
        }

        return item;
    }

    public List<Tarefa> findByName(String name, boolean concluidas, int antigas) {
        List<Tarefa> items = new ArrayList<>();

        String substrDataHora = "substr(data, 7,4) || '-' || substr(data, 4, 2)|| '-' ||  substr(data, 1, 2) ||  time(horario)  as dataHora";

        String where = "";
        if (concluidas == false) {
            where = "concluida = 0 AND";
            if (antigas == NENHUMA) {
                where += " strftime(dataHora) >= datetime('now') AND";
            }
        } else {
            if (antigas == NENHUMA) {
                where = "strftime(dataHora) >= datetime('now') AND";
            }
        }
        c = db.query("tarefas", new String[]{"_id", "descricao", "data", "horario", "concluida", "repetir", substrDataHora}, where + " descricao LIKE ? ", new String[]{"%" + name + "%"}, null, null, "datetime(dataHora) ASC", null);
        if (c.getCount() > 0) {
            c.moveToFirst();

            do {
                Tarefa item = new Tarefa();
                item.setId(c.getInt(0));
                item.setDescricao(c.getString(1));
                item.setData(c.getString(2));
                item.setHorario(c.getString(3));
                item.setConcluida(c.getInt(4));
                item.setRepetir(c.getInt(5));
                items.add(item);
            } while (c.moveToNext());
        }
        return items;
    }

    public void close() {
        this.dbHelper.close();
    }

}
