#!/usr/bin/env bash
dirname=showcase
test_app_name=net.ducksmanager.whattheduck.test
remote_app_dir=/data/local/tmp/${test_app_name}
remote_screenshot_dir=/data/data/net.ducksmanager.whattheduck/files/test-screenshots/${dirname}
shellAsApp="adb shell run-as net.ducksmanager.whattheduck"

$shellAsApp rm -r ${remote_screenshot_dir}
adb shell am force-stop ${test_app_name} && \
./gradlew :app:assembleDebugAndroidTest && \
adb push app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk ${remote_app_dir} && \
adb shell pm install -t -r ${remote_app_dir} && \
\
adb shell am instrument -w -r -e debug false -e class ListTest ${test_app_name}/android.support.test.runner.AndroidJUnitRunner && \
adb pull ${remote_screenshot_dir} `pwd` && (
    for locale in fr en sv; do
        convert \
            "$dirname/$locale/Cover search result.png" \
            \( -clone 0 -fill white -colorize 100 -fill black \
            -draw "polygon 120,375 350,375 350,610 120,610" \
            -alpha off -write mpr:mask +delete \) \
            -mask mpr:mask -blur 0x5 +mask "$dirname/$locale/Cover search result_blurred.png" && \
        rm "$dirname/$locale/Cover search result.png" && \
        composite -gravity center "$dirname/$locale/Cover search result - add issue.png"  "$dirname/$locale/Cover search result_blurred.png" "$dirname/$locale/Cover search result_blurred - with add issue.png" && \
        rm "$dirname/$locale/Cover search result - add issue.png"
    done
)