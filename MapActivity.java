package com.example.places;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity {



    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient myclient;
    Location lastLocation;
    double dlatitude, dlongitude;
    boolean flag = false;
    GoogleMap gmap;
    EditText etsearch;
    private static final int M_MAX_ENTRIES = 10;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    ListView lstPlaces;
    PlacesClient placesClient;
    String key = "AIzaSyDDgQiKdndnQmb5HQG1RZghJtzTOye_SHo";
    AutocompleteSupportFragment autocompleteSupportFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().
                        findFragmentById(R.id.map);
        myclient = LocationServices.
                getFusedLocationProviderClient(this);
        lstPlaces = findViewById(R.id.lv);
        checkLocationPermission();
        if (!Places.isInitialized())
            Places.initialize(getApplicationContext(), key);
        placesClient = Places.createClient(this);
        autocompleteSupportFragment =
                (AutocompleteSupportFragment)
                        getSupportFragmentManager().
                                findFragmentById(R.id.autocomplete_fragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList
                (Place.Field.ID, Place.Field.NAME));

        autocompleteSupportFragment.setOnPlaceSelectedListener
                (new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                        if (place != null) {

                            LatLng latLng = place.getLatLng();
                            gmap.moveCamera(CameraUpdateFactory.
                                    newLatLng(latLng));
                            if (checkSelfPermission(Manifest.permission.
                                    ACCESS_FINE_LOCATION) != PackageManager.
                                    PERMISSION_GRANTED && checkSelfPermission
                                    (Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                    PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            gmap.setMyLocationEnabled(true);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title("You selected Place is this");
                            Toast.makeText(MapActivity.this,
                                    place.getName(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onError(@NonNull Status status) {
                    }
                });
    }
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.
                            ACCESS_FINE_LOCATION}, 1);
        } else {
            flag = true;
            Toast.makeText(this,
                    "Location Permission Granted",
                    Toast.LENGTH_SHORT).show();
            myclient.getLastLocation().addOnSuccessListener
                    (new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lastLocation = location;
                                dlatitude = lastLocation.getLatitude();
                                dlongitude = lastLocation.getLongitude();
                                initMap();
                            }
                        }
                    });
        }
    }

    void initMap() {
        Toast.makeText(this, "Initialize Map", Toast.LENGTH_SHORT).show();
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gmap = googleMap;
                if (flag) {
                    LatLng mylatlng = new LatLng(dlatitude, dlongitude);
                    gmap.moveCamera(CameraUpdateFactory.newLatLng(mylatlng));
                    gmap.setMyLocationEnabled(true);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mylatlng);
                    markerOptions.title("You are here");
                    gmap.addMarker(markerOptions);

                    gmap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange(Location location) {
                            //gmap.clear();
                            LatLng mylatlng = new LatLng(location.getLatitude(), location.getLongitude());
                            gmap.moveCamera(CameraUpdateFactory.newLatLng(mylatlng));

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(mylatlng);
                            markerOptions.title("Updated Location");
                            gmap.addMarker(markerOptions);
                        }
                    });

                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void doSearch(View view) {
        String search = etsearch.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        try {
            List<Address> locationList = geocoder.getFromLocationName(search, 1);
            if (locationList.size() > 0) {
                Address address = locationList.get(0);

                LatLng mylatlng = new LatLng(address.getLatitude(), address.getLongitude());
                gmap.moveCamera(CameraUpdateFactory.newLatLng(mylatlng));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(mylatlng);
                markerOptions.title("Updated Location");
                gmap.addMarker(markerOptions);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getCurrentPlace(View view) {
        Toast.makeText(this, "in on click", Toast.LENGTH_SHORT).show();

        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        Task<FindCurrentPlaceResponse> placeresponse = placesClient.findCurrentPlace(request);

        placeresponse.addOnCompleteListener(this, new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(MapActivity.this, "Sucessful", Toast.LENGTH_SHORT).show();

                    FindCurrentPlaceResponse response = task.getResult();
                    // Set the count, handling cases where less than 5 entries are returned.
                    int count;
                    if (response.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                        count = response.getPlaceLikelihoods().size();
                    } else {
                        count = M_MAX_ENTRIES;
                    }

                    int i = 0;
                    mLikelyPlaceNames = new String[count];
                    mLikelyPlaceAddresses = new String[count];
                    mLikelyPlaceAttributions = new String[count];
                    mLikelyPlaceLatLngs = new LatLng[count];

                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Place currPlace = placeLikelihood.getPlace();
                        mLikelyPlaceNames[i] = currPlace.getName();
                        mLikelyPlaceAddresses[i] = currPlace.getAddress();
                        mLikelyPlaceAttributions[i] = (currPlace.getAttributions() == null) ? null : String.join(" ", currPlace.getAttributions());
                        mLikelyPlaceLatLngs[i] = currPlace.getLatLng();

                        String currLatLng = (mLikelyPlaceLatLngs[i] == null) ?
                                "" : mLikelyPlaceLatLngs[i].toString();

                        Log.i("TAG", String.format("Place " + currPlace.getName()
                                + " has likelihood: " + placeLikelihood.getLikelihood()
                                + " at " + currLatLng));

                        i++;
                        if (i > (count - 1)) {
                            break;
                        }
                    }

                    fillPlacesList();
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e("TAG", "Place not found: " + apiException.getStatusCode());
                    }
                }
            }
        });
    }

    private void fillPlacesList() {
        // Set up an ArrayAdapter to convert likely places into TextViews to populate the ListView
        ArrayAdapter<String> placesAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mLikelyPlaceNames);

        lstPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // position will give us the index of which place was selected in the array
                LatLng markerLatLng = mLikelyPlaceLatLngs[position];
                String markerSnippet = mLikelyPlaceAddresses[position];
                if (mLikelyPlaceAttributions[position] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[position];
                }

                gmap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[position])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                gmap.moveCamera(CameraUpdateFactory.newLatLng(markerLatLng));

            }
        });
        lstPlaces.setAdapter(placesAdapter);
    }

}



