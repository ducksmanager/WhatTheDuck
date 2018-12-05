package net.ducksmanager.whattheduck;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.retrievetasks.CoverSearch;
import net.ducksmanager.util.CoverFlowFileHandler;
import net.ducksmanager.util.Settings;
import net.ducksmanager.whattheduck.Collection.CollectionType;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.GONE;

public abstract class ItemList<Item> extends AppCompatActivity {
    public static String type = CollectionType.USER.toString();
    static final int MIN_ITEM_NUMBER_FOR_FILTER = 20;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Boolean requiresDataDownload = false;

    protected abstract boolean needsToDownloadFullList();
    protected abstract void downloadFullList();
    protected abstract boolean hasDividers();

    protected abstract boolean userHasItemsInCollectionForCurrent();

    protected abstract boolean shouldShow();
    protected abstract boolean shouldShowNavigation();
    protected abstract boolean shouldShowToolbar();
    protected abstract boolean shouldShowAddToCollectionButton();
    protected abstract boolean shouldShowFilter(List<Item> items);

    protected abstract ItemAdapter<Item> getItemAdapter();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wtd_list);

        View navigationAllCountries = this.findViewById(R.id.navigationAllCountries);
        if (navigationAllCountries != null) {
            navigationAllCountries.setOnClickListener(view ->
                goToView(CountryList.class)
            );
        }

        View navigationCurrentCountry = this.findViewById(R.id.navigationCountry);
        if (navigationCurrentCountry != null) {
            navigationCurrentCountry.findViewById(R.id.selected).setOnClickListener(view ->
                goToView(PublicationList.class)
            );
        }

        loadList();
    }

    void loadList() {
        ((WhatTheDuckApplication) getApplication()).trackActivity(this);

        if (needsToDownloadFullList()) {
            this.requiresDataDownload = true;
            downloadFullList();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (type.equals(CollectionType.USER.toString())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
            else {
                actionBar.setDisplayHomeAsUpEnabled(true);
                ((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> onBackFromAddIssueActivity());
            }
        }

        setTitle(
            type.equals(CollectionType.USER.toString())
                    ? getString(R.string.my_collection)
                    : getString(R.string.add_issue)
        );
    }

    private void goToView(Class<?> cls) {
        if (!ItemList.this.getClass().equals(cls)) {
            startActivity(new Intent(WhatTheDuck.wtd, cls));
        }
    }

    void goToAlternativeView(String collectionType) {
        type = collectionType;
        loadList();
        show();
    }

    void show() {
        if (!shouldShow()) {
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (shouldShowToolbar()) {
            toolbar.setVisibility(View.VISIBLE);
            setSupportActionBar(toolbar);
        }
        else {
            toolbar.setVisibility(GONE);
        }

        if (shouldShowNavigation()) {
            setNavigation(WhatTheDuck.getSelectedCountry(), WhatTheDuck.getSelectedPublication());
        }
        else {
            hideNavigation();
        }

        FloatingActionMenu addToCollection = this.findViewById(R.id.addToCollectionWrapper);
        if (shouldShowAddToCollectionButton()) {
            if (addToCollection != null) {
                addToCollection.setMenuButtonColorNormalResId(R.color.holo_green_dark);
                addToCollection.setMenuButtonColorPressedResId(R.color.holo_green_dark);
                addToCollection.setVisibility(type.equals(CollectionType.USER.toString()) ? View.VISIBLE : GONE);
                addToCollection.close(false);

                if (type.equals(CollectionType.USER.toString())) {
                    FloatingActionButton addToCollectionByPhotoButton = this.findViewById(R.id.addToCollectionByPhotoButton);
                    addToCollectionByPhotoButton.setOnClickListener(view ->
                        takeCoverPicture()
                    );

                    FloatingActionButton addToCollectionBySelectionButton = this.findViewById(R.id.addToCollectionBySelectionButton);
                    addToCollectionBySelectionButton.setOnClickListener(view -> {
                        addToCollection.setVisibility(GONE);
                        ItemList.this.goToAlternativeView(CollectionType.COA.toString());
                    });

                    Settings.saveSettings();
                }
            }
        }
        else {
            addToCollection.setVisibility(GONE);
        }

        ItemAdapter<Item> itemAdapter = getItemAdapter();
        if (this.requiresDataDownload) {
            itemAdapter.resetItems();
        }

        java.util.List<Item> items = itemAdapter.getItems();
        TextView emptyListText = this.findViewById(R.id.emptyList);
        if (emptyListText != null) {
            emptyListText.setVisibility(this.requiresDataDownload || items.size() > 0 ? TextView.INVISIBLE : TextView.VISIBLE);
        }

        RecyclerView recyclerView = findViewById(R.id.itemList);
        recyclerView.setAdapter(itemAdapter);

        EditText filterEditText = this.findViewById(R.id.filter);
        if (shouldShowFilter(items)) {
            itemAdapter.addOrReplaceFilterOnChangeListener(filterEditText);
        }
        else {
            filterEditText.setVisibility(GONE);
        }

        while (recyclerView.getItemDecorationCount() > 0) {
            recyclerView.removeItemDecorationAt(0);
        }
        if (hasDividers()) {
            DividerItemDecoration dividerDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                new LinearLayoutManager(this).getOrientation()
            );
            recyclerView.addItemDecoration(dividerDecoration);
        }
    }

    private void takeCoverPicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            CoverFlowFileHandler.current = new CoverFlowFileHandler();
            Uri photoURI = CoverFlowFileHandler.current.createEmptyFileForCamera(ItemList.this);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            this.findViewById(R.id.addToCollectionWrapper).setVisibility(GONE);
            this.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

            CoverFlowFileHandler.current.resizeUntilFileSize(this, new CoverFlowFileHandler.TransformationCallback() {
                @Override
                public void onComplete(File fileToUpload) {
                    new CoverSearch(new WeakReference<>(ItemList.this), fileToUpload).execute();
                }

                @Override
                public void onFail() {
                    ItemList.this.findViewById(R.id.addToCollectionWrapper).setVisibility(View.VISIBLE);
                    ItemList.this.findViewById(R.id.progressBar).setVisibility(GONE);
                }
            });
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;

        switch (item.getItemId()) {
            case R.id.action_logout:
                WhatTheDuck.userCollection = new Collection();
                WhatTheDuck.coaCollection = new Collection();
                Settings.setUsername(null);
                Settings.setPassword(null);
                Settings.saveSettings();
                i = new Intent(WhatTheDuck.wtd, WhatTheDuck.class);

                break;
            case R.id.action_about:
                WhatTheDuck.showAbout(this);

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

    private void hideNavigation() {
        this.findViewById(R.id.navigation).setVisibility(GONE);
    }

    private void setNavigation(String selectedCountry, String selectedPublication) {
        final View generalNavigationView = this.findViewById(R.id.navigation);
        final View countryNavigationView = this.findViewById(R.id.navigationCountry);
        final View publicationNavigationView = this.findViewById(R.id.navigationPublication);

        if (generalNavigationView != null) {
            generalNavigationView.setVisibility(selectedCountry == null ? GONE : View.VISIBLE);
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

                TextView currentCountryText = countryNavigationView.findViewById(R.id.selectedText);
                currentCountryText.setText(countryFullName);
            }

            if (selectedPublication != null) {
                final String publicationFullName = PublicationListing.getPublicationFullName(selectedCountry, selectedPublication);

                TextView currentPublicationBadgeText = publicationNavigationView.findViewById(R.id.selectedBadge);
                currentPublicationBadgeText.setText(selectedPublication.split("/")[1]);

                TextView currentPublicationText = publicationNavigationView.findViewById(R.id.selectedText);
                currentPublicationText.setText(publicationFullName);
            }
        }
    }

    void notifyCompleteList() {
        this.requiresDataDownload = false;
        this.show();
    }

    void onBackFromAddIssueActivity() {
        if (userHasItemsInCollectionForCurrent()) {
            goToAlternativeView(CollectionType.USER.toString());
        }
        else {
            type = CollectionType.USER.toString();
            startActivity(new Intent(WhatTheDuck.wtd, CountryList.class));
        }
    }
}
