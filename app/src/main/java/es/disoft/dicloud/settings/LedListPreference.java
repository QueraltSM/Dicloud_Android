package es.disoft.dicloud.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import es.disoft.dicloud.R;

public class LedListPreference extends ListPreference {

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private int mClickedDialogEntryIndex;

    public LedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LedListPreference(Context context) {
        super(context);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        if (which >= 0 && which < mEntries.length) {
            mClickedDialogEntryIndex = which;
            ListView listView = ((AlertDialog) dialog).getListView();
            for (int i = 0; i < mEntries.length; i++)
                ((RadioButton) listView.getChildAt(i).findViewById(R.id.led_radio_button)).setChecked(i==which);
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView summary = view.findViewById(R.id.summary);
        ImageView icon   = view.findViewById(R.id.colorSelected);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Resources resources = getContext().getResources();
        String value = preferences.getString(resources.getString(R.string.notification_led), "Red");

        String[] colors = resources.getStringArray(R.array.pref_notification_led);
        int index       = findIndexOfValue(value);
        String text     = colors[index];

        summary.setText(text);
        icon.setColorFilter(getColor(index));
    }

    private int getColor(int index) {
        switch (index) {
            case 0: default:
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.YELLOW;
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        if (builder == null)
            throw new NullPointerException("Builder is null");

        mEntries     = getEntries();
        mEntryValues = getEntryValues();

        // get the index of the selected item
        mClickedDialogEntryIndex = findIndexOfValue(this.getValue());

        if (mEntries == null || mEntryValues == null || mEntries.length != mEntryValues.length)
            throw new IllegalStateException("Invalid entries array or entryValues array");

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(), R.layout.led_picker, R.id.led_item, mEntries) {

            @Override
            public View getView(int position, View row, @NonNull ViewGroup parent) {
                if (row == null)
                    row = LayoutInflater.from(getContext()).inflate(R.layout.led_picker, parent, false);

                RadioButton button = row.findViewById(R.id.led_radio_button);
                if (position == findIndexOfValue(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getKey(), "")))
                    button.setChecked(true);

                int[][] states = new int[][]{{android.R.attr.state_checked}, {}};
                int[]   colors = new int[]{getColor(position), Color.GRAY};

                button.setButtonTintList(new ColorStateList(states, colors));
                return super.getView(position, row, parent);
            }
        };

        builder
                .setSingleChoiceItems(mEntries, mClickedDialogEntryIndex, this)
                .setAdapter(adapter, this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mClickedDialogEntryIndex >= 0 && mEntryValues != null) {
            String value = mEntryValues[mClickedDialogEntryIndex].toString();

            if (callChangeListener(value)) setValue(value);
        }
    }
}
