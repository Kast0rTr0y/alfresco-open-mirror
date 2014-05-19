<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-footer.lib.js">

// Get the initial header services and widgets...
var services = getHeaderServices(),
    widgets = getHeaderModel(msg.get("faceted-search.page.title"));

// Scope the model IDs
var rootWidgetId = "FCTSRCH_";

// Compose the search form model
var searchForm = {
   id: rootWidgetId + "SEARCH_FORM",
   name: "alfresco/forms/SingleEntryForm",
   config: {
      okButtonLabel: msg.get("faceted-search.search-form.ok-button-label"),
      okButtonPublishTopic : "ALF_SET_SEARCH_TERM",
      okButtonPublishGlobal: true,
      okButtonIconClass: "alf-white-search-icon",
      okButtonClass: "call-to-action",
      entryFieldName: "term"
   }
};

// Compose the facet menu column
var sideBarMenu = {
   id: rootWidgetId + "FACET_MENU",
   name: "alfresco/layout/LeftAndRight",
   config: {
      widgets: [
         {
            name: "alfresco/html/Label",
            align: "left",
            config: {
               label: msg.get("faceted-search.facet-menu.instruction")
            }
         }
      ]
   }
};

// Compose the individual facets
var facets = {
   id: rootWidgetId + "FACETS",
   name: "alfresco/layout/VerticalWidgets",
   config: {
      widgets: [
         {
            id: rootWidgetId + "FACET_FORMATS",
            name: "alfresco/search/FacetFilters",
            config: {
               label: msg.get("faceted-search.facet-menu.facet.formats"),
               facetQName: "{http://www.alfresco.org/model/content/1.0}content.mimetype",
               sortBy: "DESCENDING",
               maxFilters: 6
            }
         },
         {
            id: rootWidgetId + "FACET_DESCRIPTION",
            name: "alfresco/search/FacetFilters",
            config: {
               label: msg.get("faceted-search.facet-menu.facet.description"),
               facetQName: "{http://www.alfresco.org/model/content/1.0}description.__",
               sortBy: "DESCENDING",
               hitThreshold: 1,
               minFilterValueLength: 5,
               maxFilters: 6
            }
         },
         {
            id: rootWidgetId + "FACET_CREATOR",
            name: "alfresco/search/FacetFilters",
            config: {
               label: msg.get("faceted-search.facet-menu.facet.creator"),
               facetQName: "{http://www.alfresco.org/model/content/1.0}creator.__",
               sortBy: "ALPHABETICALLY",
               maxFilters: 3
            }
         },
         {
            id: rootWidgetId + "FACET_MODIFIER",
            name: "alfresco/search/FacetFilters",
            config: {
               label: msg.get("faceted-search.facet-menu.facet.modifier"),
               facetQName: "{http://www.alfresco.org/model/content/1.0}modifier.__",
               sortBy: "ALPHABETICALLY",
               maxFilters: 3
            }
         }
      ]
   }
};

// Compose the sort menu
var sortMenu = {
   id: rootWidgetId + "SORT_MENU",
   name: "alfresco/menus/AlfMenuBarSelect",
   config: {
      selectionTopic: "ALF_DOCLIST_SORT_FIELD_SELECTION",
      widgets: [
         {
            id: "DOCLIB_SORT_FIELD_SELECT_GROUP",
            name: "alfresco/menus/AlfMenuGroup",
            config: {
               // TODO: Add the remaining sort fields
               widgets: [
                  {
                     name: "alfresco/menus/AlfCheckableMenuItem",
                     config: {
                        label: msg.get("faceted-search.sort-menu.type.relevance"),
                        value: "",
                        group: "DOCUMENT_LIBRARY_SORT_FIELD",
                        publishTopic: "ALF_DOCLIST_SORT_FIELD_SELECTION",
                        checked: true,
                        publishPayload: {
                           label: "Relevance",
                           direction: "descending"
                        }
                     }
                  },
                  {
                     name: "alfresco/menus/AlfCheckableMenuItem",
                     config: {
                        label: msg.get("faceted-search.sort-menu.type.name"),
                        value: "cm:name",
                        group: "DOCUMENT_LIBRARY_SORT_FIELD",
                        publishTopic: "ALF_DOCLIST_SORT_FIELD_SELECTION",
                        checked: false,
                        publishPayload: {
                           label: "Name",
                           direction: "descending"
                        }
                     }
                  },
                  {
                     name: "alfresco/menus/AlfCheckableMenuItem",
                     config: {
                        label: msg.get("faceted-search.sort-menu.type.size"),
                        value: ".size",
                        group: "DOCUMENT_LIBRARY_SORT_FIELD",
                        publishTopic: "ALF_DOCLIST_SORT_FIELD_SELECTION",
                        checked: false,
                        publishPayload: {
                           label: "Size",
                           direction: "descending"
                        }
                     }
                  }
               ]
            }
         }
      ]
   }
};

// Compose result menu bar
var searchResultsMenuBar = {
   id: rootWidgetId + "RESULTS_MENU_BAR",
   name: "alfresco/layout/LeftAndRight",
   config: {
      widgets: [
         {
            name: "alfresco/html/Label",
            align: "left",
            config: {
               label: msg.get("faceted-search.results-menu.no-results"),
               subscriptionTopic: "ALF_SEARCH_RESULTS_COUNT"
            }
         },
         {
            name: "alfresco/menus/AlfMenuBar",
            align: "right",
            config: {
               widgets: [
                  {
                     name: "alfresco/menus/AlfMenuBarToggle",
                     config: {
                        checked: true,
                        onConfig: {
                           iconClass: "alf-sort-ascending-icon",
                           publishTopic: "ALF_DOCLIST_SORT",
                           publishPayload: {
                              direction: "ascending"
                           }
                        },
                        offConfig: {
                           iconClass: "alf-sort-descending-icon",
                           publishTopic: "ALF_DOCLIST_SORT",
                           publishPayload: {
                              direction: "descending"
                           }
                        }
                     }
                  },
                  sortMenu
               ]
            }
         }
      ]
   }
};

// Build the searchDocLib model
var searchDocLib = {
   id: rootWidgetId + "SEARCH_RESULTS_LIST",
   name: "alfresco/documentlibrary/AlfSearchList",
   config: {
      useHash: true,
      widgets: [
         {
            name: "alfresco/documentlibrary/views/AlfDocumentListView",
            config: {
               widgets: [
                  {
                     name:  "alfresco/documentlibrary/views/layouts/Row",
                     config: {
                        widgets: [
                           {
                              name: "alfresco/documentlibrary/views/layouts/Cell",
                              config: {
                                 width: "100px",
                                 widgets: [
                                    {
                                       name: "alfresco/renderers/Thumbnail",
                                       linkClickTopic: "ALF_NO_OP"
                                    }
                                 ]
                              }
                           },
                           {
                              name: "alfresco/documentlibrary/views/layouts/Column",
                              config: {
                                 widgets: [
                                    {
                                       name: "alfresco/documentlibrary/views/layouts/Cell",
                                       config: {
                                          widgets: [
                                             {
                                                name: "alfresco/renderers/PropertyLink",
                                                config: {
                                                   propertyToRender: "displayName",
                                                   renderSize: "large",
                                                   publishTopic: "ALF_SEARCH_RESULT_LINK"
                                                }
                                             },
                                             {
                                                name: "alfresco/renderers/Property",
                                                config: {
                                                   propertyToRender: "title",
                                                   renderSize: "small",
                                                   renderedValuePrefix: "(",
                                                   renderedValueSuffix: ")",
                                                   renderFilter: [
                                                      {
                                                         property: "title",
                                                         values: [""],
                                                         negate: true
                                                      }
                                                   ]
                                                }
                                             }
                                          ]
                                       }
                                    },
                                    {
                                       name: "alfresco/documentlibrary/views/layouts/Cell",
                                       config: {
                                          widgets: [
                                             {
                                                name: "alfresco/renderers/Date",
                                                config: {
                                                   modifiedDateProperty: "modifiedOn",
                                                   modifiedByProperty: "modifiedBy"
                                                }
                                             }
                                          ]
                                       }
                                    },
                                    {
                                       name: "alfresco/documentlibrary/views/layouts/Cell",
                                       config: {
                                          widgets: [
                                             {
                                                name: "alfresco/renderers/Property",
                                                config: {
                                                   propertyToRender: "description",
                                                   renderedValuePrefix: msg.get("faceted-search.doc-lib.value-prefix.description") + " ",
                                                   warnIfNotAvailable: true,
                                                   warnIfNoteAvailableMessage: msg.get("faceted-search.doc-lib.unavailable.description")
                                                }
                                             }
                                          ]
                                       }
                                    },
                                    {
                                       name: "alfresco/documentlibrary/views/layouts/Cell",
                                       config: {
                                          widgets: [
                                             {
                                                name: "alfresco/renderers/Property",
                                                config: {
                                                   propertyToRender: "site.title",
                                                   renderedValuePrefix: msg.get("faceted-search.doc-lib.value-prefix.site") + " "
                                                }
                                             }
                                          ]
                                       }
                                    }
                                 ]
                              }
                           },
                           {
                              name: "alfresco/documentlibrary/views/layouts/Cell",
                              config: {
                                 widgets: [
                                    {
                                       name: "alfresco/renderers/XhrActions"
                                    }
                                 ]
                              }
                           }
                        ]
                     }
                  }
               ]
            }
         }
      ]
   }
};

var main = {
   name: "alfresco/layout/VerticalWidgets",
   config: {
      baseClass: "side-margins",
      widgets: [
         searchForm,
         {
            name: "alfresco/layout/HorizontalWidgets",
            config: {
               widgets: [
                  {
                     name: "alfresco/layout/VerticalWidgets",
                     align: "sidebar",
                     widthPx: 350,
                     config: {
                        widgets: [
                           sideBarMenu
                        ]
                     }
                  },
                  {
                     name: "alfresco/layout/VerticalWidgets",
                     config: {
                        widgets: [
                           searchResultsMenuBar
                        ]
                     }
                  }
               ]
            }
         },
         {
            name: "alfresco/layout/HorizontalWidgets",
            config: {
               widgets: [
                  {
                     name: "alfresco/layout/VerticalWidgets",
                     align: "sidebar",
                     widthPx: 350,
                     config: {
                        widgets: [
                           facets
                        ]
                     }
                  },
                  {
                     name: "alfresco/layout/VerticalWidgets",
                     config: {
                        widgets: [
                           searchDocLib
                        ]
                     }
                  }
               ]
            }
         }
      ]
   }
};

// Append services with those required for search
services.push("alfresco/services/ContentService",
              "alfresco/services/DocumentService",
              "alfresco/dialogs/AlfDialogService",
              "alfresco/services/ActionService",
              "alfresco/services/SearchService",
              "alfresco/services/QuaddsService");

// Add in the search form and search doc lib...
widgets.push(main);

// Push services and widgets into the getFooterModel to return with a sticky footer wrapper
model.jsonModel = getFooterModel(services, widgets);