package com.donkingliang.labels;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LabelsView extends ViewGroup implements View.OnClickListener, View.OnLongClickListener {

    private Context mContext;

    private ColorStateList mTextColor;
    private float mTextSize;
    private Drawable mLabelBg;
    private int labelBgId;
    private int mLabelWidth = -2;
    private int mLabelHeight = -2;
    private int mLabelGravity = Gravity.CENTER;
    private int mTextPaddingLeft;
    private int mTextPaddingTop;
    private int mTextPaddingRight;
    private int mTextPaddingBottom;
    private int mWordMargin;
    private int mLineMargin;
    private SelectType mSelectType;
    private int mMaxSelect;
    private int mMinSelect;
    private int mMaxLines;
    private int mMaxColumns;
    private boolean isSingleLine = false;
    private boolean isTextBold = false;

    private boolean isIndicator; //只能看，不能手动改变选中状态。

    //用于保存label数据的key
    private static final int KEY_DATA = R.id.tag_key_data;
    //用于保存label位置的key
    private static final int KEY_POSITION = R.id.tag_key_position;

    private ArrayList<Object> mLabels = new ArrayList<>();
    //保存选中的label的位置
    private ArrayList<Integer> mSelectLabels = new ArrayList<>();

    //保存必选项。在多选模式下，可以设置必选项，必选项默认选中，不能反选
    private ArrayList<Integer> mCompulsorys = new ArrayList<>();

    //当前的label行数，需要在layout完成之后才知道的，调用的时候需要在对应方法之后在调用post()来获取
    private int mLines;

    private OnLabelClickListener mLabelClickListener;
    private OnLabelLongClickListener mLabelLongClickListener;
    private OnLabelSelectChangeListener mLabelSelectChangeListener;
    private OnSelectChangeIntercept mOnSelectChangeIntercept;

    /**
     * Label的选择类型
     */
    public enum SelectType {
        //不可选中，也不响应选中事件回调。（默认）
        NONE(1),
        //单选,可以反选。
        SINGLE(2),
        //单选,不可以反选。这种模式下，至少有一个是选中的，默认是第一个
        SINGLE_IRREVOCABLY(3),
        //多选
        MULTI(4);

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
                    return SINGLE_IRREVOCABLY;
                case 4:
                    return MULTI;
            }
            return NONE;
        }
    }

    public LabelsView(Context context) {
        super(context);
        mContext = context;
        showEditPreview();
    }

    public LabelsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        getAttrs(context, attrs);
        showEditPreview();
    }

    public LabelsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getAttrs(context, attrs);
        showEditPreview();
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.LabelsView);
            int type = mTypedArray.getInt(R.styleable.LabelsView_selectType, 1);
            mSelectType = SelectType.get(type);

            mMaxSelect = mTypedArray.getInteger(R.styleable.LabelsView_maxSelect, 0);
            mMinSelect = mTypedArray.getInteger(R.styleable.LabelsView_minSelect, 0);
            mMaxLines = mTypedArray.getInteger(R.styleable.LabelsView_maxLines, 0);
            mMaxColumns = mTypedArray.getInteger(R.styleable.LabelsView_maxColumns, 0);
            isIndicator = mTypedArray.getBoolean(R.styleable.LabelsView_isIndicator, false);

            mLabelGravity = mTypedArray.getInt(R.styleable.LabelsView_labelGravity, mLabelGravity);
            mLabelWidth = mTypedArray.getLayoutDimension(R.styleable.LabelsView_labelTextWidth, mLabelWidth);
            mLabelHeight = mTypedArray.getLayoutDimension(R.styleable.LabelsView_labelTextHeight, mLabelHeight);

            if (mTypedArray.hasValue(R.styleable.LabelsView_labelTextColor)) {
                mTextColor = mTypedArray.getColorStateList(R.styleable.LabelsView_labelTextColor);
            } else {
                mTextColor = ColorStateList.valueOf(0xFF000000);
            }

            mTextSize = mTypedArray.getDimension(R.styleable.LabelsView_labelTextSize,
                    sp2px(14));
            if (mTypedArray.hasValue(R.styleable.LabelsView_labelTextPadding)) {
                int textPadding = mTypedArray.getDimensionPixelOffset(
                        R.styleable.LabelsView_labelTextPadding, 0);
                mTextPaddingLeft = mTextPaddingTop = mTextPaddingRight = mTextPaddingBottom = textPadding;
            } else {
                mTextPaddingLeft = mTypedArray.getDimensionPixelOffset(
                        R.styleable.LabelsView_labelTextPaddingLeft, dp2px(10));
                mTextPaddingTop = mTypedArray.getDimensionPixelOffset(
                        R.styleable.LabelsView_labelTextPaddingTop, dp2px(5));
                mTextPaddingRight = mTypedArray.getDimensionPixelOffset(
                        R.styleable.LabelsView_labelTextPaddingRight, dp2px(10));
                mTextPaddingBottom = mTypedArray.getDimensionPixelOffset(
                        R.styleable.LabelsView_labelTextPaddingBottom, dp2px(5));
            }

            mLineMargin = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_lineMargin, dp2px(5));
            mWordMargin = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_wordMargin, dp2px(5));
            if (mTypedArray.hasValue(R.styleable.LabelsView_labelBackground)) {
                int labelBgResId = mTypedArray.getResourceId(R.styleable.LabelsView_labelBackground, 0);
                labelBgId = labelBgResId;
//                if (labelBgResId != 0) {
//
//                    mLabelBg = getResources().getDrawable(labelBgResId);
//                } else {
//                    int labelBgColor = mTypedArray.getColor(R.styleable.LabelsView_labelBackground, Color.TRANSPARENT);
//                    mLabelBg = new ColorDrawable(labelBgColor);
//                }
            } else {
                mLabelBg = getResources().getDrawable(R.drawable.default_label_bg);
            }

            isSingleLine = mTypedArray.getBoolean(R.styleable.LabelsView_singleLine, false);
            isTextBold = mTypedArray.getBoolean(R.styleable.LabelsView_isTextBold, false);

            mTypedArray.recycle();
        }
    }

    /**
     * 编辑预览
     */
    private void showEditPreview() {
        if (isInEditMode()) {
//            测试的数据
            ArrayList<String> label = new ArrayList<>();
            label.add("Label 1");
            label.add("Label 2");
            label.add("Label 3");
            label.add("Label 4");
            label.add("Label 5");
            label.add("Label 6");
            label.add("Label 7");
            setLabels(label);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isSingleLine) {
            measureSingleLine(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureMultiLine(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 测量单行模式
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private void measureSingleLine(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int contentWidth = 0; //记录内容的宽度
        int maxItemHeight = 0; //记录一行中item高度最大的高度
        for (int i = 0; i < count; i++) {
            if ((mMaxColumns > 0 && i == mMaxColumns)) {
                break;
            }

            View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
            contentWidth += view.getMeasuredWidth();
            if (i != count - 1) {
                contentWidth += mWordMargin;
            }
            maxItemHeight = Math.max(maxItemHeight, view.getMeasuredHeight());
        }
        setMeasuredDimension(measureSize(widthMeasureSpec, contentWidth + getPaddingLeft() + getPaddingRight()),
                measureSize(heightMeasureSpec, maxItemHeight + getPaddingTop() + getPaddingBottom()));

        // 如果count等于0，没有标签，则lines为0
        mLines = count > 0 ? 1 : 0;
    }

    /**
     * 测量多行模式
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private void measureMultiLine(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();

        int contentHeight = 0; //记录内容的高度
        int lineWidth = 0; //记录行的宽度
        int maxLineWidth = 0; //记录最宽的行宽
        int maxItemHeight = 0; //记录一行中item高度最大的高度
        int lineCount = 1;
        int columnsCount = 0;

        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);

            if ((lineWidth + view.getMeasuredWidth() > maxWidth)
                    || (mMaxColumns > 0 && columnsCount == mMaxColumns)) {
                lineCount++;
                if (mMaxLines > 0 && lineCount > mMaxLines) {
                    lineCount--;
                    break;
                }
                contentHeight += mLineMargin;
                contentHeight += maxItemHeight;
                maxItemHeight = 0;
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                lineWidth = 0;
                columnsCount = 0;
            }

            lineWidth += view.getMeasuredWidth();
            columnsCount++;
            maxItemHeight = Math.max(maxItemHeight, view.getMeasuredHeight());

            if (i != count - 1) {
                if (lineWidth + mWordMargin > maxWidth) {
                    // 换行
                    lineCount++;
                    if (mMaxLines > 0 && lineCount > mMaxLines) {
                        lineCount--;
                        break;
                    }
                    contentHeight += mLineMargin;
                    contentHeight += maxItemHeight;
                    maxItemHeight = 0;
                    maxLineWidth = Math.max(maxLineWidth, lineWidth);
                    lineWidth = 0;
                    columnsCount = 0;
                } else {
                    lineWidth += mWordMargin;
                }
            }
        }
        contentHeight += maxItemHeight;
        maxLineWidth = Math.max(maxLineWidth, lineWidth);

        setMeasuredDimension(measureSize(widthMeasureSpec, maxLineWidth + getPaddingLeft() + getPaddingRight()),
                measureSize(heightMeasureSpec, contentHeight + getPaddingTop() + getPaddingBottom()));

        // 如果count等于0，没有标签，则lines为0
        mLines = count > 0 ? lineCount : 0;
    }

    private int measureSize(int measureSpec, int size) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = size;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumWidth());
        result = resolveSizeAndState(result, measureSpec, 0);
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int x = getPaddingLeft();
        int y = getPaddingTop();

        int contentWidth = right - left;
        int maxItemHeight = 0;
        int lineCount = 1;
        int columnsCount = 0;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);

            if (!isSingleLine && (contentWidth < x + view.getMeasuredWidth() + getPaddingRight()
                    || (mMaxColumns > 0 && columnsCount == mMaxColumns))) {
                lineCount++;
                if (mMaxLines > 0 && lineCount > mMaxLines) {
                    break;
                }
                x = getPaddingLeft();
                y += mLineMargin;
                y += maxItemHeight;
                maxItemHeight = 0;
                columnsCount = 0;
            }

            if (isSingleLine && (mMaxColumns > 0 && columnsCount == mMaxColumns)){
                break;
            }

            view.layout(x, y, x + view.getMeasuredWidth(), y + view.getMeasuredHeight());
            x += view.getMeasuredWidth();
            x += mWordMargin;
            maxItemHeight = Math.max(maxItemHeight, view.getMeasuredHeight());
            columnsCount++;
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
    private static final String KEY_MIN_SELECT_STATE = "key_min_select_state";
    private static final String KEY_MAX_LINES_STATE = "key_max_lines_state";
    private static final String KEY_MAX_COLUMNS_STATE = "key_max_columns_state";
    private static final String KEY_INDICATOR_STATE = "key_indicator_state";
    // 由于新版(1.4.0)的标签列表允许设置任何类型的数据，而不仅仅是String。并且标签显示的内容
    // 最终由LabelTextProvider提供，所以LabelsView不再在onSaveInstanceState()和onRestoreInstanceState()
    // 中保存和恢复标签列表的数据。
    private static final String KEY_LABELS_STATE = "key_labels_state";
    private static final String KEY_SELECT_LABELS_STATE = "key_select_labels_state";
    private static final String KEY_COMPULSORY_LABELS_STATE = "key_select_compulsory_state";
    private static final String KEY_LABEL_WIDTH_STATE = "key_label_width_state";
    private static final String KEY_LABEL_HEIGHT_STATE = "key_label_height_state";
    private static final String KEY_LABEL_GRAVITY_STATE = "key_label_gravity_state";
    private static final String KEY_SINGLE_LINE_STATE = "key_single_line_state";
    private static final String KEY_TEXT_STYLE_STATE = "key_text_style_state";

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
        //保存标签背景 (由于背景改用Drawable,所以不能自动保存和恢复)
//        bundle.putInt(KEY_BG_RES_ID_STATE, mLabelBgResId);
        //保存标签宽高
        bundle.putInt(KEY_LABEL_WIDTH_STATE, mLabelWidth);
        bundle.putInt(KEY_LABEL_HEIGHT_STATE, mLabelHeight);
        //保存标签方向
        bundle.putInt(KEY_LABEL_GRAVITY_STATE, mLabelGravity);
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
        //保存标签的最少选择数量
        bundle.putInt(KEY_MIN_SELECT_STATE, mMinSelect);
        //保存标签的最大行数
        bundle.putInt(KEY_MAX_LINES_STATE, mMaxLines);
        //保存标签的最大列数
        bundle.putInt(KEY_MAX_COLUMNS_STATE, mMaxColumns);
        //保存是否是指示器模式
        bundle.putBoolean(KEY_INDICATOR_STATE, isIndicator);

        //保存标签列表
//        if (!mLabels.isEmpty()) {
//            bundle.putStringArrayList(KEY_LABELS_STATE, mLabels);
//        }
        //保存已选择的标签列表
        if (!mSelectLabels.isEmpty()) {
            bundle.putIntegerArrayList(KEY_SELECT_LABELS_STATE, mSelectLabels);
        }

        //保存必选项列表
        if (!mCompulsorys.isEmpty()) {
            bundle.putIntegerArrayList(KEY_COMPULSORY_LABELS_STATE, mCompulsorys);
        }

        bundle.putBoolean(KEY_SINGLE_LINE_STATE, isSingleLine);
        bundle.putBoolean(KEY_TEXT_STYLE_STATE, isTextBold);

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
//            //恢复标签背景  (由于背景改用Drawable,所以不能自动保存和恢复)
//            int resId = bundle.getInt(KEY_BG_RES_ID_STATE, mLabelBgResId);
//            if (resId != 0) {
//                setLabelBackgroundResource(resId);
//            }
            //恢复标签宽高
            mLabelWidth = bundle.getInt(KEY_LABEL_WIDTH_STATE, mLabelWidth);
            mLabelHeight = bundle.getInt(KEY_LABEL_HEIGHT_STATE, mLabelHeight);
            //恢复标签方向
            setLabelGravity(bundle.getInt(KEY_LABEL_GRAVITY_STATE, mLabelGravity));
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
            //恢复标签的最少选择数量
            setMinSelect(bundle.getInt(KEY_MIN_SELECT_STATE, mMinSelect));
            //恢复标签的最大行数
            setMaxLines(bundle.getInt(KEY_MAX_LINES_STATE, mMaxLines));
            //恢复标签的最大列数
            setMaxColumns(bundle.getInt(KEY_MAX_COLUMNS_STATE, mMaxColumns));
            //恢复是否是指示器模式
            setIndicator(bundle.getBoolean(KEY_INDICATOR_STATE, isIndicator));

            setSingleLine(bundle.getBoolean(KEY_SINGLE_LINE_STATE, isSingleLine));
            setTextBold(bundle.getBoolean(KEY_TEXT_STYLE_STATE, isTextBold));

//            //恢复标签列表
//            ArrayList<String> labels = bundle.getStringArrayList(KEY_LABELS_STATE);
//            if (labels != null && !labels.isEmpty()) {
//                setLabels(labels);
//            }
            //恢复必选项列表
            ArrayList<Integer> compulsory = bundle.getIntegerArrayList(KEY_COMPULSORY_LABELS_STATE);
            if (compulsory != null && !compulsory.isEmpty()) {
                setCompulsorys(compulsory);
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
    public void setLabels(List<String> labels) {
        setLabels(labels, new LabelTextProvider<String>() {
            @Override
            public CharSequence getLabelText(TextView label, int position, String data) {
                return data.trim();
            }
        });
    }

    /**
     * 设置标签列表，标签列表的数据可以是任何类型的数据，它最终显示的内容由LabelTextProvider根据标签的数据提供。
     *
     * @param labels
     * @param provider
     * @param <T>
     */
    public <T> void setLabels(List<T> labels, LabelTextProvider<T> provider) {
        //清空原有的标签
        innerClearAllSelect();
        removeAllViews();
        mLabels.clear();

        if (labels != null) {
            mLabels.addAll(labels);
            int size = labels.size();
            for (int i = 0; i < size; i++) {
                addLabel(labels.get(i), i, provider);
            }
            ensureLabelClickable();
        }

        if (mSelectType == SelectType.SINGLE_IRREVOCABLY) {
            setSelects(0);
        }
    }

    /**
     * 获取标签列表
     *
     * @return
     */
    public <T> List<T> getLabels() {
        return (List<T>) mLabels;
    }

    private <T> void addLabel(T data, int position, LabelTextProvider<T> provider) {
        final TextView label = new TextView(mContext);
        label.setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        label.setGravity(mLabelGravity);
        label.setTextColor(mTextColor);
        //设置给label的背景(Drawable)是一个Drawable对象的拷贝，
        // 因为如果所有的标签都共用一个Drawable对象，会引起背景错乱。
        if (labelBgId > 0) {
            label.setBackgroundResource(labelBgId);
        } else {
            label.setBackgroundDrawable(mLabelBg.getConstantState().newDrawable());
        }
        //label通过tag保存自己的数据(data)和位置(position)
        label.setTag(KEY_DATA, data);
        label.setTag(KEY_POSITION, position);
        label.setOnClickListener(this);
        label.setOnLongClickListener(this);
        label.getPaint().setFakeBoldText(isTextBold);
        addView(label, mLabelWidth, mLabelHeight);
        label.setText(provider.getLabelText(label, position, data));
    }

    /**
     * 确保标签是否能响应事件，如果标签可选或者标签设置了点击事件监听，则响应事件。
     */
    private void ensureLabelClickable() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View label = getChildAt(i);
            label.setClickable(mLabelClickListener != null || mSelectType != SelectType.NONE);
            label.setLongClickable(mLabelLongClickListener != null);
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            TextView label = (TextView) v;
            if (!isIndicator) {
                if (mSelectType != SelectType.NONE) {
                    if (label.isSelected()) {
                        boolean irrevocable = mSelectType == SelectType.MULTI && mCompulsorys.contains((Integer) label.getTag(KEY_POSITION));
                        irrevocable = irrevocable || (mSelectType == SelectType.MULTI && mSelectLabels.size() <= mMinSelect);
                        irrevocable = irrevocable || mSelectType == SelectType.SINGLE_IRREVOCABLY;
                        if (!irrevocable && !selectChangeIntercept(label)) {
                            setLabelSelect(label, false);
                        }
                    } else {
                        if (mSelectType == SelectType.SINGLE || mSelectType == SelectType.SINGLE_IRREVOCABLY) {
                            if (!selectChangeIntercept(label)) {
                                innerClearAllSelect();
                                setLabelSelect(label, true);
                            }
                        } else if (mSelectType == SelectType.MULTI
                                && (mMaxSelect <= 0 || mMaxSelect > mSelectLabels.size())) {
                            if (!selectChangeIntercept(label)) {
                                setLabelSelect(label, true);
                            }
                        }
                    }
                }
            }

            if (mLabelClickListener != null) {
                mLabelClickListener.onLabelClick(label, label.getTag(KEY_DATA), (int) label.getTag(KEY_POSITION));
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v instanceof TextView) {
            TextView label = (TextView) v;
            if (mLabelLongClickListener != null) {
                return mLabelLongClickListener.onLabelLongClick(label, label.getTag(KEY_DATA), (int) label.getTag(KEY_POSITION));
            }
        }

        return false;
    }

    private void setLabelSelect(TextView label, boolean isSelect) {
        if (label.isSelected() != isSelect) {
            label.setSelected(isSelect);
            if (isSelect) {
                mSelectLabels.add((Integer) label.getTag(KEY_POSITION));
            } else {
                mSelectLabels.remove((Integer) label.getTag(KEY_POSITION));
            }
            if (mLabelSelectChangeListener != null) {
                mLabelSelectChangeListener.onLabelSelectChange(label, label.getTag(KEY_DATA),
                        isSelect, (int) label.getTag(KEY_POSITION));
            }
        }
    }

    private boolean selectChangeIntercept(TextView label) {
        return mOnSelectChangeIntercept != null && mOnSelectChangeIntercept.onIntercept(label,
                label.getTag(KEY_DATA), label.isSelected(), !label.isSelected(),
                (int) label.getTag(KEY_POSITION));
    }

    /**
     * 取消所有选中的label
     */
    public void clearAllSelect() {
        if (mSelectType != SelectType.SINGLE_IRREVOCABLY) {
            if (mSelectType == SelectType.MULTI && !mCompulsorys.isEmpty()) {
                clearNotCompulsorySelect();
            } else {
                innerClearAllSelect();
            }
        }
    }

    private void innerClearAllSelect() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            setLabelSelect((TextView) getChildAt(i), false);
        }
        mSelectLabels.clear();
    }

    private void clearNotCompulsorySelect() {
        int count = getChildCount();
        List<Integer> temps = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (!mCompulsorys.contains(i)) {
                setLabelSelect((TextView) getChildAt(i), false);
                temps.add(i);
            }

        }
        mSelectLabels.removeAll(temps);
    }

    /**
     * 设置选中label
     *
     * @param positions
     */
    public void setSelects(List<Integer> positions) {
        if (positions != null) {
            int size = positions.size();
            int[] ps = new int[size];
            for (int i = 0; i < size; i++) {
                ps[i] = positions.get(i);
            }
            setSelects(ps);
        }
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
            int size = mSelectType == SelectType.SINGLE || mSelectType == SelectType.SINGLE_IRREVOCABLY
                    ? 1 : mMaxSelect;
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
     * 设置必选项，只有在多项模式下，这个方法才有效
     *
     * @param positions
     */
    public void setCompulsorys(List<Integer> positions) {
        if (mSelectType == SelectType.MULTI && positions != null) {
            mCompulsorys.clear();
            mCompulsorys.addAll(positions);
            //必选项发生改变，就要恢复到初始状态。
            innerClearAllSelect();
            setSelects(positions);
        }
    }

    /**
     * 设置必选项，只有在多项模式下，这个方法才有效
     *
     * @param positions
     */
    public void setCompulsorys(int... positions) {
        if (mSelectType == SelectType.MULTI && positions != null) {
            List<Integer> ps = new ArrayList<>(positions.length);
            for (int i : positions) {
                ps.add(i);
            }
            setCompulsorys(ps);
        }
    }

    /**
     * 获取必选项，
     *
     * @return
     */
    public List<Integer> getCompulsorys() {
        return mCompulsorys;
    }

    /**
     * 清空必选项，只有在多项模式下，这个方法才有效
     */
    public void clearCompulsorys() {
        if (mSelectType == SelectType.MULTI && !mCompulsorys.isEmpty()) {
            mCompulsorys.clear();
            //必选项发生改变，就要恢复到初始状态。
            innerClearAllSelect();
        }
    }

    /**
     * 获取选中的label(返回的是所有选中的标签的位置)
     *
     * @return
     */
    public List<Integer> getSelectLabels() {
        // 返回新的List对象，避免外部获取mSelectLabels后直接操作数据
        List<Integer> list = new ArrayList<>();
        list.addAll(mSelectLabels);
        return list;
    }

    /**
     * 获取选中的label(返回的是所头选中的标签的数据)
     *
     * @param <T>
     * @return
     */
    public <T> List<T> getSelectLabelDatas() {
        List<T> list = new ArrayList<>();
        int size = mSelectLabels.size();
        for (int i = 0; i < size; i++) {
            View label = getChildAt(mSelectLabels.get(i));
            Object data = label.getTag(KEY_DATA);
            if (data != null) {
                list.add((T) data);
            }
        }
        return list;
    }

    /**
     * 设置标签背景
     *
     * @param resId
     */
    public void setLabelBackgroundResource(int resId) {
        labelBgId = resId;
        mLabelBg = null;
        setLabelBackgroundDrawable();
    }

    /**
     * 设置标签背景
     *
     * @param color
     */
    public void setLabelBackgroundColor(int color) {
        mLabelBg = new ColorDrawable(color);
        labelBgId = 0;
        setLabelBackgroundDrawable();
    }

    /**
     * 设置标签背景
     */
    private void setLabelBackgroundDrawable() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            TextView label = (TextView) getChildAt(i);
            if (labelBgId > 0) {
                label.setBackgroundResource(labelBgId);
            } else {
                label.setBackgroundDrawable(mLabelBg.getConstantState().newDrawable());
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
            label.setTextColor(mTextColor);
        }
    }

    public ColorStateList getLabelTextColor() {
        return mTextColor;
    }

    /**
     * 设置标签显示方向
     *
     * @param gravity
     */
    public void setLabelGravity(int gravity) {
        if (mLabelGravity != gravity) {
            mLabelGravity = gravity;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                TextView label = (TextView) getChildAt(i);
                label.setGravity(gravity);
            }
        }
    }

    public int getLabelGravity() {
        return mLabelGravity;
    }

    /**
     * 设置标签字体是否为粗体
     *
     * @param isBold
     */
    public void setTextBold(boolean isBold) {
        if (this.isTextBold != isBold) {
            this.isTextBold = isBold;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                TextView label = (TextView) getChildAt(i);
                label.getPaint().setFakeBoldText(isTextBold);
                label.invalidate();
            }
        }
    }

    public boolean isTextBold() {
        return isTextBold;
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
            innerClearAllSelect();

            if (mSelectType == SelectType.SINGLE_IRREVOCABLY) {
                setSelects(0);
            }

            if (mSelectType != SelectType.MULTI) {
                mCompulsorys.clear();
            }

            ensureLabelClickable();
        }
    }

    public SelectType getSelectType() {
        return mSelectType;
    }

    /**
     * 设置最大的选择数量，只有selectType等于MULTI时有效。
     *
     * @param maxSelect
     */
    public void setMaxSelect(int maxSelect) {
        if (mMaxSelect != maxSelect) {
            mMaxSelect = maxSelect;
            if (mSelectType == SelectType.MULTI) {
                //最大选择数量发生改变，就要恢复到初始状态。
                innerClearAllSelect();
            }
        }
    }

    public int getMaxSelect() {
        return mMaxSelect;
    }

    /**
     * 设置最少的选择数量，只有selectType等于MULTI时有效。
     * 注意：minSelect只限制用户手动点击取消选中时的效果。
     * 调用setSelects()、clearAllSelect()等方法改变标签的选中状态时，不受minSelect影响。
     *
     * @param minSelect
     */
    public void setMinSelect(int minSelect) {
        this.mMinSelect = minSelect;
    }

    public int getMinSelect() {
        return mMinSelect;
    }

    /**
     * 设置最大行数，小于等于0则不限行数。
     *
     * @param maxLines
     */
    public void setMaxLines(int maxLines) {
        if (mMaxLines != maxLines) {
            mMaxLines = maxLines;
            requestLayout();
        }
    }

    public int getMaxLines() {
        return mMaxLines;
    }

    /**
     * 设置最大列数，小于等于0则不限行数。
     *
     * @param maxColumns
     */
    public void setMaxColumns(int maxColumns) {
        if (mMaxColumns != maxColumns) {
            mMaxColumns = maxColumns;
            requestLayout();
        }
    }

    public int getMaxColumns() {
        return mMaxColumns;
    }

    /**
     * 设置为指示器模式，只能看，不能手动操作。这种模式下，用户不能通过手动点击改变标签的选中状态。
     * 但是仍然可以通过调用setSelects()、clearAllSelect()等方法改变标签的选中状态。
     *
     * @param indicator
     */
    public void setIndicator(boolean indicator) {
        isIndicator = indicator;
    }

    public boolean isIndicator() {
        return isIndicator;
    }

    /**
     * 设置单行显示
     *
     * @param isSingleLine
     */
    public void setSingleLine(boolean isSingleLine) {
        if (this.isSingleLine != isSingleLine) {
            this.isSingleLine = isSingleLine;
            requestLayout();
        }
    }

    public boolean isSingleLine() {
        return isSingleLine;
    }

    /**
     * 需要在该View的layout完成之后调用，一般是使用view.post(Runable task)来获取
     * 比如设置了新的labels之后需要获取新的lines就可以这样
     *
     * @return
     */
    public int getLines() {
        return this.mLines;
    }

    /**
     * 设置标签的点击监听
     *
     * @param l
     */
    public void setOnLabelClickListener(OnLabelClickListener l) {
        mLabelClickListener = l;
        ensureLabelClickable();
    }

    /**
     * 设置标签的点击监听
     *
     * @param l
     */
    public void setOnLabelLongClickListener(OnLabelLongClickListener l) {
        mLabelLongClickListener = l;
        ensureLabelClickable();
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
     * 设置标签选中状态的点击改变拦截器
     *
     * @param intercept
     */
    public void setOnSelectChangeIntercept(OnSelectChangeIntercept intercept) {
        mOnSelectChangeIntercept = intercept;
    }

    /**
     * sp转px
     */
    private int sp2px(float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());
    }

    /**
     * dp转px
     */
    private int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    public interface OnLabelClickListener {

        /**
         * @param label    标签
         * @param data     标签对应的数据
         * @param position 标签位置
         */
        void onLabelClick(TextView label, Object data, int position);
    }

    public interface OnLabelLongClickListener {

        /**
         * @param label    标签
         * @param data     标签对应的数据
         * @param position 标签位置
         */
        boolean onLabelLongClick(TextView label, Object data, int position);
    }

    public interface OnLabelSelectChangeListener {

        /**
         * @param label    标签
         * @param data     标签对应的数据
         * @param isSelect 是否选中
         * @param position 标签位置
         */
        void onLabelSelectChange(TextView label, Object data, boolean isSelect, int position);
    }

    /**
     * 点击选中/取消选中时，拦截事件，返回true时，表示事件被拦截，不会改变标签的选中状态。
     * 当希望某个标签在特定条件下不被选中/取消选中时，可以使用事件拦截。
     * 只有用户点击改变标签选中状态时才会回调拦截，用其他方法改变时不会回调这个方法，不会被拦截。
     */
    public interface OnSelectChangeIntercept {

        /**
         * @param label     标签
         * @param data      标签对应的数据
         * @param oldSelect 旧选中状态
         * @param newSelect 新选中状态
         * @param position  标签位置
         */
        boolean onIntercept(TextView label, Object data, boolean oldSelect, boolean newSelect, int position);
    }

    /**
     * 给标签提供最终需要显示的数据。因为LabelsView的列表可以设置任何类型的数据，而LabelsView里的每个item的是一
     * 个TextView，只能显示CharSequence的数据，所以LabelTextProvider需要根据每个item的数据返回item最终要显示
     * 的CharSequence。
     *
     * @param <T>
     */
    public interface LabelTextProvider<T> {

        /**
         * 根据data和position返回label需要需要显示的数据。
         *
         * @param label
         * @param position
         * @param data
         * @return
         */
        CharSequence getLabelText(TextView label, int position, T data);
    }

    /**
     *
     */
    public interface Selectable {

        void onSelected(boolean selected);

        boolean isSelected();

    }

}
