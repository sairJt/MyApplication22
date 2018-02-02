package by.example.root.myapplication22;

import android.graphics.Bitmap;
import android.text.Spannable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

 public class Note {

    private int mId = -1;

    private Spannable Span;

    private String Title;

    private String rawText;

    private Bitmap mImage;

    private Date dateUpdate = new Date();

    private static final DateFormat dt = new SimpleDateFormat("dd.MM.yyyy, hh:mm:ss", Locale.getDefault());

    public Note() {

    }

    public Note(int id, String title, Spannable spannable, Bitmap image, Date dateUpdated) {
        this.mId = id;
        this.Title = title;
        this.Span = spannable;
        this.mImage = image;
        this.rawText = Span.toString();
        this.dateUpdate = dateUpdated;
    }

    public int getId() {
        return mId;
    }

    public String getTitle(){
        return Title;
    }

    public Spannable getSpannable() {
        return Span;
    }

    public String getRawText() {
        return rawText;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public Date getDateUpdate() {
        return dateUpdate;
    }

    public String getFormattedDateUpdatted() {
        return dt.format(dateUpdate);
    }

    public void setId(int id) {
        this.mId = id;
    }

    public void setSpannable(Spannable spannable) {
        this.Span = spannable;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public void setImage(Bitmap image) {
        this.mImage = image;
    }

    public void setDateUpdate(Date dateUpdate) {
        this.dateUpdate = dateUpdate;
    }
}