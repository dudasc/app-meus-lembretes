package br.com.esc.meuslembretes.receiver;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import br.com.esc.meuslembretes.dao.TarefasDAO;
import br.com.esc.meuslembretes.model.Tarefa;

public class DeleteNotificationReceiver extends BroadcastReceiver {
    private Bundle extras;
    private TarefasDAO dao;
    private Tarefa t;

    NotificationManager noti;

    public DeleteNotificationReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        dao = new TarefasDAO(context);
        extras = intent.getExtras();
        int id = extras.getInt("id");

        if(dao.delete(id)) {
            Toast.makeText(context, "Tarefa excluída", Toast.LENGTH_SHORT).show();
            context.sendBroadcast(new Intent("ATUALIZA_LISTA"));

            //Apaga a notificação se ainda estiver ali
            noti = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            noti.cancel(id);cancelAlarm(context, id);
            cancelAlarm(context, id);
        }
    }

    public void cancelAlarm(Context context, int id){
        Intent i = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
