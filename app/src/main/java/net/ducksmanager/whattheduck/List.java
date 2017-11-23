package net.ducksmanager.whattheduck;


import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.retrievetasks.CoverSearch;
import net.ducksmanager.util.CoverFlowFileHandler;
import net.ducksmanager.whattheduck.Collection.CollectionType;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

public abstract class List<Item> extends ListActivity{
    private static final int LOGOUT = 1;

    String type;
    ArrayList<Item> items;
    ItemAdapter itemAdapter;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WhatTheDuckApplication) getApplication()).trackActivity(this);

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

        this.findViewById(R.id.navigationCountry).findViewById(R.id.selected).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToView(PublicationList.class);
            }
        });

        Switch onlyInCollectionSwitch = this.findViewById(R.id.onlyInCollectionSwitch);
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

        if (type.equals(CollectionType.USER.toString())) {
            RelativeLayout addToCollection = this.findViewById(R.id.addToCollectionWrapper);

            addToCollection.setVisibility(View.VISIBLE);
            addToCollection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeCoverPicture();
                }
            });

            if (WhatTheDuck.getShowCoverTooltip()) {
                new SimpleTooltip.Builder(this)
                    .anchorView(addToCollection)
                    .text(R.string.add_cover_tooltip)
                    .gravity(Gravity.TOP)
                    .animated(true)
                    .margin(5.0f)
                    .transparentOverlay(true)
                    .build()
                    .show();
            }

            WhatTheDuck.setShowCoverTooltip(false);
            WhatTheDuck.saveSettings(null);
        }

        setTitle(
            type.equals(CollectionType.USER.toString())
                    ? getString(R.string.my_collection)
                    : getString(R.string.referenced_issues)
        );
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

    protected abstract void show();

    void show(ItemAdapter<Item> itemAdapter) {
        this.itemAdapter = itemAdapter;
        this.items = itemAdapter.getItems();

        if (items.size() == 0) {
            TextView emptyListText = this.findViewById(R.id.emptyList);
            emptyListText.setVisibility(TextView.VISIBLE);
        }

        setListAdapter(this.itemAdapter);

        EditText filterEditText = this.findViewById(R.id.filter);
        if (items.size() > 20) {
            getListView().setTextFilterEnabled(true);
            filterEditText.setVisibility(EditText.VISIBLE);

            filterEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) { }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    List.this.itemAdapter.updateFilteredList(s.toString());
                    List.this.itemAdapter.notifyDataSetInvalidated();
                }
            });
        }
        else {
            getListView().setTextFilterEnabled(false);
            filterEditText.setVisibility(EditText.GONE);
        }
    }

    private void takeCoverPicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            CoverFlowFileHandler.current = new CoverFlowFileHandler();
            Uri photoURI = CoverFlowFileHandler.current.createEmptyFileForCamera(List.this);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            this.findViewById(R.id.addToCollectionWrapper).setVisibility(View.GONE);
            this.findViewById(R.id.progressBarLoading).setVisibility(View.VISIBLE);

            CoverFlowFileHandler.current.resizeUntilFileSize(this, new CoverFlowFileHandler.TransformationCallback() {
                @Override
                public void onComplete(File fileToUpload) {
                    new CoverSearch(new WeakReference<Activity>(List.this), fileToUpload).execute();
                }

                @Override
                public void onFail() {
                    List.this.findViewById(R.id.addToCollectionWrapper).setVisibility(View.VISIBLE);
                    List.this.findViewById(R.id.progressBarLoading).setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, LOGOUT, 1, R.string.logout_menu);

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Intent i = null;
        switch(item.getItemId()) {
            case LOGOUT:
                WhatTheDuck.userCollection = new Collection();
                WhatTheDuck.coaCollection = new Collection();
                WhatTheDuck.setUsername(null);
                WhatTheDuck.setPassword(null);
                WhatTheDuck.saveSettings(false);
                i = new Intent(WhatTheDuck.wtd, WhatTheDuck.class);

                break;
        }
        startActivity(i);

        return super.onMenuItemSelected(featureId, item);
    }

    Collection getCollection() {
        return type == null || type.equals(CollectionType.USER.toString())
            ? WhatTheDuck.userCollection
            : WhatTheDuck.coaCollection;
    }

    void setNavigationCountry(String selectedCountry) {
        final String countryFullName = CountryListing.getCountryFullName(selectedCountry);

        View countryNavigationView = this.findViewById(R.id.navigationCountry);

        String uri = "@drawable/flags_" + selectedCountry;
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());

        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown;
        }

        ImageView currentCountryFlag = countryNavigationView.findViewById(R.id.selectedBadgeImage);
        currentCountryFlag.setVisibility(View.VISIBLE);
        currentCountryFlag.setImageResource(imageResource);

        TextView currentCountryText = countryNavigationView.findViewById(R.id.selected);
        currentCountryText.setText(countryFullName);
    }

    void setNavigationPublication(String selectedCountry, String selectedPublication) {
        final String publicationFullName = PublicationListing.getPublicationFullName(selectedCountry, selectedPublication);

        View publicationNavigationView = this.findViewById(R.id.navigationPublication);
        TextView currentPublicationBadgeText = publicationNavigationView.findViewById(R.id.selectedBadge);

        if (selectedPublication == null) {
            publicationNavigationView.setVisibility(View.INVISIBLE);
        }
        else {
            publicationNavigationView.setVisibility(View.VISIBLE);

            currentPublicationBadgeText.setText(selectedPublication.split("/")[1]);

            TextView currentPublicationText = publicationNavigationView.findViewById(R.id.selected);
            currentPublicationText.setText(publicationFullName);
        }
    }
}
