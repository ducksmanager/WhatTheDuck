#!/usr/bin/env bash
dirname=showcase
remote_dir=/data/data/net.ducksmanager.whattheduck/files/test-screenshots/${dirname}

adb shell am force-stop net.ducksmanager.whattheduck.test && \
adb shell rm -r ${remote_dir} && \
adb shell am instrument -w -r -e debug false -e class ScreenshotTest net.ducksmanager.whattheduck.test/android.support.test.runner.AndroidJUnitRunner && \
adb pull ${remote_dir} `pwd` && (
    for locale in fr en sv; do
        convert \
            "$dirname/$locale/Cover search result.png" \
            \( -clone 0 -fill white -colorize 100 -fill black \
            -draw "polygon 120,375 350,375 350,610 120,610" \
            -alpha off -write mpr:mask +delete \) \
            -mask mpr:mask -blur 0x5 +mask "$dirname/$locale/Cover search result_blurred.png" \
        && rm "$dirname/$locale/Cover search result.png"
    done
)