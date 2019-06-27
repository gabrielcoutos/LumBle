package br.com.gabrielcouto.ledtec.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;


public abstract  class Utils {

    public static final int REQUEST_LOCATION_FINE =1;
    public static final int REQUEST_ENABLE_BT =2;


    // verifica senha
    public static boolean verifyPassword(Activity activity, String senhaInserida){
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        String senhaSalva = preferences.getString(Const.SHARED_PREFERENCES_SENHA,"");
        if(senhaSalva.isEmpty()){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Const.SHARED_PREFERENCES_SENHA,senhaInserida);
            editor.commit();
            return true;
        }else{
            if(senhaInserida.equals(senhaSalva)){
                return true;
            }else{
                return false;
            }
        }
    }

}