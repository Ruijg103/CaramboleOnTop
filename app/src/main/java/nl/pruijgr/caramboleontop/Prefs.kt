package nl.pruijgr.caramboleontop

import android.content.Context
import android.content.SharedPreferences



// @SuppressLint("StaticFieldLeak")
object Prefs {

    var initOk: Boolean = false

    var bStopRequest: Boolean = false

    val context: Context?
        //
        get() {
            return MainActivity.mContext
        }

    private lateinit var mPreferences: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor


    private fun prefsInit() {
        if (initOk) return


        mPreferences = context?.getSharedPreferences(Constants.SETTINGS_PREFERENCE, Context.MODE_PRIVATE)!!


        edit = mPreferences.edit()
        edit.apply()
        initOk = true
    }

    var intervalTime: String
        get() {
            prefsInit()
            return mPreferences.getString(Constants.SETTINGS_INTERVALTIME,"20").toString()
        }
        set(s) {
            prefsInit()
            edit.putString(Constants.SETTINGS_INTERVALTIME, s)
            edit.apply()
        }

    var intervalRepeats: String
        get() {
            prefsInit()
        return mPreferences.getString(Constants.SETTINGS_INTERVALHERHALINGEN,"15").toString()
        }
        set(s) {
            prefsInit()
            edit.putString(Constants.SETTINGS_INTERVALHERHALINGEN, s)
            edit.apply()
        }








} // End of the class


