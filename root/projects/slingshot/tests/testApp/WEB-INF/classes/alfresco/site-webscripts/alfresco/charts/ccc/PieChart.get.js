model.jsonModel = {
   publishOnReady: [
      {
         publishTopic: "GLOBAL_UPDATE_TOPIC"
      }
   ],
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
      "alfresco/testing/mockservices/CCCChartTestData",
      "alfresco/services/ErrorReporter"
   ],
   widgets: [
      {
         name: "alfresco/charts/ccc/PieChart",
         config: {
            id: "PIECHART_1",
            dataTopic: "GET_SAMPLE_CHART_DATA_1",
            valuesMask: "{category}-{value}"
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