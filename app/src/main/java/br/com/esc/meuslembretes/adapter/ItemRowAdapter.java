package br.com.esc.meuslembretes.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import br.com.esc.meuslembretes.R;
import br.com.esc.meuslembretes.model.Tarefa;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class ItemRowAdapter extends ArrayAdapter {

    private final Activity context;

    private List<Tarefa> objects;
    private Handler handlerExcluir = null;
    private Handler handlerFinalizar = null;
    private Button btFinalizar, btExcluir;
    private ImageView icRepeat, icLabel;
    private TextView txtStatus;

    public ItemRowAdapter(Activity context, List objects, Handler handlerExcluir, Handler handlerFinalizar) {
        super(context, R.layout.lista_item, objects);
        this.context = context;
        this.objects = objects;
        this.handlerExcluir = handlerExcluir;
        this.handlerFinalizar = handlerFinalizar;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View row = inflater.inflate(R.layout.lista_item, parent, false);

        btFinalizar = (Button) row.findViewById(R.id.bt_concluir);
        btExcluir = (Button) row.findViewById(R.id.bt_excluir);
        icRepeat = (ImageView) row.findViewById(R.id.ic_repeat_item);
        icLabel = (ImageView) row.findViewById(R.id.ic_label);
        txtStatus = (TextView) row.findViewById(R.id.lbStatus);

        final Tarefa item = objects.get(position);
        String data = item.getData() + " " + item.getHorario() + ":00";


        if (item.getConcluida() == 0) {
            txtStatus.setText("Pendente");
           // status.setTextColor(ContextCompat.getColor(context, R.color.labelPending));
            icLabel.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_label_pending));
        }else if(item.getConcluida() == 1){
           txtStatus.setText("Concluída");
            //status.setTextColor(ContextCompat.getColor(context, R.color.labelEnd));

            icLabel.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_label_end));

            btFinalizar.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_end_disabled));
            btFinalizar.setEnabled(false);
        }

        if (item.getRepetir() == 0) {
            icRepeat.setVisibility(View.INVISIBLE);
        }


        ((TextView) row.findViewById(R.id.data_hora)).setText(comparaData(data, item.getConcluida()) + " - " + item.getHorario());
        ((TextView) row.findViewById(R.id.lbDescricao)).setText(item.getDescricao());


        btFinalizar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (handlerFinalizar != null) {
                    Bundle b = new Bundle();
                    b.putInt("position", position);
                    b.putSerializable("tarefa", item);

                    Message m = new Message();
                    m.setData(b);

                    handlerFinalizar.sendMessage(m);
                }
            }
        });
        btExcluir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (handlerExcluir != null) {
                    Bundle b = new Bundle();
                    b.putInt("position", position);
                    b.putSerializable("tarefa", item);

                    Message m = new Message();
                    m.setData(b);

                    handlerExcluir.sendMessage(m);
                }
            }
        });
        return row;
    }

    public String comparaData(String data, int finalizada) {
        Calendar cal = Calendar.getInstance();
        Date dataHoraHoje = cal.getTime();

        Date dataHoje = null;
        try {
            dataHoje = zerarHora(dataHoraHoje);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(dataHoje);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        cal2.add(Calendar.DAY_OF_MONTH, 1);
        Date dataAmanha = cal2.getTime();

        Calendar cal3 = Calendar.getInstance();
        cal3.setTime(dataHoje);
        cal3.set(Calendar.HOUR_OF_DAY, 0);
        cal3.set(Calendar.MINUTE, 0);
        cal3.set(Calendar.SECOND, 0);
        cal3.set(Calendar.MILLISECOND, 0);
        cal3.add(Calendar.DAY_OF_MONTH, -1);
        Date dataOntem = cal3.getTime();

        //DateFormat fmt = DateFormat.getDateInstance(DateFormat.FULL, "br");
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date mData = null;

        try {
            mData = fmt.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal4 = Calendar.getInstance();
        cal4.setTime(mData);
        Date dataHoraAtual = cal4.getTime();

        Date dataAtual = null;
        try {
            dataAtual = zerarHora(dataHoraAtual);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.i("Dtas: ", "HOJE - " + dataHoraHoje + "\nAmanhã - " + dataAmanha + "\nOntem - " + dataOntem + "\nAtual - " + dataHoraAtual);

        String dataExtenso = "";

        if (finalizada == 0 && dataHoraAtual.compareTo(dataHoraHoje) < 0) {
            //dataExtenso = "(Atrasada) ";
            icLabel.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_label_delayed));
        }

        if (dataAtual.compareTo(dataHoje) == 0) {
            dataExtenso += "Hoje";
        }
        if (dataAtual.compareTo(dataHoje) > 0) {
            if (dataAtual.equals(dataAmanha)) {
                dataExtenso = "Amanhã";
            } else {
                dataExtenso += "" + dataPorExtenso(mData);
            }
        }
        if (dataAtual.compareTo(dataHoje) < 0) {
            if (dataAtual.equals(dataOntem)) {
                dataExtenso += "Ontem";
            } else {
                dataExtenso += dataPorExtenso(mData);
            }
        }

        return dataExtenso;
    }

    private static Date zerarHora(Date data) throws ParseException {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.parse(format.format(data));
    }


    public static String dataPorExtenso(java.util.Date dt) {
        DateFormat dfmt = new SimpleDateFormat("EEE, d 'de' MMMM 'de' yyyy");
        return dfmt.format(dt);
    }

}
