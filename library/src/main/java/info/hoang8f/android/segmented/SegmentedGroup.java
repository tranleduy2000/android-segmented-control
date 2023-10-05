package info.hoang8f.android.segmented;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.HashMap;

public class SegmentedGroup extends RadioGroup {

    private int marginDp;
    private int cornerRadius;
    private final Resources resources;
    private ColorStateList primaryColor;
    private ColorStateList secondaryColor;
    private ColorStateList checkedTextColor = ColorStateList.valueOf(Color.WHITE);

    private final LayoutSelector mLayoutSelector;

    private OnCheckedChangeListener mCheckedChangeListener;
    private HashMap<Integer, TransitionDrawable> mDrawableMap;
    private int mLastCheckId;

    public SegmentedGroup(Context context) {
        super(context);
        resources = getResources();
        primaryColor = ColorStateList.valueOf(0xff33b5e5);
        secondaryColor = ColorStateList.valueOf(Color.TRANSPARENT);
        marginDp = (int) getResources().getDimension(R.dimen.radio_button_stroke_border);
        mLayoutSelector = new LayoutSelector();
    }

    public SegmentedGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        resources = getResources();
        primaryColor = resources.getColorStateList(R.color.radio_button_selected_color);
        secondaryColor = resources.getColorStateList(R.color.radio_button_unselected_color);
        marginDp = (int) getResources().getDimension(R.dimen.radio_button_stroke_border);
        initAttrs(attrs);
        mLayoutSelector = new LayoutSelector();
    }

    /* Reads the attributes from the layout */
    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SegmentedGroup,
                0, 0);

        try {
            marginDp = (int) typedArray.getDimension(
                    R.styleable.SegmentedGroup_sc_border_width,
                    getResources().getDimension(R.dimen.radio_button_stroke_border));

            primaryColor = typedArray.getColorStateList(
                    R.styleable.SegmentedGroup_sc_tint_color);
            if (primaryColor == null) {
                primaryColor = getResources().getColorStateList(R.color.radio_button_selected_color);
            }


            secondaryColor = typedArray.getColorStateList(
                    R.styleable.SegmentedGroup_sc_unchecked_tint_color);
            if (secondaryColor == null) {
                secondaryColor = getResources().getColorStateList(R.color.radio_button_unselected_color);
            }

            checkedTextColor = typedArray.getColorStateList(
                    R.styleable.SegmentedGroup_checked_text_color);

            // Default checked text color is unchecked background color
            if (checkedTextColor == null) {
                checkedTextColor = secondaryColor;
            }

            if (typedArray.hasValue(R.styleable.SegmentedGroup_sc_corner_radius)) {
                cornerRadius = typedArray.getDimensionPixelSize(R.styleable.SegmentedGroup_sc_corner_radius, 0);
            }

        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //Use holo light for default
        updateBackground();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateBackground();
    }

    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mCheckedChangeListener = listener;
    }

    @SuppressWarnings("unused")
    public void setPrimaryColor(int primaryColor) {
        this.primaryColor = ColorStateList.valueOf(primaryColor);
        updateBackground();
    }

    public void setTintColor(int tintColor, int checkedTextColor) {
        this.primaryColor = ColorStateList.valueOf(tintColor);
        this.checkedTextColor = ColorStateList.valueOf(checkedTextColor);
        updateBackground();
    }

    @SuppressWarnings("unused")
    public void setUnCheckedTintColor(int unCheckedTintColor, int unCheckedTextColor) {
        this.secondaryColor = ColorStateList.valueOf(unCheckedTintColor);
        updateBackground();
    }

    public void updateBackground() {
        mDrawableMap = new HashMap<>();
        int count = super.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            updateBackground(child, i, count);

            // If this is the last view, don't set LayoutParams
            if (i == count - 1) break;

            LayoutParams initParams = (LayoutParams) child.getLayoutParams();
            LayoutParams params = new LayoutParams(initParams.width, initParams.height, initParams.weight);
            // Check orientation for proper margins
            if (getOrientation() == LinearLayout.HORIZONTAL) {
                params.setMargins(0, 0, -marginDp, 0);
            } else {
                params.setMargins(0, 0, 0, -marginDp);
            }
            child.setLayoutParams(params);
        }
    }

    private void updateBackground(View view, int index, int count) {
        int checked = mLayoutSelector.getSelected();
        int unchecked = mLayoutSelector.getUnselected();
        //Set text color
        ColorStateList colorStateList = new ColorStateList(new int[][]{
                {-android.R.attr.state_checked},
                {android.R.attr.state_checked}},
                new int[]{primaryColor.getDefaultColor(), checkedTextColor.getDefaultColor()});
        ((Button) view).setTextColor(colorStateList);

        //Redraw with tint color
        GradientDrawable checkedDrawable = (GradientDrawable) resources.getDrawable(checked).mutate();
        GradientDrawable uncheckedDrawable = (GradientDrawable) resources.getDrawable(unchecked).mutate();
        checkedDrawable.setColor(primaryColor.getDefaultColor());
        checkedDrawable.setStroke(marginDp, primaryColor.getDefaultColor());
        uncheckedDrawable.setStroke(marginDp, primaryColor.getDefaultColor());
        uncheckedDrawable.setColor(secondaryColor.getDefaultColor());

        setCornerRadiusChild(checkedDrawable, index, count);
        setCornerRadiusChild(uncheckedDrawable, index, count);

        GradientDrawable maskDrawable = (GradientDrawable) resources.getDrawable(unchecked).mutate();
        maskDrawable.setStroke(marginDp, primaryColor.getDefaultColor());
        maskDrawable.setColor(secondaryColor.getDefaultColor());
        int maskColor = Color.argb(50, Color.red(primaryColor.getDefaultColor()), Color.green(primaryColor.getDefaultColor()), Color.blue(primaryColor.getDefaultColor()));
        maskDrawable.setColor(maskColor);
        setCornerRadiusChild(maskDrawable, index, count);
        LayerDrawable pressedDrawable = new LayerDrawable(new Drawable[]{uncheckedDrawable, maskDrawable});

        Drawable[] drawables = {uncheckedDrawable, checkedDrawable};
        TransitionDrawable transitionDrawable = new TransitionDrawable(drawables);
        if (((RadioButton) view).isChecked()) {
            transitionDrawable.reverseTransition(0);
            transitionDrawable.jumpToCurrentState(); // disable animation
        }

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_checked, android.R.attr.state_pressed}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, transitionDrawable);

        mDrawableMap.put(view.getId(), transitionDrawable);

        //Set button background
        view.setBackground(stateListDrawable);

        super.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                TransitionDrawable current = mDrawableMap.get(checkedId);
                if (current != null) {
                    current.reverseTransition(0);
                    current.jumpToCurrentState(); // disable animation
                }
                if (mLastCheckId != 0) {
                    TransitionDrawable last = mDrawableMap.get(mLastCheckId);
                    if (last != null) {
                        last.reverseTransition(0);
                        last.jumpToCurrentState(); // disable animation
                    }
                }
                mLastCheckId = checkedId;

                if (mCheckedChangeListener != null) {
                    mCheckedChangeListener.onCheckedChanged(group, checkedId);
                }
            }
        });
    }

    private void setCornerRadiusChild(GradientDrawable drawable, int index, int count) {
        drawable.setCornerRadii(
                new float[]{index == 0 ? cornerRadius : 0, index == 0 ? cornerRadius : 0, // top-left
                        index == count - 1 ? cornerRadius : 0, index == count - 1 ? cornerRadius : 0,  // top-right
                        index == count - 1 ? cornerRadius : 0, index == count - 1 ? cornerRadius : 0, // bottom-right
                        index == 0 ? cornerRadius : 0, index == 0 ? cornerRadius : 0 // bottom-left
                }
        );

    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        mDrawableMap.remove(child.getId());
    }

    public void checkViewAtIndex(int index) {
        if (getChildCount() > index && index >= 0) {
            View child = getChildAt(index);
            if (child instanceof CompoundButton) {
                check(child.getId());
            }
        }
    }

    public int getSelectedIndex() {
        int checkedRadioButtonId = getCheckedRadioButtonId();
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getId() == checkedRadioButtonId) {
                return i;
            }
        }
        return -1;
    }

    public void removeAllSegments() {
        removeAllViews();
    }

    /*
     * This class is used to provide the proper layout based on the view.
     * Also provides the proper radius for corners.
     * The layout is the same for each selected left/top middle or right/bottom button.
     * float tables for setting the radius via Gradient.setCornerRadii are used instead
     * of multiple xml drawables.
     */
    private static class LayoutSelector {

        private final int SELECTED_LAYOUT = R.drawable.radio_checked;
        private final int UNSELECTED_LAYOUT = R.drawable.radio_unchecked;

        LayoutSelector() {
        }

        /* Returns the selected layout id based on view */
        public int getSelected() {
            return SELECTED_LAYOUT;
        }

        /* Returns the unselected layout id based on view */
        public int getUnselected() {
            return UNSELECTED_LAYOUT;
        }

    }
}