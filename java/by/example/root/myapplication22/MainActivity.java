package by.example.root.myapplication22;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    private ArrayList<Note> allNotesSearchArray;
    private DatabaseHandler dbHandler;
    private AlertDialog alertDialogDeleteAll;
    private AlertDialog alertDialogDeleteSingleNote;
    private Note selectedNote;
    public static NoteAdapter noteAdapter;
    public static ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHandler = new DatabaseHandler(getApplicationContext());

        listView = (ListView) findViewById(R.id.listView);
        populateListView(dbHandler.getAllNotesAsArrayList());

        registerForContextMenu(listView);

        alertDialogDeleteAll = initAlertDialogDeleteAllNotes();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("id", "-1");
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedNote = (Note) parent.getAdapter().getItem(position);
                editNote(selectedNote.getId());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint(searchView.getContext().getResources().getString(R.string.search_hint));

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allNotesSearchArray = (ArrayList<Note>) noteAdapter.getData();
            }
        });

        final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {

                ArrayList<Note> filteredNotesArrayList = new ArrayList<>();
                for (Note note : allNotesSearchArray) {
                    if (note.getRawText().contains(newText)) {
                        filteredNotesArrayList.add(note);
                    }
                }

                populateListView(filteredNotesArrayList);
                noteAdapter.notifyDataSetChanged();

                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        return true;
    }


    private AlertDialog initAlertDialogDeleteAllNotes() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.confirmation)).setTitle(this.getString(R.string.delete_notes_title));
        builder.setPositiveButton(this.getString(R.string.ok_bt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllNotes();
                Toast.makeText(MainActivity.this, getString(R.string.delete_notes_success),
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(this.getString(R.string.cancel_bt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }


    private AlertDialog setupAlertDialogDeleteSingleNote() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.confirmation)).setTitle(String.format(this.getString(R.string.delete_note_number), selectedNote.getId()));
        builder.setPositiveButton(this.getString(R.string.ok_bt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHandler.deleteNote(selectedNote);
                noteAdapter.remove(selectedNote);
                noteAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, String.format(getString(R.string.delete_note), selectedNote.getId()),
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(this.getString(R.string.cancel_bt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }


    public void showAlertDialogDeleteAllNotes(MenuItem menuItem) {
        alertDialogDeleteAll.show();
    }

    private void showAlertDialogDeleteSingleNote() {
        alertDialogDeleteSingleNote.show();
    }


    public void deleteAllNotes() {
        dbHandler.clearAllNotes();
        noteAdapter.clear();
        noteAdapter.notifyDataSetChanged();
    }


    private void editNote(int noteId) {
        hideSoftKeyboard();
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("id", String.valueOf(noteId));
        startActivity(intent);
    }


    private void hideSoftKeyboard() {
        if (this.getCurrentFocus() != null) {
            try {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
            } catch (RuntimeException e) {
                //ignore
            }
        }
    }


    void populateListView(ArrayList<Note> note) {
        noteAdapter = new NoteAdapter(this,
                R.layout.listview_item_row, note);
        listView.setAdapter(noteAdapter);
    }

    public void setListViewData(ArrayList<Note> allNotes, Note newNote) {
        if (noteAdapter != null) {
            if (newNote != null){
                noteAdapter.add(newNote);
            }
            noteAdapter.setData(allNotes);
            noteAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.listView) {
            ListView listViewLocal = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            selectedNote = (Note) listViewLocal.getItemAtPosition(acmi.position);
            menu.setHeaderTitle(String.format(v.getContext().getString(R.string.choose_activity), selectedNote.getId()));
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu_note_select, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.context_menu_delete:
                alertDialogDeleteSingleNote = setupAlertDialogDeleteSingleNote();
                showAlertDialogDeleteSingleNote();
                break;
            case R.id.context_menu_edit:
                editNote(selectedNote.getId());
                break;
        }
        return super.onContextItemSelected(item);
    }

}