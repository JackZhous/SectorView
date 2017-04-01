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
        ArrayList<Integer> testData = new ArrayList<>();
        testData.add(1511);
        testData.add(1156);
        testData.add(7541);
        testData.add(5330);

        mSectorView = (SectorView) findViewById(R.id.sector);
        mSectorView.setmAngelePerStatus(testData);
    }
}
