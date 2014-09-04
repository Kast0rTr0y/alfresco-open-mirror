/**
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
 * @module alfresco/services/SiteService
 * @extends module:alfresco/core/Core
 * @mixes module:alfresco/core/CoreXhr
 * @author Erik Winlöf
 */
define(["dojo/_base/declare",
   "alfresco/core/Core",
   "alfresco/core/CoreXhr",
   "alfresco/core/NotificationUtils",
   "alfresco/core/ObjectTypeUtils",
   "dojo/request/xhr",
   "dojo/json",
   "dojo/_base/lang",
   "service/constants/Default"],
      function(declare, AlfCore, AlfXhr, NotificationUtils, ObjectTypeUtils, xhr, JSON, lang, AlfConstants) {

         return declare([AlfCore, AlfXhr, NotificationUtils], {

            TOP_SITE_CONTRIBUTOR_REPORT: "TOP_SITE_CONTRIBUTOR_REPORT",
            SITE_CONTENT_REPORT: "SITE_CONTENT_REPORT",
            SITE_USAGE_REPORT: "SITE_USAGE_REPORT",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {Array}
             */
            i18nRequirements: [{i18nFile: "./i18n/ReportService.properties"}],

            /**
             * Sets up the subscriptions for the ReportService
             *
             * @instance
             * @param {array} args The constructor arguments.
             */
            constructor: function alfresco_services_ReportService__constructor(args) {
               lang.mixin(this, args);
               this.alfSubscribe("ALF_RETRIEVE_SITE_CONTENT_REPORT", lang.hitch(this, this.getSiteContentReport));
               this.alfSubscribe("ALF_RETRIEVE_TOP_SITE_CONTRIBUTOR_REPORT", lang.hitch(this, this.getTopSiteContributorReport));
               this.alfSubscribe("ALF_RETRIEVE_SITE_USAGE_REPORT", lang.hitch(this, this.getSiteUsageReport));
            },

            /**
             *
             * @instance
             * @param {object} payload The details of the request
             */
            getSiteContentReport: function alfresco_services_ReportService__getSiteContentReport(payload) {
               var alfTopic = (payload.alfResponseTopic != null) ? payload.alfResponseTopic : "ALF_RETRIEVE_SITE_CONTENT_REPORT";
               var url = AlfConstants.PROXY_URI + "api/solr";
               if (payload.site) {
                  url += "/site/" + encodeURIComponent(payload.site);
               }
               url += "/stats?facet=content.mimetype";
               var config = {
                  alfTopic: alfTopic,
                  url: url,
                  method: "GET",
                  successCallback: this.publishSiteContentReport,
                  callbackScope: this
               };
               this.serviceXhr(config);
            },

            publishSiteContentReport: function alfresco_services_ReportService__publishSiteContentReport(response, requestConfig) {
               this.alfPublish(requestConfig.alfTopic + "_SUCCESS", {
                  requestConfig: requestConfig,
                  response: {
                     data: response,
                     dataDescriptor: {
                        crosstabMode: false,
                        seriesInRows: false
                     }
                  }
               });
            },

            /**
             * Requests data that gives an overview of the amount of created content for each user in the site.
             *
             * @instance
             * @param {object} payload The details of the request
             */
            getTopSiteContributorReport: function alfresco_services_ReportService__getTopSiteContributorReport(payload) {
               var alfTopic = (payload.alfResponseTopic != null) ? payload.alfResponseTopic : "ALF_RETRIEVE_TOP_SITE_CONTRIBUTOR_REPORT";
               var url = AlfConstants.PROXY_URI + "api/solr";
               if (payload.site)
               {
                  url += "/site/" + encodeURIComponent(payload.site);
               }
               url += "/stats?facet=content.creator";
               if (payload.startDate)
               {
                  url += "&startDate=" + encodeURIComponent(payload.startDate);
               }
               if (payload.endDate)
               {
                  url += "&endDate=" + encodeURIComponent(payload.endDate);
               }
               var config = {
                  alfTopic: alfTopic,
                  url: url,
                  method: "GET",
                  successCallback: this.publishTopSiteContributorReport,
                  callbackScope: this
               };
               this.serviceXhr(config);
            },

            publishTopSiteContributorReport: function alfresco_services_ReportService__publishTopSiteContributorReport(response, requestConfig){
               this.alfPublish(requestConfig.alfTopic + "_SUCCESS", {
                  requestConfig: requestConfig,
                  response: {
                     data: response,
                     dataDescriptor: {
                        crosstabMode: false,
                        seriesInRows: false
                     }
                  }
               });
            },

            /**
             * Requests data that gives an overview of the number of activities taking place in the top sites
             *
             * @instance
             * @param {object} payload The details of the request
             */
            getSiteUsageReport: function alfresco_services_ReportService__getSiteUsageReport(payload) {
               var alfTopic = (payload.alfResponseTopic != null) ? payload.alfResponseTopic : "ALF_RETRIEVE_SITE_USAGE_REPORT";
               var url = AlfConstants.PROXY_URI + "pentaho/content/cda/doQuery?path=solution/cda/file.cda&dataAccessId=1";
               if (payload.startDate)
               {
                  url += "&paramStartDate=" + encodeURIComponent(payload.startDate);
               }
               if (payload.activityType)
               {
                  url += "&paramActivityType=" + encodeURIComponent(payload.activityType);
               }
               var config = {
                  alfTopic: alfTopic,
                  url: url,
                  method: "GET",
                  successCallback: this.publishSiteUsageReport,
                  callbackScope: this
               };
               //this.serviceXhr(config);
               // WA Hard-code response data for now until the services are there
               this.alfPublish(alfTopic + "_SUCCESS", {
                  requestConfig: config,
                  response: {
                     data: {"queryInfo":{"totalRows":"2"},"resultset":[["All Sites","Sample: Web Site Design Project",28,15],["All Sitess","Test",3,6]],"metadata":[{"colIndex":0,"colType":"String","colName":"[Sites].[(All)]"},{"colIndex":1,"colType":"String","colName":"[Sites].[Name]"},{"colIndex":2,"colType":"Numeric","colName":"[Activity].[All Activitys].[activity.org.alfresco.documentlibrary.file-downloaded]/[Measures].[Number of Events]"},{"colIndex":3,"colType":"Numeric","colName":"[Activity].[All Activitys].[activity.org.alfresco.documentlibrary.file-previewed]/[Measures].[Number of Events]"}]},
                     dataDescriptor: {
                        crosstabMode: false,
                        seriesInRows: false
                     }
                  }
               });
            },

            publishSiteUsageReport: function alfresco_services_ReportService__publishSiteUsageReport(response, requestConfig) {
               this.alfPublish(requestConfig.alfTopic + "_SUCCESS", {
                  requestConfig: requestConfig,
                  response: {
                     data: response
                  }
               });
            }


         });
      });
