package net.ducksmanager.whattheduck;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import net.ducksmanager.whattheduck.Collection.CollectionType;

import java.util.ArrayList;
import java.util.Locale;

public abstract class List extends ListActivity{
    protected static final int INSERT_ID = 1;
    private static final int MY_COLLECTION_ID = 2;
    private static final int LOGOUT = 3;

    public String type;
    private ArrayList<String> items;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
		type = extras != null && extras.getString("type") != null
					? extras.getString("type")
					: CollectionType.USER.toString();

		setContentView(R.layout.wtd_list);

		this.findViewById(R.id.navigationAllCountries).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				goToView(CountryList.class);
			}
		});

		this.findViewById(R.id.navigationCountry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				goToView(PublicationList.class);
			}
		});

		this.findViewById(R.id.navigationPublication).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				goToView(IssueList.class);
			}
		});

		Switch onlyInCollectionSwitch = (Switch) this.findViewById(R.id.onlyInCollectionSwitch);
		onlyInCollectionSwitch.setChecked(type.equals(CollectionType.USER.toString()));

		onlyInCollectionSwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Switch onlyInCollectionSwitch = (Switch) view;
				List.this.goToAlternativeView(
					onlyInCollectionSwitch.isChecked()
						? CollectionType.USER.toString()
						: CollectionType.COA.toString()
				);
			}
		});
    }

	private void goToView(Class<?> cls) {
		if (!List.this.getClass().equals(cls)) {
			Intent i = new Intent(WhatTheDuck.wtd, cls);
			i.putExtra("type", type);
			startActivity(i);
		}
	}

	private void goToAlternativeView(String collectionType) {
		Intent i = new Intent(WhatTheDuck.wtd, this.getClass());
		i.putExtra("type", collectionType);
		startActivity(i);
	}

    public abstract void show();

    public void show(ArrayList<String> items) {
    	this.items = items;

        if (items.size() == 0) {
            TextView emptyListText = (TextView) this.findViewById(R.id.emptyList);
            emptyListText.setVisibility(TextView.VISIBLE);
        }

    	String[] lv_arr = items.toArray(new String[items.size()]);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lv_arr));

    	if (items.size() > 20) {
    		EditText filterEditText = (EditText) this.findViewById(R.id.filter);
    		filterEditText.setVisibility(EditText.VISIBLE);
    		filterEditText.requestFocus();

    		filterEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) { }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                	String typedText = s.toString();
                	ArrayList<String> filteredItems = new ArrayList<String>();
                	for (String item : List.this.items)
                		if (item.replace("* ", "").toLowerCase(Locale.FRANCE).contains(typedText.toLowerCase()))
                			filteredItems.add(item);

                	String[] lv_arr = filteredItems.toArray(new String[filteredItems.size()]);
                    setListAdapter(new ArrayAdapter<String>(List.this, android.R.layout.simple_list_item_1, lv_arr));

    			}
            });
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (type.equals(CollectionType.USER.toString()))
        	menu.add(0, INSERT_ID, 0, R.string.insert_issue_menu);
        else
        	menu.add(0, MY_COLLECTION_ID, 0, R.string.my_collection_menu);
        menu.add(0, LOGOUT, 1, R.string.logout_menu);

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	return onMenuItemSelected(featureId, item, false);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item, boolean alreadyDone) {
    	if (!alreadyDone) {
    		Intent i = null;
	        switch(item.getItemId()) {
	            case INSERT_ID:
	    	        i = new Intent(WhatTheDuck.wtd, CountryList.class);
	                i.putExtra("type", CollectionType.COA.toString());
	            break;
	            case MY_COLLECTION_ID:
	    	        i = new Intent(WhatTheDuck.wtd, CountryList.class);
	                i.putExtra("type", CollectionType.USER.toString());
	            break;
	            case LOGOUT:
	    	        i = new Intent(WhatTheDuck.wtd, WhatTheDuck.class);
	            	WhatTheDuck.userCollection = new Collection();
	            	WhatTheDuck.coaCollection = new Collection();

	            break;
	        }
	        startActivity(i);
    	}

        return super.onMenuItemSelected(featureId, item);
    }

    protected Collection getCollection() {
    	return type == null || type.equals(CollectionType.USER.toString())
			? WhatTheDuck.userCollection
			: WhatTheDuck.coaCollection;
    }

	protected void setNavigationCountry(String countryFullName, String selectedCountry) {
		View countryNavigationView = this.findViewById(R.id.navigationCountry);

		String uri = "@drawable/flags_" + selectedCountry;
		int imageResource = getResources().getIdentifier(uri, null, getPackageName());

		ImageView currentCountryFlag = (ImageView) countryNavigationView.findViewById(R.id.selectedBadgeImage);
		currentCountryFlag.setVisibility(View.VISIBLE);
		currentCountryFlag.setImageResource(imageResource);

		TextView currentCountryText = (TextView) this.findViewById(R.id.navigationCountry).findViewById(R.id.selected);
		currentCountryText.setText(countryFullName);
	}

	protected void setNavigationPublication(String publicationFullName, String selectedPublication) {
		View publicationNavigationView = this.findViewById(R.id.navigationPublication);
		TextView currentPublicationBadgeText = (TextView) publicationNavigationView.findViewById(R.id.selectedBadge);

		if (selectedPublication == null) {
			publicationNavigationView.setVisibility(View.INVISIBLE);
		}
		else {
			publicationNavigationView.setVisibility(View.VISIBLE);

			currentPublicationBadgeText.setText(selectedPublication.split("/")[1]);

			TextView currentPublicationText = (TextView) publicationNavigationView.findViewById(R.id.selected);
			currentPublicationText.setText(publicationFullName);
		}
	}
}
