## What The Duck

What The Duck is an Android app allowing users from comic-management website [DucksManager](http://www.ducksmanager.net) ([also on GitHub](http://github.com/bperel/DucksManager)) to view and modify the content of their collection from an Android mobile phone or tablet.

## Setup

Duplicate [app/src/main/assets/config.properties.sample](app/src/main/assets/config.properties.sample) and rename the copy to config.properties.sample, then modify the configuration properties.

## Features

* Login with a user's DucksManager credentials
* Browse through the user's collection
* List the existing issues for a publication, with those possessed by the user being marked
* Add an existing issue to the user's collection, either by selecting it or by taking a picture of its cover

* ... And more features are planned.

## Related projects and architecture

* [DucksManager](https://github.com/bperel/DucksManager) is a free and open-source website enabling comic book collectors to manage their Disney collection.
* [dm-server](https://github.com/bperel/dm-server) is the back-end project that DucksManager reads and writes data from/to.
* [EdgeCreator](https://github.com/bperel/EdgeCreator) is a project allowing users to upload photos of edges and create models out of them in order to generate edge pictures.
* [Duck cover ID](https://github.com/bperel/duck-cover-id) is a collection of shell scripts launched by a daily cronjob, allowing to retrieve comic book covers from the Inducks website and add the features of these pictures to a Pastec index. This index is searched whn taking a picture of a cover in the WhatTheDuck app.
* [COA updater](https://github.com/bperel/coa-updater) is a shell script launched by a daily cronjob, allowing to retrieve the structure and the contents of the Inducks database and to create a copy of this database locally.
* [DucksManager-stats](https://github.com/bperel/DucksManager-stats) contains a list of scripts launched by a daily cronjob, allowing to calculate statistics about issues that are recommended to users on DucksManager, depending on the authors that they prefer.

![DucksManager architecture](https://raw.githubusercontent.com/bperel/DucksManager/master/server_architecture.png)

## Thanks

[![BrowserStack](https://i1.wp.com/www.diogonunes.com/blog/wp-content/uploads/2016/07/browserstack-logo.png?w=250)](https://www.browserstack.com)

Thanks to the [**BrowserStack**](https://www.browserstack.com) team for offering free accounts to open-source projects!
