package by.example.root.myapplication22;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.Spannable;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.ActionMode;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Date;

public class NoteActivity extends AppCompatActivity {


    private boolean isDrawModeOn;
    private boolean isTextModeOn;
    private DrawingView drawingView;
    private static final float
            SMALL_BRUSH = 5,
            MEDIUM_BRUSH = 10,
            LARGE_BRUSH = 20;

    private static final int REQUEST_CODE = 1234;

    private DatabaseHandler dbHandler;

    private static final double MENU_MARGIN_RELATIVE_MODIFIER = 0.3;

    private LinearLayout mSliderLayout;
    private LinearLayout mDrawLayout;

    private int noteID;

    private EditText noteTitle;

    private EditText editText;

    private Spannable spannable;

    private AlertDialog alertDialogSaveNote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        noteTitle = (EditText) findViewById(R.id.activity_note_title);
        editText = (EditText) findViewById(R.id.editText);
        mSliderLayout = (LinearLayout) findViewById(R.id.formatTextSlider);
        mDrawLayout = (LinearLayout) findViewById(R.id.drawPanelSlider);
        drawingView = (DrawingView) findViewById(R.id.drawing);

        isDrawModeOn = false;
        isTextModeOn = true;

        ViewGroup.LayoutParams paramsTextFormat = mSliderLayout.getLayoutParams();
        paramsTextFormat.height = calculateMenuMargin();
        ViewGroup.LayoutParams paramsDrawPanel = mDrawLayout.getLayoutParams();
        paramsDrawPanel.height = calculateMenuMargin();

        dbHandler = new DatabaseHandler(getApplicationContext());

        spannable = editText.getText();

        alertDialogSaveNote = initAlertDialogSaveNote();

        Intent intent = getIntent();
        noteID = Integer.parseInt(intent.getStringExtra("id"));

        disableSoftInputFromAppearing(editText);

        manageContextMenuBar(editText);

        if (noteID != -1) {
            loadNote(noteID);
        }

        editText.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isDrawModeOn) {
                    drawingView.onTouchEvent(event);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }


    private AlertDialog initAlertDialogSaveNote() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.save_note_title)).setMessage(this.getString(R.string.save_note_confirmation));

        builder.setPositiveButton(this.getString(R.string.ok_bt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton(this.getString(R.string.cancel_bt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        return builder.create();
    }

    @Override
    public void onBackPressed() {

        View formatTextSliderView = findViewById(R.id.formatTextSlider);
        View drawPanelSliderView = findViewById(R.id.drawPanelSlider);

        if (formatTextSliderView.getVisibility() == View.VISIBLE) {
            formatTextSliderView.setVisibility(View.GONE);
        }
        else if (drawPanelSliderView.getVisibility() == View.VISIBLE) {
            drawPanelSliderView.setVisibility(View.GONE);
        }
        else {
            saveOrUpdateNote(null);
        }
    }


    public void toggleTextFormatMenu( MenuItem item) {

        View formatTextSliderView = findViewById(R.id.formatTextSlider);
        View drawPanelSliderView = findViewById(R.id.drawPanelSlider);

        if (formatTextSliderView.getVisibility() == View.VISIBLE) {
            formatTextSliderView.setVisibility(View.GONE);
        } else {
            if (drawPanelSliderView.getVisibility() == View.VISIBLE) {
                drawPanelSliderView.setVisibility(View.GONE);
            }
            formatTextSliderView.setVisibility(View.VISIBLE);
        }

        setDrawModeOn(formatTextSliderView.getVisibility() != View.VISIBLE
                && drawPanelSliderView.getVisibility() == View.VISIBLE);
    }

    public void toggleDrawMenu( MenuItem item) {

        View formatTextSliderView = findViewById(R.id.formatTextSlider);
        View drawPanelSliderView = findViewById(R.id.drawPanelSlider);

        if (drawPanelSliderView.getVisibility() == View.VISIBLE) {
            drawPanelSliderView.setVisibility(View.GONE);
        } else {
            if (formatTextSliderView.getVisibility() == View.VISIBLE) {
                formatTextSliderView.setVisibility(View.GONE);
            }
            hideSoftKeyboard();
            drawPanelSliderView.setVisibility(View.VISIBLE);
        }

        setDrawModeOn(drawPanelSliderView.getVisibility() == View.VISIBLE);
    }


    private void setDrawModeOn(boolean isOn) {
        isDrawModeOn = isOn;
        isTextModeOn = !isOn;
    }


    private int calculateMenuMargin() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return (int) Math.round(height * MENU_MARGIN_RELATIVE_MODIFIER);
    }

    public void toggleKeyboard( MenuItem item) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        if (findViewById(R.id.drawPanelSlider).getVisibility() == View.VISIBLE) {
            findViewById(R.id.drawPanelSlider).setVisibility(View.GONE);
        }
    }

    private void hideSoftKeyboard() {
        if (this.getCurrentFocus() != null) {
            try {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
            } catch (RuntimeException e) {
            }
        }
    }


    private static void disableSoftInputFromAppearing(EditText editText) {
        if (Build.VERSION.SDK_INT >= 16) {
            editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
            editText.setTextIsSelectable(true);
        } else {
            editText.setRawInputType(InputType.TYPE_NULL);
            editText.setFocusable(true);
        }
    }


    private void manageContextMenuBar(EditText editText) {

        editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            public void onDestroyActionMode(ActionMode mode) {
                if (findViewById(R.id.formatTextSlider).getVisibility() == View.VISIBLE) {
                    findViewById(R.id.formatTextSlider).setVisibility(View.GONE);
                }
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                if (findViewById(R.id.formatTextSlider).getVisibility() == View.GONE) {
                    findViewById(R.id.formatTextSlider).setVisibility(View.VISIBLE);
                }
                return true;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
    }


    public void formatTextActionPerformed(View view) {

        EditText editTextLocal = (EditText) findViewById(R.id.editText);
        spannable = editTextLocal.getText();

        int posStart = editTextLocal.getSelectionStart();
        int posEnd = editTextLocal.getSelectionEnd();

        if (view.getTag().toString().equals("bold")) {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), posStart, posEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (view.getTag().toString().equals("italic")) {
            spannable.setSpan(new StyleSpan(Typeface.ITALIC), posStart, posEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (view.getTag().toString().equals("underline")) {
            spannable.setSpan(new UnderlineSpan(), posStart, posEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (view.getTag().toString().equals("textBlack")) {
            spannable.setSpan(new ForegroundColorSpan(Color.BLACK), posStart, posEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (view.getTag().toString().equals("textRed")) {
            spannable.setSpan(new ForegroundColorSpan(Color.RED), posStart, posEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (view.getTag().toString().equals("textBlue")) {
            spannable.setSpan(new ForegroundColorSpan(Color.BLUE), posStart, posEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (view.getTag().toString().equals("textGreen")) {
            spannable.setSpan(new ForegroundColorSpan(Color.GREEN), posStart, posEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (view.getTag().toString().equals("textYellow")) {
            spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), posStart, posEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        editTextLocal.setText(spannable);
    }


    private void loadNote(int noteID) {

        try {
            Note n = dbHandler.getNote(noteID);

            editText.setText(n.getSpannable());
            editText.setSelection(editText.getText().toString().length());
            noteTitle.setText(n.getTitle());
            drawingView.setBitmap(n.getImage());
        }catch (SQLiteException e){
            e.printStackTrace();
        }
    }


    public void saveOrUpdateNote(MenuItem menu) {

        spannable = editText.getText();
        String title = noteTitle.getText().toString();

        Note note = new Note();

        note.setTitle(title);
        note.setSpannable(spannable);
        note.setImage(drawingView.getCanvasBitmap());
        note.setDateUpdate(new Date());

        if (noteID != -1) {
            note.setId(noteID);
        }

        new SaveOrUpdateNoteTask(this).execute(note);

        hideSoftKeyboard();
        finish();
    }


    public void speakButtonClicked(MenuItem menuItem) {
        startVoiceRecognitionActivity();
    }


    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.voice_hint);
        startActivityForResult(intent, REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            if (matches.size() > 0) {
                if (editText.getText().toString().length() == 0) {
                    editText.setText(matches.get(0));
                    editText.setSelection(editText.getText().toString().length());
                } else {
                    Spanned spanText = (SpannedString) TextUtils.concat(editText.getText(), " " + matches.get(0));
                    editText.setText(spanText);
                    editText.setSelection(editText.getText().toString().length());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void changeColor(View v) {

        if (v.getTag().toString().equals("black")) {
            drawingView.setPaintColor(Color.BLACK);
        } else if (v.getTag().toString().equals("red")) {
            drawingView.setPaintColor(Color.RED);
        } else if (v.getTag().toString().equals("blue")) {
            drawingView.setPaintColor(Color.BLUE);
        } else if (v.getTag().toString().equals("green")) {
            drawingView.setPaintColor(Color.GREEN);
        } else if (v.getTag().toString().equals("yellow")) {
            drawingView.setPaintColor(Color.YELLOW);
        }
    }


    public void changeBrushSize(View v) {

        if (v.getTag().toString().equals("small")) {
            drawingView.setBrushSize(SMALL_BRUSH);
        } else if (v.getTag().toString().equals("medium")) {
            drawingView.setBrushSize(MEDIUM_BRUSH);
        } else if (v.getTag().toString().equals("large")) {
            drawingView.setBrushSize(LARGE_BRUSH);
        }
    }

    public void eraseOrPaintMode(View v) {
        drawingView.setErase(v.getTag().toString().equals("erase"));
    }

    public void wipeCanvas(View v) {
        drawingView.startNew();
    }
}