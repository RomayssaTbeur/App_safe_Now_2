package com.example.safe_now_2.unity;
import com.unity3d.player.UnityPlayerActivity;

import android.os.Bundle;


public class UnityActivity extends UnityPlayerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String scenario = getIntent().getStringExtra("scenario");

        new android.os.Handler().postDelayed(() -> {
            sendToUnity(scenario);
        }, 1000); // attendre 1 seconde
    }

    private void sendToUnity(String scenario) {
        // Unity reçoit un message via GameObject
        com.unity3d.player.UnityPlayer.UnitySendMessage(
                "GameManager",     // nom GameObject Unity
                "SetScenario",     // méthode Unity
                scenario            // FIRE / EARTHQUAKE
        );
    }

//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        unityPlayer.pause();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        unityPlayer.resume();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unityPlayer.destroy();
//    }
}


//package com.example.safe_now_2.unity;
//
//import android.os.Bundle;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.unity3d.player.UnityPlayer;
//
//public class UnityActivity extends AppCompatActivity {
//
//    private UnityPlayer unityPlayer;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        unityPlayer = new UnityPlayer(this);
//        setContentView(unityPlayer);
//
//        String scenario = getIntent().getStringExtra("scenario");
//
//        sendToUnity(scenario);
//    }
//
//    private void sendToUnity(String scenario) {
//        UnityPlayer.UnitySendMessage(
//                "GameManager",
//                "SetScenario",
//                scenario
//        );
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        unityPlayer.pause();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        unityPlayer.resume();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unityPlayer.destroy();
//    }
//}