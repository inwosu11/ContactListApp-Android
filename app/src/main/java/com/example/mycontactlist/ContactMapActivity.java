package com.example.mycontactlist;

import static java.lang.ref.Cleaner.create;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.health.connect.datatypes.ExerciseRoute;
import android.location.*;
import android.os.Build;
import android.os.Bundle;
//import android.support.design.widget.Snackbar;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactMapActivity extends AppCompatActivity implements OnMapReadyCallback {


    final int PERMISSION_REQUEST_LOCATION = 101;
    GoogleMap gMap;
    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    TextView textDirection;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    LocationManager locationManager;
    LocationListener gpsListener;
    LocationListener networkListener;
    Location currentBestLocation;
   SupportMapFragment mapFragment;

    ArrayList<Contact> contacts = new ArrayList<>();
    Contact currentContact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLocationRequest();
        createLocationCallback();
        setContentView(R.layout.activity_contact_map);
        //startLocationUpdate();
        stopLocationUpdates();

        initMapTypeButtons();


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Bundle extras = getIntent().getExtras();
        mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        try {
            ContactDataSource ds = new ContactDataSource(ContactMapActivity.this);
            ds.open();
            if (extras != null) {
                currentContact = ds.getSpecificContact(extras.getInt("contactid"));
            } else {
                contacts = ds.getContacts("contactname", "ASC");
            }
            ds.close();
        }
        catch (Exception e) {
            Toast.makeText(this, "Contact(s) could not be retrieved.", Toast.LENGTH_LONG).show();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE) ;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ;
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) ;

        if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener (mySensorEventListener, accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST) ;
            sensorManager.registerListener (mySensorEventListener, magnetometer,
                    SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast.makeText (this, "Sensors not found", Toast.LENGTH_LONG).show();
        }
        textDirection = (TextView) findViewById(R.id.textHeading) ;

        //createLocationRequest();
        //createLocationCallback();

        initListButton();
        initMapButton();
        initSettingsButton();


    }
    public void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
            try {
                locationManager.removeUpdates(gpsListener);
                locationManager.removeUpdates(networkListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }




    private void initListButton() {
        ImageButton iblist = findViewById(R.id.imageButtonList);
        iblist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ContactMapActivity.this, ContactListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });
    }

    private void initMapButton() {
        ImageButton iblist = findViewById(R.id.imageButtonMap);
        iblist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ContactMapActivity.this, ContactMapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void initSettingsButton() {
        ImageButton iblist = findViewById(R.id.imageButtonSettings);
        iblist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ContactMapActivity.this, ContactSettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }



    private void createLocationRequest(){
        locationRequest = locationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



    }

   private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location: locationResult.getLocations()) {
                    Toast.makeText(getBaseContext(), "Lat: " + location.getLatitude() +
                            " Long: " + location.getLongitude() + " Accuracy: " + location.getAccuracy(), Toast.LENGTH_LONG).show();
                }
            };
        };
    }
    private void startLocationUpdate() {
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)) {
            return;
        }
       fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        gMap.setMyLocationEnabled(true);

        }
        private void stopLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(getBaseContext(),Manifest.permission.ACCESS_COARSE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED))
        {
            return;
        }
            if (fusedLocationProviderClient != null) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Point size = new Point();
        WindowManager w = getWindowManager();
        w.getDefaultDisplay().getSize(size);
        int measureWidth = size.x;
        int measuredHeight = size.y;

        if (contacts.size()>0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i=0; i<contacts.size(); i++) {
                currentContact = contacts.get (i);

                Geocoder geo = new Geocoder (this) ;
                List<Address> addresses = null;

                String address = currentContact.getStreetAddress () +", "+
                        currentContact.getCity() + ", "+
                        currentContact.getState() + " "+
                        currentContact.getZipCode() ;

                try {
                    addresses = geo.getFromLocationName(address, 1);
                }
                catch (IOException e) {
                    e.printStackTrace () ;
                }
                LatLng point = new LatLng (addresses.get(0).getLatitude(),
                        addresses.get(0).getLongitude());
                builder.include(point);
                gMap.addMarker (new MarkerOptions().position (point).
                        title(currentContact.getContactName()).snippet(address)) ;
            }
            gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), measureWidth, measuredHeight, 450));
        }
        else {
            if(currentContact != null) {
                Geocoder geo = new Geocoder(this);
                List<Address> addresses = null;

                String address = currentContact.getStreetAddress () + ", " +
                        currentContact. getCity () + ", " +
                        currentContact.getState() + " " +
                        currentContact. getZipCode();
                try {
                    addresses = geo.getFromLocationName(address, 1);
                }
                catch (IOException e) {
                    e.printStackTrace () ;
                }
                LatLng point = new LatLng (addresses.get (0).getLatitude(),addresses.get(0).getLongitude ());

                gMap.addMarker (new MarkerOptions().position(point).
                        title (currentContact.getContactName ()) .snippet (address)) ;
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16));
            }
            else {
                AlertDialog alertDialog = new AlertDialog.Builder(
                        ContactMapActivity.this).create();
                alertDialog.setTitle ("No Data");
                alertDialog.setMessage ("No data is available for the mapping function.");
                alertDialog.setButton (AlertDialog.BUTTON_POSITIVE,
                        "OK", new DialogInterface.OnClickListener () {
                            public void onClick (DialogInterface dialog, int which) {

                                finish();
                            } });
                alertDialog.show();
            }
        }

        RadioButton rbNormal = findViewById(R.id.radioButtonNormal);
        rbNormal.setChecked(true);
        try{
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(ContactMapActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            ContactMapActivity.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) ){

                        Snackbar.make(findViewById(R.id.activity_contact_map),
                                        "MyContactList requires this permission to locate " +
                                                "your contacts", Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(
                                                ContactMapActivity.this,
                                                new String[]{
                                                        Manifest.permission.ACCESS_FINE_LOCATION},
                                                PERMISSION_REQUEST_LOCATION);
                                    }
                                })
                                .show();

                    } else {
                        ActivityCompat.requestPermissions(ContactMapActivity.this, new
                                        String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_LOCATION);
                    }
                } else {
                    startLocationUpdate();
                }
            } else {
                startLocationUpdate();
            }
        }
        catch (Exception e) {
            Toast.makeText(getBaseContext(), " Error requesting permission",
                    Toast.LENGTH_LONG).show();
        }

    }


    private void initMapTypeButtons() {
        RadioGroup rgMapType = findViewById (R.id.radioGroupMapType) ;
        rgMapType.setOnCheckedChangeListener (new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rbNormal = findViewById(R.id.radioButtonNormal);
                if (rbNormal.isChecked()) {
                    gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                else {
                        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

            }
        }
    });
}

    private SensorEventListener mySensorEventListener = new SensorEventListener() {
        public void onAccuracyChanged (Sensor sensor, int accuracy) { }
    float [] accelerometerValues;
    float [] magneticValues;
    public void onSensorChanged (SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accelerometerValues = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magneticValues = event.values;
        if (accelerometerValues != null & magneticValues != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I,
                    accelerometerValues, magneticValues);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                float azimut = (float) Math.toDegrees(orientation[0]);
                if (azimut < 0.0f) {
                    azimut += 360.0f;
                }
                String direction;
                if (azimut >= 315 || azimut < 45) {
                    direction = "N";
                } else if (azimut >= 225 & azimut < 315) {
                    direction = "W";
                } else if (azimut >= 135 && azimut < 225) {
                    direction = "S";
                } else {
                    direction = "E";
                }
                textDirection.setText(direction);
            }
        }
    }
};
}