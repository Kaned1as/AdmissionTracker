package com.adonai.admissiontracker.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.adonai.admissiontracker.R;

/**
 * Created by adonai on 28.06.14.
 */
public class DoubleTextView extends FrameLayout {

    private TextView mHeader;
    private TextView mBody;

    public DoubleTextView(Context context) {
        super(context);
        initLayout(null);
    }

    public DoubleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(attrs);
    }

    public DoubleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initLayout(attrs);
    }

    public void initLayout(AttributeSet attrs) {
        final View layout = LayoutInflater.from(getContext()).inflate(R.layout.double_text_view, this, true);

        mHeader = (TextView) layout.findViewById(R.id.header);
        mBody = (TextView) layout.findViewById(R.id.body);

        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DoubleTextView);
            final int n = a.getIndexCount();
            for (int i = 0; i < n; ++i) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.DoubleTextView_header_text:
                        mHeader.setText(a.getString(attr));
                        break;
                    case R.styleable.DoubleTextView_body_text:
                        mBody.setText(a.getString(attr));
                        break;
                }
            }
            a.recycle();
        }
    }

    public void setText(CharSequence text) {
        mBody.setText(text);
    }

    public void setTextColor(Integer color) {
        mBody.setTextColor(color);
    }
}
