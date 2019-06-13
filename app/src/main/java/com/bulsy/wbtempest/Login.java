package com.bulsy.wbtempest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.serindlabs.pocketid.sdk.PocketIDSdk;
import com.serindlabs.pocketid.sdk.base.PocketIDListener;
import com.serindlabs.pocketid.sdk.constants.PocketIDEventType;
import com.serindlabs.pocketid.sdk.constants.PocketIDRequestCode;

public class Login extends AppCompatActivity implements PocketIDListener {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//    }


    /**
     * PocketID methods
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
        PocketIDSdk.getInstance().initialize(this, "nh(DyBAlOlVWugK_ezmqN!qEHBiKYVF)");
        super.onCreate(savedInstanceState);
        PocketIDSdk.getInstance().registerListener(this);
    }

    @Override
    public void onEvent(String event, Bundle data) {
        switch (event)  {
            case PocketIDEventType.EVENT_LOGIN_SUCCESS:
                System.out.println("DDDDDDDD");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        PocketIDSdk.getInstance().unregisterListener(this);
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        System.out.println("1123123144224!");

        if (requestCode == PocketIDRequestCode.AUTHENTICATION && resultCode == RESULT_OK) {
            // code here
            System.out.println("AAAAAA!");


//            Snackbar.make(findViewById(R.id.pocketid), R.string.app_name,
//                    Snackbar.LENGTH_SHORT)
//                    .show();

            Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(startIntent);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
            System.err.println("Wrong username or password!" + requestCode + "-----" + resultCode + "-------" + data);
        }
    }
}
