package br.com.esc.meuslembretes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.esc.meuslembretes.adapter.ItemRowAdapter;
import br.com.esc.meuslembretes.dao.TarefasDAO;
import br.com.esc.meuslembretes.model.Tarefa;
import br.com.esc.meuslembretes.preferences.PreferencesActivity;
import br.com.esc.meuslembretes.receiver.NotificationReceiver;


public class MainActivity extends AppCompatActivity {

    private AlarmManager alarmManager = null;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private TarefasDAO dao;
    private Intent intent;
    private PendingIntent pendingIntent;
    private ListView listaTarefas;
    private FloatingActionButton fab;
    private MenuItem searchItem;
    private SearchView searchView;
    private ItemRowAdapter adapter;
    private SharedPreferences preferences;
    private ImageView imageView;
    private TextView nada;
    private boolean concluidas;
    private int antigas;
    private List<Tarefa> itens;

    private Handler handlerExcluir = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            MainActivity.this.delete(inputMessage.getData().getInt("position"), (Tarefa) inputMessage.getData().getSerializable("tarefa"));
        }
    };

    private Handler handlerFinalizar = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            MainActivity.this.end(inputMessage.getData().getInt("position"), (Tarefa) inputMessage.getData().getSerializable("tarefa"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_launcher_actionbar);

        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_task);
        // getSupportActionBar().setIcon(R.drawable.ic_task);


        listaTarefas = (ListView) findViewById(R.id.lista_tarefas);
        itens = new ArrayList<>();
        imageView = (ImageView) findViewById(R.id.ic_relax);
        nada = (TextView) findViewById(R.id.nada_a_fazer);

        dao = new TarefasDAO(this);

        listaTarefas.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;
                // ListView Clicked item value
                Tarefa t = (Tarefa) listaTarefas.getItemAtPosition(position);
                // Show Alert
                //Toast.makeText(getApplicationContext(), "Position :" + itemPosition + "  ListItem : " + t.getDescricao(), Toast.LENGTH_LONG).show();
                Intent i = new Intent(getApplicationContext(), AddTarefaActivity.class);
                i.putExtra("id", t.getId());
                startActivity(i);
            }
        });

        //atualizaListView(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Função de busca do botao search na actionbar
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //MainActivity.this.buscarPorTitulo(String.valueOf(query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                MainActivity.this.findByTitle(String.valueOf(newText));
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_share) {
            share();
        }

        if (id == R.id.action_about) {
            about();


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        concluidas = preferences.getBoolean("mostrar_concluidas", false);
        antigas = Integer.parseInt(preferences.getString("mostrar_antigas", "0"));
        nada.setVisibility(View.INVISIBLE);
        refreshListView(true);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MainActivity.this.refreshListView(true);
            }
        };
        mIntentFilter = new IntentFilter("ATUALIZA_LISTA");
        registerReceiver(mReceiver, mIntentFilter);


    }

    @Override
    public void onPause() {
        super.onPause();
        //Desregistra o broadcast
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    public void onClickAddTarefa(View v) {
        Intent i = new Intent(this, AddTarefaActivity.class);
        startActivity(i);
    }

    private void refreshListView(Boolean denovo) {
        imageView.setVisibility(View.INVISIBLE);
        nada.setVisibility(View.INVISIBLE);
        if (denovo)
            itens = dao.findAll(concluidas, antigas);

        if (itens.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            nada.setVisibility(View.VISIBLE);
        }
        adapter = new ItemRowAdapter(this, itens, handlerExcluir, handlerFinalizar);
        listaTarefas.setAdapter(adapter);


       /* listaTarefas.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) listaTarefas.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(), "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG).show();

            }
        });*/
    }

    public void end(final int position, final Tarefa t) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Concluir tarefa?").setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dao.end(t.getId())) {
                    Toast.makeText(MainActivity.this, "Tarefa concluída!", Toast.LENGTH_SHORT).show();
                    itens.remove(position);
                    refreshListView(true);
                    cancelAlarm(t.getId());
                }
            }
        }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).show();
    }

    public void delete(final int position, final Tarefa t) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Excluir tarefa?").setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dao.delete(t.getId())) {
                    Toast.makeText(MainActivity.this, "Tarefa excluída!", Toast.LENGTH_SHORT).show();
                    itens.remove(position);
                    refreshListView(true);
                    cancelAlarm(t.getId());
                }
            }
        }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).show();
    }

    public void cancelAlarm(int alarmId) {
        intent = new Intent(this, NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void findByTitle(String q) {
        itens = q.isEmpty() ? dao.findAll(concluidas, antigas) : dao.findByName(q, concluidas, antigas);
        refreshListView(false);
    }

    public void share() {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        share.putExtra(Intent.EXTRA_SUBJECT,
                "Confira o aplicativos Meus Lembretes");
        share.putExtra(Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=br.com.esc.meuslembretes");

        startActivity(Intent.createChooser(share, "Compartilhar"));
    }

    public void about() {
        //Cria o gerador do AlertDialog
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //define o titulo
        builder.setTitle("Meus lembretes - vs 1.0");
        builder.setIcon(R.drawable.ic_launcher);
        //define a mensagem
        builder.setMessage(R.string.msg_about);
        //define um botão como positivo
        builder.setPositiveButton("Ok", null);

        //cria o AlertDialog
        alertDialog = builder.create();
        //Exibe
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dao.close();
        dao = null;
    }
}