package com.example.mycontactlist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactListActivity extends AppCompatActivity {
    RecyclerView contactList;
    ContactAdapter contactAdapter;
    ArrayList<Contact> contacts;

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            int contactId = contacts.get(position).getContactID();
            Intent intent = new Intent(ContactListActivity.this, MainActivity.class);
            intent.putExtra("contactID", contactId);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        initListButton();
        initMapButton();
        initSettingsButton();
        initAddContactButton();
        initDeleteSwitch();
        BroadcastReceiver batteryReceiver = new BroadcastReceiver () {
            @Override
            public void onReceive(Context context, Intent intent) {
                double batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                double levelScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int batteryPercent = (int) Math.floor(batteryLevel / levelScale * 100);
                TextView textBatteryState = (TextView) findViewById(R.id.textBatteryLevel);
                textBatteryState.setText(batteryPercent + "%");
            }
            };
            IntentFilter filter = new IntentFilter (Intent. ACTION_BATTERY_CHANGED) ;
            registerReceiver (batteryReceiver, filter);
    }

    @Override public void onResume() {
        super.onResume();


        String sortBy = getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).getString("sortfield", "contactname");
        String sortOrder = getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).getString("sortorder", "ASC");

        ContactDataSource ds = new ContactDataSource(this);
        try {
            ds.open();
            contacts = ds.getContacts(sortBy, sortOrder);
            ds.close();
            if (contacts.size() > 0) {
                contactList = findViewById(R.id.rvContacts);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
                contactList.setLayoutManager(layoutManager);
                contactAdapter = new ContactAdapter(contacts, this);
                contactAdapter.setOnItemClickListener(onItemClickListener);
                contactList.setAdapter(contactAdapter);
            } else {
                Intent intent = new Intent(ContactListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error retrieving contacts", Toast.LENGTH_LONG).show();
        }
    }

        private void initListButton() {
            ImageButton iblist = findViewById(R.id.imageButtonList);
            iblist.setEnabled(false);
        }


        private void initMapButton () {
            ImageButton iblist = findViewById(R.id.imageButtonMap);
            iblist.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(ContactListActivity.this, ContactMapActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }

        private void initSettingsButton () {
            ImageButton iblist = findViewById(R.id.imageButtonSettings);
            iblist.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(ContactListActivity.this, ContactSettingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });

        }
        private void initAddContactButton () {
            Button newContact = findViewById(R.id.buttonAddContact);
            newContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ContactListActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
        }

        private void initDeleteSwitch() {
            Switch s = findViewById(R.id.switchDelete);
            s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Boolean status = compoundButton.isChecked();
                    contactAdapter.setDelete(status);
                    contactAdapter.notifyDataSetChanged();
                }
            });
        }
    }
