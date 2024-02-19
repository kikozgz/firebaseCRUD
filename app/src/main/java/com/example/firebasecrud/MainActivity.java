package com.example.firebasecrud;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextDNI, editTextName, editTextEmail;
    private Button buttonAdd, buttonRetrieve, buttonUpdate, buttonDelete;
    private ListView listView;
    private DatabaseReference databaseReference;
    private ArrayList<String> items = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextDNI = findViewById(R.id.editTextDNI);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonRetrieve = findViewById(R.id.buttonRetrieve);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);
        listView = findViewById(R.id.listView);

        databaseReference = FirebaseDatabase.getInstance().getReference("people");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        buttonAdd.setOnClickListener(v -> addPerson());
        buttonRetrieve.setOnClickListener(v -> retrievePeople());
        buttonUpdate.setOnClickListener(v -> updatePerson());
        buttonDelete.setOnClickListener(v -> deletePerson());

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
               /* String person = dataSnapshot.getValue(String.class);
                items.add(person);
                adapter.notifyDataSetChanged();*/
            }

            @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void addPerson() {
        String dni = editTextDNI.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        if (!dni.isEmpty() && !name.isEmpty() && !email.isEmpty()) {
            String id = databaseReference.push().getKey();
            databaseReference.child(id).setValue("DNI: " + dni + ", Nombre: " + name + ", Email: " + email);
            clearFields();
            showToast("Persona agregada");
        } else {
            showToast("Por favor, completa todos los campos");
        }
    }

    private void retrievePeople() {

        String dniToRetrieve = editTextDNI.getText().toString().trim();
        if (!dniToRetrieve.isEmpty()) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String person = snapshot.getValue(String.class);
                        if (person != null && person.contains("DNI: " + dniToRetrieve)) {
                            // Mostrar los datos en los EditText
                            String[] personData = person.split(", ");
                            editTextName.setText(personData[1].split(": ")[1]);
                            editTextEmail.setText(personData[2].split(": ")[1]);
                            // Mostrar los datos en un Toast
                            showToast("Persona recuperada:\n" + person);
                            return;
                        }
                    }
                    showToast("Persona con DNI " + dniToRetrieve + " no encontrada");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showToast("Error al acceder a la base de datos: " + databaseError.getMessage());
                }
            });
        } else {
            showToast("Por favor, introduce un DNI para recuperar");
        }
    }

    private void updatePerson() {
        String dniToUpdate = editTextDNI.getText().toString().trim();
        if (!dniToUpdate.isEmpty()) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String person = snapshot.getValue(String.class);
                        if (person != null && person.contains("DNI: " + dniToUpdate)) {
                            // Actualizar los datos de la persona
                            String updatedName = editTextName.getText().toString().trim();
                            String updatedEmail = editTextEmail.getText().toString().trim();
                            if (!updatedName.isEmpty() && !updatedEmail.isEmpty()) {
                                String updatedPerson = "DNI: " + dniToUpdate + ", Nombre: " + updatedName + ", Email: " + updatedEmail;
                                snapshot.getRef().setValue(updatedPerson)
                                        .addOnSuccessListener(aVoid -> {
                                            showToast("Persona actualizada en la base de datos");
                                            clearFields();
                                        })
                                        .addOnFailureListener(e -> showToast("Error al actualizar persona: " + e.getMessage()));
                                return;
                            } else {
                                showToast("Por favor, completa todos los campos para actualizar");
                                return;
                            }
                        }
                    }
                    showToast("Persona con DNI " + dniToUpdate + " no encontrada");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showToast("Error al acceder a la base de datos: " + databaseError.getMessage());
                }
            });
        } else {
            showToast("Por favor, introduce un DNI para actualizar");
        }
    }


    private void deletePerson() {
        String dniToDelete = editTextDNI.getText().toString().trim();
        if (!dniToDelete.isEmpty()) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String person = snapshot.getValue(String.class);
                        if (person != null && person.contains("DNI: " + dniToDelete)) {
                            snapshot.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        showToast("Persona eliminada de la base de datos");
                                        clearFields();
                                    })
                                    .addOnFailureListener(e -> showToast("Error al eliminar persona: " + e.getMessage()));
                            return;
                        }
                    }
                    showToast("Persona con DNI " + dniToDelete + " no encontrada");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showToast("Error al acceder a la base de datos: " + databaseError.getMessage());
                }
            });
        } else {
            showToast("Por favor, introduce un DNI para eliminar");
        }
    }



    private void clearFields() {
        editTextDNI.setText("");
        editTextName.setText("");
        editTextEmail.setText("");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
