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
 * This provides the configuration for test suites.
 * 
 * @author Richard Smith
 */
define({

   /**
    * This is the base array of non-functional test suites
    *
    * @instance
    * @type [string]
    */
   baseNonFunctionalSuites: null,

   // Uncomment and add specific tests as necessary during development!
   // baseFunctionalSuites: ['tests/alfresco/accessibility/AccessibilityMenuTest'],

   /**
    * This is the base array of functional test suites
    *
    * @instance
    * @type [string]
    */
   baseFunctionalSuites: [
      'tests/alfresco/accessibility/AccessibilityMenuTest',

      'tests/alfresco/charts/ccc/PieChartTest',

      'tests/alfresco/core/PublishPayloadMixinTest',
      'tests/alfresco/core/RenderFilterTest',
      'tests/alfresco/core/VisibilityConfigTest',
      'tests/alfresco/core/WidgetCreationTest',

      'tests/alfresco/documentlibrary/DocumentListTest',
      'tests/alfresco/documentlibrary/DocumentSelectorTest',
      'tests/alfresco/documentlibrary/SearchListTest',
      'tests/alfresco/documentlibrary/SearchListScrollTest', // PASSING - TODO: NEEDS IMPLEMENTING PROPERLY,
      'tests/alfresco/documentlibrary/views/AlfDocumentListWithHeaderTest',

      'tests/alfresco/footer/FooterTest',

      'tests/alfresco/forms/DynamicFormTest.js',,
      'tests/alfresco/forms/FormsTest',
      'tests/alfresco/forms/SingleTextFieldFormTest',
      // 'tests/alfresco/forms/controls/DocumentPickerTest', // TODO: NEEDS FIXING - WIDGET POSSIBLY BROKEN
      'tests/alfresco/forms/controls/DojoSelectTest',
      'tests/alfresco/forms/controls/DojoDateTextBoxTest',  // TODO: NEEDS FIXING
      'tests/alfresco/forms/controls/DojoValidationTextBoxTest',
      'tests/alfresco/forms/controls/FormButtonDialogTest',
      'tests/alfresco/forms/controls/MultipleEntryFormControlTest',

      'alfresco/footer/FooterTest', 

      'tests/alfresco/header/HeaderWidgetsTest',
      'tests/alfresco/header/WarningTest',

      'tests/alfresco/html/LabelTest',

      'tests/alfresco/layout/AlfSideBarContainerTest',
      'tests/alfresco/layout/BasicLayoutTest',

      'tests/alfresco/menus/AlfCheckableMenuItemTest',
      'tests/alfresco/menus/AlfContextMenuTest',
      'tests/alfresco/menus/AlfFormDialogMenuItemTest',
      'tests/alfresco/menus/AlfMenuBarSelectItemsTest',
      'tests/alfresco/menus/AlfMenuBarSelectTest',
      'tests/alfresco/menus/AlfMenuBarToggleTest',
      'tests/alfresco/menus/AlfMenuItemWrapperTest',
      'tests/alfresco/menus/AlfMenuTextForClipboardTest',
      'tests/alfresco/menus/AlfVerticalMenuBarTest',
      'tests/alfresco/menus/MenuTests',

      // 'tests/alfresco/misc/AlfTooltipTest', - COMMENTED OUT - THE TOOLTIP ITSELF NEEDS REWRITING
      'tests/alfresco/misc/TableAndFormDialogTest',

      'tests/alfresco/renderers/BooleanTest',
      'tests/alfresco/renderers/DateTest',
      'tests/alfresco/renderers/DateLinkTest',
      'tests/alfresco/renderers/PropertyTest',
      'tests/alfresco/renderers/PropertyLinkTest',
      'tests/alfresco/renderers/PublishingDropDownMenuTest',
      'tests/alfresco/renderers/PublishPayloadMixinOnActionsTest',
      'tests/alfresco/renderers/SearchResultPropertyLinkTest',
      'tests/alfresco/renderers/SocialRenderersTest',

      'tests/alfresco/renderers/XhrActionsTest',

      'tests/alfresco/search/FacetFiltersTest'
   ],

   /**
    * This is the array of functional test suites that should only be applied to local tests
    *
    * @instance
    * @type [string]
    */
   localOnlyFunctionalSuites: [], //['tests/alfresco/CodeCoverageBalancer'],

   /**
    * This is the full array of functional test suites for local tests
    *
    * @instance
    * @type [string]
    */
   localFunctionalSuites: function localFunctionalSuites(){
      return this.setupFunctionalSuites.concat(
         this.baseFunctionalSuites.concat(
            this.localOnlyFunctionalSuites.concat(
               this.teardownFunctionalSuites
            )
         )
      );
   },

   /**
    * This is the array of functional test suites that should only be applied to virtual machine tests
    *
    * @instance
    * @type [string]
    */
   vmOnlyFunctionalSuites: ['tests/alfresco/CodeCoverageBalancer'],

   /**
    * This is the full array of functional test suites for virtual machine tests
    *
    * @instance
    * @type [string]
    */
   vmFunctionalSuites: function vmFunctionalSuites(){
      return this.setupFunctionalSuites.concat(
         this.baseFunctionalSuites.concat(
            this.vmOnlyFunctionalSuites.concat(
               this.teardownFunctionalSuites
            )
         )
      );
   },

   /**
    * This is the array of functional test suites that should only be applied to sauce labs tests
    *
    * @instance
    * @type [string]
    */
   slOnlyFunctionalSuites: [],

   /**
    * This is the full array of functional test suites for sauce labs tests
    *
    * @instance
    * @type [string]
    */
   slFunctionalSuites: function slFunctionalSuites(){
      return this.setupFunctionalSuites.concat(
         this.baseFunctionalSuites.concat(
            this.slOnlyFunctionalSuites.concat(
               this.teardownFunctionalSuites
            )
         )
      );
   },

   /**
    * This is the array of functional test suites that should only be applied to selenium grid tests
    *
    * @instance
    * @type [string]
    */
   gridOnlyFunctionalSuites: [],

   /**
    * This is the full array of functional test suites for selenium grid tests
    *
    * @instance
    * @type [string]
    */
   gridFunctionalSuites: function gridFunctionalSuites(){
      return this.setupFunctionalSuites.concat(
         this.baseFunctionalSuites.concat(
            this.gridOnlyFunctionalSuites.concat(
               this.teardownFunctionalSuites
            )
         )
      );
   },

   /**
    * This is the array of functional test suites for setup purposes
    *
    * @instance
    * @type [string]
    */
   setupFunctionalSuites: [],//['tests/alfresco/DebugEnable'],

   /**
    * This is the array of functional test suites for teardown purposes
    *
    * @instance
    * @type [string]
    */
   teardownFunctionalSuites: []//['tests/alfresco/DebugDisable']

});