@file:Suppress("PropertyName")

package nl.pruijgr.caramboleontop

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep


class MyService : Service() {



    private val sCHANNELID = "ServiceChannel"
    val TAG="PETERSERVICE"

    //  ====================================
    // ON  create of the service
    //  ====================================

    override fun onCreate() {
        super.onCreate()
        serviceMessage=""
        Log.d(TAG,"== ON CREATE SERVICE==")
    }

    //  ====================================
    //  On actual start request
    //  ====================================

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var name = ""

        if (intent != null) {
            name = intent.getStringExtra("name").toString()
            Log.d(TAG,"==ONSTARTCOMMAND name=$name")
        } else {
            if (name == "") {
                name = "No Intent"
                Log.d(TAG, "==ONSTARTCOMMEND WITHOUT INTENT")
            }
        }

        try {
            val notification = createNotification()
            isServiceStarted=true
            startForeground(1001, notification)

        } catch (e: Exception) {
            Log.d(TAG,"START ERROR:$e")
            // Toast.makeText(MainActivity.mContext,"ERROR\n$e", Toast.LENGTH_LONG).show()
            serviceMessage="ERROR\n $e"

        }

        serviceMessage=""

        Log.d(TAG,"BEFORE GLOBALSCOPE START")

        sleep(10)   // Wait for a while so startup is really ready !!

        GlobalScope.launch(Dispatchers.Main) {
            Log.d(TAG,"Inside GlobalScope: status=$isServiceStarted" )
            var loopCount = Prefs.intervalRepeats.toInt()

            try {
                val pm: PackageManager = getPackageManager()
                myIntentToStart = pm.getLaunchIntentForPackage(Constants.CARAMBOLECOMPUTER)

                if (myIntentToStart == null) {
                    Log.d(TAG,"Inside Launcher, " + Constants.CARAMBOLECOMPUTER + " not found")
                   }
               } catch ( e: Exception  ) {
                Log.d(TAG,"Exception getting intent")
                serviceMessage="ERROR Getting intent\n$e"
            }
            while (loopCount> 0) {

                launch(Dispatchers.IO) {
                    serviceMessage="Resterende herhalingen: $loopCount"
                    loopCount=loopCount-1

                    if (Prefs.bStopRequest) {
                        Log.d(TAG, "RECEIVED STOP REQUEST")
                        serviceMessage="Service Stopped"
                        loopCount=0
                    } else {
                        startCarambole()
                    }

                    Log.d(TAG,"LOOP COUNT =$loopCount")
                }
                delay(1 * Prefs.intervalTime.toLong() * 1000)
            }
            Log.d(TAG,"GLOBAL: Exiting service loop")
            stopSelf()
            //log("End of the loop for the service")
        }
        Log.d(TAG,"== EXITING SERVICE START-NOT-STICKY")

        return START_NOT_STICKY

     }

    //  ======================================================
    //  Create Notification to ensure service keeps running
    //  ======================================================

    private fun createNotification(): Notification {

        val serviceChannel = NotificationChannel(sCHANNELID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)



        return NotificationCompat.Builder(this, sCHANNELID)
            .setContentTitle("Carambole on Top")
            .setContentText("Zet CaramboleComputer (weer) op de voorgrond")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your own icon
            .setContentIntent(pendingIntent)
            .build()
    }



    // execution of the service will
    // stop on calling this method
    override fun onDestroy() {
        Log.d(TAG,"== ON DESTROY ==")
        super.onDestroy()


    }

    //  =========================================
    //  Bring carambolecomputer app to the front
    //  =========================================

    fun startCarambole() {
        Log.d(TAG,"---Starting CARAMBOLECOMPUTER NOW with Intent: $myIntentToStart")
        if (myIntentToStart!=null) {
            myIntentToStart?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            myIntentToStart!!.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            myIntentToStart?.setAction(Intent.ACTION_MAIN)
            myIntentToStart?.addCategory(Intent.CATEGORY_LAUNCHER)
            startActivity(myIntentToStart)
         } else {
            Log.d(TAG,"During start no Intent !!")
       }

    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG,"OnBind...")
        return null
    }

    //  ========================================
    //  Persistent data shared with MainActivity
    //  ========================================

    companion object {

        var serviceMessage: String = ""

        var isServiceStarted: Boolean = false
        var myIntentToStart: Intent? = null

    }
}
