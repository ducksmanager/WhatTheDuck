Version 2.14.1

    Add Tunisia flag

Version 2.14.0

    Make the story list of a suggestion collapsible
    Show the cover of the issue that's about to be added
    Allow to add an issue from a list of recent issues
    Add ripple effect on countries, publications and recent issues
    Animate item to scroll to

Version 2.13.5

    Fix crash on clicking on "Add a copy" tab in some race conditions

Version 2.13.4

    Fix crash on camera opening due to unrequested permissions
    Fix crash on edge upload screen for Android < 8
    Fix edge upload suggestion message appearing even when all the edges are available

Version 2.13.0

    Allow to search for issues by story title
    Allow to send edge photos

Version 2.12.0

    Show issue popularity in cover search results
    Fix purchase list refresh after purchase creation
    Fix "No issue in list" message not shown when the user has no issue at all
    Fix total number of issues shown as 0 when in offline mode

Version 2.11.2

    Improve cover search results

Version 2.11.1

    Fix displayed app name
    Make the zoom wrapper be non-draggable
    Add default cover if it can't be retrieved

Version 2.11.0

    Add gallery view
    Migrate the carousel layout to a new library
    Show number of copies on stats screen
    Redirect to the Inducks story webpage when the user taps on a story in the suggestion list
    Add missing Luxembourg flag

Version 2.10.2

    Fix crash when an Inducks issue doesn't have a publication code

Version 2.10.1

    Fix new version detection

Version 2.10.0 "Fantomius"

    Add fast scroll
    Show the number of issues owned for each country
    Show the total number of issues owned in the item list activity title
    Set a condition by default when adding issues
    Stats: Fix text if the user only owns issues from a single country or a single publication

Version 2.9.3

    Redirect to the login screen if the user has changed his password elsewhere

Version 2.9.2

    Stats: Fix wrong x-axis label if the user has issues with an unknown purchase date

Version 2.9.1

    Stats: Fix crash when the user only has issues more recent than a year ago

Version 2.9.0 "Phantom Blot"
    
    Add Stats screen
    Add social network links on Settings page
    Fix notifications not registered in some cases
    Fix calculation of number/percentage of owned issues per publication and per country when duplicate issues are owned 

Version 2.8.1

    Fix crash when attempting to add multiple non-owned issues
    Fix crash when the current issue's purchase is not found in the list of purchases

Version 2.8.0 "Little Helper"

    Allow to add multiple copies of the same issue
    Show user medals in navigation drawer

Version 2.7.4

    Fix crash on cover search results when owned issues have an unspecified condition

Version 2.7.3

    Fix crash on return press when selecting a cover file
    Add Sentry integration

Version 2.7.2

    Add quotation information on cover results, when available
    Allow to search a cover by picking a file

Version 2.7.1

    Fix missing suggestions when sorting them by release date
    Improve performance of suggestion screen

Version 2.7.0 "Jubal Pomp"

    Add "My favorite authors" screen
    Add decimal to country ownership percentage

Version 2.6.4

    Fix crash on old Android versions due to unsupported SQLite syntax

Version 2.6.3

    Show ownership percentage per country
    Fix country order when they contain accents

Version 2.6.2

    Fix inverted logic for DB sync check
    Show details in case of internal errors
    Possible crash fix on landscape edge view
    Fix edge view orientation

Version 2.6.1

    Bump version due to Play Console issue

Version 2.6.0 "OK Quack"

    Allow to update and remove issues
    Add "Forgot password" page
    Add context menu to copy the list of owned or missing issues in the current view
    Show message when a new version is available

Version 2.5.1

    Fix background in notified countries list

Version 2.5.0 "Ludwig Von Drake"

    Show the amount of issues possessed by the user for each publication
    Add "Report" screen
    Add loading screen with app logo when loading the collection during app launch

Version 2.4.4

    Fix showing collection after adding an issue (empty screen)

Version 2.4.3 "Fethry"

    Offline mode

Version 2.3.1

    Fix suggested countries layout

Version 2.3.0 "Gyro"

    Show issue titles when they exist

Version 2.2.0 "Flintheart"

    Allow adding multiple issues to the collection at once

Version 2.1.0 "Gus"

    Bump Gradle and various dependencies
    Show a specific message if no suggestions are available to the current user

Version 2.0.6

    Clear cache on app start
    
Version 2.0.5

    Fix Matomo tracking

Version 2.0.4

    Fix new user form

Version 2.0.3

    Distinguish between enabling notifications and enabling suggestions

Version 2.0.2

    Fix cover search results layout

Version 2.0.1

    Not released

Version 2.0.0 "Cornelius"
    
    Add suggestion screen and suggestion notifications
    Convert to Kotlin

Version 1.9.1

    Fix cover search not working

Version 1.9.0 "Elvira"

    Fetch data from the DucksManager API
    Store local data in a DB instead of in memory
    Always remember the user credentials

Version 1.8.7

    Fix back button sometimes not working
    
Version 1.8.6

    Fix network issue on Android Pie

Version 1.8.5

    Access the WTD server directly
    Minor improvements

Version 1.8.4

    Android Support library => AndroidX

Version 1.8.3

    Improve error handling when retrieving the user's collection

Version 1.8.2

    Improve performance on list views

Version 1.8.1

    Add bookcase view on issue lists

Version 1.7.5

    Improve error handling

Version 1.7.4

    Fix resizing cover photos before uploading them

Version 1.7.3

    Fix issue leading to the list of issues not showing properly

Version 1.7.2

    Fix filters not working
    Show progress bars in the middle of the screen
    Fix rare crash when tracking user events after the application context was lost

Version 1.7.1

    Remove "Add issue by cover" tooltip
    Fix half-hidden fields when creating a new purchase date
    Fix purchase creation not working when the description contains special characters or spaces

Version 1.7.0

    Use RecyclerView for all lists
    Use a sub-menu in the "+" floating button to add issues by selection instead of the "Only in collection <-> Referenced issues" switch

Version 1.6.5

    Retrieve COA lists from the DM server instead of the DM gateway
    Fix first cover photographed is always used for cover search
    Init taking automatic screenshots during tests
    Fix test image included in release build

Version 1.6.4

    Use an AppBar instead of an ActionBar

Version 1.6.3

    Improve performance when loading lists

Version 1.6.2

    Fix empty list of purchase dates after clicking on a cover following a cover search

Version 1.6.1

    Show purchase dates in the issue list

Version 1.6.0

    Add possibility to specify a purchase date when adding issues

Version 1.5.6

    Add analytics

Version 1.5.5

    Update build tools

Version 1.5.4

    Auto-login if the user has saved credentials
    Small bugfixes

Version 1.5.3.1

    Fix filter box shown before list loading

Version 1.5.3

    Improve GUI: show items in bold (instead of prefixing them with a star) when possessed
    Small bugfixes
    Internal changes

Version 1.5.2

    Resize cover before upload
    Fix error when attempting to show alerts
    Fix "Remember my credentials" not working
    
Version 1.5.1

    Add Swedish translation
    
Version 1.5.0

    Set English as default language and add its translations
    Cover search minor fixes

Version 1.4.0

    Introduce adding an issue by taking a picture of its cover

Version 1.3.1

    Fix wrong issue condition images shown when scrolling in the issue list
    
Version 1.3
    
    New countries <-> publications <-> issues navigation menu
    Faster loading of COA lists
    Fix going back to collection when pressing the Back button from login view
    
Version 1.2.3
    
    Disable focus on the filter text input when loading list views
    
Version 1.2.2
    
    Technical upgrade (migrate to Gradle)

Version 1.2

    Add signup screen

Version 1.1.5.1

    Fix startup crash

Version 1.1.5

    Fix error on login

Version 1.1.4 

    Preparation for version 1.2

Version 1.1.3 

    Network problems are now more precisely identified
    Some dialogs (such as the one which confirms that an issue has been added to the collection) are now non-modal, they disappear automatically after a few seconds.

Version 1.1.2 

    Security improvement

Version 1.1.1 

    New logo !

Version 1.1 

    The issue condition is displayed in the issue list.
    When adding an issue in the collection, the issue condition can be given.
