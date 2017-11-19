package com.status.googleencoder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private final Context mContext;
    private List<Location> mResults;


    public LocationAutoCompleteAdapter(Context context) {
        mContext = context;
        mResults = new ArrayList<Location>();
    }

    @Override
    public int getCount() {
        return mResults.size();
    }

    @Override
    public Location getItem(int index) {
        return mResults.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        Location location = getItem(position);
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(location.getName());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Location> locations = findLocations(constraint.toString());
                    // Assign the data to the FilterResults
                    filterResults.values = locations;
                    filterResults.count = locations.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mResults = (List<Location>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};

        return filter;
    }

    private List<Location> findLocations(String part) {

        String name, place_id;
        double lat, lng;
        JSONArray jsonArray;
        JSONObject jsonObject, jObject, jsonObjectPlace, jsonObjectLocation;
        List<Location> l = new ArrayList<Location>();

        part = part.replaceAll(" ", ",");
        StringBuilder buf = MainActivity.getStringJSON("https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + part + "&key=" + mContext.getResources().getString(R.string.API_KEY));
        try{
            jsonObject = new JSONObject(buf.toString());
            String status = jsonObject.getString("status");
            if(status.equals("OK")){
                jsonArray = jsonObject.getJSONArray("predictions");
                for(int i = jsonArray.length() - 1; i >= 0; i--){
                    jObject = jsonArray.getJSONObject(i);
                    name = jObject.getString("description");
                    place_id = jObject.getString("place_id");
                    buf = MainActivity.getStringJSON("https://maps.googleapis.com/maps/api/geocode/json?place_id=" + place_id + "&key=" + mContext.getResources().getString(R.string.API_KEY));
                    try{
                        jsonObjectPlace = new JSONObject(buf.toString());
                        status = jsonObjectPlace.getString("status");
                        if(status.equals("OK")) {
                            jsonObjectLocation = jsonObjectPlace.getJSONArray("results")
                                    .getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                            lat = jsonObjectLocation.getDouble("lat");
                            lng = jsonObjectLocation.getDouble("lng");
                            l.add(new Location(name,lat,lng));
                        }
                        else{
                            Toast.makeText(mContext,"Invalid result from Google geoencoder",Toast.LENGTH_SHORT).show();
                            return null;
                        }
                    }
                    catch (JSONException e){};

                }
                Collections.reverse(l);
                return l;

            }

            else{
                Toast.makeText(mContext,"Invalid result from Google geoencoder",Toast.LENGTH_SHORT).show();
                return null;
            }



        }
        catch (JSONException e){};

        return  null;
    }

    }
