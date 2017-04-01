package com.jack.sectorview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jackzhous on 2017/4/1.
 */

public class SectorView extends View{
    private static final String TAG = "j_sector_tag";

    private Paint mPaint;
    private Paint                       mTextPaint;
    private int                         mViewWidth;
    private int[]                       mColorPerStatus;


    private static int                  mDefaultLostColor = Color.parseColor("#9370DB");
    private static int                  mDefaultOpenColor = Color.parseColor("#CD6090");
    private static int                  mDefaultNormalColor = Color.parseColor("#98FB98");
    private static int                  mDefaultLowColor = Color.parseColor("#EEDC82");
    private static String[]             mStatusInfo;

    private CirclePoint                 mCircle;
    private static final int                         mHorizonLineLength = 150;
    private static final int                         mSlashLineLength   = 50;           //直线和斜线添加的长度

    private int[]                       mAngelePerStatus;                  //每个状态占用的数量,状态分别是失联、打开、正常、低电
    private int[]                       mAngeleSort;                       //每种角度大小排序排第几位，比如失联状态的数据占第一位


    static {
        mDefaultLostColor = Color.parseColor("#9370DB");
        mDefaultNormalColor = Color.parseColor("#98FB98");
        mDefaultOpenColor = Color.parseColor("#CD6090");
        mDefaultLowColor = Color.parseColor("#EEDC82");

        mStatusInfo = new String[]{"状态0", "状态1", "状态2", "状态3"};
    }


    public SectorView(Context context) {
        this(context, null, 0);
    }

    public SectorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    /**
     * 初始化
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs){

        mCircle = new CirclePoint();
        mAngelePerStatus = new int[4];
        mAngeleSort = new int[4];
        mColorPerStatus = new int[4];

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(40);
        mTextPaint.setColor(Color.WHITE);

        if(attrs == null){
            mColorPerStatus[0] = mDefaultLostColor;
            mColorPerStatus[1] = mDefaultOpenColor;
            mColorPerStatus[2] = mDefaultNormalColor;
            mColorPerStatus[3] = mDefaultLowColor;
            return;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.sector);
        mColorPerStatus[0] = typedArray.getColor(R.styleable.sector_lost_status, mDefaultLostColor);
        mColorPerStatus[1] = typedArray.getColor(R.styleable.sector_open_status, mDefaultOpenColor);
        mColorPerStatus[2] = typedArray.getColor(R.styleable.sector_normal_status, mDefaultNormalColor);
        mColorPerStatus[3] = typedArray.getColor(R.styleable.sector_lowpower_status, mDefaultLowColor);
        typedArray.recycle();
    }


    public void setmAngelePerStatus(ArrayList<Integer> list){
        cacluteAngle(list);
    }

    /**
     * 计算每个状态量所占用的角度
     * @param list
     */
    private void cacluteAngle(ArrayList<Integer> list){
        float maxNum = 0;
        for(int i = 0; i < list.size(); i++){
            maxNum = maxNum + list.get(i);
        }

        float angle;
        for(int i = 0; i < list.size(); i++){
            angle = list.get(i) * 360  / maxNum;
            Log.i(TAG, "angle --- " + angle);
            //处于0~2度之间的值设置为2度  不然画不出圆弧和斜线，这里折磨多以后会导致总角度和大于360度，后面排序
            //要在减掉多余的度数
            if(angle < 2 && angle > 0){
                angle = 2;
            }
            mAngelePerStatus[i] = (int) angle;
            Log.i(TAG, "angle " + mAngelePerStatus[i]);
        }

        sortAngele();
    }

    /**
     * 排序每种状态，看每种状态是第几名
     */
    private void sortAngele(){

        int length = mAngelePerStatus.length;
        int remainAngle = angleBuChang(mAngelePerStatus) - 360;         //角度总和的差

        int[] copyAngle = Arrays.copyOf(mAngelePerStatus, length);
        //排序
        int temp, totalAngle = 0;
        for(int i = 0; i < length; i++){

            totalAngle += mAngelePerStatus[i];
            for(int j = i + 1; j < length ; j++){
                if(copyAngle[i] > copyAngle[j]){
                    temp = copyAngle[i];
                    copyAngle[i] = copyAngle[j];
                    copyAngle[j] = temp;
                }
            }
        }

        //给每个状态附上它排第几名
        for(int i = 0; i < length; i++){
            for(int j = 0; j < copyAngle.length; j++){

                if(mAngelePerStatus[i] == copyAngle[j]){
                    if(j == length - 1){                    //最大的角度来补偿
                        mAngelePerStatus[i] -= remainAngle;
                    }
                    mAngeleSort[i] = j;
                    Log.i(TAG, "angleSort " + j);
                    continue;
                }
            }
        }

    }

    /**
     * 角度小于5度的情况，并且相邻角度相差不大的要相邻状态其中一个要加一个角度,
     * 否则写字时文字会重合在一起
     * @param array
     */
    private int angleBuChang(int[] array){

        int length = array.length - 1;

        if(length <= 2){
            return 360;
        }
        int angle0, angle1;
        for(int i = 0; i< length + 1; i++){
            angle0 = array[i];
            if(i + 1 > length){
                angle1 = array[0];
            }else{
                angle1 = array[i+1];
            }

            int angleRemain = Math.abs(angle1 - angle0);
            if(angle0 < 5 && angleRemain < 5){      //这两个加数就是调整角度差的
                if(i+1 > length){
                    array[0] += angleRemain + 10;
                }else {
                    array[i+1] += angleRemain + 15;
                }
            }
        }

        int totalAngle = 0;
        for(int i = 0; i <  length + 1; i++){
            totalAngle += array[i];
        }

        return  totalAngle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //drawCycle(canvas);
        drawSector(canvas);
        drawLineAndText(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int viewHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        mViewWidth = viewWidth;
        mCircle.x = viewWidth / 2;
        mCircle.y = viewHeight / 2;
        mCircle.radius = mCircle.x > mCircle.y ? mCircle.y : mCircle.x;

        int parts = (mAngelePerStatus.length <= 0 ? 4 : mAngelePerStatus.length) + 2;
        int radiusInterval = mCircle.radius / parts;                    //要分成几个半径圆

        mCircle.radiusLevel[0] = (int) (radiusInterval * 1.8);
        mCircle.radiusLevel[1] = (int) (radiusInterval * 2.2);
        mCircle.radiusLevel[2] = (int) (radiusInterval * 2.5);
        mCircle.radiusLevel[3] = (int) (radiusInterval * 3);
        Log.i(TAG, "view宽高属性: radius " + mCircle.radius + " length " + mAngelePerStatus.length);
    }

    /**
     * 画基础的扇形，必须先画扇形后面再画横线斜线等
     * @param canvas
     */
    private void drawSector(Canvas canvas){

        RectF rectF;
        int startAngle = 0;
        for(int i = 0; i < mAngelePerStatus.length; i++){
            if(mAngelePerStatus[i] <= 0){                      //小于等于0说明这部分没有的
                continue;
            }
            int sweep = mAngelePerStatus[i];
            if(i == mAngelePerStatus.length - 1) {
                sweep = 360 - startAngle;
            }
            int currentAngleLevel = mAngeleSort[i];
            int currentAngleRadius = mCircle.radiusLevel[currentAngleLevel];
            int startX = mCircle.x - currentAngleRadius;
            int startY = mCircle.y - currentAngleRadius;
            int endX   = mCircle.x + currentAngleRadius;
            int endY   = mCircle.y + currentAngleRadius;
            rectF = new RectF(startX, startY, endX, endY);
            mPaint.setColor(mColorPerStatus[i]);
            canvas.drawArc(rectF, startAngle, sweep, true, mPaint);

            startAngle += mAngelePerStatus[i];
        }


    }

    /**
     * 画线和字符
     * @param canvas
     */
    private void drawLineAndText(Canvas canvas){

        int baseAngle = 0;
        int src;

        for(int i = 0; i < mAngelePerStatus.length; i++){
            src = canvas.save();

            if(mAngelePerStatus[i] <= 0){
                continue;
            }

            int currentAngleLevel = mAngeleSort[i];
            int rotateAngle =  baseAngle + mAngelePerStatus[i] / 2;             //需要旋转的角度
            //刚好在坐标轴上的情况，画斜线会变成垂直水平的线  不好看  稍微偏移一下
            if(rotateAngle%90 == 0){
                rotateAngle = rotateAngle + 3;
            }
            int slashLineEndX = mCircle.x + mCircle.radiusLevel[currentAngleLevel] + mSlashLineLength;


            canvas.rotate(rotateAngle, mCircle.x, mCircle.y);
            mPaint.setColor(mColorPerStatus[i]);
            canvas.drawLine(mCircle.x, mCircle.y, slashLineEndX, mCircle.y, mPaint);        //画斜线
            canvas.rotate(-rotateAngle, slashLineEndX, mCircle.y);


            int horizonLineEndX;
            int textStartY;
            int textStartX;
            //画水平直线时要考虑在左边还是右边，两边起点和终点是有区别的
            if(rotateAngle > 90 && rotateAngle < 270){

                horizonLineEndX = mCircle.x + mCircle.radiusLevel[currentAngleLevel] - mHorizonLineLength;
                if(horizonLineEndX < 0){                                                 //防止其超出左边边界
                    horizonLineEndX = 0;
                }
                textStartX = horizonLineEndX;

            }else{

                horizonLineEndX = mCircle.x + mCircle.radiusLevel[currentAngleLevel] + mHorizonLineLength;
                if(horizonLineEndX > mViewWidth){                                       //防止超出右边界
                    horizonLineEndX = mViewWidth;
                }
                textStartX = slashLineEndX + 5;
            }
            Log.i(TAG, "horizontal start " + slashLineEndX + " " + horizonLineEndX);
            canvas.drawLine(slashLineEndX, mCircle.y, horizonLineEndX, mCircle.y, mPaint);  //画直线

            //画字时考虑上半圆和瞎下半圆时，上半圆字在上面，下半圆字在下面
            if(rotateAngle < 180){
                textStartY = mCircle.y + 40;

            }else{
                textStartY = mCircle.y - 20;
            }

            canvas.drawText(mStatusInfo[i], textStartX, textStartY, mTextPaint);
            canvas.restoreToCount(src);
            baseAngle += mAngelePerStatus[i];
            Log.i(TAG, "baseAngle " + baseAngle + " rotateAngle " + rotateAngle);

        }

    }

    /**
     * 圆心类，标注圆心得位置
     */
    private class CirclePoint{
        int x;
        int y;
        int radius;
        int[] radiusLevel = new int[4];                 //四个半径等级
    }
}
