model.jsonModel = {
   services: [
      {
         name: "alfresco/services/LoggingService",
         config: {
            loggingPreferences: {
               enabled: true,
               all: true
            }
         }
      },
      "alfresco/services/ErrorReporter"
   ],
   widgets:[
      {
         id: "ITEM1",
         name: "alfresco/renderers/FileType",
         align: "left",
         config: {
            size: "small",
            altText: "File type test",
            currentItem: {
               fileName: "abc.gif",
               node:{
                  nodeRef: "12345",
                  type: "cm:cmobject"
               }
            }
         },
         customTypeMappings: {
            "cm:test": "test"
         },
         customExtnMappings: {
            test: "test"
         }
      },
      {
         id: "ITEM2",
         name: "alfresco/renderers/FileType",
         align: "right",
         config: {
            size: "medium",
            altText: "File type test",
            currentItem: {
               fileName: "def.xls",
               node:{
                  nodeRef: "93875",
                  type: "cm:content"
               }
            }
         }
      },
      {
         id: "ITEM3",
         name: "alfresco/renderers/FileType",
         align: "left",
         config: {
            size: "large",
            altText: "File type test",
            currentItem: {
               fileName: "ghi.ppt",
               node:{
                  nodeRef: "02983",
                  type: "cm:thumbnail"
               }
            }
         }
      },
      {
         id: "ITEM4",
         name: "alfresco/renderers/FileType",
         align: "right",
         config: {
            size: "large",
            altText: "File type test",
            currentItem: {
               fileName: "ghi.iuy",
               node:{
                  nodeRef: "28734",
                  type: "cm:notreal"
               }
            }
         }
      },
      {
         id: "ITEM5",
         name: "alfresco/renderers/FileType",
         align: "right",
         config: {
            size: "large",
            altText: "File type test",
            currentItem: {
               node:{
                  nodeRef: "98353",
                  type: "cm:thumbnail"
               }
            }
         }
      },
      {
         id: "ITEM6",
         name: "alfresco/renderers/FileType",
         align: "right",
         config: {
            size: "large",
            altText: "File type test",
            currentItem: null
         }
      },
      {
         name: "alfresco/testing/SubscriptionLog"
      },
      {
         name: "alfresco/testing/TestCoverageResults"
      }
   ]
};