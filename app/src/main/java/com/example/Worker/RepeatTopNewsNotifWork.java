package com.example.Worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.Api.ApiClient;
import com.example.Api.ApiInterface;
import com.example.Models.Article;
import com.example.Models.Constants;
import com.example.Models.NewsResponse;
import com.example.fastnews.PendingNotificationActivity;
import com.example.fastnews.R;
import com.example.fastnews.Util;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.constraint.Constraints.TAG;

public class RepeatTopNewsNotifWork extends Worker {

    public RepeatTopNewsNotifWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: Started the request");
        final boolean[] flag = {false};
        ApiInterface request = ApiClient.getClient().create(ApiInterface.class);
        Call<NewsResponse> call = request.getTopCountryHeadlines(Util.getPrefCountry(getApplicationContext()), Constants.API_KEY);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {

                if (response.isSuccessful() && response.body().getArticles() != null) {

                    Article article = response.body().getArticles().get(0);
                    showNotification(article, "topnews");
                    flag[0] = true;
                    Log.d(TAG, "onResponse: Getting result");
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), "ERROR IN GETTING RESPONSE", Toast.LENGTH_SHORT).show();
            }
        });

        if (flag[0]) {
            setupRepeatTask();
            return Result.success();
        }
        return Result.retry();
    }

    void showNotification(Article article, String CHANNEL_ID) {
        NotificationManager manager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(getApplicationContext(), PendingNotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("data", article);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(article.getTitle())
                .setContentText(article.getDescription())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);


        manager.notify(1, builder.build());

        Log.d(TAG, "showNotification: Showing the notification");
    }


    void setupRepeatTask() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();

        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        dueDate.set(Calendar.HOUR, 8);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }
        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        OneTimeWorkRequest oneTimeWorkRequest
                = new OneTimeWorkRequest
                .Builder(RepeatTopNewsNotifWork.class)
                .setConstraints(constraints)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS).build();

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);
    }
}
