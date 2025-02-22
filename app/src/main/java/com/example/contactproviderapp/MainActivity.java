package com.example.contactproviderapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Déclaration des composants de l'interface utilisateur
    private EditText etName, etPhone;
    private Button btnAdd;
    private ListView listViewContacts;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contactsList;
    private ArrayList<Integer> contactsIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des composants
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        btnAdd = findViewById(R.id.btnAdd);
        listViewContacts = findViewById(R.id.listViewContacts);

        // Initialisation des listes pour stocker les contacts
        contactsList = new ArrayList<>();
        contactsIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        listViewContacts.setAdapter(adapter);

        // Charger les contacts existants depuis le ContentProvider
        loadContacts();

        // Ajouter un nouveau contact lorsqu'on clique sur le bouton
        btnAdd.setOnClickListener(view -> addContact());

        // Supprimer un contact lorsqu'on clique sur un élément de la liste
        listViewContacts.setOnItemClickListener((adapterView, view, position, id) -> {
            int contactId = contactsIds.get(position);
            deleteContact(contactId);
        });
    }

    // Méthode pour ajouter un contact
    private void addContact() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Vérification que les champs ne sont pas vides
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un nom et un numéro", Toast.LENGTH_SHORT).show();
            return;
        }

        // Création d'un ContentValues pour stocker les données du contact
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);

        // URI du ContentProvider pour insérer le contact
        Uri uri = Uri.parse("content://com.example.contactproviderapp.provider/contacts");
        getContentResolver().insert(uri, values);

        // Réinitialisation des champs après l'ajout
        etName.setText("");
        etPhone.setText("");
        loadContacts(); // Rafraîchir la liste des contacts
    }

    // Méthode pour charger les contacts depuis le ContentProvider
    private void loadContacts() {
        contactsList.clear(); // Vider la liste avant de recharger les contacts
        contactsIds.clear();

        // URI du ContentProvider pour récupérer les contacts
        Uri uri = Uri.parse("content://com.example.contactproviderapp.provider/contacts");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("_id"));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex("phone"));

                // Ajouter le contact à la liste
                contactsList.add(name + " - " + phone);
                contactsIds.add(id);
            }
            cursor.close(); // Fermer le curseur après utilisation
        }

        adapter.notifyDataSetChanged(); // Mettre à jour l'affichage
    }

    // Méthode pour supprimer un contact
    private void deleteContact(int contactId) {
        // URI spécifique au contact à supprimer
        Uri uri = Uri.parse("content://com.example.contactproviderapp.provider/contacts/" + contactId);
        int deletedRows = getContentResolver().delete(uri, "_id=?", new String[]{String.valueOf(contactId)});

        // Vérifier si la suppression a réussi
        if (deletedRows > 0) {
            Toast.makeText(this, "Contact supprimé", Toast.LENGTH_SHORT).show();
            loadContacts(); // Rafraîchir la liste
        } else {
            Toast.makeText(this, "Erreur de suppression", Toast.LENGTH_SHORT).show();
        }
    }
}
