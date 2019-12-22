#!/usr/bin/env bash

set -e

blurPicture() {
    source=$1
    destination=$2
    polygon=$3

    convert \
        "$source" \
        \( -clone 0 -fill white -colorize 100 -fill black \
        -draw "polygon $polygon" \
        -alpha off -write mpr:mask +delete \) \
        -mask mpr:mask -blur 0x5 +mask "$destination"
}

showcasedir=showcase
test_app_name=net.ducksmanager.whattheduck.test
remote_app_dir=/data/local/tmp/${test_app_name}
remote_screenshot_dir=/data/data/net.ducksmanager.whattheduck/files/test-screenshots/${showcasedir}
shellAsApp="adb shell run-as net.ducksmanager.whattheduck"

$shellAsApp rm -rf ${remote_screenshot_dir}
adb shell am force-stop ${test_app_name}
./gradlew :app:assembleDebugAndroidTest
adb push app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk ${remote_app_dir}
adb shell pm install -t -r ${remote_app_dir}
\

adb shell am instrument -w -r -e debug false -e class ListTest,CoverSearchTest,SuggestionsTest ${test_app_name}/androidx.test.runner.AndroidJUnitRunner
$shellAsApp cp -r $remote_screenshot_dir /sdcard
adb pull /sdcard/$showcasedir `pwd`

for locale in fr en sv; do
    blurPicture \
        "$showcasedir/$locale/Cover search result.png" \
        "$showcasedir/$locale/Cover search result_blurred.png" \
        "580,940 700,940 700,1100 580,1100" && \
    blurPicture \
        "$showcasedir/$locale/Cover search result_blurred.png" \
        "$showcasedir/$locale/Cover search result_blurred.png" \
        "410,970 410,1080 550,1080 550,970" && \
    blurPicture \
        "$showcasedir/$locale/Cover search result_blurred.png" \
        "$showcasedir/$locale/Cover search result_blurred.png" \
        "0,940 50,940 50,990 0,990" && \
    blurPicture \
        "$showcasedir/$locale/Cover search result_blurred.png" \
        "$showcasedir/$locale/Cover search result_blurred.png" \
        "1040,1000 1078,1000 1078,1060 1040,1060" && \
    rm "$showcasedir/$locale/Cover search result.png" && \
    blurPicture \
        "$showcasedir/$locale/Collection - Issue list - edge view.png" \
        "$showcasedir/$locale/Collection - Issue list - edge view_blurred.png" \
        "470,430 690,430 690,620 470,620" && \
    blurPicture \
        "$showcasedir/$locale/Collection - Issue list - edge view_blurred.png" \
        "$showcasedir/$locale/Collection - Issue list - edge view_blurred.png" \
        "360,910 480,910 480,1020 360,1020" && \
    rm "$showcasedir/$locale/Collection - Issue list - edge view.png" && \
    composite -gravity center "$showcasedir/$locale/Cover search result - add issue.png"  "$showcasedir/$locale/Cover search result_blurred.png" "$showcasedir/$locale/Cover search result_blurred - with add issue.png" && \
    rm "$showcasedir/$locale/Cover search result - add issue.png"
done