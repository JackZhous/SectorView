package com.jack.sectorview;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private SectorView                  mSectorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //模拟数据  index 0~3分别对应扇形图的 状态0~3
        int[] data = new int[]{1111, 2222, 7431, 5330};

        mSectorView = (SectorView) findViewById(R.id.sector);
        mSectorView.setmAngelePerStatus(data);
    }
}
