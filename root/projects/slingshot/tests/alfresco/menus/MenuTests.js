/**
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This registers the test suites for the alfresco/menus package.
 * 
 * @author Dave Draper
 */
define(["intern!object",
        "intern/chai!assert",
        "require",
        "alfresco/TestCommon",
        "intern/dojo/node!leadfoot/keys"], 
        function (registerSuite, assert, require, TestCommon, specialKeys) {

   registerSuite({
      name: 'Basic Menus Test',
      'Menus Test': function () {
    	  
    	 var browser = this.remote;
         return TestCommon.bootstrapTest(this.remote, "./tests/alfresco/menus/page_models/BasicMenuTestPage.json")
            
            .end()
         
            // Test #1 
            // Open the drop-down menu and select the FIRST menut item using the space bar...
            .pressKeys(specialKeys.Tab)
            .pressKeys(specialKeys["Down arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_1"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_1 after Test #1");
            })
            .end()

            // Test #2
            // Open the drop-down menu and select the SECOND menu item using the return key...
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Return"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_2"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_2 in Test #2");
            })
            .end()

            // Test #3
            // Open the menu and select the first item in the SECOND group (tests cross-group navigation)...
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Return"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_3"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_3 in Test #3");
            })
            .end()

            // Test #4
            // Test cross group navigation both up and down groups...
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Up arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_2"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_2 in Test #4");
            })
            .end()

            // Test #5
            // Test going from first item in first group to last item in last group...
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Up arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_6"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_6 in Test #5");
            })
            .end()

            // Test #6
            // Test going from the last item in the last group to the first item in the first group...
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Up arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_1"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_1 in Test #6");
            })
            .end()

            // Test #7
            // Test going along the menu bar (the menu bar should already have focus)...
            .pressKeys(specialKeys["Right arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "url", "MENU_BAR_ITEM_1"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_BAR_ITEM_1 in Test #7");
            })
            .end()

            // Test #8
            // Test navigating between UNGROUPED menu items in a drop down menu...
            // (Moving to the menu will open it and have the first item selected)
            .pressKeys(specialKeys["Right arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "url", "MENU_ITEM_8"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_8 in Test #8");
            })
            .end()

            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Up arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "url", "MENU_ITEM_7"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_7 in Test #8");
            })
            .end()

            // Test #9
            // Test cascade menu keyboard navigation (opening and closing cascades)...
            .pressKeys(specialKeys["Right arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Down arrow"]) // Go past and back to cascade
            .pressKeys(specialKeys["Up arrow"])
            .pressKeys(specialKeys["Right arrow"]) // Open the cascade
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_11"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_11 in Test #9");
            })
            .end()

            // Test #10
            // Test opening cascades within cascades...
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Right arrow"]) // Open the FIRST cascade
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Right arrow"]) // Open the SECOND cascade
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_13"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_13 in Test #10");
            })
            .end()

            // Test #11
            // Test closing cascades
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Right arrow"]) // Open the cascade
            .pressKeys(specialKeys["Left arrow"])  // Close the cascade 
            .pressKeys(specialKeys["Down arrow"])  // Select the next menu item
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_14"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_14 in Test #11");
            })
            .end()

            // Test #12
            // Test menu item wrapper navigation (e.g. that you can navigate over non-menu items)
            .pressKeys(specialKeys["Right arrow"])
            .pressKeys(specialKeys["Down arrow"]) // This should jump over the logo widget inserted into the menu
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_10"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_10 in Test #12");
            })
            .end()

            // Test #13
            // Test menu item wrapper navigation (e.g. that you can navigate back up over non-menu items)
            .pressKeys(specialKeys["Down arrow"])
            .pressKeys(specialKeys["Down arrow"]) // This should jump over the logo widget inserted into the menu
            .pressKeys(specialKeys["Up arrow"]) // This should jump over the logo widget inserted into the menu
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_9"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_9 in Test #13");
            })
            .end()

            // Test #14
            // Test right cursor wrapping on menu...
            .pressKeys(specialKeys["Right arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_2"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_2 in Test #14");
            })
            .end()

            // Test #15
            // Test left cursor wrapping on menu...
            .pressKeys(specialKeys["Left arrow"])
            .pressKeys(specialKeys["Down arrow"])
            .sleep(1000)
            .pressKeys(specialKeys["Space"])
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_10"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_10 in Test #15");
            })
            .end()

            // Test #16
            // Test drop-down menu using the mouse...
            .findByCssSelector("#DROP_DOWN_MENU_1")
               .moveTo()
               .click()
               .end()
            .findByCssSelector("#MENU_ITEM_1")
               .moveTo()
               .click()
               .end()
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_1"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_1 in Test #16");
            })
            .end()

            // Test #17
            // Test cascade menus using the mouse...
            .findByCssSelector("#DROP_DOWN_MENU_3")
               .moveTo()
               .click()
               .end()
            .findByCssSelector("#CASCADING_MENU_1")
               .moveTo()
               .click()
               .end()
            .findByCssSelector("#CASCADING_MENU_2")
               .moveTo()
               .click()
               .end()
            .findByCssSelector("#MENU_ITEM_13")
               .moveTo()
               .click()
               .end()
            .hasElementByCss(TestCommon.pubSubDataCssSelector("last", "item", "MENU_ITEM_13"))
            .then(function(result) {
               assert(result == true, "Could not find MENU_ITEM_13 in Test #17");
            })
            .end()

            // Post the coverage results...
            .then(function() {
               TestCommon.postCoverageResults(browser);
            })
            .end();
      }
   });
});