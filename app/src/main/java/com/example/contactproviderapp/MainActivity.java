package com.example.contactproviderapp;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etPhone, etSearch;
    private Button btnAdd;
    private ListView listViewContacts;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contactsList;
    private ArrayList<String> originalContactsList; // Liste originale des contacts
    private ArrayList<Integer> contactsIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etSearch = findViewById(R.id.etSearch);
        btnAdd = findViewById(R.id.btnAdd);
        listViewContacts = findViewById(R.id.listViewContacts);

        contactsList = new ArrayList<>();
        originalContactsList = new ArrayList<>(); // Initialiser la liste originale
        contactsIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        listViewContacts.setAdapter(adapter);

        loadContacts();

        btnAdd.setOnClickListener(view -> addContact());

        listViewContacts.setOnItemClickListener((adapterView, view, position, id) -> {
            int contactId = contactsIds.get(position);
            deleteContact(contactId);
        });

        // Ajouter un écouteur de texte pour la recherche
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void addContact() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un nom et un numéro", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("^(07|06|05)[0-9]{8}$")) {
            Toast.makeText(this, "Numéro invalide ! Utilisez un format 07XXXXXXXX, 06XXXXXXXX ou 05XXXXXXXX.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier si le contact existe déjà
        if (isContactExists(phone)) {
            Toast.makeText(this, "Ce numéro existe déjà dans la liste des contacts.", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);

        Uri uri = Uri.parse("content://com.example.contactproviderapp.provider/contacts");
        getContentResolver().insert(uri, values);

        etName.setText("");
        etPhone.setText("");

        // Cacher le clavier après l'ajout
        hideKeyboard();

        loadContacts();
    }

    private void hideKeyboard() {
    }

    // Vérifier si un contact existe déjà
    private boolean isContactExists(String phone) {
        for (String contact : contactsList) {
            if (contact.contains(phone)) {
                return true;
            }
        }
        return false;
    }

    private void loadContacts() {
        contactsList.clear();
        originalContactsList.clear(); // Réinitialiser la liste originale
        contactsIds.clear();

        Uri uri = Uri.parse("content://com.example.contactproviderapp.provider/contacts");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("_id"));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex("phone"));

                String contact = name + " - " + phone;
                contactsList.add(contact);
                originalContactsList.add(contact); // Stocker l'original
                contactsIds.add(id);
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private void deleteContact(int contactId) {
        Uri uri = Uri.parse("content://com.example.contactproviderapp.provider/contacts/" + contactId);
        int deletedRows = getContentResolver().delete(uri, "_id=?", new String[]{String.valueOf(contactId)});

        if (deletedRows > 0) {
            Toast.makeText(this, "Contact supprimé", Toast.LENGTH_SHORT).show();
            loadContacts();
        } else {
            Toast.makeText(this, "Erreur de suppression", Toast.LENGTH_SHORT).show();
        }
    }

    // Fonction pour filtrer les contacts
    private void filterContacts(String query) {
        query = query.toLowerCase().trim();
        contactsList.clear();

        if (query.isEmpty()) {
            contactsList.addAll(originalContactsList);
        } else {
            for (String contact : originalContactsList) {
                if (contact.toLowerCase().contains(query)) {
                    contactsList.add(contact);
                }
            }
        }

        // Si la liste filtrée est vide, afficher un message
        if (contactsList.isEmpty()) {
            contactsList.add("Aucun contact trouvé...");
        }

        adapter.notifyDataSetChanged();
    }

}
