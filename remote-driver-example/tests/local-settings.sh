#!/bin/bash

addGoal clean
addGoal install

addProfile "local-dev"
addDef "galenium.skipTests=false"
addDef "galenium.webdriver.lazy=true"
addDef "selenium.host=https://user:token@hub-cloud.browserstack.com/wd/hub"

# Always update snapshots
# addArg "-U";
# Never download dependencies
# addArg "-o";
# addArg "-X";

# verbose reporting
addDef "galenium.report.sparse=false"
addDef "selenium.runmode=remote"

# addTests "MyTest"

# show browser
# addDef "galenium.headless=false"

addDef "maven.javadoc.skip=true"
