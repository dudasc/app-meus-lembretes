package br.com.esc.meuslembretes.model;

import android.content.ContentValues;

import java.io.Serializable;


/**
 * Created by duda on 07/05/2016.
 */

public class Tarefa implements Serializable {

    private Integer id;
    // private String titulo;
    private String descricao;
    private String data;
    private String horario;
    private Integer concluida;
    private int repetir;
    private int lembrar;

    public Tarefa() {
    }

    public Tarefa(Integer id, String descricao, String data, String horario, Integer concluida, int repetir, int lembrar) {
        this.id = id;
        this.descricao = descricao;
        this.data = data;
        this.horario = horario;
        this.concluida = concluida;
        this.repetir = repetir;
        this.lembrar = lembrar;
    }

    public Integer getConcluida() {
        return concluida;
    }

    public void setConcluida(Integer concluida) {
        this.concluida = concluida;
    }

    public int getRepetir() {
        return repetir;
    }

    public void setRepetir(int repetir) {
        this.repetir = repetir;
    }

    public int getLembrar() {
        return lembrar;
    }

    public void setLembrar(int lembrar) {
        this.lembrar = lembrar;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put("descricao", this.descricao);
        values.put("data", String.valueOf(this.data));
        values.put("horario", String.valueOf(this.horario));
        values.put("concluida", this.concluida);
        values.put("lembrar", this.lembrar);
        values.put("repetir", this.repetir);

        if (id != null) {
            values.put("id", this.id);
        }

        return values;
    }
}
