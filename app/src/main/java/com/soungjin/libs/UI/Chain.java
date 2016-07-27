package com.soungjin.libs.UI;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.soungjin.libs.R;

/**
 * Created by 류 on 2016-04-30.
 */
public class Chain extends LinearLayout implements View.OnClickListener{


    public static final int MAX_CHAIN = 7;
    Context context;
    Button chain[];
    int index_chain = 0;//0~7
    public int length = 0;

    @Override
    public void onClick(View v) {
        selectChain(v.getId());
        if (onChainClick != null)
            onChainClick.onChainClick(v, v.getId());
    }

    public interface OnChainClickListener{
        void onChainClick(View view, int id);
    }

    OnChainClickListener onChainClick;

    public Chain(Context context) {
        this(context, null);
    }

    public Chain(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        Create();
    }

    public Chain(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        Create();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Chain(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        Create();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec/8, heightMeasureSpec);
        length = heightMeasureSpec/8;
    }

    void Create(){
        chain = new Button[8];
        setOrientation(VERTICAL);
        setBackgroundColor(getResources().getColor(R.color.colorBackground));
        for (int i = 0; i < chain.length; i++) {
            chain[i] = new Button(context);
            chain[i].setBackground(context.getResources().getDrawable(R.drawable.chain_button_space));
            chain[i].setOnClickListener(this);
            chain[i].setId(i);
        }
    }

    public void setOnChainClick(OnChainClickListener onChainClick) {
        this.onChainClick = onChainClick;
    }

    public void setMaxChain(int max){//0~7
        if ( max >= 8)
            return;
        if (max == MAX_CHAIN)
            return;
        if (MAX_CHAIN > max){//제거
            if (index_chain> max)
                selectChain(max);
            for (int x = MAX_CHAIN; x > max; x--){
                chain[x].setOnClickListener(null);
                removeView(chain[x]);
            }
        }else{//추가
            for (int x = max; x < MAX_CHAIN; x++){
                addView(chain[x], length, length);
                chain[x].setOnClickListener(this);
            }
        }
    }

    public void setLength(int length) {
        this.length = length/8;
    }

    public int getChain(){
        return index_chain;
    }

    public void selectChain(int index){
        if (index > MAX_CHAIN)
            return;
        if (index == index_chain)
            return;
        chain[index_chain].setBackground(context.getResources().getDrawable(R.drawable.chain_button_space));
        chain[index].setBackground(context.getResources().getDrawable(R.drawable.chain_button_pool));
        index_chain = index;
    }

    public void init(){
        if (length == 0)
            return;
        if (indexOfChild(chain[0]) != -1)
            return;
        for (int i = 0; i < chain.length; i++)
                addView(chain[i], this.length, this.length);
        chain[0].setBackground(context.getResources().getDrawable(R.drawable.chain_button_pool));
        selectChain(0);
    }
}
