package br.com.esc.meuslembretes.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import br.com.esc.meuslembretes.AddTarefaActivity;
import br.com.esc.meuslembretes.MainActivity;
import br.com.esc.meuslembretes.R;
import br.com.esc.meuslembretes.dao.TarefasDAO;
import br.com.esc.meuslembretes.model.Tarefa;

public class NotificationReceiver extends BroadcastReceiver {

    private Tarefa t;
    private TarefasDAO dao;
    private Intent i, ie, ic;
    private PendingIntent pendingIntent, pendingIntentExcluir, pendingIntentEnd, pendingIntentEdit;
    private Notification noti;

    private SharedPreferences preferences;
    private Bundle extras;

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        extras = intent.getExtras();
        int id = extras.getInt("id");
        dao = new TarefasDAO(context);
        t = dao.findById(id);
        setNotification(context, t);
    }


    public void setNotification(Context context, Tarefa t) {
        i = new Intent(context, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        //monta a notificação com o ID da tarefa
        //ie = new Intent(context, DeleteNotificationReceiver.class);
        //ie.putExtra("id", t.getId());
        //cancela a notificação quando clica FLAG_CANCEL_CURRENT
        // pendingIntentExcluir = PendingIntent.getBroadcast(context, t.getId(), ie, PendingIntent.FLAG_UPDATE_CURRENT);

        ic = new Intent(context, EndNotificationReceiver.class);
        ic.putExtra("id", t.getId());
        pendingIntentEnd = PendingIntent.getBroadcast(context, t.getId(), ic, PendingIntent.FLAG_UPDATE_CURRENT);

        ie = new Intent(context, AddTarefaActivity.class);
        ie.putExtra("id", t.getId());
        pendingIntentEdit = PendingIntent.getActivity(context, t.getId(), ie, PendingIntent.FLAG_UPDATE_CURRENT);

        String dia = t.getData();
        String hora = t.getHorario();

        noti = new Notification.Builder(context)
                .setTicker("Você tem uma tarefa agendada")
                .setContentTitle("Lembrete")
                .setContentText(t.getDescricao() + ". Dia " + dia + " às " + hora)

                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_assignment)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .addAction(R.drawable.ic_edit, "Editar", pendingIntentEdit)
        .addAction(R.drawable.ic_end, "Concluir", pendingIntentEnd).build();

        //sendBroadcast(new Intent("ACAO"));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Cancela a notificação depois q ela eh clicada
        //noti.flags |= Notification.FLAG_AUTO_CANCEL;

        //vribra o celular
        if (preferences.getBoolean("vibrar", true)) {
            noti.vibrate = new long[]{150, 300, 150, 600};
        }

        if (preferences.getBoolean("som", true)) {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            noti.sound = alarmSound;
        }
        notificationManager.notify(t.getId(), noti);
    }
}
