package net.ducksmanager.whattheduck;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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

public abstract class List<Item> extends AppCompatActivity {
    ListView lv;

    String type;
    private Boolean requiresDataDownload = false;
    private ItemAdapter itemAdapter;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    protected abstract boolean needsToDownloadFullList();
    protected abstract void downloadFullList();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        type = extras != null && extras.getString("type") != null
                    ? extras.getString("type")
                    : CollectionType.USER.toString();

        setContentView(R.layout.wtd_list);

        this.findViewById(R.id.navigationAllCountries).setOnClickListener(view ->
            goToView(CountryList.class)
        );

        this.findViewById(R.id.navigationCountry).findViewById(R.id.selected).setOnClickListener(view ->
            goToView(PublicationList.class)
        );

        this.findViewById(R.id.navigationCountry).findViewById(R.id.selectedBadgeImage).setOnClickListener(view ->
            goToView(PublicationList.class)
        );

        Switch onlyInCollectionSwitch = this.findViewById(R.id.onlyInCollectionSwitch);
        onlyInCollectionSwitch.setChecked(type.equals(CollectionType.USER.toString()));

        onlyInCollectionSwitch.setOnClickListener(view -> {
            Switch onlyInCollectionSwitch1 = (Switch) view;
            List.this.goToAlternativeView(
                onlyInCollectionSwitch1.isChecked()
                    ? CollectionType.USER.toString()
                    : CollectionType.COA.toString()
            );
        });

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        loadList();
    }

    private void loadList() {
        ((WhatTheDuckApplication) getApplication()).trackActivity(this);

        if (needsToDownloadFullList()) {
            this.requiresDataDownload = true;
            downloadFullList();
        }

        RelativeLayout addToCollection = this.findViewById(R.id.addToCollectionWrapper);
        addToCollection.setVisibility(type.equals(CollectionType.USER.toString()) ? View.VISIBLE : View.GONE);

        if (type.equals(CollectionType.USER.toString())) {
            addToCollection.setOnClickListener(view ->
                takeCoverPicture()
            );

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
        type = collectionType;
        loadList();
        show();
    }

    protected abstract void show();

    void show(ItemAdapter<Item> itemAdapter) {
        this.itemAdapter = itemAdapter;

        setNavigation(WhatTheDuck.getSelectedCountry(), WhatTheDuck.getSelectedPublication());

        ProgressBar loadingProgressBar = this.findViewById(R.id.progressBarLoading);
        TextView emptyListText = this.findViewById(R.id.emptyList);

        ArrayList<Item> items;
        if (this.requiresDataDownload) {
            items = new ArrayList<>();
            emptyListText.setVisibility(TextView.INVISIBLE);
            loadingProgressBar.setVisibility(TextView.VISIBLE);
        }
        else {
            items = itemAdapter.getItems();

            emptyListText.setVisibility(items.size() == 0 ? TextView.VISIBLE : TextView.INVISIBLE);
            loadingProgressBar.setVisibility(TextView.INVISIBLE);
        }

        lv = this.findViewById(R.id.itemList);
        lv.setAdapter(this.itemAdapter);
        lv.setOnItemClickListener(getOnItemClickListener());

        EditText filterEditText = this.findViewById(R.id.filter);
        if (items.size() > 20) {
            lv.setTextFilterEnabled(true);
            filterEditText.setVisibility(EditText.VISIBLE);
            filterEditText.setText("");

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
            lv.setTextFilterEnabled(false);
            filterEditText.setVisibility(EditText.GONE);
        }
    }

    protected abstract AdapterView.OnItemClickListener getOnItemClickListener();

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
                    new CoverSearch(new WeakReference<>(List.this), fileToUpload).execute();
                }

                @Override
                public void onFail() {
                    List.this.findViewById(R.id.addToCollectionWrapper).setVisibility(View.VISIBLE);
                    List.this.findViewById(R.id.progressBarLoading).setVisibility(View.GONE);
                }
            });
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;

        switch (item.getItemId()) {
            case R.id.action_logout:
                WhatTheDuck.userCollection = new Collection();
                WhatTheDuck.coaCollection = new Collection();
                WhatTheDuck.setUsername(null);
                WhatTheDuck.setPassword(null);
                WhatTheDuck.saveSettings(false);
                i = new Intent(WhatTheDuck.wtd, WhatTheDuck.class);

                break;
        }
        if (i == null) {
            return super.onOptionsItemSelected(item);
        }
        else {
            startActivity(i);
            return true;
        }
    }

    Collection getCollection() {
        return type == null || type.equals(CollectionType.USER.toString())
            ? WhatTheDuck.userCollection
            : WhatTheDuck.coaCollection;
    }

    private void setNavigation(String selectedCountry, String selectedPublication) {
        final View generalNavigationView = this.findViewById(R.id.navigation);
        final View countryNavigationView = this.findViewById(R.id.navigationCountry);
        final View publicationNavigationView = this.findViewById(R.id.navigationPublication);

        generalNavigationView.setVisibility(selectedCountry == null ? View.GONE : View.VISIBLE);
        publicationNavigationView.setVisibility(selectedPublication == null ? View.INVISIBLE : View.VISIBLE);

        if (selectedCountry != null) {
            final String countryFullName = CountryListing.getCountryFullName(selectedCountry);

            String uri = "@drawable/flags_" + selectedCountry;
            int imageResource = getResources().getIdentifier(uri, null, getPackageName());

            if (imageResource == 0) {
                imageResource = R.drawable.flags_unknown;
            }

            ImageView currentCountryFlag = countryNavigationView.findViewById(R.id.selectedBadgeImage);
            currentCountryFlag.setImageResource(imageResource);

            TextView currentCountryText = countryNavigationView.findViewById(R.id.selected);
            currentCountryText.setText(countryFullName);
        }

        if (selectedPublication != null) {
            final String publicationFullName = PublicationListing.getPublicationFullName(selectedCountry, selectedPublication);

            TextView currentPublicationBadgeText = publicationNavigationView.findViewById(R.id.selectedBadge);
            currentPublicationBadgeText.setText(selectedPublication.split("/")[1]);

            TextView currentPublicationText = publicationNavigationView.findViewById(R.id.selected);
            currentPublicationText.setText(publicationFullName);
        }
    }

    void notifyCompleteList() {
        this.requiresDataDownload = false;
        this.show();
    }
}
