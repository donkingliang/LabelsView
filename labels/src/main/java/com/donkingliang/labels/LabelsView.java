package com.donkingliang.labels;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class LabelsView extends ViewGroup implements View.OnClickListener {

    private Context mContext;

    private ColorStateList mTextColor;
    private float mTextSize;
    private int mLabelBgResId;
    private int mTextPaddingLeft;
    private int mTextPaddingTop;
    private int mTextPaddingRight;
    private int mTextPaddingBottom;
    private int mWordMargin;
    private int mLineMargin;
    private SelectType mSelectType;
    private int mMaxSelect;

    private ArrayList<String> mLabels = new ArrayList<>();
    //保存选中的label的位置
    private ArrayList<Integer> mSelectLabels = new ArrayList<>();

    private OnLabelClickListener mLabelClickListener;
    private OnLabelSelectChangeListener mLabelSelectChangeListener;

    /**
     * Label的选择类型
     */
    public enum SelectType {
        //不可选中，也不响应选中事件回调。（默认）
        NONE(1),
        //单选
        SINGLE(2),
        //多选
        MULTI(3);

        int value;

        SelectType(int value) {
            this.value = value;
        }

        static SelectType get(int value) {
            switch (value) {
                case 1:
                    return NONE;
                case 2:
                    return SINGLE;
                case 3:
                    return MULTI;
            }
            return NONE;
        }
    }

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
            int type = mTypedArray.getInt(R.styleable.labels_view_selectType, 1);
            mSelectType = SelectType.get(type);

            mMaxSelect = mTypedArray.getInteger(R.styleable.labels_view_maxSelect, 0);
            mTextColor = mTypedArray.getColorStateList(R.styleable.labels_view_labelTextColor);
            mTextSize = mTypedArray.getDimension(R.styleable.labels_view_labelTextSize,
                    sp2px(context, 14));
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
            mLabelBgResId = mTypedArray.getResourceId(R.styleable.labels_view_labelBackground, 0);
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
            if (!begin) {
                lineWidth += mWordMargin;
            } else {
                begin = false;
            }
            lineWidth += view.getMeasuredWidth();
        }

        contentHeight += maxItemHeight;
        maxLineWidth = Math.max(maxLineWidth, lineWidth);

        setMeasuredDimension(measureWidth(widthMeasureSpec, maxLineWidth),
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

            if (contentWidth < x + view.getMeasuredWidth() + getPaddingRight()) {
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

    /*  用于保存View的信息的key  */
    private static final String KEY_SUPER_STATE = "key_super_state";
    private static final String KEY_TEXT_COLOR_STATE = "key_text_color_state";
    private static final String KEY_TEXT_SIZE_STATE = "key_text_size_state";
    private static final String KEY_BG_RES_ID_STATE = "key_bg_res_id_state";
    private static final String KEY_PADDING_STATE = "key_padding_state";
    private static final String KEY_WORD_MARGIN_STATE = "key_word_margin_state";
    private static final String KEY_LINE_MARGIN_STATE = "key_line_margin_state";
    private static final String KEY_SELECT_TYPE_STATE = "key_select_type_state";
    private static final String KEY_MAX_SELECT_STATE = "key_max_select_state";
    private static final String KEY_LABELS_STATE = "key_labels_state";
    private static final String KEY_SELECT_LABELS_STATE = "key_select_labels_state";

    @Override
    protected Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();
        //保存父类的信息
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
        //保存标签文字颜色
        if (mTextColor != null) {
            bundle.putParcelable(KEY_TEXT_COLOR_STATE, mTextColor);
        }
        //保存标签文字大小
        bundle.putFloat(KEY_TEXT_SIZE_STATE, mTextSize);
        //保存标签背景
        bundle.putInt(KEY_BG_RES_ID_STATE, mLabelBgResId);
        //保存标签内边距
        bundle.putIntArray(KEY_PADDING_STATE, new int[]{mTextPaddingLeft, mTextPaddingTop,
                mTextPaddingRight, mTextPaddingBottom});
        //保存标签间隔
        bundle.putInt(KEY_WORD_MARGIN_STATE, mWordMargin);
        //保存行间隔
        bundle.putInt(KEY_LINE_MARGIN_STATE, mLineMargin);
        //保存标签的选择类型
        bundle.putInt(KEY_SELECT_TYPE_STATE, mSelectType.value);
        //保存标签的最大选择数量
        bundle.putInt(KEY_MAX_SELECT_STATE, mMaxSelect);
        //保存标签列表
        if (!mLabels.isEmpty()) {
            bundle.putStringArrayList(KEY_LABELS_STATE, mLabels);
        }
        //保存已选择的标签列表
        if (!mSelectLabels.isEmpty()) {
            bundle.putIntegerArrayList(KEY_SELECT_LABELS_STATE, mSelectLabels);
        }

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            //恢复父类信息
            super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPER_STATE));

            //恢复标签文字颜色
            ColorStateList color = bundle.getParcelable(KEY_TEXT_COLOR_STATE);
            if (color != null) {
                setLabelTextColor(color);
            }
            //恢复标签文字大小
            setLabelTextSize(bundle.getFloat(KEY_TEXT_SIZE_STATE, mTextSize));
            //恢复标签背景
            int resId = bundle.getInt(KEY_BG_RES_ID_STATE, mLabelBgResId);
            if (resId != 0) {
                setLabelBackgroundResource(resId);
            }
            //恢复标签内边距
            int[] padding = bundle.getIntArray(KEY_PADDING_STATE);
            if (padding != null && padding.length == 4) {
                setLabelTextPadding(padding[0], padding[1], padding[2], padding[3]);
            }
            //恢复标签间隔
            setWordMargin(bundle.getInt(KEY_WORD_MARGIN_STATE, mWordMargin));
            //恢复行间隔
            setLineMargin(bundle.getInt(KEY_LINE_MARGIN_STATE, mLineMargin));
            //恢复标签的选择类型
            setSelectType(SelectType.get(bundle.getInt(KEY_SELECT_TYPE_STATE, mSelectType.value)));
            //恢复标签的最大选择数量
            setMaxSelect(bundle.getInt(KEY_MAX_SELECT_STATE, mMaxSelect));
            //恢复标签列表
            ArrayList<String> labels = bundle.getStringArrayList(KEY_LABELS_STATE);
            if (labels != null && !labels.isEmpty()) {
                setLabels(labels);
            }
            //恢复已选择的标签列表
            ArrayList<Integer> selectLabel = bundle.getIntegerArrayList(KEY_SELECT_LABELS_STATE);
            if (selectLabel != null && !selectLabel.isEmpty()) {
                int size = selectLabel.size();
                int[] positions = new int[size];
                for (int i = 0; i < size; i++) {
                    positions[i] = selectLabel.get(i);
                }
                setSelects(positions);
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * 设置标签列表
     *
     * @param labels
     */
    public void setLabels(ArrayList<String> labels) {
        //清空原有的标签
        clearAllSelect();
        removeAllViews();
        mLabels.clear();

        if (labels != null) {
            mLabels.addAll(labels);
            int size = labels.size();
            for (int i = 0; i < size; i++) {
                addLabel(labels.get(i), i);
            }
        }
    }

    /**
     * 获取标签列表
     *
     * @return
     */
    public ArrayList<String> getLabels() {
        return mLabels;
    }

    private void addLabel(String text, int position) {
        final TextView label = new TextView(mContext);
        label.setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        label.setTextColor(mTextColor != null ? mTextColor : ColorStateList.valueOf(0xFF000000));
        label.setText(text);
        if (mLabelBgResId != 0) {
            label.setBackgroundResource(mLabelBgResId);
        }
        //label通过tag保存自己的位置(position)
        label.setTag(position);
        label.setOnClickListener(this);
        addView(label);
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            TextView label = (TextView) v;
            if (mSelectType != SelectType.NONE) {
                if (label.isSelected()) {
                    setLabelSelect(label, false);
                } else if (mSelectType == SelectType.SINGLE) {
                    clearAllSelect();
                    setLabelSelect(label, true);
                } else if (mSelectType == SelectType.MULTI
                        && (mMaxSelect <= 0 || mMaxSelect > mSelectLabels.size())) {
                    setLabelSelect(label, true);
                }
            }

            if (mLabelClickListener != null) {
                mLabelClickListener.onLabelClick(label, label.getText().toString(), (int) v.getTag());
            }
        }
    }

    private void setLabelSelect(TextView label, boolean isSelect) {
        if (label.isSelected() != isSelect) {
            label.setSelected(isSelect);
            if (isSelect) {
                mSelectLabels.add((Integer) label.getTag());
            } else {
                mSelectLabels.remove((Integer) label.getTag());
            }
            if (mLabelSelectChangeListener != null) {
                mLabelSelectChangeListener.onLabelSelectChange(label, label.getText().toString(),
                        isSelect, (int) label.getTag());
            }
        }
    }

    /**
     * 取消所有选中的label
     */
    public void clearAllSelect() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            setLabelSelect((TextView) getChildAt(i), false);
        }
        mSelectLabels.clear();
    }

    /**
     * 设置选中label
     *
     * @param positions
     */
    public void setSelects(int... positions) {
        if (mSelectType != SelectType.NONE) {
            ArrayList<TextView> selectLabels = new ArrayList<>();
            int count = getChildCount();
            int size = mSelectType == SelectType.SINGLE ? 1 : mMaxSelect;
            for (int p : positions) {
                if (p < count) {
                    TextView label = (TextView) getChildAt(p);
                    if (!selectLabels.contains(label)) {
                        setLabelSelect(label, true);
                        selectLabels.add(label);
                    }
                    if (size > 0 && selectLabels.size() == size) {
                        break;
                    }
                }
            }

            for (int i = 0; i < count; i++) {
                TextView label = (TextView) getChildAt(i);
                if (!selectLabels.contains(label)) {
                    setLabelSelect(label, false);
                }
            }
        }
    }

    /**
     * 获取选中的label
     *
     * @return
     */
    public ArrayList<Integer> getSelectLabels() {
        return mSelectLabels;
    }

    /**
     * 设置标签背景
     *
     * @param resId
     */
    public void setLabelBackgroundResource(int resId) {
        if (mLabelBgResId != resId) {
            mLabelBgResId = resId;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                TextView label = (TextView) getChildAt(i);
                label.setBackgroundResource(mLabelBgResId);
            }
        }
    }

    /**
     * 设置标签内边距
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
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

    public int getTextPaddingLeft() {
        return mTextPaddingLeft;
    }

    public int getTextPaddingTop() {
        return mTextPaddingTop;
    }

    public int getTextPaddingRight() {
        return mTextPaddingRight;
    }

    public int getTextPaddingBottom() {
        return mTextPaddingBottom;
    }

    /**
     * 设置标签的文字大小（单位是px）
     *
     * @param size
     */
    public void setLabelTextSize(float size) {
        if (mTextSize != size) {
            mTextSize = size;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                TextView label = (TextView) getChildAt(i);
                label.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            }
        }
    }

    public float getLabelTextSize() {
        return mTextSize;
    }

    /**
     * 设置标签的文字颜色
     *
     * @param color
     */
    public void setLabelTextColor(int color) {
        setLabelTextColor(ColorStateList.valueOf(color));
    }

    /**
     * 设置标签的文字颜色
     *
     * @param color
     */
    public void setLabelTextColor(ColorStateList color) {
        mTextColor = color;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            TextView label = (TextView) getChildAt(i);
            label.setTextColor(mTextColor != null ? mTextColor : ColorStateList.valueOf(0xFF000000));
        }
    }

    public ColorStateList getLabelTextColor() {
        return mTextColor;
    }

    /**
     * 设置行间隔
     */
    public void setLineMargin(int margin) {
        if (mLineMargin != margin) {
            mLineMargin = margin;
            requestLayout();
        }
    }

    public int getLineMargin() {
        return mLineMargin;
    }

    /**
     * 设置标签的间隔
     */
    public void setWordMargin(int margin) {
        if (mWordMargin != margin) {
            mWordMargin = margin;
            requestLayout();
        }
    }

    public int getWordMargin() {
        return mWordMargin;
    }

    /**
     * 设置标签的选择类型
     *
     * @param selectType
     */
    public void setSelectType(SelectType selectType) {
        if (mSelectType != selectType) {
            mSelectType = selectType;
            //选择类型发生改变，就要恢复到初始状态。
            clearAllSelect();
        }
    }

    public SelectType getSelectType() {
        return mSelectType;
    }

    /**
     * 设置最大的选择数量
     *
     * @param maxSelect
     */
    public void setMaxSelect(int maxSelect) {
        if (mMaxSelect != maxSelect) {
            mMaxSelect = maxSelect;
            if (mSelectType == SelectType.MULTI) {
                //最大选择数量发生改变，就要恢复到初始状态。
                clearAllSelect();
            }
        }
    }

    public int getMaxSelect() {
        return mMaxSelect;
    }

    /**
     * 设置标签的点击监听
     *
     * @param l
     */
    public void setOnLabelClickListener(OnLabelClickListener l) {
        mLabelClickListener = l;
    }

    /**
     * 设置标签的选择监听
     *
     * @param l
     */
    public void setOnLabelSelectChangeListener(OnLabelSelectChangeListener l) {
        mLabelSelectChangeListener = l;
    }

    /**
     * sp转px
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    public interface OnLabelClickListener {
        void onLabelClick(View label, String labelText, int position);
    }

    public interface OnLabelSelectChangeListener {
        void onLabelSelectChange(View label, String labelText, boolean isSelect, int position);
    }

}
