package com.bulsy.wbtempest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.serindlabs.pocketid.sdk.PocketIDSdk;
import com.serindlabs.pocketid.sdk.base.PocketIDListener;
import com.serindlabs.pocketid.sdk.constants.PocketIDEventType;
import com.serindlabs.pocketid.sdk.constants.PocketIDRequestCode;
import com.serindlabs.pocketid.sdk.constants.PocketIDTheme;

public class Login extends AppCompatActivity implements PocketIDListener {

    /**
     * PocketID methods
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
        PocketIDSdk.getInstance()
//                .setInProd(false)
                .initialize(this, "nh(DyBAlOlVWugK_ezmqN!qEHBiKYVF)");
        super.onCreate(savedInstanceState);
        PocketIDSdk.getInstance().registerListener(this);

        // Set icon, name and theme
        PocketIDSdk.getInstance()
                .customize()
                .setLogo(R.drawable.icon)
                .setAppName(getString(R.string.app_name))
                .setTheme(PocketIDTheme.DARK);
    }

    @Override
    public void onEvent(String event, Bundle data) {
        switch (event)  {
            case PocketIDEventType.EVENT_LOGIN_SUCCESS:
                System.out.println("Authenticated");
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
        if (requestCode == PocketIDRequestCode.AUTHENTICATION && resultCode == RESULT_OK) {
            // code here
            System.out.println("Authenticated!");


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
