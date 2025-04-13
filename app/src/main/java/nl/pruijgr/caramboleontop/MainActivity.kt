package nl.pruijgr.caramboleontop

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {

    lateinit var tvText: TextView

    lateinit var mStatusText: String

    lateinit var btnStartStop: Button

    lateinit var btnExit: Button

    lateinit var tvIntervalTime: TextView

    lateinit var tvIntervalRepeats: TextView

    lateinit var toolbar: Toolbar

    @Suppress("PropertyName")
    val TAG="PETERMAIN"


    var bPackageFound: Boolean = false

    var mIntervalTime: Int = 19

    var mIntervalRepeats: Int = 5

    //  ====================================
    //  Permission routine
    //  ====================================

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "request Permission Granted?")
            // Permission Granted
        } else {
            Log.d(TAG, "Not granted !?!?!")
            // Permission Denied / Cancel
        }
    }

    //  ====================================
    //  On starting the app
    //  ====================================


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        var intentToTop: Intent? = null
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE


        mContext=this
        mStatusText=""

        btnStartStop=findViewById(R.id.btnStartStop)
        btnStartStop.setOnClickListener{ startStopPushed() }
        btnStartStop.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                Log.d(TAG,"btnStart lost focus")
                btnStartStop.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_200))
            } else {
                Log.d(TAG,"btnStart got focus")
                btnStartStop.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))

            }
        } 
        btnStartStop.text="Start Service"

        btnExit=findViewById(R.id.btnExit)
        btnExit.setOnClickListener { exitPushed() }
        btnExit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                Log.d(TAG, "btnExit Lost focus")
                // code to execute when EditText loses focus
                btnExit.setBackgroundColor(ContextCompat.getColor(this,R.color.purple_200))
            } else {
                Log.d(TAG,"btnExit got focus")
                btnExit.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))

            }
        }

        tvIntervalTime=findViewById(R.id.tvIntervalTime)
        tvIntervalTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // code to execute when EditText loses focus
                updateUI()
            }
        }
        tvIntervalRepeats=findViewById(R.id.tvintervalHerhalingen)
        tvIntervalRepeats.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // code to execute when EditText loses focus
                updateUI()
            }
        }

        tvText = findViewById(R.id.textView)


        mStatusText="NIET ACTIEF\n"
        tvText.text=mStatusText


        mIntervalTime=Prefs.intervalTime.toInt()
        tvIntervalTime.text=mIntervalTime.toString()

        mIntervalRepeats=Prefs.intervalRepeats.toInt()
        tvIntervalRepeats.text=mIntervalRepeats.toString()

        Prefs.bStopRequest=false

        initToolBar()

        btnStartStop.setFocusableInTouchMode(true)
        btnStartStop.requestFocus()
        btnStartStop.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))



        try {
            val pm: PackageManager = packageManager
            intentToTop = pm.getLaunchIntentForPackage(Constants.CARAMBOLECOMPUTER)

            if (intentToTop == null) {
                mStatusText="${Constants.CARAMBOLECOMPUTER}\nNiet gevonden"
                tvText.text=mStatusText

                btnExit.setFocusableInTouchMode(true)
                btnExit.requestFocus()
                btnExit.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))

                btnStartStop.setBackgroundColor(ContextCompat.getColor(this, R.color.LightGrey))
                Log.d(TAG,mStatusText)
                Log.d(TAG,"Package not found..exit with delay")
                exitWithDelay()
            } else {
                if (pm.isPackageSuspended(Constants.CARAMBOLECOMPUTER)) {
                    Log.d(TAG, "\nPackage ${Constants.CARAMBOLECOMPUTER} Suspendend")
                }
                bPackageFound=true

                if (startOnBoot) {
                    Log.d(TAG, "Starting during boot Process")
                    startIt()
                    updateUI()
                } else {
                    Log.d(TAG,"Not booted")
                    checkNotificationPermission()
                    settingsWaarschuwing()
                    updateUI()
                }
            }
        } catch ( e: Exception  ) {
            mStatusText="ERROR \n$e"
            tvText.text=mStatusText
        }
    }

    fun initToolBar() {
        toolbar = findViewById(R.id.main_toolbar)
        var versie: String  =  BuildConfig.VERSIE

        val buildDate = Calendar.getInstance()
        buildDate.timeInMillis = BuildConfig.BUILD_TIME


        versie = versie + " " + String.format(Locale.ENGLISH, "%02d-%02d-%d %02d:%02d",
            buildDate[Calendar.DAY_OF_MONTH],
            (buildDate[Calendar.MONTH] + 1),
            buildDate[Calendar.YEAR],
            buildDate[Calendar.HOUR_OF_DAY],
            buildDate[Calendar.MINUTE] )



        toolbar.setTitle("Maak \"CaramboleComputer\" zichtbaar        ($versie)")
        setSupportActionBar(toolbar)
    }

    // ==============================================
    // Controleer Notifications Permisson
    // ==============================================

    fun checkNotificationPermission() {

        Log.d(TAG, "Check Notification Persmission")

        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED  ) {
                Log.d(TAG,"Permission Notifcations granted")
                return
            }
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                Log.d(TAG,"Notifcations permission: Should request for rationale")
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d(TAG,"Notification Persmission: Ask for Permission")
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            Log.d(TAG, "Android 12 permission OK")
            // Below Android 13 You don't need to ask for notification permission.
        }

    }

    //  ====================================
    //  Refresh User Interface
    //  ====================================

    @SuppressLint("SetTextI18n")
    fun updateUI() {
        Log.d(TAG,"UPDATEUI")

        if (tvIntervalTime.text.isDigitsOnly() ) {
            mIntervalTime=tvIntervalTime.text.toString().toInt()
            Prefs.intervalTime=mIntervalTime.toString()
            tvIntervalTime.text=mIntervalTime.toString()
        }
        if (tvIntervalRepeats.text.isDigitsOnly() ) {
            mIntervalRepeats=tvIntervalRepeats.text.toString().toInt()
            Prefs.intervalRepeats=mIntervalRepeats.toString()
            tvIntervalRepeats.text=mIntervalRepeats.toString()
        }
        if (bServiceStarted) {
            btnStartStop.text="Stop\nService"
            mStatusText="AKTIEF"
        } else {
            btnStartStop.text="Start\nService"
            mStatusText="NIET AKTIEF"

        }
        showStatus()
    }

    //  ====================================
    //  Start / Stop gekozen
    //  ====================================

    fun startStopPushed() {
        if (!bPackageFound) {
            btnStartStop.visibility=View.INVISIBLE
            updateUI()
            return
        }
        if (!bServiceStarted) {
            mStatusText="ACTIEF\n"
            tvText.text=mStatusText
            Prefs.bStopRequest=false
            startIt()
        } else {
            mStatusText="NIET ACTIEF\n"
            tvText.text=mStatusText
            Log.d(TAG,"Stop Serice request send")
            Prefs.bStopRequest=true
            bServiceStarted=false
            // Log.d(TAG,"Calling stopService()")
            // stopService(intentOfService)
        }
        updateUI()
    }

    //  ====================================
    //  Exit App gekozen
    //  ====================================

    fun exitPushed() {
            Log.d(TAG,"Exiting MAIN and stopping service")
            mStatusText="Stop Request"
            tvText.text=mStatusText
            btnStartStop.visibility=View.INVISIBLE
            btnExit.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))

            Prefs.bStopRequest=true
            if (intentOfService!=null) {
                stopService(intentOfService)
             } else {
                Log.d(TAG,"Exiting with null intentofservicr")
            }
            bServiceStarted=false

            exitWithDelay()
    }

    //  ====================================
    //  Start de Foreground service
    //  ====================================

    @SuppressLint("SetTextI18n")
    fun startIt() {
        intentOfService = Intent(this, MyService::class.java)
        if (intentOfService != null ) {
            intentOfService?.setType("specialuse")
            intentOfService?.putExtra("name", "Carambole On Top Service")
            mStatusText="ACTIEF\n"
            btnStartStop.text="Stop Service"
            bServiceStarted=true
            startForegroundService(intentOfService)
        } else {
            Log.d(TAG,"INTENT IS EMPTY ON START")
            mStatusText="ERROR:\nIntent empty on start"
        }
    }

    //  ====================================
    //  Controleer de status van de Foreground Service
    //  ====================================


    fun showStatus() {
        if (countDownStatus != null) {
          countDownStatus?.cancel()
       }
       if (countDownExit != null) {
           countDownExit?.cancel()
       }


        countDownStatus = object :
            CountDownTimer((mIntervalTime*1000).toLong(), 1000) {
            var countNr=mIntervalTime
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                countNr =countNr - 1
                if (bServiceStarted) {
                    tvText.text="$mStatusText \n" + MyService.serviceMessage + "\nVolgende controle in\n$countNr\nseconden"
                } else{
                    cancel()
                }
            }

            override fun onFinish() {
                Log.d(TAG,"MAINLOOP Done Waiting\n")
            }
        }.start()


    }

    //  ====================================
    //  Beeindig de app met vertraging
    //  ====================================


    fun exitWithDelay() {
        bExiting=true
        if (countDownStatus!=null) {
            countDownStatus?.cancel()
         }
        if (countDownExit!=null) {
            countDownExit?.cancel()
        }
        Log.d(TAG,"Now exiting with Delay")

        btnStartStop.visibility=View.INVISIBLE

        countDownExit = object : CountDownTimer(10000, 1000) {

            // Callback function, fired on regular interval
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG,"Before setting message")

                var localMessage: String ="${mStatusText}\nExit in:\n" + millisUntilFinished / 1000 + "\nseconds\n"
                Log.d(TAG,"Inside delay counting")
                tvText.text=localMessage
            }

            override fun onFinish() {
                Log.d(TAG,"Finishing")
               finish()
            }
        }.start()
    }

    //  ====================================
    //  Waarschuwing "Display Over Apps"
    //  ====================================

    fun settingsWaarschuwing() {

        Toast.makeText(mContext, "Controleer Apps instelling: \nDisplay Over Other Apps !!!",
            Toast.LENGTH_SHORT).show()
    }



    override fun onResume() {
        Log.d(TAG,"On Resume, servcestatus=$bServiceStarted")
        if (!bExiting) updateUI()
        super.onResume()
    }

    //  =========================================
    //  Persistent and Shared data with MyService
    //  =========================================

    companion object {

        @SuppressLint("StaticFieldLeak")
        var mContext: Context? = null
        var bServiceStarted: Boolean = false
        var intentOfService: Intent?  = null
        var bExiting: Boolean = false
        var startOnBoot : Boolean = false
        var countDownStatus: CountDownTimer? = null
        var countDownExit: CountDownTimer? = null
    }



}


