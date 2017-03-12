package com.donkingliang.labels;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class LabelsView extends ViewGroup {

    private Context mContext;

    private int mTextColor = Color.BLACK;
    private float mTextSize = 42.0f;
    private int mLabelBgRes;
    private int mTextPaddingLeft;
    private int mTextPaddingTop;
    private int mTextPaddingRight;
    private int mTextPaddingBottom;
    private int mWordMargin;
    private int mLineMargin;

    private OnLabelClickListener mListener;

    public LabelsView(Context context) {
        super(context);
        mContext = context;
    }

    public LabelsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        getAttrs(context, attrs);
    }

    public LabelsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getAttrs(context, attrs);
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.labels_view);

            mTextColor = mTypedArray.getColor(R.styleable.labels_view_labelTextColor, Color.BLACK);
            mTextSize = mTypedArray.getDimension(R.styleable.labels_view_labelTextSize, 42.0f);
            mTextPaddingLeft = mTypedArray.getDimensionPixelOffset(
                    R.styleable.labels_view_labelTextPaddingLeft, 0);
            mTextPaddingTop = mTypedArray.getDimensionPixelOffset(
                    R.styleable.labels_view_labelTextPaddingTop, 0);
            mTextPaddingRight = mTypedArray.getDimensionPixelOffset(
                    R.styleable.labels_view_labelTextPaddingRight, 0);
            mTextPaddingBottom = mTypedArray.getDimensionPixelOffset(
                    R.styleable.labels_view_labelTextPaddingBottom, 0);
            mLineMargin = mTypedArray.getDimensionPixelOffset(R.styleable.labels_view_lineMargin, 0);
            mWordMargin = mTypedArray.getDimensionPixelOffset(R.styleable.labels_view_wordMargin, 0);
            mLabelBgRes = mTypedArray.getResourceId(R.styleable.labels_view_labelBackground, -1);
            mTypedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int count = getChildCount();
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();

        int contentHeight = 0; //记录内容的高度
        int lineWidth = 0; //记录行的宽度
        int maxLineWidth = 0; //记录最宽的行宽
        int maxItemHeight = 0; //记录一行中item高度最大的高度
        boolean begin = true; //是否是行的开头

        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);

            if (maxWidth < lineWidth + view.getMeasuredWidth()) {
                contentHeight += mLineMargin;
                contentHeight += maxItemHeight;
                maxItemHeight = 0;
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                lineWidth = 0;
                begin = true;
            }
            maxItemHeight = Math.max(maxItemHeight, view.getMeasuredHeight());
            if(!begin) {
                lineWidth += mWordMargin;
            }else {
                begin = false;
            }
            lineWidth += view.getMeasuredWidth();
        }

        contentHeight += maxItemHeight;
        maxLineWidth = Math.max(maxLineWidth, lineWidth);

        setMeasuredDimension(measureWidth(widthMeasureSpec,maxLineWidth),
                measureHeight(heightMeasureSpec, contentHeight));

    }

    private int measureWidth(int measureSpec, int contentWidth) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = contentWidth + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumWidth());
        return result;
    }

    private int measureHeight(int measureSpec, int contentHeight) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = contentHeight + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumHeight());
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int x = getPaddingLeft();
        int y = getPaddingTop();

        int contentWidth = right - left;
        int maxItemHeight = 0;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);

            if (contentWidth < x + view.getMeasuredWidth()) {
                x = getPaddingLeft();
                y += mLineMargin;
                y += maxItemHeight;
                maxItemHeight = 0;
            }
            view.layout(x, y, x + view.getMeasuredWidth(), y + view.getMeasuredHeight());
            x += view.getMeasuredWidth();
            x += mWordMargin;
            maxItemHeight = Math.max(maxItemHeight, view.getMeasuredHeight());
        }
    }

    public void setLabels(ArrayList<String> labels) {
        removeAllViews();
        if (labels != null) {
            int size = labels.size();
            for (int i = 0; i < size; i++) {
                addLabel(labels.get(i), i);
            }
        }
    }

    private void addLabel(String text, int position) {
        TextView label = new TextView(mContext);
        label.setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        label.setTextColor(mTextColor);
        label.setText(text);
        if (mLabelBgRes != 0) {
            label.setBackgroundResource(mLabelBgRes);
        }
        label.setTag(position);
        label.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               if(mListener != null){
                   mListener.onLabelClick((TextView) v,(int)v.getTag());
               }
            }
        });
        addView(label);
    }

    public void setLabelBackgroundResource(int res) {
        if (mLabelBgRes != res) {
            mLabelBgRes = res;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                TextView label = (TextView) getChildAt(i);
                label.setBackgroundResource(res);
            }
        }
    }

    public void setLabelTextPadding(int left, int top, int right, int bottom) {
        if (mTextPaddingLeft != left || mTextPaddingTop != top
                || mTextPaddingRight != right || mTextPaddingBottom != bottom) {
            mTextPaddingLeft = left;
            mTextPaddingTop = top;
            mTextPaddingRight = right;
            mTextPaddingBottom = bottom;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                TextView label = (TextView) getChildAt(i);
                label.setPadding(left, top, right, bottom);
            }
        }
    }

    public void setLabelTextSize(int size) {
        if (mTextSize != size) {
            mTextSize = size;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                TextView label = (TextView) getChildAt(i);
                label.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            }
        }
    }

    public void setLabelTextColor(int color) {
        if (mTextColor != color) {
            mTextColor = color;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                TextView label = (TextView) getChildAt(i);
                label.setTextColor(color);
            }
        }
    }

    public void setLineMargin(int margin) {
        if (mLineMargin != margin) {
            mLineMargin = margin;
            requestLayout();
        }
    }

    public void setWordMargin(int margin) {
        if (mWordMargin != margin) {
            mWordMargin = margin;
            requestLayout();
        }
    }

    public void setOnLabelClickListener(OnLabelClickListener l) {
        mListener = l;
    }

    public interface OnLabelClickListener {
        void onLabelClick(TextView label, int position);
    }

}
