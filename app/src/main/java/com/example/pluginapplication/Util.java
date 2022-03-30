package com.example.pluginapplication;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Fuzhicheng on 2022/3/28
 */
public class Util {
    private Context mContext;

    public Util(Context context) {
        mContext = context;
    }

    public void showAsmToast(){
        Toast.makeText(mContext,"来自Asm toast",Toast.LENGTH_LONG);
    }
}
