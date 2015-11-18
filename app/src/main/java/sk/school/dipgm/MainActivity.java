package sk.school.dipgm;

import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    MapView mapView = null;
    List<MarkerOptions> markerOptionses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setStyleUrl(getString(R.string.map_style));
        mapView.setAccessToken(getString(R.string.access_token));
        mapView.setCenterCoordinate(new LatLng(48.152983, 17.0712967));
        mapView.setZoomLevel(11);
        mapView.onCreate(savedInstanceState);
    }

    private void makeMarkers(final String requestString){
        new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(getString(R.string.server_address));
                    URLConnection connection = url.openConnection();

                    connection.setDoOutput(true);
                    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                    out.write(requestString);
                    out.close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String returnString = "", result = "";
                    while ((returnString = in.readLine()) != null) {
                        if (!TextUtils.isEmpty(returnString)) {
                            result += returnString;
                        }
                    }

                    in.close();

                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String name = jsonObject.optString("name");
                        if (TextUtils.isEmpty(name)) {
                            name = "Hotel";
                        }

                        String points = jsonObject.getString("points");

                        String[] leftRight = points.split(Pattern.quote(" "));

                        String y = leftRight[0].split(Pattern.quote("("))[1];
                        String x = leftRight[1].split(Pattern.quote(")"))[0];

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(Double.parseDouble(x), Double.parseDouble(y)));
                        markerOptions.title(name);

                        markerOptionses.add(markerOptions);
                        mapView.addMarker(markerOptions);
                    }
                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                }
            }
        }).start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        removeMarkers();

        String nearQuery = "with index_query as (\n" +
                "\tselect name, st_astext(way) as points, way <-> ST_SetSRID(ST_MakePoint(17.0712967, 48.152983),4326) as distance\n" +
                "\tfrom planet_osm_point\n" +
                "\twhere tourism like lower('%s')\n" +
                "\torder by way <-> ST_SetSRID(ST_MakePoint(17.0712967, 48.152983),4326) asc\n" +
                "\tlimit 100\n" +
                ")\n" +
                "\n" +
                "select name, points from index_query order by distance limit 3;";

        String query = "select name, " +
                "st_astext(way) as points " +
                "from planet_osm_point " +
                "where tourism like '%s';";

        if (id == R.id.action_hotels) {
            makeMarkers(String.format(query, "hotel"));
        } else if (id == R.id.action_near_hotels){
            makeMarkers(String.format(nearQuery, "hotel"));
        } else if (id == R.id.action_hostels){
            makeMarkers(String.format(query, "hostel"));
        } else if (id == R.id.action_near_hostels){
            makeMarkers(String.format(nearQuery, "hostel"));
        } else if (id == R.id.action_guest_house){
            makeMarkers(String.format(query, "guest_house"));
        } else if (id == R.id.action_near_guest_house){
            makeMarkers(String.format(nearQuery, "guest_house"));
        }

        return super.onOptionsItemSelected(item);
    }

    void removeMarkers(){
        for (MarkerOptions markerOption : markerOptionses) {
            markerOption.getMarker().remove();
        }
        markerOptionses = new ArrayList<>();
    }
}
