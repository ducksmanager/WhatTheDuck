## What The Duck

What The Duck is an Android app allowing users from comic-management website [DucksManager](http://www.ducksmanager.net) ([also on GitHub](http://github.com/bperel/DucksManager)) to view and modify the content of their collection from an Android mobile phone or tablet.

## Setup

Duplicate [app/src/main/assets/config.properties.sample](app/src/main/assets/config.properties.sample) and rename the copy to config.properties.sample, then modify the configuration properties.

## Features

* Login with a user's DucksManager credentials
* Browse through the user's collection
* List the existing issues for a publication, with those possessed by the user being marked
* Add an existing issue to the user's collection
* ... And more features are planned.


## Architecture overview

What The Duck connects to a server owned by DucksManager (hereunder being known as "the COA database") to retrieve both the user's collection and the numbers of the issues he doesn't have.
