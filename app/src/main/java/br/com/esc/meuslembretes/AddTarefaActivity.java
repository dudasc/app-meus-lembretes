package br.com.esc.meuslembretes;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.esc.meuslembretes.dao.TarefasDAO;
import br.com.esc.meuslembretes.model.Tarefa;
import br.com.esc.meuslembretes.receiver.NotificationReceiver;

public class AddTarefaActivity extends AppCompatActivity {

    private TarefasDAO dao;
    private Tarefa t;

    private SharedPreferences preferences;
    private NotificationManager notificationManager;
    private EditText descricao, dataa, horario;

    private Bundle extras;
    private Intent intent;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private TextInputLayout lbDescricaoLayout, lbDataLayout, lbHoraLayout;
    private Button btCleanDesc, btCleanDate, btCleanTime;
    private Spinner spinnerRepetir, spinnerNotificacao;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    //valores da repetições dos alarmes em millis
    private final static long UMA_HORA = 3600000, DUAS_HORAS = 7200000, SEIS_HORAS = 21600000,
            OITO_HORAS = 28800000, DOZE_HORAS = 43200000, UM_DIA = 86400000, DOIS_DIAS = 172800000,
            UMA_SEMANA = 604800000, UM_MES = UM_DIA * 30;
    //valores do horario das notificações


    private Date data;
    private Calendar calendar;

    private int dia, mes, ano, hor, min;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tarefa);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        descricao = (EditText) findViewById(R.id.descricao);
        dataa = (EditText) findViewById(R.id.data);
        horario = (EditText) findViewById(R.id.horario);

        btCleanDesc = (Button) findViewById(R.id.clean_desc);
        btCleanDate = (Button) findViewById(R.id.clean_date);
        btCleanTime = (Button) findViewById(R.id.clean_time);

        spinnerRepetir = (Spinner) findViewById(R.id.repetir);
        spinnerNotificacao = (Spinner) findViewById(R.id.data_notificacao);

        intent = getIntent();
        dao = new TarefasDAO(this);


        extras = intent.getExtras();
        if (extras != null) {
            setTitle("Editar tarefa");
            int id = extras.getInt("id");

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(id);

            t = dao.findById(id);

            descricao.setText(t.getDescricao());
            dataa.setText(t.getData());
            horario.setText(t.getHorario());
        } else {
            calendar = Calendar.getInstance();
            data = calendar.getTime();
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat fmth = new SimpleDateFormat("HH:mm");

            String d = fmt.format(data);
            String h = fmth.format(data);
            dataa.setText(d);
            horario.setText(h);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (extras == null) {
            getMenuInflater().inflate(R.menu.menu_add, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_edit, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save) {
            saveTarefa();
            return true;
        }
        if (id == R.id.save_edit) {
            //updateTarefa();
            return true;
        }
        if (id == R.id.delete) {
            deleteTarefa(t.getId());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickSpeak(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickSelecionaData(View v) {
        calendar = Calendar.getInstance();

        ano = calendar.get(Calendar.YEAR);
        mes = calendar.get(Calendar.MONTH);
        dia = calendar.get(Calendar.DAY_OF_MONTH);
        //txData.setText(n(mDay) + "/" + n(mMonth+1) + "/" + n(mYear));

        datePickerDialog = new DatePickerDialog(this, DatePicker.SCROLL_AXIS_VERTICAL, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dataa.setText(n(dayOfMonth) + "/" + n(monthOfYear + 1) + "/" + n(year));
            }
        }, ano, mes, dia);
        datePickerDialog.show();
    }

    public void onClickSelecionaHora(View v) {
        calendar = Calendar.getInstance();

        hor = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(this, TimePicker.SCROLL_AXIS_VERTICAL, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                horario.setText(n(hourOfDay) + ":" + n(minute));
            }
        }, hor, min, true);

        timePickerDialog.show();
    }

    public void onClickLimparDescricao(View v) {
        descricao.setText("");
    }

    public void onClickLimparData(View v) {
        dataa.setText("");
    }

    public void onClickLimparHora(View v) {
        horario.setText("00:00");
    }

    private String n(int z) {
        return "" + (z < 10 ? "0" + z : z);
    }

    public void saveTarefa() {
        String desc = descricao.getText().toString();
        String data = dataa.getText().toString();
        String hora = horario.getText().toString();


        int spinner_pos = spinnerRepetir.getSelectedItemPosition();
        String[] size_values = getResources().getStringArray(R.array.list_repetir_values);
        int valorRepetir = Integer.valueOf(size_values[spinner_pos]); // 12, 16, 20

        //pega o valor do spinner para definir o horário do alarme
        int spinner_pos2 = spinnerNotificacao.getSelectedItemPosition();
        String[] size_values2 = getResources().getStringArray(R.array.list_notificacao_values);
        int valorNotificao = Integer.valueOf(size_values2[spinner_pos2]); // 12, 16, 20


        if ("".equals(desc) || desc.isEmpty() || desc.toString().trim() == "") {
            //lbDescricaoLayout.setErrorEnabled(true);
            descricao.setError("Campo obrigatório");
        } else if ("".equals(data) || data.isEmpty() || data.toString().trim() == "") {
            //lbDataLayout.setErrorEnabled(true);
            dataa.setError("Campo obrigatório");
        } else if ("".equals(hora) || hora.isEmpty() || hora.toString().trim() == "") {
            //lbHoraLayout.setErrorEnabled(true);
            horario.setError("Campo obrigatório");
        } else {
            t = new Tarefa();
            t.setDescricao(desc);
            t.setData(data);
            t.setHorario(hora);
            t.setLembrar(valorNotificao);
            t.setRepetir(valorRepetir);

            // dao = new TarefasDAO(this);

            if (dao.salvar(t) == true) {
                Toast.makeText(AddTarefaActivity.this, "Tarefa agendada.", Toast.LENGTH_SHORT).show();
                setAlarm(t);
                finish();
            }
        }
    }

    public void deleteTarefa(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Excluir tarefa?").setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dao.delete(t.getId())) {
                    Toast.makeText(AddTarefaActivity.this, "Tarefa excluída!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).show();

    }

    public void setAlarm(Tarefa t) {
        dao = new TarefasDAO(this);
        Intent i = new Intent(this, NotificationReceiver.class);
        // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("id", dao.getLastId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, dao.getLastId(), i, 0);

        //pega a data e hora da tarefa
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        String dateString = t.getData() + " " + t.getHorario();
        //converte para Date
        Date data = null;
        try {
            data = new java.util.Date(sdf.parse(dateString).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Converte para millis
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        Long dataMillis = calendar.getTime().getTime();


        //pega o valor do spinner para repetir ou não o alarme de acordo com o tempo
        //  int spinner_pos = spinnerRepetir.getSelectedItemPosition();
        //  String[] size_values = getResources().getStringArray(R.array.list_repetir_values);
        // int valorRepetir = Integer.valueOf(size_values[spinner_pos]); // 12, 16, 20

        //pega o valor do spinner para definir o horário do alarme
        //  int spinner_pos2 = spinnerNotificacao.getSelectedItemPosition();
        // String[] size_values2 = getResources().getStringArray(R.array.list_notificacao_values);
        // int valorNotificao = Integer.valueOf(size_values2[spinner_pos2]); // 12, 16, 20


        Long dataRepeticaoMillis = null;
        Long dataNotificacaoMillis = null;

        switch (t.getLembrar()) {
            case 1:
                dataNotificacaoMillis = dataMillis - UMA_HORA;
                break;
            case 2:
                dataNotificacaoMillis = dataMillis - DUAS_HORAS;
                break;
            case 6:
                dataNotificacaoMillis = dataMillis - SEIS_HORAS;
                break;
            case 12:
                dataNotificacaoMillis = dataMillis - DOZE_HORAS;
                break;
            case 24:
                dataNotificacaoMillis = dataMillis - UM_DIA;
                break;
            case 48:
                dataNotificacaoMillis = dataMillis - DOIS_DIAS;
                break;
            case 168:
                dataNotificacaoMillis = dataMillis - UMA_SEMANA;
                break;
            default:
                dataNotificacaoMillis = dataMillis;
                break;
        }


        switch (t.getRepetir()) {
            case 2:
                dataRepeticaoMillis = DUAS_HORAS;
                break;
            case 6:
                dataRepeticaoMillis = SEIS_HORAS;
                break;
            case 8:
                dataRepeticaoMillis = OITO_HORAS;
                break;
            case 12:
                dataNotificacaoMillis = DOZE_HORAS;
                break;
            case 24:
                dataRepeticaoMillis = UM_DIA;
                break;
            case 168:
                dataRepeticaoMillis = UMA_SEMANA;
                break;
            case 720:
                dataRepeticaoMillis = UM_DIA * 30;
                break;
            default:
                dataRepeticaoMillis = null;
        }


        //Define o alarme
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (dataRepeticaoMillis != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, dataNotificacaoMillis, dataRepeticaoMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dataNotificacaoMillis, pendingIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //pegando o audio quando usa o microfone
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    descricao.setText(result.get(0));
                }
                break;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dao.close();
        dao = null;
    }
}
