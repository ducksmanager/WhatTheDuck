#!/usr/bin/env bash
dirname=showcase
remote_dir=/data/data/net.ducksmanager.whattheduck/files/test-screenshots/${dirname}
test_app_name=net.ducksmanager.whattheduck.test

adb shell am force-stop ${test_app_name} && \
adb shell rm -r ${remote_dir} && \
adb shell am instrument -w -r -e debug false -e class ScreenshotTest ${test_app_name}/android.support.test.runner.AndroidJUnitRunner && \
adb pull ${remote_dir} `pwd` && (
    for locale in fr en sv; do
        convert \
            "$dirname/$locale/Cover search result.png" \
            \( -clone 0 -fill white -colorize 100 -fill black \
            -draw "polygon 120,375 350,375 350,610 120,610" \
            -alpha off -write mpr:mask +delete \) \
            -mask mpr:mask -blur 0x5 +mask "$dirname/$locale/Cover search result_blurred.png" \
        && rm "$dirname/$locale/Cover search result.png" \
        && composite -gravity center "$dirname/$locale/Cover search result - add issue.png"  "$dirname/$locale/Cover search result_blurred.png" "$dirname/$locale/Cover search result_blurred - with add issue.png"
    done
)