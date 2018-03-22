#!/usr/bin/env bash
adb shell am force-stop net.ducksmanager.whattheduck.test
adb shell am instrument -w -r -e debug false -e class ScreenshotTest net.ducksmanager.whattheduck.test/android.support.test.runner.AndroidJUnitRunner
adb pull '/data/data/net.ducksmanager.whattheduck/files/test-screenshots/showcase' `pwd`
for locale in fr en sv; do
    convert \
        "showcase/$locale/Cover search result.png" \
        \( -clone 0 -fill white -colorize 100 -fill black \
        -draw "polygon 120,375 350,375 350,610 120,610" \
        -alpha off -write mpr:mask +delete \) \
        -mask mpr:mask -blur 0x5 +mask "showcase/$locale/Cover search result_blurred.png" \
    && rm "showcase/$locale/Cover search result.png"
done