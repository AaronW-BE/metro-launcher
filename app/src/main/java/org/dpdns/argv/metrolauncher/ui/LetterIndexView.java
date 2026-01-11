package org.dpdns.argv.metrolauncher.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

public class LetterIndexView extends FrameLayout {

    public interface OnLetterSelectedListener {
        void onLetterSelected(String letter);
    }

    private OnLetterSelectedListener listener;

    public LetterIndexView(Context context) {
        super(context);
        init();
    }

    public LetterIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(0xCC000000);
        setClickable(true);

        GridLayout grid = new GridLayout(getContext());
        grid.setColumnCount(4);
        grid.setRowCount(7);
        grid.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        );

        addView(grid);

        for (char c = 'A'; c <= 'Z'; c++) {
            TextView tv = createLetterView(String.valueOf(c));
            grid.addView(tv);
        }
    }

    private TextView createLetterView(final String letter) {
        TextView tv = new TextView(getContext());
        tv.setText(letter);
        tv.setTextSize(32);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        tv.setLayoutParams(lp);

        tv.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLetterSelected(letter);
            }
//            setVisibility(GONE);
            animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> setVisibility(GONE))
                    .start();
        });

        return tv;
    }

    public void setOnLetterSelectedListener(OnLetterSelectedListener l) {
        listener = l;
    }
}
