package droid.demos.com.camerapermissions.views;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import droid.demos.com.camerapermissions.R;
import droid.demos.com.camerapermissions.fragments.CameraFragment;

public class MainActivity extends AppCompatActivity {

    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showCameraFragment();
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 1) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    private void showCameraFragment() {
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();

        CameraFragment cameraFragment = new CameraFragment();

        transaction.addToBackStack(CameraFragment.TAG);
        transaction.add(R.id.conteinerFragments, cameraFragment, CameraFragment.TAG).commit();
    }

}
